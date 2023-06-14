package baf.chat

import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object Bafmod3Sender {
    fun sendToAIChatbot(message: String) {
        Bafmod3Deliver.sendChatMessage(message, Bafmod3Deliver.MessageType.USER_MESSAGE) // Call sendChatMessage() before sending the message to the API

        val configJson = Bafmod3ConfigHandler.getConfigJson()
        val apiUrl = configJson.getAsJsonPrimitive("apiUrl")?.asString
        val prompt = configJson.getAsJsonPrimitive("prompt")?.asString
        val stream = configJson.getAsJsonPrimitive("stream")?.asBoolean
        val maxTokens = configJson.getAsJsonPrimitive("max_tokens")?.asInt
        val temperature = configJson.getAsJsonPrimitive("temperature")?.asDouble
        val topP = configJson.getAsJsonPrimitive("top_p")?.asDouble

        if (apiUrl == null) {
            Bafmod3Deliver.sendChatMessage("apiUrl not found in the config file", Bafmod3Deliver.MessageType.BAFCHAT_MESSAGE)
            return
        }

        if (prompt == null) {
            Bafmod3Deliver.sendChatMessage("prompt not found in the config file", Bafmod3Deliver.MessageType.BAFCHAT_MESSAGE)
            return
        }

        val formattedPrompt = prompt.replace("{{message}}", message)
        println("Formatted Prompt: $formattedPrompt")

        GlobalScope.launch(Dispatchers.IO) {
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true

            val postData = """
                {
                    "prompt": "$formattedPrompt",
                    "stream": $stream,
                    "max_tokens": $maxTokens,
                    "temperature": $temperature,
                    "top_p": $topP
                }
            """.trimIndent()

            val postDataBytes = postData.toByteArray(StandardCharsets.UTF_8)
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Content-Length", postDataBytes.size.toString())

            val outputStream = DataOutputStream(connection.outputStream)
            outputStream.write(postDataBytes)
            outputStream.flush()
            outputStream.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseStringBuilder = StringBuilder()

                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String? = reader.readLine()

                while (line != null) {
                    if (line.startsWith("data: ")) {
                        val responseData = line.substring("data: ".length)
                        println("Received response data: $responseData")

                        if (responseData == "[DONE]") {
                            // Handle the completion of the response stream
                            break
                        }

                        try {
                            val jsonResponse = JsonParser.parseString(responseData).asJsonObject
                            val choicesArray = jsonResponse.getAsJsonArray("choices")

                            if (choicesArray != null && choicesArray.size() > 0) {
                                val choice = choicesArray[0].asJsonObject
                                val choiceText = choice.getAsJsonPrimitive("text").asString
                                responseStringBuilder.append(choiceText).append("")
                            }
                        } catch (e: Exception) {
                            Bafmod3Deliver.sendChatMessage("Error processing the API response: ${e.message}", Bafmod3Deliver.MessageType.BAFCHAT_MESSAGE)
                        }
                    }

                    line = reader.readLine()
                }

                reader.close()

                val responseString = responseStringBuilder.toString()
                println("Response: $responseString")

                Bafmod3Deliver.sendChatMessage(responseString, Bafmod3Deliver.MessageType.RESPONSE_MESSAGE)

                println("Message successfully sent to the API.")
            } else {
                Bafmod3Deliver.sendChatMessage("Failed to send the message. Response code: $responseCode", Bafmod3Deliver.MessageType.BAFCHAT_MESSAGE)
                // Handle the error if needed
            }

            connection.disconnect()
        }
    }
}
