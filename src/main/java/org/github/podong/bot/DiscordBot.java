package org.github.podong.bot;

import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DiscordBot implements CommandLineRunner {

    @Autowired
    private BotTokenManager botTokenManager;

    public static void main(String[] args) {
        SpringApplication.run(DiscordBot.class, args);
    }

    @Override
    public void run(String... args) {
        String token = botTokenManager.getDiscordBotToken();
        JDABuilder.createDefault(token)
                .build();
        System.out.println("✅ 디스코드 봇 실행 완료!");
    }
}
