package me.name.bot;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class MyListener extends ListenerAdapter {
    private SheetHandler sheetHandler;

    MyListener () throws GeneralSecurityException, IOException {
        this.sheetHandler = new SheetHandler();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String msg = "Games for";
        String[] gamerTags;
        if (event.getAuthor().isBot()) return;
        // We don't want to respond to other bot accounts, including our self
        Message message = event.getMessage();
        String content = message.getContentRaw();
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        if (content.contains("!lfg"))
        {
            Member member = message.getMember();
            VoiceChannel voiceChannel = member.getVoiceState().getChannel();
            MessageChannel textChannel = event.getChannel();
            List<User> users = message.getMentionedUsers();
            if (users.isEmpty()) {
                List<Member> members = voiceChannel.getMembers();
                gamerTags = new String[members.size()];
                int i = 0;
                for (Member channelMember : members) {
                    String channelMemberString = channelMember.getUser().getName();
                    gamerTags[i++] = channelMemberString;
                    msg += " " + channelMemberString;
                }
            } else {
                gamerTags = new String[users.size()];
                int i = 0;
                for (User channelUsers : users) {
                    String channelMemberString = channelUsers.getName();
                    gamerTags[i++] = channelMemberString;
                    msg += " " + channelMemberString;
                }
            }
            System.out.println(msg);
            try {
                msg += "\n"+sheetHandler.analyseSheet(gamerTags);
            } catch (IOException e) {
                msg = "\nError";
            }

            textChannel.sendMessage(msg).queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
        }
    }
}