package parkmineum.exercise_ai.service.feedback

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import parkmineum.exercise_ai.common.BusinessException
import parkmineum.exercise_ai.common.ErrorCode
import parkmineum.exercise_ai.domain.Feedback
import parkmineum.exercise_ai.domain.FeedbackStatus
import parkmineum.exercise_ai.domain.UserRole
import parkmineum.exercise_ai.dto.FeedbackRequest
import parkmineum.exercise_ai.dto.FeedbackResponse
import parkmineum.exercise_ai.repository.ChatRepository
import parkmineum.exercise_ai.repository.FeedbackRepository
import parkmineum.exercise_ai.repository.UserRepository

/**
 * 사용자 피드백(좋아요/싫어요) 및 상태 관리를 처리하는 서비스
 * 
 * [설계 의도]
 * - 데이터 무결성: 유저당 대화 메시지 하나에 하나의 피드백만 남길 수 있도록 검증
 */
@Service
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) {

    @Transactional
    fun createFeedback(email: String, request: FeedbackRequest): FeedbackResponse {
        val user = userRepository.findByEmail(email) ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
        val chat = chatRepository.findById(request.chatId).orElseThrow { BusinessException(ErrorCode.CHAT_NOT_FOUND) }

        // 본인 대화인지 확인 (데이터 보안 강화)
        if (chat.thread.user.id != user.id) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        if (feedbackRepository.existsByUserAndChatId(user, request.chatId)) {
            throw BusinessException(ErrorCode.FEEDBACK_ALREADY_EXISTS)
        }

        val feedback = Feedback(
            user = user,
            chat = chat,
            isPositive = request.isPositive
        )
        val saved = feedbackRepository.save(feedback)

        return FeedbackResponse(
            id = saved.id!!,
            chatId = chat.id!!,
            userName = user.name,
            isPositive = saved.isPositive,
            status = saved.status,
            createdAt = saved.createdAt
        )
    }

    @Transactional(readOnly = true)
    fun getAllFeedbacks(email: String, pageable: Pageable): Page<FeedbackResponse> {
        val user = userRepository.findByEmail(email) ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val feedbackPage = if (user.role == UserRole.ADMIN) {
            feedbackRepository.findAll(pageable)
        } else {
            feedbackRepository.findAllByUser(user, pageable)
        }

        return feedbackPage.map { 
            FeedbackResponse(
                id = it.id!!,
                chatId = it.chat.id!!,
                userName = it.user.name,
                isPositive = it.isPositive,
                status = it.status,
                createdAt = it.createdAt
            )
        }
    }

    @Transactional
    fun updateFeedbackStatus(email: String, feedbackId: Long, status: FeedbackStatus): FeedbackResponse {
        val user = userRepository.findByEmail(email) ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
        if (user.role != UserRole.ADMIN) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        val feedback = feedbackRepository.findById(feedbackId).orElseThrow { BusinessException(ErrorCode.FEEDBACK_NOT_FOUND) }
        feedback.status = status
        
        return FeedbackResponse(
            id = feedback.id!!,
            chatId = feedback.chat.id!!,
            userName = feedback.user.name,
            isPositive = feedback.isPositive,
            status = feedback.status,
            createdAt = feedback.createdAt
        )
    }
}
