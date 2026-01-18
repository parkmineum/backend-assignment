package parkmineum.exercise_ai.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import parkmineum.exercise_ai.common.ApiResponse
import parkmineum.exercise_ai.service.analysis.AnalysisService

@Tag(name = "Analysis", description = "관리자 전용 분석 및 보고서 API")
@RestController
@RequestMapping("/api/v1/analysis")
class AnalysisController(
    private val analysisService: AnalysisService
) {

    @Operation(summary = "일일 활동 요약", description = "오늘 생성된 유저, 스레드, 대화 수를 조회합니다. (관리자 전용)")
    @GetMapping("/summary")
    fun getSummary(@AuthenticationPrincipal email: String): ApiResponse<Map<String, Any>> {
        val response = analysisService.getDailySummary(email)
        return ApiResponse.success(response)
    }

    @Operation(summary = "대화 보고서 다운로드 (CSV)", description = "전체 대화 내역을 CSV 파일로 다운로드합니다. (관리자 전용)")
    @GetMapping("/report/csv")
    fun downloadCsv(@AuthenticationPrincipal email: String): ResponseEntity<ByteArray> {
        val csvData = analysisService.generateReportCsv(email)
        val fileName = "chat_report_${java.time.LocalDate.now()}.csv"
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csvData.toByteArray(Charsets.UTF_8))
    }
}
