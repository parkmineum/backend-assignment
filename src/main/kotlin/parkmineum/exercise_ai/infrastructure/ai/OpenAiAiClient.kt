package parkmineum.exercise_ai.infrastructure.ai

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Primary
@Component
class OpenAiAiClient(
    @Value("\${ai.openai.api-key}") private val apiKey: String,
    @Value("\${ai.openai.base-url:https://api.openai.com/v1}") private val baseUrl: String,
    @Value("\${ai.openai.model:gpt-4o-mini}") private val defaultModel: String
) : AiClient {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.builder().baseUrl(baseUrl).build()

    override fun chat(prompt: String, history: List<ChatMessage>, model: String?): String? {
        try {
            val messages = buildMessages(prompt, history)
            val request = mapOf(
                "model" to (model ?: defaultModel),
                "messages" to messages
            )

            val response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map::class.java)

            return extractText(response)
        } catch (e: Exception) {
            logger.error("OpenAI API 채팅 요청 중 오류 발생", e)
            return null
        }
    }

    override fun chatStream(prompt: String, history: List<ChatMessage>, model: String?): Any? {
        return null
    }

    private fun buildMessages(prompt: String, history: List<ChatMessage>): List<Map<String, String>> {
        val messages = history.map { mapOf("role" to it.role.name.lowercase(), "content" to it.content) }.toMutableList()
        messages.add(mapOf("role" to "user", "content" to prompt))
        return messages
    }

    private fun extractText(response: Map<*, *>?): String? {
        return (response?.get("choices") as? List<*>)?.firstOrNull()?.let {
            val choice = it as? Map<*, *>
            val message = choice?.get("message") as? Map<*, *>
            message?.get("content") as? String
        }
    }
}
