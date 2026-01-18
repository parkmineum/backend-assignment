package parkmineum.exercise_ai.dto

/**
 * 사용자 관련 DTO를 하나의 파일에서 관리합니다.
 * 이는 빠른 개발과, 관련 도메인의 데이터 구조를 한눈에 파악하기 위함이며,
 * Kotlin의 파일 내 다중 클래스 정의 기능을 활용하여 코드 응집도를 높였습니다.
 */
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import parkmineum.exercise_ai.domain.UserRole

data class SignupRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank @field:Size(min = 4) val password: String,
    @field:NotBlank val name: String,
    val role: UserRole = UserRole.MEMBER
)

data class LoginRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String
)

data class AuthResponse(
    val email: String,
    val name: String,
    val role: UserRole,
    val accessToken: String
)