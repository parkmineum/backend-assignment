package parkmineum.exercise_ai.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import parkmineum.exercise_ai.domain.UserRole
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT 생성 및 검증을 담당하는 컴포넌트입니다.
 * 사용자 식별 정보와 권한을 포함한 토큰을 관리합니다.
 */
@Component
class JwtProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expiration: Long
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    fun createToken(email: String, role: UserRole): String {
        val now = Date()
        val validity = Date(now.time + expiration)

        return Jwts.builder()
            .subject(email)
            .claim("role", role.name)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
    }

    fun getEmail(token: String): String {
        return getClaims(token).subject
    }

    fun getRole(token: String): UserRole {
        val roleStr = getClaims(token).get("role", String::class.java)
        return UserRole.valueOf(roleStr)
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
