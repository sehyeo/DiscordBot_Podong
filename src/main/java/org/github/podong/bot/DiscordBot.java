package org.github.podong.bot;

import net.dv8tion.jda.api.JDABuilder;
import org.github.podong.bot.manager.activity.BotActivityManager;
import org.github.podong.bot.manager.token.BotTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DiscordBot implements CommandLineRunner {

    @Autowired
    private BotTokenManager botTokenManager;

    @Autowired
    private BotActivityManager botActivityManager;

    public static void main(String[] args) {
        SpringApplication.run(DiscordBot.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            String token = botTokenManager.getDiscordBotToken();
            JDABuilder.createDefault(token)
                    .setActivity(botActivityManager.getDefaultActivity()) // 봇 상태 설정
                    .build();

            System.out.println("✅ 디스코드 봇 실행 완료!");
        } catch (Exception e) {
            System.out.println("❌ 봇 실행 중 오류 발생: " + e.getMessage());
        }
    }
}
