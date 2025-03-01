package discord.podongbot.reaction;

import discord.podongbot.music.PlayerManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class ChattingReaction extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();
        String[] parts = msg.split(" ", 2);

        PlayerManager.handleAutoPlayMusic(event); // 음악 자동 재생

        switch(parts[0]) {
            case "!노래":
            case "!재생":
            case "!play":
                playMusic(event, parts[1]);
                break;
        }
    }

    public void playMusic(MessageReceivedEvent event, String text) {
        Member member = event.getMember();

        if(member == null || !member.getVoiceState().inAudioChannel()) {
            event.getChannel().sendMessage("소속해있는 음성채널이 없습니다.").queue();
            return;
        }

        if(!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
            final AudioManager audioManager = event.getGuild().getAudioManager();
            final VoiceChannel memberChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();

            audioManager.openAudioConnection(memberChannel);
        }

        String link = "ytsearch: " + text + " 노래";
        PlayerManager.getINSTANCE().loadAndPlay(event.getChannel().asTextChannel(), link);
    }
}
