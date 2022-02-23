package SimpleDiscordSoundBot.loading;

import SimpleDiscordSoundBot.config.BotConfig;
import SimpleDiscordSoundBot.config.AudioConfig;
import SimpleDiscordSoundBot.logging.SimpleLogger;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ConfigDataContainer {
    private static ConfigDataContainer _instance;

    private BotConfig _botConfig;
    private AudioConfig _audioConfig;

    private ConfigDataContainer(String configFilePath) {
        _loadConfigFile(configFilePath);
    }

    public static ConfigDataContainer getInstance() {
        if (_instance == null) _instance = new ConfigDataContainer("cfg/config.json");
        return _instance;
    }

    private void _loadConfigFile(String configFilePath) {
        try {
            InputStream input = new FileInputStream(configFilePath);
            String jsonText = IOUtils.toString(input, StandardCharsets.UTF_8);
            JSONObject configFileJson = new JSONObject(jsonText);

            _botConfig = new BotConfig(configFileJson.getJSONObject("bot"));
            _audioConfig = new AudioConfig(configFileJson.getJSONObject("audio"));
        } catch (JSONException e) {
            SimpleLogger.logException(e, "JSON error while reading config file");
            System.exit(-2);
        } catch (FileNotFoundException e) {
            SimpleLogger.logException(e, "Config file not found");
            System.exit(-2);
        } catch (IOException e) {
            SimpleLogger.logException(e, "Error while reading config file");
            System.exit(-2);
        }
    }

    public BotConfig getBotConfig() {
        return _botConfig;
    }

    public AudioConfig getAudioConfig() {
        return _audioConfig;
    }
}