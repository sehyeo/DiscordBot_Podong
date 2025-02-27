package org.github.podong.bot.response;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.github.podong.bot.volume.VolumeControl;

import java.util.ArrayList;
import java.util.List;

public class SlashCommandReaction extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch(event.getName()) {
            case "ping":
                long ping = event.getJDA().getGatewayPing(); // 현재 봇의 핑 가져오기
                event.reply("포동봇의 핑: " + ping + "ms").queue();
                break;
            case "volume":
                VolumeControl.handleVolumeCommand(event);
                break;
        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandDatas = new ArrayList<>();
        commandDatas.add(
                Commands.slash("ping", "포동봇의 현재 핑 상태를 보여줍니다.")
        );
        commandDatas.add(
                Commands.slash("volume", "볼륨 크기를 설정합니다.")
                        .addOption(OptionType.INTEGER, "value", "설정할 볼륨 크기 (0~100)", true)
        );

        event.getGuild().updateCommands().addCommands(commandDatas).queue();
    }
}
