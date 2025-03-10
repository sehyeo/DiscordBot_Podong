package discord.podongbot.help;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import java.awt.Color;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class HelpManager extends ListenerAdapter {

    private static LocalDate lastUpdateDate = LocalDate.now();
    private static List<CommandData> commands;

    public static void updateCommands(List<CommandData> updatedCommands) {
        commands = updatedCommands;
        lastUpdateDate = LocalDate.now();
    }

    public static void handleHelpCommands(SlashCommandInteractionEvent event) {
        if (event.getName().equals("도움말")) {
            long daysSinceUpdate = ChronoUnit.DAYS.between(lastUpdateDate, LocalDate.now());
            int commandCount = commands != null ? commands.size() : 0;

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("포동봇");
            embed.setDescription("음악, 게임, 관리, 리더보드 명령어들을 보유한 봇");
            embed.addField("업타임", lastUpdateDate + " (" + daysSinceUpdate + "일 전)", false);
            embed.addField("명령어 개수", commandCount + "개", false);
            embed.setColor(Color.GREEN);
            embed.setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

            StringSelectMenu menu = StringSelectMenu.create("help-category")
                    .setPlaceholder("카테고리를 선택해 주세요")
                    .addOption("음악", "music", "음성채널에서 음악을 들을 수 있는 명령어", null)
                    //.addOption("게임", "game", "게임을 하는 명령어", null)
                    .addOption("관리", "admin", "서버를 관리할 수 있는 명령어", null)
                    //.addOption("일반", "general", "일반 명령어", null)
                    //.addOption("리더보드", "leaderboard", "리더보드를 확인할 수 있는 명령어", null)
                    .build();

            Button noticeButton = Button.success("notice", "📄 공지사항 보기");
            Button dashboardButton = Button.primary("dashboard", "⚙ 대시보드");
            Button supportButton = Button.danger("support", "🆘 서포트 서버");

            event.replyEmbeds(embed.build())
                    .addActionRow(menu)
                    .addActionRow(noticeButton, dashboardButton, supportButton)
                    .queue();
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("help-category")) {
            String selectedCategory = event.getValues().get(0);
            EmbedBuilder embed = new EmbedBuilder();

            switch (selectedCategory) {
                case "music":
                    embed.setTitle("🎵 음악 명령어");
                    embed.setColor(Color.BLUE);
                    embed.addField("/재생", "음악을 재생해요", true);
                    embed.addField("/대기열", "대기열을 보여줘요", true);
                    embed.addField("/반복", "음악을 반복해요", true);
                    embed.addField("/볼륨", "볼륨을 조절해요", true);
                    embed.addField("/셔플", "음악을 셔플해요", true);
                    embed.addField("/스킵", "음악을 스킵해요", true);
                    embed.addField("/일시정지", "음악을 일시정지해요", true);
                    embed.addField("/정지", "음악을 종료해요", true);
                    break;
                //case "game":
                    //embed.setTitle("🎮 게임 명령어");
                    //embed.setColor(Color.RED);
                    //embed.addField("/게임시작", "게임을 시작합니다", true);
                    //embed.addField("/랭킹", "현재 게임 랭킹을 확인합니다", true);
                    //break;
                case "admin":
                    embed.setTitle("🔧 관리 명령어");
                    embed.setColor(Color.ORANGE);
                    embed.addField("/청소", "메시지를 삭제합니다", true);
                    //embed.addField("/밴", "유저를 밴합니다", true);
                    break;
                //case "general":
                    //embed.setTitle("🎈 일반 명령어");
                    //embed.setColor(Color.MAGENTA);
                    //embed.addField("/날씨", "현재 날씨를 조회합니다", true);
                    //embed.addField("/시간", "현재 시간을 알려줍니다", true);
                    //break;
                //case "leaderboard":
                    //embed.setTitle("🏆 리더보드 명령어");
                    //embed.setColor(Color.YELLOW);
                    //embed.addField("/점수", "현재 점수를 확인합니다", true);
                    //embed.addField("/순위", "랭킹을 확인합니다", true);
                    //break;
                default:
                    return;
            }
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }
    }

    @Override
    public void onGuildReady(net.dv8tion.jda.api.events.guild.GuildReadyEvent event) {
        List<CommandData> commandDataList = event.getGuild().retrieveCommands().complete().stream()
                .map(command -> (CommandData) Commands.slash(command.getName(), command.getDescription()))
                .toList();

        updateCommands(commandDataList);
    }


}