package de.bfbot;

import de.bfbot.functions.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.List;

public class Bot {

    public static final Logger logger = LogManager.getLogger(Bot.class);
    public static DiscordApi api;
    public static Optional<Role> Verified, HotNewsRole, SocialMediaNewsRole, MatchdayNewsRole;
    public static Optional<Server> BfServer;
    public static Optional<ServerChannel> MemberChannel, OnlineChannel, InVoiceChannel, VerificationChannel, RoleChannel;
    public static Optional<ServerTextChannel> BotChannel;
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
        BfServer            = api.getServerById(IDs.BFServer);
        Verified            = api.getRoleById(IDs.VerifiedRole);
        VerificationChannel = api.getServerChannelById(IDs.VerificationChannel);
        RoleChannel         = api.getServerChannelById(IDs.RoleChannel);
        BotChannel          = api.getServerTextChannelById(IDs.BotChannel);

        MemberChannel       = api.getServerChannelById(IDs.MemberChannel);
        OnlineChannel       = api.getServerChannelById(IDs.OnlineChannel);
        InVoiceChannel      = api.getServerChannelById(IDs.InVoiceChannel);

        HotNewsRole     = api.getRoleById(IDs.HotNewsRole);
        SocialMediaNewsRole = api.getRoleById(IDs.SocialMediaNewsRole);
        MatchdayNewsRole    = api.getRoleById(IDs.MatchDayNewsRole);

        BotChannel.ifPresent(serverTextChannel -> {
            serverTextChannel.addMessageCreateListener(event -> {
                if(event.getMessageContent().equalsIgnoreCase("!Disconnect")){
                    Disconnect();
                }
            });
        });

        VerificationChannel.flatMap(Channel::asTextChannel).ifPresent(textChannel -> {
            VerificationMessage = textChannel.getMessageById(IDs.VerificationMessage).whenComplete((message, throwable) -> {
                message.addReactionAddListener(new VerefiedFunctionAdd());
                message.addReactionRemoveListener(new VerifiedFunctionRemove());
            });
        });

        RoleChannel.flatMap(Channel::asTextChannel).ifPresent(textChannel -> {
            RoleMessage = textChannel.getMessageById(IDs.RoleMessage).whenComplete((message, throwable) -> {
                message.addReactionAddListener(new NewsRoleAddFunction());
                message.addReactionRemoveListener(new NewsRoleRemoveFunction());
            });
        });

        new ChannelNameTimer().start();

    }

    public static int GetCurrentUserCount(){
        return BfServer.map(Server::getMemberCount).orElse(-1);
    }

    public static int GetCurrentOnlineCount() {
        int res = 0;
        Collection<User> users = BfServer.map(Server::getEveryoneRole).map(Role::getUsers).orElse(null);
        for (User user: users) {
            if (!user.getStatus().equals(UserStatus.OFFLINE)) {
                res++;
            }
        }
        return res;
    }

    public static int GetCurrentInVoiceCount() {
        int res = 0;
        List<ServerVoiceChannel> vcs = BfServer.map(Server::getVoiceChannels).orElse(null);
        if(vcs!=null){
            for (ServerVoiceChannel vc:vcs) {
                res += vc.getConnectedUsers().size();
            }
        }else{
            res = -1;
        }
        return res;
    }

    private void Disconnect(){
        api.disconnect();
        System.exit(0);
    }

    public static void main(String[] args){
        logger.info("Bot Starting");

        if(args.length<1){
            System.out.print("Missing Bot Parameter");
            System.exit(0);
        }else{
            Bot b = new Bot(args[0]);
        }
    }

}
