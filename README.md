# SimpleDiscordSoundBot

This is a simple Discord bot that plays audio by capturing it from the sound card. It's a minified and feature-reduced version of a private project.

It may be useful for RPG Dungeon Masters playing remotely via Discord, enabling them to play sounds through this bot. The bot uses the Discord API and needs an audio input device that fits the audio format requirements of the API, such as Virtual Audio Cable.

## Requirements

- **Java 11 or higher** - Required to run the bot
- **Maven 3.x** - Required to build the project
- **Virtual Audio Cable** - Required for audio capture (or similar virtual audio device)
- **Discord Bot Token** - Create a bot at https://discord.com/developers/applications

## Setup Instructions

### 1. Install Prerequisites

Ensure you have Java 11+ and Maven installed:

```bash
java -version    # Should show version 11 or higher
mvn -version     # Should show Maven 3.x
```

### 2. Build the Project

Run the setup script to build the project:

```bash
./setup.sh
```

This will:
- Download all dependencies
- Compile the source code
- Create an executable jar file at `target/SimpleDiscordSoundBot-1.0.1.jar`

Alternatively, you can run the Maven command directly:

```bash
mvn clean package
```

### 3. Configure the Bot

1. Navigate to the `cfg` directory
2. Copy `config_example.json` to `config.json`:
   ```bash
   cp cfg/config_example.json cfg/config.json
   ```
3. Edit `cfg/config.json` and fill in your configuration:
   - `token`: Your Discord bot token from https://discord.com/developers/applications
   - `botTextChannelId`: The ID of the text channel where the bot will send messages
   - `botVoiceChannelId`: The ID of the voice channel where the bot will join
   - `guildId`: Your Discord server (guild) ID
   - `deviceName`: The name of your virtual audio capture device (see below for how to find this)
   - `bufferMultiplier`: Set to -1 to use system default (recommended)

**Getting Discord IDs:**
- Enable Developer Mode in Discord: User Settings → Advanced → Developer Mode
- Right-click on channels/servers to copy their IDs

**Finding Your Audio Device Name:**

When you start the bot for the first time, it will automatically list all available audio input devices. Look for the device name in the console output under "Compatible Audio Input Devices". The bot will display:
- All audio input devices on your system
- Which devices are compatible with the required format (48kHz 16-bit stereo)
- Device descriptions to help you identify the correct one

Copy the exact device name (as shown in quotes) and paste it into the `deviceName` field in your config file.

### 4. Setup Virtual Audio Cable

For audio capture, you need a virtual audio device:

**Windows:**
- Install [VB-Audio Virtual Cable](https://vb-audio.com/Cable/)
- Configure your audio software to output to the virtual cable
- The bot will display available devices on startup - look for "CABLE Output" or similar

**macOS:**
- Install [BlackHole](https://existential.audio/blackhole/)
- Configure your audio software to output to BlackHole
- The bot will display available devices on startup - look for "BlackHole 2ch" or similar

**Linux:**
- Use PulseAudio or PipeWire virtual sinks
- The bot will display available devices on startup - look for your configured virtual device

**Note:** After installing your virtual audio cable, you can run the bot to see the exact device name, then stop it and update your config accordingly.

### 5. Run the Bot

Start the bot using the start script:

```bash
./start.sh
```

Or run directly with Java:

```bash
java -jar target/SimpleDiscordSoundBot-1.0.1.jar
```

## Configuration Details

### Buffer Multiplier

The `bufferMultiplier` setting controls audio buffer size:
- `-1`: System determines buffer size automatically (recommended)
- `> 0`: Buffer size = 20ms × multiplier

## Troubleshooting

- **Build fails**: Ensure Maven and Java 11+ are installed
- **Bot won't start**: Verify `cfg/config.json` exists and is properly configured
- **No audio**: Check that your audio software is outputting to the virtual audio device
- **Bot can't join voice channel**: Verify the bot has proper permissions in your Discord server
- **"No compatible audio devices found"**: Install a virtual audio cable that supports 48kHz 16-bit stereo
- **Wrong device name in config**: Start the bot to see the list of available devices, then copy the exact name from the output

## Testing Status

This project was initially developed and tested on Windows 10. The updated version has been tested with:
- Java 11
- Maven 3.x
- JDA 4.4.0 (Discord API library)
- Spring Boot 2.5.4

## Technical Details

- **Built with**: Spring Boot, JDA (Java Discord API)
- **Java Version**: 11
- **Main Dependencies**:
  - JDA 4.4.0_352
  - Spring Boot 2.5.4
  - Commons IO 2.6