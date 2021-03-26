package de.bfbot;

import de.bfbot.functions.VerefiedFunctionAdd;
import de.bfbot.functions.VerifiedFunctionRemove;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Bot {

    private static final Logger logger = LogManager.getLogger(Bot.class);
    public static DiscordApi api;
    public static Optional<Role> Verified;
    public static Optional<Server> Server;
    public static Optional<ServerChannel> MitgliederChannl, OnlineChannel, InVoiceChannel, VerificationChannel, RoleChannel;
    public static CompletableFuture<Message> VerificationMessage, RoleMessage;
    public Bot(String token){
        api = new DiscordApiBuilder()
                .setAllIntents()
                .setToken(token)
                .login().join();
        Init();
        logger.info("Bot Started");
    }

    private void Init() {
        Server = api.getServerById(IDs.BFServer);
        Verified = api.getRoleById(IDs.VerifiedRole);
        MitgliederChannl = api.getServerChannelById(IDs.MitgliederChannel);
        OnlineChannel = api.getServerChannelById(IDs.OnlineChannel);
        InVoiceChannel = api.getServerChannelById(IDs.InVoiceChannel);
        VerificationChannel = api.getServerChannelById(IDs.VerificationChannel);
        RoleChannel = api.getServerChannelById(IDs.RoleChannel);



        VerificationChannel.flatMap(Channel::asTextChannel).ifPresent(textChannel -> {
            VerificationMessage = textChannel.getMessageById(IDs.VerificationMessage).whenComplete((message, throwable) -> {
                message.addReactionAddListener(new VerefiedFunctionAdd());
                message.addReactionRemoveListener(new VerifiedFunctionRemove());
            });
        });

        RoleChannel.flatMap(Channel::asTextChannel).ifPresent(textChannel -> {
            textChannel.getMessageById(IDs.RoleMessage);
        });


    }


    public static void main(String[] args){
        logger.info("Bot Starting");

        if(args.length<1){
            System.out.print("Missing Bot Parameter");
            System.exit(0);
        }else{
            Bot b = new Bot(args[1]);
        }
    }

}
