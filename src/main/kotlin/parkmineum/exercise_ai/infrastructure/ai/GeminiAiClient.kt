package parkmineum.exercise_ai.infrastructure.ai

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Primary
@Component
class GeminiAiClient(
    @Value("\${ai.gemini.api-key}") private val apiKey: String,
    @Value("\${ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") private val baseUrl: String,
    @Value("\${ai.gemini.model:gemini-1.5-flash}") private val defaultModel: String
) : AiClient {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.builder().baseUrl(baseUrl).build()

    override fun chat(prompt: String, history: List<ChatMessage>, model: String?): String? {
        try {
            val targetModel = model ?: defaultModel
            val contents = buildContents(prompt, history)
            val request = mapOf("contents" to contents)

            val response = restClient.post()
                .uri { uriBuilder ->
                    uriBuilder.path("/models/$targetModel:generateContent")
                        .queryParam("key", apiKey)
                        .build()
                }
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map::class.java)

            return extractText(response)
        } catch (e: Exception) {
            logger.error("Gemini API 채팅 요청 중 오류 발생", e)
            return null
        }
    }

    override fun chatStream(prompt: String, history: List<ChatMessage>, model: String?): Any? {
        return null
    }

    private fun buildContents(prompt: String, history: List<ChatMessage>): List<Map<String, Any>> {
        val contents = history.map { 
            val role = if (it.role == Role.USER) "user" else "model"
            mapOf(
                "role" to role,
                "parts" to listOf(mapOf("text" to it.content))
            )
        }.toMutableList()

        contents.add(
            mapOf(
                "role" to "user",
                "parts" to listOf(mapOf("text" to prompt))
            )
        )
        return contents
    }

    private fun extractText(response: Map<*, *>?): String? {
        val candidates = response?.get("candidates") as? List<*>
        val firstCandidate = candidates?.firstOrNull() as? Map<*, *>
        val content = firstCandidate?.get("content") as? Map<*, *>
        val parts = content?.get("parts") as? List<*>
        val firstPart = parts?.firstOrNull() as? Map<*, *>
        return firstPart?.get("text") as? String
    }
}
