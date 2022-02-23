package SimpleDiscordSoundBot.discord;

import SimpleDiscordSoundBot.audio.AudioSender;
import SimpleDiscordSoundBot.config.BotConfig;
import SimpleDiscordSoundBot.loading.ConfigDataContainer;
import SimpleDiscordSoundBot.logging.SimpleLogger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.stereotype.Component;

@Component
public class DiscordBot {
    protected JDA _discordApi;
    protected final String _guildId;

    private final String _token;
    private final AudioSender _audioSender;
    private final String _musicChannelId;
    private final String _textChannelId;
    private AudioManager _audioManager;
    private boolean _initialized = false;

    public DiscordBot() {
        BotConfig config = ConfigDataContainer.getInstance().getBotConfig();
        _audioSender = new AudioSender(ConfigDataContainer.getInstance().getAudioConfig());
        _token = config.token;
        _musicChannelId = config.botVoiceChannelId;
        _textChannelId = config.botTextChannelId;
        _guildId = config.guildId;

        _init();
    }

    void _init() {
        try {
            SimpleLogger.info("Initializing bot");

            _discordApi = JDABuilder.createDefault(_token).build();
            _discordApi.addEventListener(new DiscordReadyListener(
                v -> {
                    _discordApiLoaded();
                    return null;
                }
            ));

            int waitTime = 0;
            while (!_initialized && waitTime <= 60) {
                if (waitTime >= 60) {
                    this.stopBot();
                    SimpleLogger.warn("Bot initialization timeout");
                    System.exit(-3);
                } else {
                    Thread.sleep(500);
                    waitTime++;
                }
            }
        } catch (Exception e) {
            SimpleLogger.logException(e, "Failed to initialize bot");
        }
    }

    public void stopBot() {
        if (_audioManager != null) {
            _audioManager.setSelfMuted(true);
            _audioManager.closeAudioConnection();
        }
        _sendMessage("Shutting down.");
    }

    void _discordApiLoaded() {
        Guild guild = _discordApi.getGuildById(_guildId);
        VoiceChannel voiceChannel = _discordApi.getVoiceChannelById(_musicChannelId);

        if (guild == null) {
            SimpleLogger.warn("Unable to find Guild with ID: " + _guildId);
            System.exit(-1);
        }
        if (voiceChannel == null) {
            SimpleLogger.warn("Unable to find voice channel with ID: " + _musicChannelId);
            System.exit(-1);
        }
        try {
            _audioSender.start();
            _audioManager = guild.getAudioManager();
            _audioManager.setSelfMuted(true);
            _audioManager.setSendingHandler(_audioSender);
        } catch (Exception e) {
            _sendMessage(String.format("Failed to setup audio capturing. Error:\n```%s```", e.getMessage()));
            SimpleLogger.logException(e, "Failed to setup audio");
            System.exit(-1);
        }

        _audioManager.openAudioConnection(voiceChannel);
        _audioManager.setSelfMuted(false);
        _initialized = true;

        String deviceName = ConfigDataContainer.getInstance().getAudioConfig().getDeviceName();
        _sendMessage(String.format("SimpleDiscordSoundBot is now ready and capturing audio from `%s`!", deviceName));
    }

    void _sendMessage(String message) {
        SimpleLogger.info("Sending message: " + message);
        try {
            _discordApi.getGuildById(_guildId).getTextChannelById(_textChannelId).sendMessage(message).complete();
        } catch (Exception e) {
            SimpleLogger.logException(e, "Could not send message to channel.");
        }
    }
}