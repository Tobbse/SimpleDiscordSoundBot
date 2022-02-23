package SimpleDiscordSoundBot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Autowired
    public App() {
        // The setup is handled by spring. Check out `DiscordBot`.
        System.out.println("Initializing...");
    }
}