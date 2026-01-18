package parkmineum.exercise_ai.common

import org.springframework.http.HttpStatus

/**
 * 비즈니스 로직 실행 중 발생하는 예외를 정의하며,
 * 사전에 정의된 ErrorCode를 통해 구체적인 에러 상황을 전달합니다.
 */
enum class ErrorCode(val httpStatus: HttpStatus, val code: String, val message: String) {
    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증되지 않은 사용자입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A003", "권한이 없습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "A004", "로그인에 실패했습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "A005", "이미 존재하는 이메일입니다."),

    // Chat
    CHAT_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "대화를 찾을 수 없습니다."),
    THREAD_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "스레드를 찾을 수 없습니다."),
    INVALID_AI_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "AI 응답 처리에 실패했습니다."),

    // Feedback
    FEEDBACK_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "F001", "이미 해당 대화에 피드백을 남겼습니다."),
    FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "F002", "피드백을 찾을 수 없습니다."),

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "G001", "올바르지 않은 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G002", "서버 내부 오류가 발생했습니다.")
}

class BusinessException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)
