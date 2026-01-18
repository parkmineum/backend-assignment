package parkmineum.exercise_ai.service.analysis

import com.opencsv.CSVWriter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import parkmineum.exercise_ai.common.BusinessException
import parkmineum.exercise_ai.common.ErrorCode
import parkmineum.exercise_ai.domain.UserRole
import parkmineum.exercise_ai.repository.ChatRepository
import parkmineum.exercise_ai.repository.ThreadRepository
import parkmineum.exercise_ai.repository.UserRepository
import java.io.StringWriter
import java.time.LocalDate

/**
 * 관리자용 시스템 이용 현황 분석 및 보고서 생성을 담당하는 서비스
 * 
 * [설계 의도]
 * - 관심사 분리: 핵심 비즈니스 로직과 통계/분석 로직을 분리하여 각 도메인의 목적에 집중
 * - 데이터 가공: 텍스트 기반의 대화 데이터를 CSV 등의 리포트 형태로 가공하여 제공
 */
@Service
class AnalysisService(
    private val userRepository: UserRepository,
    private val threadRepository: ThreadRepository,
    private val chatRepository: ChatRepository
) {

    @Transactional(readOnly = true)
    fun getDailySummary(email: String): Map<String, Any> {
        val user = userRepository.findByEmail(email) ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
        if (user.role != UserRole.ADMIN) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        val startOfToday = LocalDate.now().atStartOfDay()
        
        return mapOf(
            "date" to LocalDate.now().toString(),
            "newUsersCount" to userRepository.countByCreatedAtAfter(startOfToday),
            "newThreadsCount" to threadRepository.countByCreatedAtAfter(startOfToday),
            "newChatsCount" to chatRepository.countByCreatedAtAfter(startOfToday)
        )
    }



    @Transactional(readOnly = true)
    fun generateReportCsv(email: String): String {
        val user = userRepository.findByEmail(email) ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
        if (user.role != UserRole.ADMIN) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        val chats = chatRepository.findAll()
        val stringWriter = StringWriter()
        val csvWriter = CSVWriter(stringWriter)

        // Header
        csvWriter.writeNext(arrayOf("ID", "ThreadID", "Question", "Answer", "CreatedAt"))

        // Data
        chats.forEach {
            csvWriter.writeNext(arrayOf(
                it.id.toString(),
                it.thread.id.toString(),
                it.question,
                it.answer,
                it.createdAt.toString()
            ))
        }

        csvWriter.close()
        return stringWriter.toString()
    }
}
