package discord.podongbot.channel;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class ChannelManager extends ListenerAdapter {
    private static final Map<Long, Long> guildChannelMap = new HashMap<>(); // 길드별 전용 채널 저장
    private static final Map<Long, Long> pinnedMessageMap = new HashMap<>(); // 유지할 메시지 저장 (길드 ID -> 메시지 ID)

    public static void handleChannelSetupCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("이 명령어는 서버에서만 사용할 수 있습니다.").setEphemeral(true).queue();
            return;
        }

        // 기존 음악 채널이 있는지 확인
        TextChannel existingChannel = guild.getTextChannels().stream()
                .filter(channel -> channel.getName().equalsIgnoreCase("포동봇-음악채널"))
                .findFirst()
                .orElse(null);

        if (existingChannel != null) {
            event.reply("이미 전용 음악 채널이 설정되어 있습니다: " + existingChannel.getAsMention()).queue();
            return;
        }

        // 음악 전용 채널 생성
        guild.createTextChannel("포동봇-음악채널").queue(channel -> {
            guildChannelMap.put(guild.getIdLong(), channel.getIdLong());

            // 📌 새로운 안내 메시지를 생성하고 pinnedMessageMap을 업데이트
            sendPinnedMessage(channel);

            event.reply("전용 음악 채널이 생성되었습니다: " + channel.getAsMention()).queue();
        });
    }

    /**
     * 📌 새 채널에 안내 메시지를 보내고, 즉시 pinnedMessageMap을 반영
     */
    private static void sendPinnedMessage(TextChannel channel) {
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
        embed.setThumbnail(channel.getJDA().getSelfUser().getEffectiveAvatarUrl());

        // 🎶 음악 검색 버튼 추가
        Button searchButton = Button.link("https://www.youtube.com/", "🔍 음악 검색하기");

        // 📌 새 메시지를 보내기 전에 먼저 pinnedMessageMap을 업데이트하여 삭제 방지
        channel.sendMessageEmbeds(embed.build())
                .setActionRow(searchButton)
                .queue(message -> {
                    pinnedMessageMap.put(channel.getGuild().getIdLong(), message.getIdLong());
                    System.out.println("[DEBUG] 안내 메시지 등록됨 (먼저 저장됨): " + message.getId());
                });
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        TextChannel channel = event.getChannel().asTextChannel();
        Guild guild = event.getGuild();
        Message message = event.getMessage();

        // 현재 서버의 음악 채널 ID 확인
        Long musicChannelId = guildChannelMap.get(guild.getIdLong());
        if (musicChannelId == null) {
            guild.getTextChannels().stream()
                    .filter(ch -> ch.getName().equalsIgnoreCase("포동봇-음악채널"))
                    .findFirst()
                    .ifPresent(existingChannel -> guildChannelMap.put(guild.getIdLong(), existingChannel.getIdLong()));
        }

        // 음악 채널이 아닐 경우 삭제하지 않음
        musicChannelId = guildChannelMap.get(guild.getIdLong());
        if (musicChannelId == null || channel.getIdLong() != musicChannelId) return;

        // 📌 안내 메시지가 아직 등록되지 않은 경우 삭제 방지
        if (!pinnedMessageMap.containsKey(guild.getIdLong())) {
            System.out.println("[DEBUG] 안내 메시지가 아직 등록되지 않음 → 삭제 방지");
            return;
        }

        // 📌 기존 안내 메시지인지 확인 (고정된 메시지는 삭제하지 않음)
        Long pinnedMessageId = pinnedMessageMap.get(guild.getIdLong());
        if (pinnedMessageId != null && message.getIdLong() == pinnedMessageId) {
            System.out.println("[DEBUG] 안내 메시지는 유지됨: " + message.getId());
            return;
        }

        // 📌 봇이 보낸 일반 메시지도 삭제 (단, 안내 메시지는 제외)
        System.out.println("[DEBUG] 메시지 삭제됨: " + message.getContentRaw());
        message.delete().queueAfter(3, TimeUnit.SECONDS);
    }

    // 음악 채널이 삭제되면 pinnedMessageMap을 정리
    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        TextChannel deletedChannel = event.getChannel().asTextChannel();
        Guild guild = event.getGuild();

        // 삭제된 채널이 음악 채널인지 확인
        Long musicChannelId = guildChannelMap.get(guild.getIdLong());
        if (musicChannelId != null && deletedChannel.getIdLong() == musicChannelId) {
            System.out.println("[DEBUG] 음악 채널이 삭제됨: " + deletedChannel.getName());

            // 채널 삭제 시 pinnedMessageMap도 제거
            pinnedMessageMap.remove(guild.getIdLong());
            guildChannelMap.remove(guild.getIdLong());
        }
    }
}
