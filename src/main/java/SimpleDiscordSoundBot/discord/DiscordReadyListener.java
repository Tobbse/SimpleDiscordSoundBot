package SimpleDiscordSoundBot.discord;

import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.function.Function;

public class DiscordReadyListener extends ListenerAdapter {
    private final Function<Void,Void> _readyCallback;

    public DiscordReadyListener(Function<Void,Void> readyCallback) {
        super();
        _readyCallback = readyCallback;
    }

    @Override
    public void onReady(ReadyEvent ev) {
        _readyCallback.apply(null);
    }
}