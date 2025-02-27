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
            case "ping" :
            case "핑" :
                event.getChannel().sendMessage("Pong!").queue();
                break;

            case "대답" :
            case "reply" :
                event.getMessage().reply("Reply!").queue();
                break;

            case "노래" :
            case "play" :
                playMusic(event, parts[1]);
                break;
        }
    }

    public void playMusic(MessageReceivedEvent event, String text) {
        if(!event.getMember().getVoiceState().inAudioChannel()) {
            event.getChannel().sendMessage("소속해 있는 보이스 채널이 없습니다.").queue();
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
