package parkmineum.exercise_ai.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 모든 API 요청에서 JWT 토큰의 유효성을 검사하는 인증 필터입니다.
 * 유효한 토큰일 경우 SecurityContext에 인증 정보를 설정합니다.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)

        if (token != null && jwtProvider.validateToken(token)) {
            val email = jwtProvider.getEmail(token)
            val role = jwtProvider.getRole(token)
            
            val auth = UsernamePasswordAuthenticationToken(
                email,
                null,
                listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
            )
            SecurityContextHolder.getContext().authentication = auth
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
}
