package discord.podongbot.game;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.Random;

public class GameManager extends ListenerAdapter {

    private static final Random random = new Random();

    public static void ChooseCommand(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("골라")) return;

        String input = event.getOption("options").getAsString();
        if (input == null || input.isBlank()) {
            event.reply("⚠️ 선택할 항목을 입력해주세요! 예: /골라 사과 포도 바나나").queue();
            return;
        }

        // 입력된 문자열을 공백 기준으로 분리하여 리스트로 변환
        List<String> options = List.of(input.split("\\s+"));

        if (options.size() < 2) {
            event.reply("⚠️ 두 개 이상의 항목을 입력해주세요! 예: /골라 사과 포도").queue();
            return;
        }

        // 랜덤으로 하나 선택
        String chosen = options.get(random.nextInt(options.size()));

        event.reply("🎲 선택된 항목: **" + chosen + "**").queue();
    }
}