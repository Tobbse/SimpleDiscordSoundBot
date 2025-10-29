package SimpleDiscordSoundBot.monitoring;

import SimpleDiscordSoundBot.audio.AudioSender;
import SimpleDiscordSoundBot.config.StatusMonitoringConfig;
import SimpleDiscordSoundBot.discord.DiscordBot;
import SimpleDiscordSoundBot.loading.ConfigDataContainer;
import SimpleDiscordSoundBot.logging.SimpleLogger;
import SimpleDiscordSoundBot.status.AudioStatus;
import SimpleDiscordSoundBot.status.StatusReport;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class StatusMonitor {
    private final DiscordBot _discordBot;
    private final StatusMonitoringConfig _config;
    private long _lastPacketCount = 0;

    @Autowired
    public StatusMonitor(DiscordBot discordBot) {
        _discordBot = discordBot;
        _config = ConfigDataContainer.getInstance().getStatusMonitoringConfig();
    }

    @PostConstruct
    public void init() {
        if (_config.isEnabled()) {
            SimpleLogger.info("Status monitoring enabled. Console reports every " + _config.getIntervalSeconds() + " seconds.");
        } else {
            SimpleLogger.info("Status monitoring is disabled.");
        }
    }

    @Scheduled(fixedDelayString = "#{@statusMonitor.getIntervalMillis()}", initialDelayString = "#{@statusMonitor.getIntervalMillis()}")
    public void reportStatus() {
        if (!_config.isEnabled()) {
            return;
        }

        // Only report if bot is initialized
        if (!_discordBot.isInitialized()) {
            return;
        }

        try {
            StatusReport report = collectStatus();
            String consoleSummary = formatConsoleSummary(report);

            // Log to console
            SimpleLogger.info(consoleSummary);
        } catch (Exception e) {
            SimpleLogger.logException(e, "Failed to generate status report");
        }
    }

    public long getIntervalMillis() {
        return _config.getIntervalMillis();
    }

    private String formatConsoleSummary(StatusReport report) {
        AudioStatus audioStatus = report.getAudioStatus();
        String status = audioStatus.isAudioFlowing() ? "STREAMING" :
                       audioStatus.isLineOpen() ? "IDLE" : "STOPPED";
        String health = report.getHealth().toString();

        StringBuilder sb = new StringBuilder();
        sb.append("[Status Monitor] ");
        sb.append("Audio: ").append(status);
        sb.append(" | Health: ").append(health);
        sb.append(" | Packets: ").append(report.getPacketsLastInterval());
        sb.append(" | Buffer: ").append(audioStatus.getBufferLevel()).append("%");

        if (!report.getWarnings().isEmpty()) {
            sb.append(" | Warnings: ").append(report.getWarnings().size());
        }

        // Add verbose Audio Manager state to console when verbose mode is enabled
        if (_config.isVerboseMode()) {
            sb.append("\n  Audio Manager State:");
            if (_discordBot.getAudioManager() != null) {
                AudioManager audioManager = _discordBot.getAudioManager();
                sb.append("\n    Connected: ").append(audioManager.isConnected());
                sb.append("\n    Sending handler: ").append(audioManager.getSendingHandler() != null ? audioManager.getSendingHandler().getClass().getSimpleName() : "null");
                sb.append("\n    Self-muted: ").append(audioManager.isSelfMuted());
                sb.append("\n    Self-deafened: ").append(audioManager.isSelfDeafened());
                sb.append("\n    Channel: ").append(audioManager.getConnectedChannel() != null ? audioManager.getConnectedChannel().getName() : "null");
            } else {
                sb.append("\n    Not initialized");
            }
        }

        return sb.toString();
    }

    private StatusReport collectStatus() {
        AudioSender audioSender = _discordBot.getAudioSender();
        AudioStatus audioStatus = audioSender.getStatus();

        boolean discordConnected = _discordBot.isConnected();
        boolean inVoiceChannel = _discordBot.isInVoiceChannel();
        boolean audioManagerActive = _discordBot.getAudioManager() != null;

        long currentPacketCount = audioStatus.getPacketsProvided();
        long packetsLastInterval = currentPacketCount - _lastPacketCount;
        _lastPacketCount = currentPacketCount;

        // Determine overall health
        StatusReport.OverallHealth health = determineHealth(audioStatus, discordConnected, inVoiceChannel, packetsLastInterval);

        StatusReport report = new StatusReport(
            audioStatus,
            discordConnected,
            inVoiceChannel,
            audioManagerActive,
            packetsLastInterval,
            health
        );

        // Add warnings
        addWarnings(report, packetsLastInterval);

        return report;
    }

    private StatusReport.OverallHealth determineHealth(AudioStatus audioStatus, boolean discordConnected,
                                                        boolean inVoiceChannel, long packetsLastInterval) {
        // Critical conditions
        if (!audioStatus.isLineOpen() || !discordConnected || !inVoiceChannel) {
            return StatusReport.OverallHealth.CRITICAL;
        }

        // Degraded conditions
        if (audioStatus.isBlocking() || audioStatus.getBufferLevel() > 80 || packetsLastInterval < 1000) {
            return StatusReport.OverallHealth.DEGRADED;
        }

        return StatusReport.OverallHealth.HEALTHY;
    }

    private void addWarnings(StatusReport report, long packetsLastInterval) {
        AudioStatus audioStatus = report.getAudioStatus();

        if (!audioStatus.isLineOpen()) {
            report.addWarning("Audio line is not open!");
        }

        if (!report.isDiscordConnected()) {
            report.addWarning("Discord connection lost!");
        }

        if (!report.isInVoiceChannel()) {
            report.addWarning("Not connected to voice channel!");
        }

        if (audioStatus.isBlocking()) {
            report.addWarning("Audio read operations are blocking (slow performance)");
        }

        if (audioStatus.getBufferLevel() > 80) {
            report.addWarning("Buffer level high (" + audioStatus.getBufferLevel() + "%) - risk of overflow");
        }

        if (audioStatus.getBufferLevel() < 10 && audioStatus.isLineOpen()) {
            report.addWarning("Buffer level low (" + audioStatus.getBufferLevel() + "%) - risk of underflow");
        }

        // Expected packets per interval (approximately 50 per second)
        long expectedPackets = _config.getIntervalSeconds() * 50L;
        if (packetsLastInterval < expectedPackets * 0.8 && audioStatus.isLineOpen()) {
            report.addWarning("Packet rate below expected (" + packetsLastInterval + " vs ~" + expectedPackets + ")");
        }

        if (!audioStatus.isAudioFlowing() && audioStatus.isLineOpen()) {
            report.addWarning("Audio line open but no audio flowing");
        }
    }
}
