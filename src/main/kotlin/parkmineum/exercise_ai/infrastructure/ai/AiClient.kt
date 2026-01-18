package parkmineum.exercise_ai.infrastructure.ai

/**
 * AI 모델 통신 인터페이스
 * 새로운 AI 모델이 추가되어도 비즈니스 변경 없이 인프라 계층만 확장되도록 설계되었습니다.
 */
interface AiClient {
    /**
     * @param prompt 현재 사용자의 질문
     * @param history 이전 대화 내역 (Role-Content 쌍)
     * @param model 요청 시 사용할 특정 모델명 (null일 경우 기본 모델 사용)
     */
    fun chat(prompt: String, history: List<ChatMessage> = emptyList(), model: String? = null): String?

    /**
     * 스트리밍 방식(요구사항 명세에 따른 규격 정의용이며, 실제 구현은 생략됨)
     */
    fun chatStream(prompt: String, history: List<ChatMessage> = emptyList(), model: String? = null): Any?
}

data class ChatMessage(
    val role: Role,
    val content: String
)

enum class Role {
    USER, ASSISTANT
}