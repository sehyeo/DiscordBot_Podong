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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

// 서버별 GuildMusicManager를 중앙에서 관리
public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;
    private TextChannel textChannel;

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

    public GuildMusicManager getMusicManager(Guild guild, TextChannel textChannel) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager, guild, textChannel);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void loadAndPlay(TextChannel textChannel, String trackURL) {
        final GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild(), textChannel);
        boolean isPlaying = musicManager.audioPlayer.getPlayingTrack() != null;

        this.audioPlayerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                musicManager.scheduler.queue(audioTrack);
                // 트랙 대기열 추가
                if (isPlaying) {
                    textChannel.sendMessageFormat("대기열에 추가됨: **%s** (by %s)",
                            audioTrack.getInfo().title,
                            audioTrack.getInfo().author
                    ).queue();
                } else {
                    textChannel.sendMessageFormat("재생 중인 곡: **%s** (by %s)",
                            audioTrack.getInfo().title,
                            audioTrack.getInfo().author
                    ).queue();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                // 플레이리스트 처리
                AudioTrack firstTrack = audioPlaylist.getSelectedTrack() != null
                        ? audioPlaylist.getSelectedTrack()
                        : audioPlaylist.getTracks().get(0);

                musicManager.scheduler.queue(firstTrack);
                if (isPlaying) {
                    textChannel.sendMessageFormat("대기열에 추가됨: **%s** (by %s)",
                            firstTrack.getInfo().title,
                            firstTrack.getInfo().author
                    ).queue();
                } else {
                    textChannel.sendMessageFormat("재생 중인 곡: **%s** (by %s)",
                            firstTrack.getInfo().title,
                            firstTrack.getInfo().author
                    ).queue();
                }
            }

            @Override
            public void noMatches() {
                textChannel.sendMessage("일치하는 결과가 없습니다. " + trackURL).queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                if (e.getMessage().contains("blocked it from display")) {
                    textChannel.sendMessage("⚠️ 해당 영상은 저작권 문제로 인해 Discord에서 재생할 수 없습니다.\n"
                            + "🔗 YouTube에서 직접 시청하세요: " + trackURL).queue();
                } else {
                    textChannel.sendMessage("재생할 수 없습니다: " + e.getMessage()).queue();
                }
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
        PlayerManager.getINSTANCE().loadAndPlay(event.getChannel().asTextChannel(), link);
    }

    // 대기열 관리
    public static void handleQueueCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        TextChannel textChannel = event.getChannel().asTextChannel();
        GuildMusicManager musicManager = getINSTANCE().getMusicManager(guild, textChannel);
        List<AudioTrack> queue = musicManager.scheduler.getQueue();

        if (queue.isEmpty()) {
            event.reply("대기열이 비어 있습니다.").queue();
            return;
        }

        String queueList = queue.stream()
                .map(track -> String.format("- **%s** (by %s)", track.getInfo().title, track.getInfo().author))
                .collect(Collectors.joining("\n"));

        event.reply("현재 대기열:\n" + queueList).queue();
    }

    // 음악 일시정지
    public static void handleTogglePauseCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        TextChannel textChannel = event.getChannel().asTextChannel();

        if (guild == null) return;

        GuildMusicManager musicManager = getINSTANCE().getMusicManager(guild, textChannel);

        // 현재 재생 중인 트랙이 있는지 확인
        AudioTrack currentTrack = musicManager.audioPlayer.getPlayingTrack();
        if (currentTrack == null) {
            event.reply("⚠\uFE0F 음악이 재생되고 있지 않습니다!").queue();
            return;
        }

        // 현재 음악 플레이어가 재생 중인지 확인
        boolean isPaused = musicManager.audioPlayer.isPaused();

        // 상태 변경: 재생 중이면 일시 정지, 일시 정지 상태면 다시 재생
        musicManager.audioPlayer.setPaused(!isPaused);

        if (isPaused) {
            event.reply("▶\uFE0F 음악을 다시 재생합니다!").queue();
        } else {
            event.reply("⏸\uFE0F 음악을 일시정지했습니다!").queue();
        }
    }

    // 음악 정지
    public static void handleStopCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        TextChannel textChannel = event.getChannel().asTextChannel();

        if (guild == null) return;

        GuildMusicManager musicManager = getINSTANCE().getMusicManager(guild, textChannel);

        // 현재 재생 중인지 확인
        if (musicManager.audioPlayer.getPlayingTrack() == null && musicManager.scheduler.getQueue().isEmpty()) {
            event.reply("⚠\uFE0F 음악이 재생되고 있지 않습니다!").queue();
            return;
        }

        // 음악 정지
        musicManager.audioPlayer.stopTrack();

        // Lavaplayer 내부 queue를 강제로 초기화
        try {
            Field queueField = musicManager.scheduler.getClass().getDeclaredField("queue");
            queueField.setAccessible(true);
            queueField.set(musicManager.scheduler, new LinkedBlockingQueue<>()); // 새로운 빈 대기열 설정
        } catch (NoSuchFieldException | IllegalAccessException e) {
            event.getHook().sendMessage("🚨 대기열 초기화 중 오류 발생!").queue();
            return;
        }

        // 음성 채널에서 봇 나가기
        guild.getAudioManager().closeAudioConnection();

        // 메시지 출력
        event.reply("⛔ 음악이 끝났습니다!").queue();
    }

    // 음악 반복
    public static void handleRepeatCommand(SlashCommandInteractionEvent event, int mode) {
        Guild guild = event.getGuild();
        TextChannel textChannel = event.getChannel().asTextChannel();

        if (guild == null) return;

        GuildMusicManager musicManager = getINSTANCE().getMusicManager(guild, textChannel);
        TrackScheduler scheduler = musicManager.scheduler;

        // 현재 재생 중인지 확인
        if (musicManager.audioPlayer.getPlayingTrack() == null && musicManager.scheduler.getQueue().isEmpty()) {
            event.reply("⚠\uFE0F 음악이 재생되고 있지 않습니다!").queue();
            return;
        }

        if (mode == 0) {
            scheduler.setRepeatMode(0);
            event.reply("🔁 반복이 비활성화되었습니다.").queue();
        } else if (mode == 1) {
            scheduler.setRepeatMode(1);
            event.reply("🔂 현재 재생 중인 음악이 반복됩니다.").queue();
        } else if (mode == 2) {
            scheduler.setRepeatMode(2);
            event.reply("🔁 대기열의 모든 음악이 반복됩니다.").queue();
        } else {
            event.reply("❌ 잘못된 입력입니다. `/반복 0`, `/반복 1`, `/반복 2` 중 하나를 입력해주세요.").queue();
        }
    }



}
