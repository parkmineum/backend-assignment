package parkmineum.exercise_ai.dto

import parkmineum.exercise_ai.domain.FeedbackStatus
import java.time.LocalDateTime


/**
 * 피드백 관련 DTO를 하나의 파일에서 관리합니다.
 * 이는 빠른 개발과, 관련 도메인의 데이터 구조를 한눈에 파악하기 위함이며,
 * 파일 내 다중 클래스 정의 기능을 활용하여 코드 응집도를 높였습니다.
 */
data class FeedbackRequest(
    val chatId: Long,
    val isPositive: Boolean
)

data class FeedbackResponse(
    val id: Long,
    val chatId: Long,
    val userName: String,
    val isPositive: Boolean,
    val status: FeedbackStatus,
    val createdAt: LocalDateTime
)

data class FeedbackStatusUpdate(
    val status: FeedbackStatus
)
