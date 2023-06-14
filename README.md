# Bafchat
While there are already plenty of mods around which bring OpenAI's API into Minecraft, there hasn't yet been the same abundance of mods seeking to bring local LLMs into the game. Bafchat is here to save the day! Ask your new AI buddy questions, or maybe get some help when you've forgotten a command or crafting recipe. If your model can do it, Bafchat can do it too (provided you have the prompt correctly configured in Bafmod3Config.json).

Also, OpenAI's API doesn't have unlimited free use, so once you run out of credits you have to pay. Bafchat is powered by LLMs hosted on your own computer (or local network), so you can experiment without worry of racking up charges.
## Default Configuration
By defualt, Bafchat is pre-configured to work with [local.ai's server](https://github.com/louisgv/local.ai), but I believe it should work with others such as those provided by Kobold and Oobabooga).
The recommended model as of right now is [RedPajama-INCITE-Chat-3B-v1](https://huggingface.co/rustformers/redpajama-3b-ggml/blob/main/RedPajama-INCITE-Chat-3B-v1-q4_0.bin) because it is a small model that processes prompts relatively fast on CPU, however as a trade-off, its responses leave much to be desired.

**NOTE:** If you do elect to choose a different model, you will have to modify the prompting style in Bafmod3Config.json, otherwise you may get unextpected results.

**OTHER NOTE:** When editing the prompt in Bafmod3Config.json, please ensure you keep the placeholder {{message}} in the prompt, placing it where you want your in-game messages to go in the template.
## Features
* A mostly functional frontend for locally hosted APIs of large language models.
* Use /llm to send a message to your model.
* Prompts and API URL can be adjusted in the Bafmod3Config.json located in your .minecraft/config
### Requirements
* A locally hosted API for an LLM
* Fabric Kotlin
* Fabric API
* Fabric
* Minecraft 1.20 and newer
#### Slight issues
* If the API is hosted on the same machine as your Minecraft server (which includes the client if playing singleplayer) and there is no system in place to limit CPU utilisation below 100% for the LLM's server, your game may temporarily slow down while the prompt is processed.
