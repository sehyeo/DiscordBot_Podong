package discord.podongbot.volume;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord.podongbot.music.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import discord.podongbot.music.PlayerManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;

// 음악 볼륨을 조절하는 클래스
public class VolumeControl {
    private final AudioPlayer audioPlayer;
    private int volume; // 볼륨 (0~100 범위)
    private static final Map<Long, Long> guildChannelMap = new HashMap<>(); // 길드별 전용 채널 저장

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

    public static VolumeControl getVolumeControl(Guild guild) {
        GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(guild);
        return musicManager.getVolumeControl();
    }

    public static void handleVolumeCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
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

        VolumeControl volumeControl = getVolumeControl(guild);
        volumeControl.setVolume(volume);
        event.reply("볼륨이 " + volume + "%로 설정되었습니다.").queue();
    }

    public static void handleChannelSetupCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("이 명령어는 서버에서만 사용할 수 있습니다.").setEphemeral(true).queue();
            return;
        }

        // 서버에서 "포동봇-음악채널"이 이미 존재하는지 확인
        for (TextChannel channel : guild.getTextChannels()) {
            if (channel.getName().equalsIgnoreCase("포동봇-음악채널")) {
                event.reply("이미 전용 음악 채널이 설정되어 있습니다: " + channel.getAsMention()).queue();
                return;
            }
        }

        guild.createTextChannel("포동봇-음악채널").queue(channel -> {
            guildChannelMap.put(guild.getIdLong(), channel.getIdLong());
            event.reply("전용 음악 채널이 생성되었습니다: " + channel.getAsMention()).queue();
        });
    }

    public static void handleAutoPlayMusic(MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        // 봇의 메시지는 무시
        if (event.getAuthor().isBot()) return;

        TextChannel musicChannel = null;
        for (TextChannel channel : guild.getTextChannels()) {
            if (channel.getName().equalsIgnoreCase("포동봇-음악채널")) {
                musicChannel = channel;
                break;
            }
        }

        // 전용 채널이 없으면 무시
        if (musicChannel == null || event.getChannel().getIdLong() != musicChannel.getIdLong()) return;

        String musicQuery = event.getMessage().getContentRaw();
        System.out.println("입력된 음악 제목: " + musicQuery); // 디버깅 출력

        if (musicQuery.isEmpty()) {
            event.getChannel().sendMessage("검색할 노래 제목을 입력해주세요!").queue();
            return;
        }

        // 사용자가 음성 채널에 있는지 확인
        if (!event.getMember().getVoiceState().inAudioChannel()) {
            event.getChannel().sendMessage("음성 채널에 먼저 접속해주세요!").queue();
            return;
        }

        // 봇이 음성 채널에 없으면 자동으로 연결
        if (!guild.getAudioManager().isConnected()) {
            VoiceChannel userChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();
            guild.getAudioManager().openAudioConnection(userChannel);
        }
        String link = "ytsearch: " + musicQuery;
        PlayerManager.getINSTANCE().loadAndPlay(event.getChannel().asTextChannel(), link, event.getMember());
    }
}
