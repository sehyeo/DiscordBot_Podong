package org.github.podong.bot.manager.token;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class BotTokenManager {

    @Value("${discord.token}")
    private String discordBotToken;

}
