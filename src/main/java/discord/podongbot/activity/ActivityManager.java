package discord.podongbot.activity;

import net.dv8tion.jda.api.entities.Activity;
import org.springframework.stereotype.Component;

@Component
public class ActivityManager {

    public Activity getDefaultActivity() {
        return Activity.playing("봇만들기");
    }

    public Activity getCompetingActivity() {
        return Activity.competing("게임");
    }

    public Activity getPlayingActivity() {
        return Activity.playing("음악");
    }

    public Activity getWatchingActivity() {
        return Activity.watching("영화");
    }

    public Activity getCustomStatus() {
        return Activity.customStatus("명령어 기다리는 중...");
    }
}
