package org.github.podong.bot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.github.podong.bot.manager.activity.BotActivityManager;
import org.github.podong.bot.manager.token.BotTokenManager;
import org.github.podong.bot.response.ChattingReaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.EnumSet;

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

            EnumSet<GatewayIntent> intents = EnumSet.of(
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.DIRECT_MESSAGES,
                    GatewayIntent.MESSAGE_CONTENT,
                    GatewayIntent.GUILD_PRESENCES,
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_VOICE_STATES);

            JDABuilder.createDefault(token)
                    .enableIntents(intents)
                    .setActivity(botActivityManager.getDefaultActivity()) // 봇 상태 설정
                    .addEventListeners(new ChattingReaction())
                    .build();

            System.out.println("✅ 디스코드 봇 실행 완료!");
        } catch (Exception e) {
            System.out.println("❌ 봇 실행 중 오류 발생: " + e.getMessage());
        }
    }
}
