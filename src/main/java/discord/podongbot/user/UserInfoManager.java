package discord.podongbot.user;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.Color;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class UserInfoManager extends ListenerAdapter {

    // `/유저정보` 실행 시 서버 멤버 목록을 선택 메뉴로 제공
    public static void handleUserListCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        List<Member> members = guild.getMembers();
        if (members.isEmpty()) {
            event.reply("⚠️ 서버에 유저가 없습니다.").setEphemeral(true).queue();
            return;
        }

        // 선택할 유저 목록 최대 25명 (Discord 제한)
        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("user-info-select")
                .setPlaceholder("유저를 선택하세요");

        members.stream().limit(25).forEach(member ->
                menuBuilder.addOption(member.getEffectiveName(), member.getId()));

        event.reply("🔍 확인할 유저를 선택하세요!")
                .addActionRow(menuBuilder.build())
                .setEphemeral(true)
                .queue();
    }

    // 유저 선택 후 해당 유저의 정보를 출력
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!event.getComponentId().equals("user-info-select")) return;

        String userId = event.getValues().get(0);
        Guild guild = event.getGuild();
        if (guild == null) return;

        Member member = guild.getMemberById(userId);
        if (member == null) {
            event.reply("⚠️ 해당 유저를 찾을 수 없습니다.").setEphemeral(true).queue();
            return;
        }

        User user = member.getUser();
        String accountType = user.isBot() ? "🤖 봇 계정" : "👤 사람 계정";

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("유저 정보")
                .setColor(Color.BLUE)
                .setThumbnail(user.getEffectiveAvatarUrl())
                .addField("👤 유저", "<@" + user.getId() + "> (" + user.getName() + ")", false)
                .addField("📌 계정 유형", accountType, false)
                .addField("🆔 고유 ID", user.getId(), false)
                .addField("📅 계정 생성", formatTime(user.getTimeCreated()) + " (" + getDuration(user.getTimeCreated()) + ")", false)
                .addField("👋 서버 입장", formatTime(member.getTimeJoined()) + " (" + getDuration(member.getTimeJoined()) + ")", false);
        event.replyEmbeds(embed.build()).queue();
    }

    // 날짜 형식 변환
    private static String formatTime(OffsetDateTime time) {
        return time.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime().toString();
    }

    // 몇 년 전인지 계산
    private static String getDuration(OffsetDateTime time) {
        long years = ChronoUnit.YEARS.between(time, Instant.now().atOffset(ZoneId.systemDefault().getRules().getOffset(Instant.now())));
        long months = ChronoUnit.MONTHS.between(time, Instant.now().atOffset(ZoneId.systemDefault().getRules().getOffset(Instant.now()))) % 12;
        return years + "년 " + months + "개월 전";
    }
}
