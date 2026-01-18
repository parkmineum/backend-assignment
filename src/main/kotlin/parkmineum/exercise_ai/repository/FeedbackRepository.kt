package parkmineum.exercise_ai.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import parkmineum.exercise_ai.domain.Feedback
import parkmineum.exercise_ai.domain.User

interface FeedbackRepository : JpaRepository<Feedback, Long> {
    fun existsByUserAndChatId(user: User, chatId: Long): Boolean

    fun findAllByUser(user: User, pageable: Pageable): Page<Feedback>
}
