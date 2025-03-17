package discord.podongbot.reaction;

import discord.podongbot.channel.ChannelManager;
import discord.podongbot.clean.CleanManager;
import discord.podongbot.game.GameManager;
import discord.podongbot.help.HelpManager;
import discord.podongbot.music.PlayerManager;
import discord.podongbot.server.ServerManager;
import discord.podongbot.voice.VoiceController;
import discord.podongbot.volume.VolumeController;
import discord.podongbot.user.UserInfoManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.ArrayList;
import java.util.List;

public class SlashCommandReaction extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        // "포동봇-음악채널"이 존재하는지 확인
        boolean hasMusicChannel = guild.getTextChannels().stream()
                .anyMatch(channel -> channel.getName().equalsIgnoreCase("포동봇-음악채널"));

        if (event.getName().equals("채널설정")) {
            ChannelManager.handleChannelSetupCommand(event);
            return;
        }

        if(event.getName().equals("핑")) {
            long ping = event.getJDA().getGatewayPing(); // 현재 봇의 핑 가져오기
            event.reply("포동봇의 핑: " + ping + "ms").queue();
            return;
        }

        if(event.getName().equals("서버정보")) {
            ServerManager.handleServerInfoCommand(event);
            return;
        }

        if(event.getName().equals("청소")) {
            int amount = event.getOption("amount").getAsInt();
            CleanManager.handleChattingCleanCommand(event, amount);
            return;
        }

        if(event.getName().equals("도움말")) {
            HelpManager.handleHelpCommands(event);
            return;
        }

        if (event.getName().equals("유저정보")) {
            UserInfoManager.handleUserListCommand(event);
            return;
        }

        if (event.getName().equals("골라")) {
            GameManager.ChooseCommand(event);
            return;
        }
        // 음악 채널 생성 필요
        if (!hasMusicChannel) {
            event.reply("⚠️ 음악 채널을 생성해주세요! (채널 이름: **포동봇-음악채널**)").queue();
            return;
        }

        switch(event.getName()) {
            case "볼륨":
                VolumeController.handleVolumeCommand(event);
                break;
            case "입장":
                VoiceController.joinVoiceChannel(event);
                break;
            case "퇴장":
                VoiceController.leaveVoiceChannel(event);
                break;
            case "대기열":
                PlayerManager.handleQueueCommand(event);
                break;
            case "일시정지":
                PlayerManager.handleTogglePauseCommand(event);
                break;
            case "정지":
                PlayerManager.handleStopCommand(event);
                break;
            case "반복":
                int mode = event.getOption("mode").getAsInt();
                PlayerManager.handleRepeatCommand(event, mode);
                break;
            case "셔플":
                PlayerManager.handleShuffleCommand(event);
                break;
            case "삭제":
                int index = event.getOption("index").getAsInt();
                PlayerManager.handleRemoveCommand(event, index);
                break;
            case "스킵":
                PlayerManager.handleSkipCommand(event);
                break;
        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandDatas = new ArrayList<>();
        commandDatas.add(
                Commands.slash("핑", "포동봇의 현재 핑 상태를 보여줍니다.")
        );
        commandDatas.add(
                Commands.slash("볼륨", "포동봇의 볼륨을 조절합니다.")
                        .addOption(OptionType.INTEGER, "value", "설정할 볼륨 크기 (0~100)", true)
        );
        commandDatas.add(
                Commands.slash("채널설정", "포동봇 전용 채널을 설정합니다.")
        );
        commandDatas.add(
                Commands.slash("입장", "봇을 현재 음성 채널에 입장시킵니다.")
        );
        commandDatas.add(
                Commands.slash("퇴장", "봇을 현재 음성 채널에서 퇴장시킵니다.")
        );
        commandDatas.add(
                Commands.slash("대기열", "현재 대기열을 보여줍니다.")
        );
        commandDatas.add(
                Commands.slash("일시정지", "음악을 일시정지합니다.")
        );
        commandDatas.add(
                Commands.slash("정지", "모든 플레이어를 초기화합니다.")
        );
        commandDatas.add(
                Commands.slash("반복", "반복 모드를 설정합니다.")
                        .addOption(OptionType.INTEGER, "mode", "반복 모드 (0: 반복 없음, 1: 현재 트랙 반복, 2: 대기열 전체 반복)", true)
        );
        commandDatas.add(
                Commands.slash("셔플", "모든 곡의 순서를 섞습니다.")
        );
        commandDatas.add(
                Commands.slash("삭제", "대기열에서 특정 곡을 삭제합니다.")
                        .addOption(OptionType.INTEGER, "index", "삭제할 곡의 순서 (1부터 시작)", true)
        );
        commandDatas.add(
                Commands.slash("스킵", "현재 재생되고 있는 음악을 스킵합니다.")
        );
        commandDatas.add(
                Commands.slash("서버정보", "서버의 정보를 확인합니다.")
        );
        commandDatas.add(
                Commands.slash("청소", "최근 메시지를 삭제합니다.")
                        .addOption(OptionType.INTEGER, "amount", "삭제할 메시지 개수 (최대 100개)", true)
        );
        commandDatas.add(
                Commands.slash("도움말", "사용 가능한 명령어 목록을 확인합니다.")
        );
        commandDatas.add(
                Commands.slash("유저정보", "서버에 있는 유저 정보를 확인합니다.")
        );
        commandDatas.add(
                Commands.slash("골라", "입력한 항목 중 하나를 랜덤으로 선택합니다.")
                        .addOption(OptionType.STRING, "options", "선택할 항목들을 쉼표(,)로 구분하여 입력", true)
        );
        event.getGuild().updateCommands().addCommands(commandDatas).queue();
    }
}
