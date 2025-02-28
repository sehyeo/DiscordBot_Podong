package discord.podongbot.channel;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ChannelManager extends ListenerAdapter {
    private static final Map<Long, Long> guildChannelMap = new HashMap<>(); // 길드별 전용 채널 저장
    private static final Map<Long, Long> pinnedMessageMap = new HashMap<>(); // 유지할 메시지 저장 (길드 ID -> 메시지 ID)

    public static void handleChannelSetupCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("이 명령어는 서버에서만 사용할 수 있습니다.").setEphemeral(true).queue();
            return;
        }

        // 이미 음악 채널이 존재하는지 확인
        for (TextChannel channel : guild.getTextChannels()) {
            if (channel.getName().equalsIgnoreCase("포동봇-음악채널")) {
                event.reply("이미 전용 음악 채널이 설정되어 있습니다: " + channel.getAsMention()).queue();
                return;
            }
        }

        // 음악 전용 채널 생성
        guild.createTextChannel("포동봇-음악채널").queue(channel -> {
            guildChannelMap.put(guild.getIdLong(), channel.getIdLong());

            // 안내 메시지 생성
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("🎵 포동봇 - 음악 채널 🎵");
            embed.setDescription("이 채널에서 음악을 검색하고 플레이하세요!\n\n" +
                    "✅ **99.9% 업타임 보장**\n" +
                    "🎶 **최적의 사용자 경험 제공**\n" +
                    "💖 **커스텀 봇으로 고품질 서비스 유지**\n\n" +
                    "📌 **사용 가능한 명령어**:\n" +
                    "`/재생 <노래>` - 음악 재생\n" +
                    "`/일시정지` - 음악 일시 정지\n" +
                    "`/스킵` - 다음 곡으로 이동\n" +
                    "`/볼륨 <숫자>` - 음악 볼륨 조절\n" +
                    "`/반복` - 음악 반복 모드\n" +
                    "`/셔플` - 음악 셔플 모드\n" +
                    "`/정지` - 음악 정지\n" +
                    "`/대기열` - 현재 대기열 확인\n" +
                    "`/삭제 <번호>` - 대기열에 있는 음악 삭제\n\n" +
                    "💡 **음악을 검색하려면 아래 버튼을 클릭하세요!**");
            embed.setColor(Color.BLUE);
            embed.setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

            // 음악 검색 버튼 추가
            Button searchButton = Button.link("https://www.youtube.com/", "🔍 음악 검색하기");

            // 메시지 전송 (이 메시지는 유지)
            channel.sendMessageEmbeds(embed.build())
                    .setActionRow(searchButton)
                    .queue(message -> pinnedMessageMap.put(guild.getIdLong(), message.getIdLong()));

            event.reply("전용 음악 채널이 생성되었습니다: " + channel.getAsMention()).queue();
        });
    }

    // 특정 채널에서 사용자의 메시지와 불필요한 봇 메시지를 1초 후 삭제
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        TextChannel channel = event.getChannel().asTextChannel();
        Guild guild = event.getGuild();

        // 현재 서버의 음악 채널 ID 확인
        Long musicChannelId = guildChannelMap.get(guild.getIdLong());
        if (musicChannelId == null || channel.getIdLong() != musicChannelId) return;

        Message message = event.getMessage();

        // 유지해야 하는 메시지인지 확인
        Long pinnedMessageId = pinnedMessageMap.get(guild.getIdLong());
        if (pinnedMessageId != null && message.getIdLong() == pinnedMessageId) {
            return; // 메인 메시지는 삭제하지 않음
        }

        // 사용자의 메시지나 불필요한 봇 메시지 1초 후 삭제
        message.delete().queueAfter(1, TimeUnit.SECONDS);
    }
}
