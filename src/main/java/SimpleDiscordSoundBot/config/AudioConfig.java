package SimpleDiscordSoundBot.config;

import org.json.JSONObject;

import javax.sound.sampled.AudioFormat;

/**
 * From the JDA documentation: "The provided audio data needs to be in the format: 48KHz 16bit stereo signed BigEndian PCM."
 */
public class AudioConfig extends AudioFormat {
    private final int num20msBytes; // Amount of bytes per 20 ms (for 2 channels)
    private final int _bufferMultiplier;
    private final String _deviceName;

    public AudioConfig(JSONObject audioJsonCfg) {
        super(48000,
                16,
                2,
                true,
                true);

        int samplesPer20ms = (int) Math.ceil(this.getSampleRate() / 50);
        num20msBytes = samplesPer20ms * 2 * (this.getSampleSizeInBits() / 8);

        _bufferMultiplier = audioJsonCfg.getInt("bufferMultiplier");
        _deviceName = audioJsonCfg.getString("deviceName");
    }

    public int getNum20msBytes() {
        return num20msBytes;
    }

    public int getBufferMultiplier() {
        return _bufferMultiplier;
    }

    public String getDeviceName() {
        return _deviceName;
    }
}