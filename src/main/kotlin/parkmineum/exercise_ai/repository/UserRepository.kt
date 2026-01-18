package parkmineum.exercise_ai.repository

import org.springframework.data.jpa.repository.JpaRepository
import parkmineum.exercise_ai.domain.User
import java.time.LocalDateTime

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?

    fun existsByEmail(email: String): Boolean

    fun countByCreatedAtAfter(dateTime: LocalDateTime): Long
}
