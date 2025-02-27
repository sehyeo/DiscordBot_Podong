package org.github.podong.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BotTokenManager {

    @Value("${discord.token}")
    private String discordBotToken;

    public String getDiscordBotToken() {
        return discordBotToken;
    }
}
