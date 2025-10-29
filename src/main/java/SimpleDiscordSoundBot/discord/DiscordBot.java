package SimpleDiscordSoundBot.discord;

import SimpleDiscordSoundBot.audio.AudioSender;
import SimpleDiscordSoundBot.config.BotConfig;
import SimpleDiscordSoundBot.loading.ConfigDataContainer;
import SimpleDiscordSoundBot.logging.SimpleLogger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
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

        // List all available audio devices to help users configure the bot
        SimpleLogger.info("\nScanning for audio devices...");
        AudioSender.listAvailableAudioDevices();

        _init();
    }

    void _init() {
        try {
            SimpleLogger.info("Initializing bot");

            SimpleLogger.info("Building JDA with GUILD_VOICE_STATES intent...");
            _discordApi = JDABuilder.createDefault(_token)
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOJI, CacheFlag.CLIENT_STATUS)
                .build();

            SimpleLogger.info("Waiting for JDA to be ready...");
            _discordApi.awaitReady();
            SimpleLogger.info("JDA is ready!");

            // Add voice state listener to debug connection issues
            _discordApi.addEventListener(new ListenerAdapter() {
                @Override
                public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
                    if (event.getMember().equals(event.getGuild().getSelfMember())) {
                        SimpleLogger.info("Bot voice state changed - Joined: " + event.getChannelJoined() + ", Left: " + event.getChannelLeft());

                        // Log additional diagnostic info
                        if (event.getChannelLeft() != null) {
                            SimpleLogger.warn("BOT WAS DISCONNECTED FROM CHANNEL! This usually means:");
                            SimpleLogger.warn("  1. Discord kicked the bot for not sending audio");
                            SimpleLogger.warn("  2. Audio connection failed to initialize properly");
                            SimpleLogger.warn("  3. JDA's audio system encountered an error");

                            // Check audio manager state when disconnected
                            if (_audioManager != null) {
                                SimpleLogger.info("Audio Manager State at disconnect:");
                                SimpleLogger.info("  Connected: " + _audioManager.isConnected());
                                SimpleLogger.info("  Handler set: " + (_audioManager.getSendingHandler() != null));
                                SimpleLogger.info("  Self-muted: " + _audioManager.isSelfMuted());
                            }
                        }
                    }
                }
            });

            _discordApiLoaded();
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
            SimpleLogger.info("Starting audio sender");
            _audioSender.start();

            SimpleLogger.info("Obtaining audio manager from guild");
            _audioManager = guild.getAudioManager();

            SimpleLogger.info("Setting connection listener");
            _audioManager.setConnectionListener(new net.dv8tion.jda.api.audio.hooks.ConnectionListener() {
                @Override
                public void onPing(long ping) {
                    SimpleLogger.info("[Audio Connection] Ping: " + ping + "ms");
                }

                @Override
                public void onStatusChange(net.dv8tion.jda.api.audio.hooks.ConnectionStatus status) {
                    SimpleLogger.info("[Audio Connection] Status changed to: " + status);
                }

                @Override
                public void onUserSpeaking(net.dv8tion.jda.api.entities.User user, boolean speaking) {
                    // Not relevant for sending
                }
            });


            SimpleLogger.info("Configuring audio manager and setting sending handler");
            _audioManager.setSendingHandler(_audioSender);
            _audioManager.setSelfMuted(false);
            _audioManager.setSelfDeafened(false);

            SimpleLogger.info("Attempting to open audio connection to voice channel: " + voiceChannel.getName());
            try {
                _audioManager.openAudioConnection(voiceChannel);
                SimpleLogger.info("openAudioConnection() call completed without exception");
            } catch (Exception e) {
                SimpleLogger.logException(e, "Exception during openAudioConnection()");
                throw e;
            }

            SimpleLogger.info("Waiting for connection to establish...");
            for (int i = 0; i < 20; i++) {
                Thread.sleep(500);
                if (_audioManager.isConnected()) {
                    SimpleLogger.info("Successfully connected to voice channel after " + (i * 500) + "ms");
                    break;
                }
            }

            if (!_audioManager.isConnected()) {
                SimpleLogger.warn("FAILED to connect to voice channel!");
                throw new RuntimeException("Could not connect to voice channel");
            }

            // Give Discord a moment to start requesting audio
            Thread.sleep(1000);

            // Log complete audio manager state for debugging
            SimpleLogger.info("=== Audio Manager State ===");
            SimpleLogger.info("  Connected: " + _audioManager.isConnected());
            SimpleLogger.info("  Sending handler set: " + (_audioManager.getSendingHandler() != null));
            SimpleLogger.info("  Sending handler class: " + (_audioManager.getSendingHandler() != null ? _audioManager.getSendingHandler().getClass().getName() : "null"));
            SimpleLogger.info("  Self-muted: " + _audioManager.isSelfMuted());
            SimpleLogger.info("  Self-deafened: " + _audioManager.isSelfDeafened());
            SimpleLogger.info("  Connected channel: " + _audioManager.getConnectedChannel());

            // Try to manually call canProvide to verify our handler works
            SimpleLogger.info("=== Manual Handler Test ===");
            try {
                boolean canProvide = _audioSender.canProvide();
                SimpleLogger.info("  Manual canProvide() call result: " + canProvide);
            } catch (Exception e) {
                SimpleLogger.logException(e, "Error calling canProvide() manually");
            }
            SimpleLogger.info("=========================");

            SimpleLogger.info("Audio setup complete. Sending handler: " + (_audioManager.getSendingHandler() != null) + ", Muted: " + _audioManager.isSelfMuted());

        } catch (Exception e) {
            _sendMessage(String.format("Failed to setup audio capturing. Error:\n```%s```", e.getMessage()));
            SimpleLogger.logException(e, "Failed to setup audio");
            System.exit(-1);
        }

        _initialized = true;

        if (ConfigDataContainer.getInstance().getBotConfig().sendStartupMessage) {
            String deviceName = ConfigDataContainer.getInstance().getAudioConfig().getDeviceName();
            _sendMessage(String.format("SimpleDiscordSoundBot is now ready and capturing audio from `%s`!", deviceName));
        }
    }

    void _sendMessage(String message) {
        SimpleLogger.info("Sending message: " + message);
        try {
            _discordApi.getGuildById(_guildId).getTextChannelById(_textChannelId).sendMessage(message).complete();
        } catch (Exception e) {
            SimpleLogger.logException(e, "Could not send message to channel.");
        }
    }

    // Public methods for status monitoring
    public void sendStatusMessage(String message) {
        _sendMessage(message);
    }

    public boolean isConnected() {
        return _discordApi != null && _discordApi.getStatus() == JDA.Status.CONNECTED;
    }

    public boolean isInVoiceChannel() {
        return _audioManager != null && _audioManager.isConnected();
    }

    public AudioManager getAudioManager() {
        return _audioManager;
    }

    public AudioSender getAudioSender() {
        return _audioSender;
    }

    public boolean isInitialized() {
        return _initialized;
    }
}