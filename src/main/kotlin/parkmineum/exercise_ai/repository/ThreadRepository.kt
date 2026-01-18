package parkmineum.exercise_ai.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import parkmineum.exercise_ai.domain.Thread
import parkmineum.exercise_ai.domain.User
import java.time.LocalDateTime


interface ThreadRepository : JpaRepository<Thread, Long> {

    fun findTopByUserOrderByUpdatedAtDesc(user: User): Thread?

    fun findAllByUser(user: User, pageable: Pageable): Page<Thread>

    fun countByCreatedAtAfter(dateTime: LocalDateTime): Long
}