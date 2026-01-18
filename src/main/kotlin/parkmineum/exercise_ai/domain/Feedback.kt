package parkmineum.exercise_ai.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tb_feedbacks", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "chat_id"])])
class Feedback(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chat: Chat,

    @Column(nullable = false)
    val isPositive: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FeedbackStatus = FeedbackStatus.PENDING
) : BaseEntity()

enum class FeedbackStatus {
    PENDING, RESOLVED
}
