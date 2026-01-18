package parkmineum.exercise_ai.service.chat

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import parkmineum.exercise_ai.common.BusinessException
import parkmineum.exercise_ai.common.ErrorCode
import parkmineum.exercise_ai.domain.Chat
import parkmineum.exercise_ai.domain.Thread
import parkmineum.exercise_ai.domain.User
import parkmineum.exercise_ai.infrastructure.ai.ChatMessage
import parkmineum.exercise_ai.infrastructure.ai.Role
import parkmineum.exercise_ai.repository.ChatRepository
import parkmineum.exercise_ai.repository.ThreadRepository
import parkmineum.exercise_ai.repository.UserRepository
import java.time.LocalDateTime

/**
 * 대화 관련 DB 트랜잭션 처리 서비스
 * AI 호출과 같이 오래 걸리는 외부 작업과 트랜잭션을 분리하기 위해 사용합니다.
 */
@Service
class ChatTransactionService(
    private val threadRepository: ThreadRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun prepareThreadAndHistory(email: String): Pair<Thread, List<ChatMessage>> {
        val user = userRepository.findByEmail(email) ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
        val thread = getOrCreateThread(user)
        
        val history = thread.chats.map { 
            ChatMessage(Role.USER, it.question) to ChatMessage(Role.ASSISTANT, it.answer)
        }.flatMap { listOf(it.first, it.second) }
        
        return thread to history
    }

    @Transactional
    fun saveChatResult(threadId: Long, question: String, answer: String): Chat {
        val thread = threadRepository.findById(threadId).orElseThrow { BusinessException(ErrorCode.THREAD_NOT_FOUND) }
        
        val chat = Chat(
            thread = thread,
            question = question,
            answer = answer
        )
        val savedChat = chatRepository.save(chat)
        
        thread.touch()
        threadRepository.save(thread)
        
        return savedChat
    }

    private fun getOrCreateThread(user: User): Thread {
        val lastThread = threadRepository.findTopByUserOrderByUpdatedAtDesc(user)
        return if (lastThread == null || lastThread.updatedAt.isBefore(LocalDateTime.now().minusMinutes(30))) {
            threadRepository.save(Thread(user = user))
        } else {
            lastThread
        }
    }
}
