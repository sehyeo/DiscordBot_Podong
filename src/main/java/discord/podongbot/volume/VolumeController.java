package discord.podongbot.volume;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord.podongbot.music.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import discord.podongbot.music.PlayerManager;

import javax.swing.text.TabExpander;

// 음악 볼륨을 조절하는 클래스
public class VolumeControl {
    private final AudioPlayer audioPlayer;
    private int volume; // 볼륨 (0~100 범위)

    public VolumeControl(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.volume = 100; // 기본 볼륨 100%
        this.audioPlayer.setVolume(this.volume);
    }

    public void setVolume(int volume) {
        if (volume < 0) volume = 0;
        if (volume > 100) volume = 100;
        this.volume = volume;
        this.audioPlayer.setVolume(volume);
    }

    public static VolumeControl getVolumeControl(Guild guild, TextChannel textChannel) {
        GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(guild, textChannel);
        return musicManager.getVolumeControl();
    }

    public static void handleVolumeCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        TextChannel textChannel = event.getChannel().asTextChannel();
        if (guild == null) {
            event.reply("이 명령어는 서버에서만 사용할 수 있습니다.").setEphemeral(true).queue();
            return;
        }

        if (event.getOption("value") == null) {
            event.reply("볼륨 값을 입력해주세요. 예: `/volume 50`").setEphemeral(true).queue();
            return;
        }

        int volume = event.getOption("value").getAsInt();

        if (volume < 0 || volume > 100) {
            event.reply("올바른 숫자를 입력해주세요 (0~100). 예: `/volume 50`").setEphemeral(true).queue();
            return;
        }

        VolumeControl volumeControl = getVolumeControl(guild, textChannel);
        volumeControl.setVolume(volume);
        event.reply("볼륨이 " + volume + "%로 설정되었습니다.").queue();
    }


}
