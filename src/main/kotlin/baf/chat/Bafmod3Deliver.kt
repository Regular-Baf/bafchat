package baf.chat

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

object Bafmod3Deliver {
    fun sendChatMessage(message: String, messageType: MessageType = MessageType.USER_MESSAGE) {
        val minecraftClient = MinecraftClient.getInstance()
        val playerName = minecraftClient.player?.name?.toString()?.replace(Regex("literal\\{(.*?)\\}"), "$1") ?: ""

        val configJson = Bafmod3ConfigHandler.getConfigJson()
        val bafchatName = configJson.get("bafchatName")?.asString ?: "Bafchat"

        val formattedMessage = when (messageType) {
            MessageType.USER_MESSAGE -> "§3[$playerName] §f$message"
            MessageType.RESPONSE_MESSAGE -> "§9[$bafchatName] §f$message"
            MessageType.BAFCHAT_MESSAGE -> "§c[Bafchat Error] §e$message"
            MessageType.SUCCESS_MESSAGE -> "§a[Bafchat Success] §f$message"
        }

        val chatText = Text.of(formattedMessage)
        minecraftClient.inGameHud.chatHud.addMessage(chatText)
    }

    enum class MessageType {
        USER_MESSAGE,
        RESPONSE_MESSAGE,
        BAFCHAT_MESSAGE,
        SUCCESS_MESSAGE
    }
}
