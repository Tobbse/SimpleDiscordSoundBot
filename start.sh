#!/bin/bash

# Check if jar file exists
if [ ! -f "target/SimpleDiscordSoundBot-1.0.1.jar" ]; then
    echo "Error: Executable jar not found. Please run './setup.sh' first to build the project."
    exit 1
fi

# Check if config exists
if [ ! -f "cfg/config.json" ]; then
    echo "Error: Configuration file not found at cfg/config.json"
    echo "Please copy cfg/config_example.json to cfg/config.json and configure it with your bot token and settings."
    exit 1
fi

# Start the bot
java -jar target/SimpleDiscordSoundBot-1.0.1.jar