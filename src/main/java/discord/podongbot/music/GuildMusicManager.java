package discord.podongbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import discord.podongbot.volume.VolumeControl;

// 서버별로 플레이어와 트랙 관리를 담당
public class GuildMusicManager {
    public final AudioPlayer audioPlayer;
    public final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;
    private final VolumeControl volumeControl;

    public GuildMusicManager(AudioPlayerManager manager) {
        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.audioPlayer);
        this.audioPlayer.addListener(this.scheduler);
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
        this.volumeControl = new VolumeControl(this.audioPlayer);

        // 기본 볼륨을 강제로 20%로 설정
        this.audioPlayer.setVolume(20);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return this.sendHandler;
    }

    public VolumeControl getVolumeControl() {
        return this.volumeControl;
    }
}
