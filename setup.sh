#!/bin/bash

# Build the project with Maven
mvn clean package

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful! The executable jar is located at: target/SimpleDiscordSoundBot-1.0.1.jar"
    echo "Run './start.sh' to start the bot."
else
    echo "Build failed. Please check the error messages above."
    exit 1
fi