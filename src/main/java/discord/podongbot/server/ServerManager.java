package discord.podongbot.server;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.List;

public class ServerManager extends ListenerAdapter {

    public static void handleServerInfoCommand(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("서버정보")) return;

        Guild guild = event.getGuild();
        if (guild == null) return;

        String owner = guild.getOwner() != null ? guild.getOwner().getEffectiveName() : "알 수 없음";
        int boostCount = guild.getBoostCount();
        int memberCount = guild.getMemberCount();
        int onlineCount = (int) guild.getMembers().stream().filter(m -> m.getOnlineStatus().name().equals("ONLINE")).count();
        String securityLevel = guild.getVerificationLevel().name();
        long serverId = guild.getIdLong();
        String creationTime = guild.getTimeCreated().toString();

        String response = "**Test 서버의 정보!**\n"
                + "\n👑 **서버 주인**\n" + owner
                + "\n🚀 **서버 부스트**\n" + boostCount + "개"
                + "\n🔒 **서버 보안 수준**\n" + securityLevel
                + "\n🆔 **서버 아이디**\n" + serverId
                + "\n📅 **서버 생성일**\n" + creationTime
                + "\n🌐 **온라인, 오프라인 멤버 수**\n" + "온라인 " + onlineCount + "명 | 오프라인 " + (memberCount - onlineCount) + "명"
                + "\n👥 **총 멤버 수**\n" + memberCount + "명";

        event.reply(response).queue();
    }
}
