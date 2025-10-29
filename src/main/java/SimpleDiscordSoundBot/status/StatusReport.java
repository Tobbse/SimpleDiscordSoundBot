package SimpleDiscordSoundBot.status;

import java.util.ArrayList;
import java.util.List;

public class StatusReport {
    private final AudioStatus audioStatus;
    private final boolean discordConnected;
    private final boolean inVoiceChannel;
    private final boolean audioManagerActive;
    private final long packetsLastInterval;
    private final OverallHealth health;
    private final List<String> warnings;

    public enum OverallHealth {
        HEALTHY,
        DEGRADED,
        CRITICAL
    }

    public StatusReport(AudioStatus audioStatus, boolean discordConnected,
                        boolean inVoiceChannel, boolean audioManagerActive,
                        long packetsLastInterval, OverallHealth health) {
        this.audioStatus = audioStatus;
        this.discordConnected = discordConnected;
        this.inVoiceChannel = inVoiceChannel;
        this.audioManagerActive = audioManagerActive;
        this.packetsLastInterval = packetsLastInterval;
        this.health = health;
        this.warnings = new ArrayList<>();
    }

    public AudioStatus getAudioStatus() {
        return audioStatus;
    }

    public boolean isDiscordConnected() {
        return discordConnected;
    }

    public boolean isInVoiceChannel() {
        return inVoiceChannel;
    }

    public boolean isAudioManagerActive() {
        return audioManagerActive;
    }

    public long getPacketsLastInterval() {
        return packetsLastInterval;
    }

    public OverallHealth getHealth() {
        return health;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }
}
