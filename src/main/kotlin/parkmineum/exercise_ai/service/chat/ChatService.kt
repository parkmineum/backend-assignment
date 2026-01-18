package parkmineum.exercise_ai.service.chat

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import parkmineum.exercise_ai.common.BusinessException
import parkmineum.exercise_ai.common.ErrorCode
import parkmineum.exercise_ai.domain.Chat
import parkmineum.exercise_ai.domain.Thread
import parkmineum.exercise_ai.domain.User
import parkmineum.exercise_ai.domain.UserRole
import parkmineum.exercise_ai.dto.ChatRequest
import parkmineum.exercise_ai.dto.ChatResponse
import parkmineum.exercise_ai.dto.ThreadResponse
import parkmineum.exercise_ai.infrastructure.ai.AiClient
import parkmineum.exercise_ai.infrastructure.ai.ChatMessage
import parkmineum.exercise_ai.infrastructure.ai.Role
import parkmineum.exercise_ai.repository.ChatRepository
import parkmineum.exercise_ai.repository.ThreadRepository
import parkmineum.exercise_ai.repository.UserRepository
import java.time.LocalDateTime

/**
 * 대화 및 스레드 기반의 비즈니스 로직을 처리하는 서비스
 * 
 * [설계 의도]
 * - 세션 관리: 30분 활동 기반의 스레드 생명주기 로직을 캡슐화하여 관리
 * - 문맥 유지: AI 요청 시 이전 대화 내역을 포함하여 문맥이 끊기지 않는 대화 환경을 제공
 */
@Service
class ChatService(
    private val threadRepository: ThreadRepository,
    private val userRepository: UserRepository,
    private val aiClient: AiClient,
    private val chatTransactionService: ChatTransactionService
) {

    /**
     * 비동기 방식으로 대화를 생성합니다.
     * AI 호출 중에는 DB 트랜잭션을 점유하지 않도록 설계되었습니다.
     */
    fun createChatAsync(email: String, request: ChatRequest): java.util.concurrent.CompletableFuture<ChatResponse> {
        // 1. 트랜잭션 내에서 스레드 준비 및 이력 조회
        val (thread, history) = chatTransactionService.prepareThreadAndHistory(email)
        val threadId = thread.id!!

        // 2. 외부 AI 호출 및 결과 저장 (비동기 병렬 처리 가능)
        return java.util.concurrent.CompletableFuture.supplyAsync({
            val answer = aiClient.chat(request.question, history, request.model)
                ?: throw BusinessException(ErrorCode.INVALID_AI_RESPONSE)
            
            // 3. 다시 트랜잭션을 열어 결과 저장
            val savedChat = chatTransactionService.saveChatResult(threadId, request.question, answer)
            
            ChatResponse(
                id = savedChat.id!!,
                threadId = threadId,
                question = savedChat.question,
                answer = savedChat.answer,
                createdAt = savedChat.createdAt
            )
        })
    }

    fun createChat(email: String, request: ChatRequest): ChatResponse {
        // 동기 방식이 필요한 경우를 위해 유지 (또는 createChatAsync.get() 활용)
        return createChatAsync(email, request).join()
    }


    @Transactional(readOnly = true)
    fun getAllThreads(email: String, pageable: Pageable): Page<ThreadResponse> {
        val user = userRepository.findByEmail(email) ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
        
        val threadPage = if (user.role == UserRole.ADMIN) {
            threadRepository.findAll(pageable)
        } else {
            threadRepository.findAllByUser(user, pageable)
        }

        return threadPage.map { thread ->
            ThreadResponse(
                id = thread.id!!,
                updatedAt = thread.updatedAt,
                chats = thread.chats.map { chat ->
                    ChatResponse(chat.id!!, thread.id!!, chat.question, chat.answer, chat.createdAt)
                }.sortedBy { it.createdAt }
            )
        }
    }

    @Transactional
    fun deleteThread(email: String, threadId: Long) {
        val user = userRepository.findByEmail(email) ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
        val thread = threadRepository.findById(threadId).orElseThrow { BusinessException(ErrorCode.THREAD_NOT_FOUND) }

        if (thread.user.id != user.id && user.role != UserRole.ADMIN) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        threadRepository.delete(thread)
    }
}
