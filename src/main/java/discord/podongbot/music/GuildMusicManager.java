package discord.podongbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import discord.podongbot.volume.VolumeController;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

// 서버별로 플레이어와 트랙 관리를 담당
public class GuildMusicManager {
    public final AudioPlayer audioPlayer;
    public final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;
    private final VolumeController volumeControl;

    public GuildMusicManager(AudioPlayerManager manager, Guild guild, TextChannel textChannel) {
        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.audioPlayer, guild);
        this.audioPlayer.addListener(this.scheduler);
        this.scheduler.setTextChannel(textChannel);
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
        this.volumeControl = new VolumeController(this.audioPlayer);

        // 기본 볼륨을 강제로 20%로 설정
        this.audioPlayer.setVolume(20);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return this.sendHandler;
    }

    public VolumeController getVolumeControl() {
        return this.volumeControl;
    }
}
