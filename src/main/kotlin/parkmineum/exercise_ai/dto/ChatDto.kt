package parkmineum.exercise_ai.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * 대화 관련 DTO를 하나의 파일에서 관리합니다.
 * 이는 빠른 개발과, 관련 도메인의 데이터 구조를 한눈에 파악하기 위함이며,
 * 파일 내 다중 클래스 정의 기능을 활용하여 코드 응집도를 높였습니다.
 */
data class ChatRequest(
    @Schema(description = "질문 내용", example = "안녕하세요, 오늘 날씨 어때요?")
    val question: String,
    
    @Schema(description = "스트리밍 여부 (현재 미지원)", example = "false")
    val isStreaming: Boolean = false,
    
    @Schema(description = "사용할 AI 모델 (선택 사항)", example = "gemini-2.5-flash")
    val model: String? = null
)

data class ChatResponse(
    val id: Long,
    val threadId: Long,
    val question: String,
    val answer: String,
    val createdAt: LocalDateTime
)

data class ThreadResponse(
    val id: Long,
    val updatedAt: LocalDateTime,
    val chats: List<ChatResponse>
)