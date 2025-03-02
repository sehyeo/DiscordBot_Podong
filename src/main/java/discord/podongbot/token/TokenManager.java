package discord.podongbot.token;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class TokenManager {

    @Value("${discord.token}")
    private String discordBotToken;

}
