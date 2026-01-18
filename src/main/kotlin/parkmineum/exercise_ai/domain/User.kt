package parkmineum.exercise_ai.domain

import jakarta.persistence.*

@Entity
@Table(name = "tb_users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole
) : BaseEntity()

enum class UserRole {
    MEMBER, ADMIN
}