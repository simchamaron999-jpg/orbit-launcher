package com.manus.orbitlauncher.voice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.manus.orbitlauncher.data.LaunchableApp
import java.text.Normalizer
import java.util.Locale

/** One-shot, user-initiated speech recognition for app launching. */
class VoiceAppLauncher(private val context: Context) {
    private var recognizer: SpeechRecognizer? = null

    fun start(
        apps: List<LaunchableApp>,
        onListening: () -> Unit,
        onPartialPhrase: (String) -> Unit,
        onResolved: (LaunchableApp, String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onFailure("Speech recognition is not available on this device")
            return
        }
        release()
        recognizer = createRecognizer()
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle?) = onListening()
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit

            override fun onError(error: Int) {
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "There was a microphone problem"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech service is unavailable right now"
                    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I did not catch an app name"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognition is busy. Try again."
                    else -> "Voice launch could not start. Try again."
                }
                release()
                onFailure(message)
            }

            override fun onResults(results: android.os.Bundle?) {
                val phrases = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                val candidate = phrases.firstNotNullOfOrNull { phrase ->
                    findBestMatch(phrase, apps)?.let { it to phrase }
                }
                release()
                if (candidate == null) {
                    onFailure(if (phrases.isEmpty()) "I did not catch an app name" else "No installed app matched \"${phrases.first()}\"")
                } else {
                    onResolved(candidate.first, candidate.second)
                }
            }

            override fun onPartialResults(partialResults: android.os.Bundle?) {
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?.let(onPartialPhrase)
            }

            override fun onEvent(eventType: Int, params: android.os.Bundle?) = Unit
        })
        recognizer?.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            }
        )
    }

    /** Captures one spoken request without applying app-name matching. */
    fun startTranscript(
        onListening: () -> Unit,
        onPartialPhrase: (String) -> Unit,
        onResult: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onFailure("Speech recognition is not available on this device")
            return
        }
        release()
        recognizer = createRecognizer()
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle?) = onListening()
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit

            override fun onError(error: Int) {
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "There was a microphone problem"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech service is unavailable right now"
                    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I did not catch your request"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognition is busy. Try again."
                    else -> "AI voice could not start. Try again."
                }
                release()
                onFailure(message)
            }

            override fun onResults(results: android.os.Bundle?) {
                val phrase = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.trim()
                release()
                if (phrase.isNullOrBlank()) onFailure("I did not catch your request") else onResult(phrase)
            }

            override fun onPartialResults(partialResults: android.os.Bundle?) {
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?.let(onPartialPhrase)
            }

            override fun onEvent(eventType: Int, params: android.os.Bundle?) = Unit
        })
        recognizer?.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
        )
    }

    fun cancel() {
        recognizer?.cancel()
        release()
    }

    fun release() {
        recognizer?.destroy()
        recognizer = null
    }

    private fun createRecognizer(): SpeechRecognizer {
        return if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        ) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }
    }

    private fun findBestMatch(phrase: String, apps: List<LaunchableApp>): LaunchableApp? {
        val spoken = normalize(removeLaunchCommand(phrase))
        if (spoken.isBlank()) return null
        return apps.map { app -> app to score(spoken, normalize(app.label)) }
            .filter { it.second > 0 }
            .maxWithOrNull(compareBy<Pair<LaunchableApp, Int>> { it.second }.thenBy { -it.first.label.length })
            ?.takeIf { it.second >= MIN_MATCH_SCORE }
            ?.first
    }

    private fun score(spoken: String, label: String): Int = when {
        spoken == label -> 1000
        label.startsWith(spoken) || spoken.startsWith(label) -> 850
        label.contains(spoken) || spoken.contains(label) -> 700
        spoken.split(' ').any { token -> token.length > 2 && label.split(' ').contains(token) } -> 500
        else -> 0
    }

    private fun removeLaunchCommand(value: String): String = value
        .replace(Regex("^(open|launch|start|run)\\s+", RegexOption.IGNORE_CASE), "")

    private fun normalize(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}"), "")
        .lowercase(Locale.getDefault())
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()

    fun launch(app: LaunchableApp): Boolean = runCatching {
        context.startActivity(Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName(app.packageName, app.activityName)
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }.isSuccess

    private companion object {
        const val MIN_MATCH_SCORE = 500
    }
}
