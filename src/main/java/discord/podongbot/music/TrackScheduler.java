package discord.podongbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

// 음악 재생 대기열(Queue) 관리
public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer audioPlayer;
    private final BlockingQueue<AudioTrack> queue;
    private final Guild guild;
    private TextChannel textChannel;
    private int repeatMode = 0; // 0: 반복 없음, 1: 현재 트랙 반복, 2: 대기열 전체 반복

    public TrackScheduler(AudioPlayer audioPlayer, Guild guild) {
        this.audioPlayer = audioPlayer;
        this.queue = new LinkedBlockingDeque<>();
        this.guild = guild;
    }

    public void queue(AudioTrack track) {
        if(!this.audioPlayer.startTrack(track, true)) {
            this.queue.offer(track);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if(repeatMode == 1) {
                this.audioPlayer.startTrack(track.makeClone(), false); // 현재 트랙만 반복
            }
            else if(repeatMode == 2) {
                this.queue.offer(track.makeClone()); // 대기열 끝에 현재 트랙 추가
                if(!this.queue.isEmpty()) {
                    nextTrack();
                }
                else {
                    leaveVoiceChannel();
                }
            }
            else {
                nextTrack();
                if(this.queue.isEmpty()) {
                    leaveVoiceChannel();
                }
            }
        }
    }

    public void nextTrack() {
        this.audioPlayer.startTrack(this.queue.poll(), false);
    }

    public List<AudioTrack> getQueue() {
        return this.queue.stream().collect(Collectors.toList());
    }

    public void setRepeatMode(int mode) {
        this.repeatMode = mode;
    }

    public void setTextChannel(TextChannel textChannel) {
        this.textChannel = textChannel;
    }

    private void leaveVoiceChannel() {
        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.isConnected()) {
            audioManager.closeAudioConnection();
            if(textChannel != null) {
                textChannel.sendMessage("⛔ 음악이 끝났습니다!").queue();
            }
        }
    }

    public void setQueue(List<AudioTrack> newQueue) {
        this.queue.clear();
        this.queue.addAll(newQueue);
    }
}
