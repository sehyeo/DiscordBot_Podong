package discord.podongbot;

import discord.podongbot.activity.ActivityManager;
import discord.podongbot.channel.ChannelManager;
import discord.podongbot.help.HelpManager;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import discord.podongbot.token.TokenManager;
import discord.podongbot.reaction.ChattingReaction;
import discord.podongbot.reaction.SlashCommandReaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.EnumSet;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DiscordBot implements CommandLineRunner {

    @Autowired
    private TokenManager botTokenManager;

    @Autowired
    private ActivityManager botActivityManager;

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
                    GatewayIntent.GUILD_VOICE_STATES
            );

            JDABuilder.createDefault(token)
                    .enableIntents(intents)
                    .setActivity(botActivityManager.getDefaultActivity()) // 봇 상태 설정
                    .addEventListeners(
                            new ChattingReaction(),
                            new SlashCommandReaction(),
                            new ChannelManager(),
                            new HelpManager())
                    .build();

            System.out.println("⭕ 포동봇 실행 완료!");
        } catch (Exception e) {
            System.out.println("❌ 포동봇 실행 중 오류 발생: " + e.getMessage());
        }
    }
}
