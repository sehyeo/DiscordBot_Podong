package discord.podongbot.voice;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class VoiceController {

    public static void joinVoiceChannel(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null || !member.getVoiceState().inAudioChannel()) {
            event.reply("음성 채널에 먼저 들어가 주세요!").setEphemeral(true).queue();
            return;
        }

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("길드 정보를 가져올 수 없습니다.").setEphemeral(true).queue();
            return;
        }

        VoiceChannel channel = member.getVoiceState().getChannel().asVoiceChannel();
        AudioManager audioManager = guild.getAudioManager();

        audioManager.openAudioConnection(channel);
        event.reply("음성 채널에 입장했습니다. 현재 채널: **" + channel.getName() + "**").queue();
    }

    public static void leaveVoiceChannel(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("길드 정보를 가져올 수 없습니다.").setEphemeral(true).queue();
            return;
        }

        AudioManager audioManager = guild.getAudioManager();
        if (!audioManager.isConnected()) {
            event.reply("봇이 현재 음성 채널에 연결되어 있지 않습니다.").setEphemeral(true).queue();
            return;
        }

        audioManager.closeAudioConnection();
        event.reply("음성 채널에서 퇴장했습니다.").queue();
    }
}
