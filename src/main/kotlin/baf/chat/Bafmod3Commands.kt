package baf.chat

import baf.chat.Bafmod3Sender.sendToAIChatbot
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import java.io.File
import java.io.FileWriter

object Bafmod3Commands {
    private const val CONFIG_PATH = "./config/Bafmod3Config.json"

    fun registerCommands(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val localAiCommand = literal<ServerCommandSource>("bafchat")
            .then(literal<ServerCommandSource>("set")
                .then(CommandManager.argument("key", StringArgumentType.word())
                    .suggests(suggestConfigKeys())
                    .then(CommandManager.argument("value", StringArgumentType.greedyString())
                        .executes { context ->
                            val key = StringArgumentType.getString(context, "key")
                            val value = StringArgumentType.getString(context, "value")
                            updateConfigValue(key, value, context.source)
                            1
                        })
                )
            )
            .then(CommandManager.argument("message", StringArgumentType.greedyString())
                .executes { context ->
                    val message = StringArgumentType.getString(context, "message")
                    sendToAIChatbot(message)
                    1
                })
            .then(literal<ServerCommandSource>("list")
                .executes { context ->
                    listConfigValues(context.source)
                    1
                }
            )
            .then(literal<ServerCommandSource>("reset")
                .executes { context ->
                    resetConfig(context.source)
                    1
                }
            )

        val llmCommand = literal<ServerCommandSource>("llm")
            .then(CommandManager.argument("message", StringArgumentType.greedyString())
                .executes { context ->
                    val message = StringArgumentType.getString(context, "message")
                    sendToAIChatbot(message)
                    1
                })

        dispatcher.register(localAiCommand)
        dispatcher.register(llmCommand)
    }

    private fun updateConfigValue(key: String, value: String, source: ServerCommandSource) {
        val configFile = File(CONFIG_PATH)

        if (!configFile.exists()) {
            Bafmod3Deliver.sendChatMessage("Config file not found: $CONFIG_PATH", Bafmod3Deliver.MessageType.BAFCHAT_MESSAGE)
            return
        }

        val configFileContents = configFile.readText()
        val configJson = JsonParser.parseString(configFileContents).asJsonObject

        if (!configJson.has(key)) {
            Bafmod3Deliver.sendChatMessage("Key '$key' does not exist in the config file.", Bafmod3Deliver.MessageType.BAFCHAT_MESSAGE)
            return
        }

        when (key) {
            "stream" -> {
                if (value !in listOf("true", "false")) {
                    Bafmod3Deliver.sendChatMessage("Invalid value for '$key'. Allowed values: true, false", Bafmod3Deliver.MessageType.BAFCHAT_MESSAGE)
                    return
                }
            }
            "max_tokens" -> {
                val intValue = value.toIntOrNull()
                if (intValue == null || intValue < 0) {
                    Bafmod3Deliver.sendChatMessage("Invalid value for '$key'. Expected a non-negative integer.", Bafmod3Deliver.MessageType.BAFCHAT_MESSAGE)
                    return
                }
            }
            "temperature", "top_p" -> {
                val doubleValue = value.toDoubleOrNull()
                if (doubleValue == null || doubleValue <= 0 || doubleValue > 1) {
                    Bafmod3Deliver.sendChatMessage("Invalid value for '$key'. Expected a double between 0 and 1 (exclusive).", Bafmod3Deliver.MessageType.BAFCHAT_MESSAGE)
                    return
                }
            }
        }

        configJson.addProperty(key, value)

        try {
            val gson = Gson()
            val prettyJson = gson.toJson(configJson)

            if (prettyJson == configFileContents) {
                Bafmod3Deliver.sendChatMessage("No changes were made to the config file for key '$key'.", Bafmod3Deliver.MessageType.BAFCHAT_MESSAGE)
                return
            }

            FileWriter(configFile).use { writer ->
                writer.write(prettyJson)
            }
            Bafmod3Deliver.sendChatMessage("Config value updated: $key = $value", Bafmod3Deliver.MessageType.SUCCESS_MESSAGE)
        } catch (e: Exception) {
            Bafmod3Deliver.sendChatMessage("Failed to update config value: $key", Bafmod3Deliver.MessageType.BAFCHAT_MESSAGE)
            e.printStackTrace()
        }
    }

    private fun suggestConfigKeys(): SuggestionProvider<ServerCommandSource> {
        return SuggestionProvider { context, builder ->
            val configJson = Bafmod3ConfigHandler.getConfigJson()
            val keys = configJson.keySet()
            val remaining = builder.remaining.lowercase()
            keys.filter { it.startsWith(remaining) }.forEach(builder::suggest)
            builder.buildFuture()
        }
    }

    private fun listConfigValues(source: ServerCommandSource) {
        val configFile = File(CONFIG_PATH)

        if (!configFile.exists()) {
            Bafmod3Deliver.sendChatMessage("Config file not found: $CONFIG_PATH", Bafmod3Deliver.MessageType.BAFCHAT_MESSAGE)
            return
        }

        val configFileContents = configFile.readText()
        val configJson = JsonParser.parseString(configFileContents).asJsonObject

        val configList = configJson.entrySet().joinToString("\n") { (key, value) ->
            "$key = $value"
        }

        Bafmod3Deliver.sendChatMessage("Current config values:\n$configList", Bafmod3Deliver.MessageType.SUCCESS_MESSAGE)
    }

    private fun resetConfig(source: ServerCommandSource) {
        val configFile = File(CONFIG_PATH)

        if (!configFile.exists()) {
            Bafmod3Deliver.sendChatMessage("Config file not found: $CONFIG_PATH", Bafmod3Deliver.MessageType.BAFCHAT_MESSAGE)
            return
        }

        try {
            configFile.delete()
            Bafmod3ConfigHandler.createConfigFileIfNeeded()
            Bafmod3Deliver.sendChatMessage("Config file reset.", Bafmod3Deliver.MessageType.SUCCESS_MESSAGE)
        } catch (e: Exception) {
            Bafmod3Deliver.sendChatMessage("Failed to reset the config file.", Bafmod3Deliver.MessageType.BAFCHAT_MESSAGE)
            e.printStackTrace()
        }
    }
}
