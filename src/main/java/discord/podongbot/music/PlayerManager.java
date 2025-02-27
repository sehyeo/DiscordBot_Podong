package discord.podongbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;

// 서버별 GuildMusicManager를 중앙에서 관리
public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    private PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        audioPlayerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
        audioPlayerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);


        YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager(true);
        this.audioPlayerManager.registerSourceManager(youtube);


        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public static PlayerManager getINSTANCE() {
        if(INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void loadAndPlay(TextChannel textChannel, String trackURL, Member client) {
        final GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());
        this.audioPlayerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                // 트랙 대기열 추가
                musicManager.scheduler.queue(audioTrack);
                textChannel.sendMessageFormat("재생 중인 곡: `%s` (by `%s`)",
                        audioTrack.getInfo().title,
                        audioTrack.getInfo().author
                ).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                // 플레이리스트 처리
                AudioTrack firstTrack = audioPlaylist.getSelectedTrack() != null
                        ? audioPlaylist.getSelectedTrack()
                        : audioPlaylist.getTracks().get(0);

                musicManager.scheduler.queue(firstTrack);
                textChannel.sendMessageFormat(
                        "재생 중인 곡: `%s` (by `%s`)",
                        firstTrack.getInfo().title,
                        firstTrack.getInfo().author
                ).queue();
            }

            @Override
            public void noMatches() {
                textChannel.sendMessage("일치하는 결과가 없습니다. " + trackURL).queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                textChannel.sendMessage("재생할 수 없습니다. " +  e.getMessage()).queue();
            }
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
