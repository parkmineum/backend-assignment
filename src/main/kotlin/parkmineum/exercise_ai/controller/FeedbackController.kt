package parkmineum.exercise_ai.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import parkmineum.exercise_ai.common.ApiResponse
import parkmineum.exercise_ai.dto.FeedbackRequest
import parkmineum.exercise_ai.dto.FeedbackResponse
import parkmineum.exercise_ai.dto.FeedbackStatusUpdate
import parkmineum.exercise_ai.service.feedback.FeedbackService

@Tag(name = "Feedback", description = "피드백 관리 API")
@RestController
@RequestMapping("/api/v1/feedbacks")
class FeedbackController(
    private val feedbackService: FeedbackService
) {

    @Operation(summary = "피드백 생성", description = "대화 메시지에 대해 긍정/부정 피드백을 남깁니다.")
    @PostMapping
    fun createFeedback(
        @AuthenticationPrincipal email: String,
        @Valid @RequestBody request: FeedbackRequest
    ): ApiResponse<FeedbackResponse> {
        val response = feedbackService.createFeedback(email, request)
        return ApiResponse.success(response)
    }

    @Operation(summary = "피드백 목록 조회", description = "사용자는 본인의 피드백을, 관리자는 전체 피드백을 조회합니다.")
    @GetMapping
    fun getAllFeedbacks(
        @AuthenticationPrincipal email: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "createdAt,desc") sort: String
    ): ApiResponse<org.springframework.data.domain.Page<FeedbackResponse>> {
        val sortParams = sort.split(",")
        val property = sortParams[0]
        val direction = if (sortParams.size > 1) org.springframework.data.domain.Sort.Direction.fromString(sortParams[1]) else org.springframework.data.domain.Sort.Direction.DESC
        
        val pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, property))
        val response = feedbackService.getAllFeedbacks(email, pageable)
        return ApiResponse.success(response)
    }

    @Operation(summary = "피드백 상태 변경", description = "관리자가 피드백 상태(PENDING/RESOLVED)를 변경합니다.")
    @PatchMapping("/{feedbackId}/status")
    fun updateFeedbackStatus(
        @AuthenticationPrincipal email: String,
        @PathVariable feedbackId: Long,
        @Valid @RequestBody request: FeedbackStatusUpdate
    ): ApiResponse<FeedbackResponse> {
        val response = feedbackService.updateFeedbackStatus(email, feedbackId, request.status)
        return ApiResponse.success(response)
    }
}
