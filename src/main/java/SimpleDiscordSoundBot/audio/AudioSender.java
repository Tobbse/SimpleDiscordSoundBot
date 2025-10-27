package SimpleDiscordSoundBot.audio;

import SimpleDiscordSoundBot.config.AudioConfig;
import SimpleDiscordSoundBot.logging.SimpleLogger;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * From the JDA documentation: "The provided audio data needs to be in the format: 48KHz 16bit stereo signed BigEndian PCM."
 */
public class AudioSender implements AudioSendHandler {
    private final AudioConfig _audioConfig;
    private TargetDataLine _line;
    private byte[] _buffer;

    public AudioSender(AudioConfig audioConfig) {
        _audioConfig = audioConfig;
        _buffer = new byte[_audioConfig.getNum20msBytes()];
    }

    public void start() throws LineUnavailableException {
        SimpleLogger.info("Starting to look for matching audio lines. The required device must match the name set in the config and support 48 kHz stereo 16 bit audio.");
        SimpleLogger.info("Selected audio device from config: \"" + _audioConfig.getDeviceName() + "\"");

        DataLine.Info requiredInfo = new DataLine.Info(TargetDataLine.class, _audioConfig);

        if (!AudioSystem.isLineSupported(requiredInfo)) {
            SimpleLogger.logException(null, "Line matching " + requiredInfo + " not supported.");
        }

        try {
            _line = _getLine();

            if (_line == null) {
                SimpleLogger.warn("Could not find supported line for device with name " + _audioConfig.getDeviceName());
                SimpleLogger.warn("Trying to use fallback audio device...");

                _line = AudioSystem.getTargetDataLine(_audioConfig);
            }
            if (_line == null) {
                throw new LineUnavailableException("Could not find any device supporting the required audio format.");
            }

            int bufferMultiplier = _audioConfig.getBufferMultiplier();
            if (bufferMultiplier > 0) {
                _line.open(_audioConfig, _audioConfig.getNum20msBytes() * _audioConfig.getBufferMultiplier());
            } else {
                _line.open(_audioConfig);
            }
            _line.start();
        } catch (LineUnavailableException e) {
            throw e;
        }
    }

    private TargetDataLine _getLine() throws LineUnavailableException{
        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
            if (mixerInfo.getName().contains(_audioConfig.getDeviceName()) && mixerInfo.getClass().getName().contains("DirectAudioDevice")) {
               return AudioSystem.getTargetDataLine(_audioConfig, mixerInfo);
            }
        }
        return null;
    }

    /**
     * No need to wait here for enough bytes. The line.read(..) is blocking until the Bytes are complete.
     * As this is called every 20ms (unlike `provide20MsAudio`, which is only called when 20ms of audio are needed),
     * we fill the buffer from here. Otherwise this will cause delays if the audio is not available at some point, because
     * we don't read info from the line and the line buffer grows.
     */
    @Override
    public boolean canProvide() {
        if (_line == null) {
            return false;
        }
        _fillBuffer();
        return _line.isOpen();
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(_buffer).order(ByteOrder.BIG_ENDIAN);
    }

    private void _fillBuffer() {
        _line.read(_buffer,0, _audioConfig.getNum20msBytes());
    }

    /**
     * Lists all available audio input devices and their capabilities.
     * This is useful for users to identify which device name to use in the config.
     */
    public static void listAvailableAudioDevices() {
        SimpleLogger.info("=== Available Audio Input Devices ===");

        // Create the required audio format (48kHz 16bit stereo signed BigEndian PCM)
        AudioFormat requiredFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            48000.0f,  // 48 kHz
            16,        // 16 bit
            2,         // stereo
            4,         // frame size
            48000.0f,  // frame rate
            true       // big endian
        );

        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        List<String> compatibleDevices = new ArrayList<>();
        List<String> incompatibleDevices = new ArrayList<>();

        if (mixerInfos.length == 0) {
            SimpleLogger.warn("No audio devices found on this system.");
            return;
        }

        for (Mixer.Info mixerInfo : mixerInfos) {
            try {
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                Line.Info[] targetLineInfos = mixer.getTargetLineInfo();

                // Only consider devices that have target (input/capture) lines
                if (targetLineInfos.length > 0) {
                    String deviceName = mixerInfo.getName();
                    String deviceDescription = mixerInfo.getDescription();
                    boolean supportsRequiredFormat = false;

                    // Check if this device supports the required format
                    DataLine.Info requiredInfo = new DataLine.Info(TargetDataLine.class, requiredFormat);
                    if (mixer.isLineSupported(requiredInfo)) {
                        supportsRequiredFormat = true;
                    }

                    String deviceInfo = String.format(
                        "  - Name: \"%s\"\n    Description: %s\n    Compatible with bot: %s",
                        deviceName,
                        deviceDescription,
                        supportsRequiredFormat ? "YES ✓" : "NO ✗ (doesn't support 48kHz 16-bit stereo)"
                    );

                    if (supportsRequiredFormat) {
                        compatibleDevices.add(deviceInfo);
                    } else {
                        incompatibleDevices.add(deviceInfo);
                    }
                }
            } catch (Exception e) {
                // Skip devices that throw exceptions
            }
        }

        // Display compatible devices first
        if (!compatibleDevices.isEmpty()) {
            SimpleLogger.info("\nCompatible Audio Input Devices (recommended):");
            for (String device : compatibleDevices) {
                SimpleLogger.info(device);
            }
        }

        // Then display incompatible devices
        if (!incompatibleDevices.isEmpty()) {
            SimpleLogger.info("\nOther Audio Input Devices (not compatible):");
            for (String device : incompatibleDevices) {
                SimpleLogger.info(device);
            }
        }

        if (compatibleDevices.isEmpty() && incompatibleDevices.isEmpty()) {
            SimpleLogger.warn("No audio input devices found. Please check your audio setup.");
        } else if (compatibleDevices.isEmpty()) {
            SimpleLogger.warn("\nWARNING: No compatible audio devices found!");
            SimpleLogger.warn("Make sure you have a virtual audio cable installed that supports 48kHz 16-bit stereo.");
        }

        SimpleLogger.info("\n=== End of Audio Device List ===\n");
        SimpleLogger.info("To use a device, copy its exact name (including quotes) to the 'deviceName' field in cfg/config.json");
    }
}