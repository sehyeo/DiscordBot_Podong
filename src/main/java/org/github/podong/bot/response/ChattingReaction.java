package org.github.podong.bot.response;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChattingReaction extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String str = event.getMessage().getContentDisplay();

        switch(str) {
            case "ping" :
            case "핑" :
                event.getChannel().sendMessage("Pong!").queue();
                break;

            case "대답" :
            case "reply" :
                event.getMessage().reply("Reply!").queue();
                break;
        }
    }
}
