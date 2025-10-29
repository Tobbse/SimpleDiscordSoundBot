package SimpleDiscordSoundBot.config;

import org.json.JSONObject;

public class StatusMonitoringConfig {
    private final boolean enabled;
    private final int intervalSeconds;
    private final boolean verboseMode;
    private final String statusFilePath;

    public StatusMonitoringConfig(JSONObject jsonConfig) {
        this.enabled = jsonConfig.optBoolean("enabled", true);
        this.intervalSeconds = jsonConfig.optInt("intervalSeconds", 30);
        this.verboseMode = jsonConfig.optBoolean("verboseMode", false);
        this.statusFilePath = jsonConfig.optString("statusFilePath", "status.txt");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public boolean isVerboseMode() {
        return verboseMode;
    }

    public long getIntervalMillis() {
        return intervalSeconds * 1000L;
    }

    public String getStatusFilePath() {
        return statusFilePath;
    }
}
