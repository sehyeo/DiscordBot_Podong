package discord.podongbot.clean;

import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.Permission;

import java.util.concurrent.TimeUnit;

public class CleanManager extends ListenerAdapter {

    public static void handleChattingCleanCommand(SlashCommandInteractionEvent event, int amount) {
        if (!event.getName().equals("청소")) return;

        // 관리자 권한 확인 (getMember()가 null일 가능성 방지)
        if (event.getMember() == null || !event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.reply("❌ 이 명령어를 사용할 권한이 없습니다.").setEphemeral(true).queue();
            return;
        }

        // 유효성 검사 (amount 값이 올바른지 확인)
        if (amount < 1 || amount > 100) {
            event.reply("⚠️ 1~100개 사이의 메시지만 삭제할 수 있습니다.").setEphemeral(true).queue();
            return;
        }

        GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();

        // 메시지 삭제
        channel.getHistory().retrievePast(amount).queue(messages -> {
            if (messages.isEmpty()) {
                event.reply("⚠️ 삭제할 메시지가 없습니다.").setEphemeral(true).queue();
                return;
            }

            try {
                channel.purgeMessages(messages);
                // channel.purgeMessagesById(messageIds); 명령어는 JDA 5.X 버전에서 삭제됨..

                // 메시지 삭제 완료 알림 (3초 후 자동 삭제)
                event.reply("✅ 최근 " + amount + "개의 메시지를 삭제했습니다.")
                        .queue(msg -> msg.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));

            } catch (Exception e) {
                event.reply("⚠️ 메시지 삭제 중 오류가 발생했습니다.").setEphemeral(true).queue();
                e.printStackTrace();
            }
        });
    }
}
