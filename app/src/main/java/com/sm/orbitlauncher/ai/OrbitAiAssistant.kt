package com.sm.orbitlauncher.ai

import com.sm.orbitlauncher.data.LaunchableApp
import org.json.JSONObject

/**
 * High-level assistant that translates natural language into launcher actions.
 */
class OrbitAiAssistant(private val client: AiClient) {
    
    suspend fun resolveSearch(query: String, apps: List<LaunchableApp>): LaunchableApp? {
        val appList = apps.joinToString("\n") { "- ${it.label} (${it.packageName})" }
        val prompt = """
            User query: "$query"
            Available apps:
            $appList
            
            Identify the single best app the user is looking for. Return only the package name. 
            If no app matches, return "NONE".
        """.trimIndent()
        
        val result = client.complete(prompt)
        val packageName = result.getOrNull()?.trim() ?: return null
        return apps.find { it.packageName == packageName }
    }

    suspend fun parseCommand(command: String): AiCommand {
        val prompt = """
            User command: "$command"
            
            Identify the intent. Available intents: MESSAGE, ALARM, STUDY_MODE, SEARCH, UNKNOWN.
            Return a JSON object: {"intent": "INTENT_NAME", "payload": "details"}.
        """.trimIndent()
        
        val result = client.complete(prompt)
        return try {
            val json = JSONObject(result.getOrNull() ?: "{}")
            AiCommand(json.getString("intent"), json.optString("payload"))
        } catch (e: Exception) {
            AiCommand("UNKNOWN", null)
        }
    }
}

data class AiCommand(val intent: String, val payload: String?)
