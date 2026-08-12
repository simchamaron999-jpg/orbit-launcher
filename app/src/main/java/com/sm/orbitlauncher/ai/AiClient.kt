package com.sm.orbitlauncher.ai

import com.sm.orbitlauncher.data.AiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * BYOK client for OpenAI, Anthropic, Google Gemini, OpenRouter, and a user-supplied endpoint.
 * API keys are supplied by the user and remain in the app's private preferences.
 */
class AiClient(
    private val provider: AiProvider,
    private val apiKey: String,
    private val customEndpoint: String? = null
) {
    suspend fun complete(prompt: String, systemPrompt: String? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            val endpoint = customEndpoint ?: provider.endpoint
                ?: return@withContext Result.failure(Exception("Endpoint not configured"))
            val url = URL(
                if (provider == AiProvider.GOOGLE) {
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
                } else {
                    "$endpoint/chat/completions"
                }
            )

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.setRequestProperty("Content-Type", "application/json")

            when (provider) {
                AiProvider.ANTHROPIC -> {
                    connection.setRequestProperty("x-api-key", apiKey)
                    connection.setRequestProperty("anthropic-version", "2023-06-01")
                }
                AiProvider.GOOGLE -> Unit
                else -> connection.setRequestProperty("Authorization", "Bearer $apiKey")
            }

            val body = buildRequestBody(prompt, systemPrompt)
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Result.success(extractText(JSONObject(response)))
            } else {
                val detail = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                Result.failure(Exception("HTTP ${connection.responseCode}: $detail"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildRequestBody(prompt: String, systemPrompt: String?): String = when (provider) {
        AiProvider.GOOGLE -> {
            JSONObject().apply {
                put(
                    "contents",
                    JSONArray().put(
                        JSONObject().put(
                            "parts",
                            JSONArray().put(
                                JSONObject().put("text", listOfNotNull(systemPrompt, prompt).joinToString("\n\n"))
                            )
                        )
                    )
                )
            }.toString()
        }
        AiProvider.ANTHROPIC -> {
            JSONObject().apply {
                put("model", "claude-3-5-sonnet-20240620")
                put("max_tokens", 512)
                systemPrompt?.takeIf { it.isNotBlank() }?.let { put("system", it) }
                put(
                    "messages",
                    JSONArray().put(
                        JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        }
                    )
                )
            }.toString()
        }
        else -> {
            JSONObject().apply {
                put("model", "gpt-4o")
                put(
                    "messages",
                    JSONArray().apply {
                        systemPrompt?.takeIf { it.isNotBlank() }?.let { instruction ->
                            put(JSONObject().put("role", "system").put("content", instruction))
                        }
                        put(JSONObject().put("role", "user").put("content", prompt))
                    }
                )
            }.toString()
        }
    }

    private fun extractText(json: JSONObject): String = when (provider) {
        AiProvider.GOOGLE -> json.getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
        AiProvider.ANTHROPIC -> json.getJSONArray("content").getJSONObject(0).getString("text")
        else -> json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
    }
}
