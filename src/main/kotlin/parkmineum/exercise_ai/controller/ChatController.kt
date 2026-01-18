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
import parkmineum.exercise_ai.dto.ChatRequest
import parkmineum.exercise_ai.dto.ChatResponse
import parkmineum.exercise_ai.dto.ThreadResponse
import parkmineum.exercise_ai.service.chat.ChatService

@Tag(name = "Chat", description = "대화 관리 API")
@RestController
@RequestMapping("/api/v1/chats")
class ChatController(
    private val chatService: ChatService
) {

    @Operation(summary = "대화 생성", description = "AI에게 질문을 보내고 답변을 생성합니다. 30분 이내 질문 시 기존 스레드가 유지됩니다.")
    @PostMapping
    fun createChat(
        @AuthenticationPrincipal email: String,
        @Valid @RequestBody request: ChatRequest
    ): ApiResponse<ChatResponse> {
        val response = chatService.createChat(email, request)
        return ApiResponse.success(response)
    }

    @Operation(summary = "대화 목록 조회", description = "유저의 모든 대화를 스레드 단위로 그룹화하여 조회합니다. 관리자는 모든 대화를 볼 수 있습니다.")
    @GetMapping
    fun getAllChats(
        @AuthenticationPrincipal email: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "updatedAt,desc") sort: String
    ): ApiResponse<org.springframework.data.domain.Page<ThreadResponse>> {
        val sortParams = sort.split(",")
        val property = sortParams[0]
        val direction = if (sortParams.size > 1) org.springframework.data.domain.Sort.Direction.fromString(sortParams[1]) else org.springframework.data.domain.Sort.Direction.DESC
        
        val pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, property))
        val response = chatService.getAllThreads(email, pageable)
        return ApiResponse.success(response)
    }

    @Operation(summary = "스레드 삭제", description = "특정 스레드와 그 하위 대화 내역 전체를 삭제합니다.")
    @DeleteMapping("/threads/{threadId}")
    fun deleteThread(
        @AuthenticationPrincipal email: String,
        @PathVariable threadId: Long
    ): ApiResponse<Unit> {
        chatService.deleteThread(email, threadId)
        return ApiResponse.success(Unit)
    }
}