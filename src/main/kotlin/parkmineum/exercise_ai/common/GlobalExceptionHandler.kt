package parkmineum.exercise_ai.common

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 애플리케이션 전역에서 발생하는 예외를 핸들링하여
 * 시스템 표준 응답 포맷(ApiResponse)으로 변환하는 핸들러입니다.
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 로그 예시 : [WARN] Business Exception: A005 - 이미 존재하는 이메일입니다.
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("Business Exception: ${e.errorCode.code} - ${e.message}")
        return ResponseEntity
            .status(e.errorCode.httpStatus)
            .body(ApiResponse.error(e.errorCode.code, e.errorCode.message))
    }

    // 로그 예시 : [WARN] Validation Exception: email: 올바른 이메일 형식이 아닙니다., password: size must be between 4 and 2147483647
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> {
        val errorMessage = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        logger.warn("Validation Exception: $errorMessage")
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.httpStatus)
            .body(ApiResponse.error(ErrorCode.INVALID_INPUT.code, errorMessage))
    }

    // 로그 예시 : [ERROR] Unexpected Exception: java.lang.NullPointerException: null ... (stack trace)
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Unit>> {
        logger.error("Unexpected Exception: ", e)
        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.httpStatus)
            .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.code, ErrorCode.INTERNAL_SERVER_ERROR.message))
    }
}
