package discord.podongbot.server;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ServerManager extends ListenerAdapter {

    public static void handleServerInfoCommand(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("서버정보")) return;

        Guild guild = event.getGuild();
        if (guild == null) return;

        String serverName = guild.getName();
        String owner = guild.getOwner() != null ? guild.getOwner().getEffectiveName() : "알 수 없음";
        int boostCount = guild.getBoostCount();
        int memberCount = guild.getMemberCount();
        int onlineCount = (int) guild.getMembers().stream().filter(m -> m.getOnlineStatus().name().equals("ONLINE")).count();
        String securityLevel = guild.getVerificationLevel().name();
        long serverId = guild.getIdLong();
        String creationTime = guild.getTimeCreated().toString();
        String serverIcon = guild.getIconUrl();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(serverName + " 서버의 정보");
        if (serverIcon != null) {
            embed.setThumbnail(serverIcon);
        }
        embed.addField("👑 서버 주인", owner, false);
        embed.addField("🚀 서버 부스트", boostCount + "개", false);
        embed.addField("🔒 서버 보안 수준", securityLevel, false);
        embed.addField("🆔 서버 아이디", String.valueOf(serverId), false);
        embed.addField("📅 서버 생성일", creationTime, false);
        embed.addField("🌐 온라인, 오프라인 멤버 수", "온라인 " + onlineCount + "명 | 오프라인 " + (memberCount - onlineCount) + "명", false);
        embed.addField("👥 총 멤버 수", memberCount + "명", false);

        event.replyEmbeds(embed.build()).queue();
    }
}
