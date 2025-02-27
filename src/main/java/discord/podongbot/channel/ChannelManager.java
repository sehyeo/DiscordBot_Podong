package discord.podongbot.channel;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import java.util.HashMap;
import java.util.Map;

public class ChannelManager {
    private static final Map<Long, Long> guildChannelMap = new HashMap<>(); // 길드별 전용 채널 저장

    public static void handleChannelSetupCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("이 명령어는 서버에서만 사용할 수 있습니다.").setEphemeral(true).queue();
            return;
        }

        // 서버에서 "포동봇-음악채널"이 이미 존재하는지 확인
        for (TextChannel channel : guild.getTextChannels()) {
            if (channel.getName().equalsIgnoreCase("포동봇-음악채널")) {
                event.reply("이미 전용 음악 채널이 설정되어 있습니다: " + channel.getAsMention()).queue();
                return;
            }
        }

        guild.createTextChannel("포동봇-음악채널").queue(channel -> {
            guildChannelMap.put(guild.getIdLong(), channel.getIdLong());
            event.reply("전용 음악 채널이 생성되었습니다: " + channel.getAsMention()).queue();
        });
    }
}
