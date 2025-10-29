package SimpleDiscordSoundBot.status;

public class AudioStatus {
    private final boolean lineOpen;
    private final boolean audioFlowing;
    private final long packetsProvided;
    private final int bufferLevel;
    private final long availableBytes;
    private final boolean blocking;
    private final long lastReadDurationMs;
    private final long totalBytesRead;
    private final String deviceName;

    public AudioStatus(boolean lineOpen, boolean audioFlowing, long packetsProvided,
                       int bufferLevel, long availableBytes, boolean blocking,
                       long lastReadDurationMs, long totalBytesRead, String deviceName) {
        this.lineOpen = lineOpen;
        this.audioFlowing = audioFlowing;
        this.packetsProvided = packetsProvided;
        this.bufferLevel = bufferLevel;
        this.availableBytes = availableBytes;
        this.blocking = blocking;
        this.lastReadDurationMs = lastReadDurationMs;
        this.totalBytesRead = totalBytesRead;
        this.deviceName = deviceName;
    }

    public boolean isLineOpen() {
        return lineOpen;
    }

    public boolean isAudioFlowing() {
        return audioFlowing;
    }

    public long getPacketsProvided() {
        return packetsProvided;
    }

    public int getBufferLevel() {
        return bufferLevel;
    }

    public long getAvailableBytes() {
        return availableBytes;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public long getLastReadDurationMs() {
        return lastReadDurationMs;
    }

    public long getTotalBytesRead() {
        return totalBytesRead;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
