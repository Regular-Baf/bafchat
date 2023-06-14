package baf.chat

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import java.io.FileWriter

object Bafmod3ConfigHandler {
    private const val CONFIG_PATH = "./config/Bafmod3Config.json"

    fun createConfigFileIfNeeded() {
        val configFile = File(CONFIG_PATH)

        if (!configFile.exists()) {
            val defaultConfigContent = """
                {
                  "apiUrl": "http://localhost:8000/completions",
                  "prompt": "You are a helpful Minecraft assistant who helps answer the player's questions with short, concise, but friendly answers in 60 completion_tokens or less.\\n<human>: Hey can you help me?\\n<bot>: Sure, let me know what you need help with, and I'll do my best to help.\\n<human>: {{message}}\\n<bot>:",
                  "stream": true,
                  "max_tokens": 64,
                  "temperature": 0.5,
                  "top_p": 0.9,
                  "bafchatName": "Bafchat"
                }
            """.trimIndent()

            try {
                FileWriter(configFile).use { writer ->
                    writer.write(defaultConfigContent)
                }
                println("Config file created: $CONFIG_PATH")
            } catch (e: Exception) {
                println("Failed to create the config file: $CONFIG_PATH")
                e.printStackTrace()
            }
        }
    }

    fun getConfigJson(): JsonObject {
        val configFile = File(CONFIG_PATH)

        if (!configFile.exists()) {
            println("Config file not found: $CONFIG_PATH")
            return JsonObject()
        }

        val configFileContents = configFile.readText()
        return Gson().fromJson(configFileContents, JsonObject::class.java)
    }
}
