# SimpleDiscordSoundBot
This is a simple discord bot that plays audio by capturing it from the sound card. It's a minified and feature-reduced version of a private project.
It may be useful eg. for RPG Dungeon Masters that are playing remotely via discord, enabling them to play sounds through this bot.  
It uses the Discord API and needs an input line that fits the audio format requirements of the API, such as Virtual Audio Cable.

# Setup
You will first need to install maven. Then run setup.sh to install dependencies and build. Then to start run start.sh.

Navigate to cfg, copy the example config and rename it to "config.json".

The channel ids determine in which channel the bot will spawn and send messages. The bot token is the token you receive when creating a bot here:
https://discord.com/developers/docs/intro

For the audio setup you probably need to install VirtualAudioCable because reading audio streams only easily works with capturing devices.
After you installed VirtualAudioCable and managed to set things up, give a name to the VirtualAudioCable capture device, copy that name and enter it in the config.

You can also change the buffer size, but I recommend to leave it at -1 in which case the system will determine the buffer size.
If the buffer multiplier is > 0, the size of the buffer will be 20ms * m, with m being the multiplier.

Note that I've tested this only on Windows 10. But not really.