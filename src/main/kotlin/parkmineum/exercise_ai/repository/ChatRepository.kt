package parkmineum.exercise_ai.repository

import org.springframework.data.jpa.repository.JpaRepository
import parkmineum.exercise_ai.domain.Chat
import java.time.LocalDateTime


interface ChatRepository : JpaRepository<Chat, Long> {
    fun countByCreatedAtAfter(dateTime: LocalDateTime): Long
}
