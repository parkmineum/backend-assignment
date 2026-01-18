package parkmineum.exercise_ai.controller

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import parkmineum.exercise_ai.common.ApiResponse
import parkmineum.exercise_ai.dto.AuthResponse
import parkmineum.exercise_ai.dto.LoginRequest
import parkmineum.exercise_ai.dto.SignupRequest
import parkmineum.exercise_ai.service.auth.UserService

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    @Operation(summary = "회원 가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ApiResponse<AuthResponse> {
        val response = userService.signup(request)
        return ApiResponse.success(response)
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<AuthResponse> {
        val response = userService.login(request)
        return ApiResponse.success(response)
    }
}