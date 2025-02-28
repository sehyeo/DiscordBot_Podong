package discord.podongbot.response;

import discord.podongbot.channel.ChannelManager;
import discord.podongbot.music.PlayerManager;
import discord.podongbot.voice.BotVoiceControl;
import discord.podongbot.volume.VolumeControl;
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
        switch(event.getName()) {
            case "핑":
                long ping = event.getJDA().getGatewayPing(); // 현재 봇의 핑 가져오기
                event.reply("포동봇의 핑: " + ping + "ms").queue();
                break;
            case "볼륨":
                VolumeControl.handleVolumeCommand(event);
                break;
            case "채널설정":
                ChannelManager.handleChannelSetupCommand(event);
                break;
            case "입장":
                BotVoiceControl.joinVoiceChannel(event);
                break;
            case "퇴장":
                BotVoiceControl.leaveVoiceChannel(event);
                break;
            case "대기열":
                PlayerManager.handleQueueCommand(event);
                break;
            case "중지":
                PlayerManager.handleTogglePauseCommand(event);
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
                Commands.slash("중지", "음악을 일시정지합니다.")
        );
        event.getGuild().updateCommands().addCommands(commandDatas).queue();
    }
}
