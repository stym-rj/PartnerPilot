package com.example.gemniapi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.gemniapi.database.ChatMessage
import com.example.gemniapi.database.ChatMessagesDatabase
import com.example.gemniapi.databinding.ActivityMainBinding
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: ChatMessagesDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        val apiKey = BuildConfig.apiKey
        val generativeModel = GenerativeModel(
            // Use a model that's applicable for your use case (see "Implement basic use cases" below)
            modelName = "gemini-pro",
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = apiKey
        )

        thread {
            database = Room.databaseBuilder(
                this@MainActivity,
                ChatMessagesDatabase::class.java,
                "chatMessagesDB"
            ).build()
        }

        binding.btnSend.setOnClickListener {
            val prompt = binding.tietPrompt.text
            if (prompt.isNullOrBlank()) {
                Toast.makeText(this@MainActivity, "Please enter a prompt!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = generativeModel.generateContent(prompt.toString())
                    binding.tvResponse.text = response.text
                    var chat = ChatMessage(text = prompt.toString(), sender = "Me", timeStamp = System.currentTimeMillis())
                    database.chatMessagesDao().insert(chat)
                    chat = ChatMessage(text = response.text.toString(), sender = "Gemini", timeStamp = System.currentTimeMillis())
                    database.chatMessagesDao().insert(chat)

                    binding.tietPrompt.text?.clear()
                } catch (e : Exception) {
                    Toast.makeText(this@MainActivity, "Cannot generate text with this prompt", Toast.LENGTH_LONG).show()
                }
            }
        }





    }
}