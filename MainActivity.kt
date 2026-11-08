package com.projectjarvis

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var status: TextView
    private lateinit var tts: TextToSpeech
    private var recognizer: SpeechRecognizer? = null
    private val requestAudio = 41

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 48, 32, 48)
        }
        val title = TextView(this).apply {
            text = "J.A.R.V.I.S. 2.0"
            textSize = 30f
            gravity = Gravity.CENTER
        }
        status = TextView(this).apply {
            text = "প্রস্তুত। নিচের বোতাম চাপুন এবং কথা বলুন।"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 32)
        }
        val listen = Button(this).apply {
            text = "কথা বলুন"
            setOnClickListener { startListening() }
        }
        layout.addView(title)
        layout.addView(status)
        layout.addView(listen)
        setContentView(layout)

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), requestAudio)
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "এই ফোনে Speech Recognition service পাওয়া যায়নি।"
            return
        }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { status.text = "শুনছি..." }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { status.text = "কথা প্রক্রিয়া করছি..." }
            override fun onError(error: Int) { status.text = "শোনা যায়নি। আবার চেষ্টা করুন।" }
            override fun onResults(results: Bundle?) {
                val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull().orEmpty()
                status.text = if (spoken.isBlank()) "কোনো কথা শনাক্ত হয়নি।" else "তুমি বলেছ: $spoken"
                if (spoken.isNotBlank()) speak("আমি শুনেছি: $spoken")
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "JARVIS-কে বলুন")
        }
        recognizer?.startListening(intent)
    }

    private fun speak(message: String) {
        tts.language = Locale("bn", "IN")
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jarvis_reply")
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) {
            tts.language = Locale("bn", "IN")
        }
    }

    override fun onDestroy() {
        recognizer?.destroy()
        tts.shutdown()
        super.onDestroy()
    }
}
