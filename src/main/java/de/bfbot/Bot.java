package de.bfbot;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.ini4j.Ini;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.permission.Permissions;

import java.io.File;
import java.io.IOException;

public class Bot {

    public static final Logger logger = LogManager.getLogger(Bot.class);
    public static DiscordApi api;


    public static void main(String[] args) throws IOException {
        logger.info("Bot Starting");
        Bot b = new Bot();
    }
    public Bot() throws IOException {
        var ini = new Ini(new File("bot.ini"));
        var token = ini.get("config", "token");
        var channel_id = ini.get("config", "channel_id");
        var msg_id = ini.get("config", "msg_id");
        var role_id = ini.get("config", "role_id");
        var reaction_emoji = ini.get("config", "reaction_emoji");




        api = new DiscordApiBuilder()
                .setAllIntents()
                .setToken(token)
                .login().join();
        logger.info(api.createBotInvite(Permissions.fromBitmask(8)));
        Init(channel_id, msg_id, role_id, reaction_emoji);
        logger.info("Bot Started");
    }

    private void Init(String channel_id, String msg_id, String role_id, String reaction_emoji) throws IOException {

        var channel = api.getTextChannelById(channel_id).get();
        var msg = api.getMessageById(msg_id, channel).join();
        var role = api.getRoleById(role_id).get();

        api.addReactionAddListener(reactionAddEvent -> {
            var message = reactionAddEvent.getMessage().get();
            var emoji = reactionAddEvent.getReaction().get().getEmoji();
            logger.info(emoji);
            logger.info(reaction_emoji);
            logger.info(emoji.equalsEmoji(reaction_emoji));
            if((message == msg) && emoji.equalsEmoji(reaction_emoji)) {
                role.addUser(reactionAddEvent.getUser().get()).join();
            }
        });

        api.addReactionRemoveListener(reactionRemoveEvent -> {
            var message = reactionRemoveEvent.getMessage().get();
            var emoji = reactionRemoveEvent.getReaction().get().getEmoji();
            logger.info(emoji);
            logger.info(reaction_emoji);
            logger.info(emoji.equalsEmoji(reaction_emoji));
            if((message == msg) && emoji.equalsEmoji(reaction_emoji)) {
                role.removeUser(reactionRemoveEvent.getUser().get()).join();
            }
        });
    }

    private void Disconnect() {
        api.disconnect();
        System.exit(0);
    }

}
