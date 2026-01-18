package parkmineum.exercise_ai.service.auth

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import parkmineum.exercise_ai.common.BusinessException
import parkmineum.exercise_ai.common.ErrorCode
import parkmineum.exercise_ai.domain.User
import parkmineum.exercise_ai.dto.AuthResponse
import parkmineum.exercise_ai.dto.LoginRequest
import parkmineum.exercise_ai.dto.SignupRequest
import parkmineum.exercise_ai.infrastructure.security.JwtProvider
import parkmineum.exercise_ai.repository.UserRepository

/**
 * 사용자 기반의 비즈니스 로직(회원가입, 로그인)을 처리하는 서비스입니다.
 *
 * [설계 의도]
 * - 도메인 응집도: 현재 규모에서는 User 도메인의 생명주기를 한 서비스에서 관리하는 것이 명확하다고 판단
 * - 확장 고려: 향후 외부 연동이나 보안 정책 확장 시, 기능 단위로 서비스 분리가 가능하도록 메서드 책임을 분리
 */
@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider
) {

    @Transactional
    fun signup(request: SignupRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS)
        }

        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            role = request.role
        )
        val savedUser = userRepository.save(user)
        
        val token = jwtProvider.createToken(savedUser.email, savedUser.role)
        return AuthResponse(savedUser.email, savedUser.name, savedUser.role, token)
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw BusinessException(ErrorCode.LOGIN_FAILED)

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw BusinessException(ErrorCode.LOGIN_FAILED)
        }

        val token = jwtProvider.createToken(user.email, user.role)
        return AuthResponse(user.email, user.name, user.role, token)
    }
}
