package parkmineum.exercise_ai.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 대화 관련 도메인 엔티티입니다.
 * - Thread(1) : Chat(N) 관계이며, 하나의 세션 단위로 묶여 관리됩니다.
 * - 강한 연관성(Composition)을 가지므로 응집도를 위해 하나의 파일에서 관리하도록 하였습니다.
 */
@Entity
@Table(name = "tb_threads")
class Thread(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
) : BaseEntity() {
    @OneToMany(mappedBy = "thread", cascade = [CascadeType.ALL], orphanRemoval = true)
    val chats: MutableList<Chat> = mutableListOf()

    /**
     * 스레드의 마지막 활동 시간을 명시적으로 갱신합니다.
     */
    fun touch() {
        this.updatedAt = LocalDateTime.now()
    }
}

@Entity
@Table(name = "chats")
class Chat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    val thread: Thread,

    @Column(columnDefinition = "TEXT", nullable = false)
    val question: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    var answer: String
) : BaseEntity()
