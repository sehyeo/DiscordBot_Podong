package org.github.podong.bot.response;

import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.github.podong.bot.music.PlayerManager;

public class ChattingReaction extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();
        String[] parts = msg.split(" ", 2);

        switch(parts[0]) {
            case "!ping" :
            case "!핑" :
                long ping = event.getJDA().getGatewayPing(); // 현재 봇의 핑 가져오기
                event.getChannel().sendMessage("포동봇의 핑: " + ping + "ms").queue();
                break;

            case "!노래" :
            case "!재생" :
            case "!play" :
                playMusic(event, parts[1]);
                break;
        }
    }

    public void playMusic(MessageReceivedEvent event, String text) {
        if(!event.getMember().getVoiceState().inAudioChannel()) {
            event.getChannel().sendMessage("소속해 있는 음성채널이 없습니다.").queue();
            return;
        }

        if(!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
            final AudioManager audioManager = event.getGuild().getAudioManager();
            final VoiceChannel memberChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();

            audioManager.openAudioConnection(memberChannel);
        }

        String link = "ytsearch: " + text + " 노래";
        PlayerManager.getINSTANCE().loadAndPlay(event.getChannel().asTextChannel(), link, event.getMember());
    }
}
