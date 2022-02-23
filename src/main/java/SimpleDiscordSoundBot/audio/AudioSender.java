package SimpleDiscordSoundBot.audio;

import SimpleDiscordSoundBot.config.AudioConfig;
import SimpleDiscordSoundBot.logging.SimpleLogger;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
        return ByteBuffer.wrap(_buffer).order(ByteOrder.nativeOrder());
    }

    private void _fillBuffer() {
        _line.read(_buffer,0, _audioConfig.getNum20msBytes());
    }
}