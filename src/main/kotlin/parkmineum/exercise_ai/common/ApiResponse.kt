package parkmineum.exercise_ai.common

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * 모든 API 응답의 표준 형식을 정의합니다.
 * success, data, error, metadata 필드를 통해 일관된 응답 구조를 보장합니다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetail? = null,
    val metadata: Map<String, Any>? = null
) {
    companion object {
        fun <T> success(data: T, metadata: Map<String, Any>? = null): ApiResponse<T> {
            return ApiResponse(success = true, data = data, metadata = metadata)
        }

        fun <T> error(code: String, message: String): ApiResponse<T> {
            return ApiResponse(success = false, error = ErrorDetail(code, message))
        }
    }
}

data class ErrorDetail(
    val code: String,
    val message: String
)