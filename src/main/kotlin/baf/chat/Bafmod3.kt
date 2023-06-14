package baf.chat

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback

class Bafmod3 : ModInitializer {
    override fun onInitialize() {
        Bafmod3ConfigHandler.createConfigFileIfNeeded()
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, _ ->
            Bafmod3Commands.registerCommands(dispatcher)
        })
    }
}
