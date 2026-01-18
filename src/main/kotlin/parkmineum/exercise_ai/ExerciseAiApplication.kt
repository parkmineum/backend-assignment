package parkmineum.exercise_ai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@EnableJpaAuditing
@SpringBootApplication
class ExerciseAiApplication

fun main(args: Array<String>) {
	runApplication<ExerciseAiApplication>(*args)
}
