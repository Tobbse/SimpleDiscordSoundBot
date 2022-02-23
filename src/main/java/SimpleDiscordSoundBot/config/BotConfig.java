package SimpleDiscordSoundBot.config;

import org.json.JSONObject;

public class BotConfig {
    public final String token;
    public final String botTextChannelId;
    public final String botVoiceChannelId;
    public final String guildId;

    public BotConfig(JSONObject jsonConfig) {
        token = jsonConfig.getString("token");
        botTextChannelId = jsonConfig.getString("botTextChannelId");
        botVoiceChannelId = jsonConfig.getString("botVoiceChannelId");
        guildId = jsonConfig.getString("guildId");
    }
}