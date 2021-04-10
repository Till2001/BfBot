package de.bfbot;

import de.bfbot.functions.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class Bot {

    public static final Logger logger = LogManager.getLogger(Bot.class);
    public static DiscordApi api;
    public static Optional<Role> Verified, HotNewsRole, SocialMediaNewsRole, MatchdayNewsRole, Month1Role, Month2Role, Month3Role;
    public static Optional<Server> BfServer;
    public static Optional<ServerChannel> MemberChannel, OnlineChannel, InVoiceChannel, VerificationChannel, RoleChannel;
    public static ArrayList<Optional<Role>> MembershipRoles;
    public static Optional<ServerTextChannel> BotChannel;
    public static CompletableFuture<Message> VerificationMessage, RoleMessage;
    private static Timer Timer;


    public Bot(String token) {
        api = new DiscordApiBuilder()
                .setAllIntents()
                .setToken(token)
                .login().join();
        Init();
        logger.info(api.createBotInvite(Permissions.fromBitmask(8)));
        logger.info("Bot Started");
    }

    private void Init() {
        BfServer = api.getServerById(IDs.BFServer);
        Verified = api.getRoleById(IDs.VerifiedRole);
        VerificationChannel = api.getServerChannelById(IDs.VerificationChannel);
        RoleChannel = api.getServerChannelById(IDs.RoleChannel);
        BotChannel = api.getServerTextChannelById(IDs.BotChannel);

        MemberChannel = api.getServerChannelById(IDs.MemberChannel);
        OnlineChannel = api.getServerChannelById(IDs.OnlineChannel);
        InVoiceChannel = api.getServerChannelById(IDs.InVoiceChannel);

        HotNewsRole = api.getRoleById(IDs.HotNewsRole);
        SocialMediaNewsRole = api.getRoleById(IDs.SocialMediaNewsRole);
        MatchdayNewsRole = api.getRoleById(IDs.MatchDayNewsRole);

        Month1Role = api.getRoleById(IDs.Month1RoleID);
        Month2Role = api.getRoleById(IDs.Month2RoleID);
        Month3Role = api.getRoleById(IDs.Month3RoleID);

        MembershipRoles = new ArrayList<Optional<Role>>();
        MembershipRoles.add(Month1Role);
        MembershipRoles.add(Month2Role);
        MembershipRoles.add(Month3Role);

        Timer = new Timer();

        BotChannel.ifPresent(serverTextChannel -> {
            serverTextChannel.addMessageCreateListener(event -> {
                if (event.getMessageContent().equalsIgnoreCase("!Disconnect")) {
                    Disconnect();
                }
                if(event.getMessageContent().equalsIgnoreCase("!UpdateMonthRanks")){
                    BfServer.ifPresent(server -> {
                        setTimeBasedRank(server.getEveryoneRole().getUsers());
                    });
                    logger.info("Forced Monthly Rank Update ran");
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

        SetupRepeatedCode();

        BfServer.ifPresent(server -> server.addServerMemberLeaveListener(event -> {
            Bot.logger.info("User: " + event.getUser() + " left");
            VerificationMessage.whenComplete((message, throwable) -> {
                message.getReactions().get(0).removeUser(event.getUser()).whenComplete((unused, throwable1) -> {
                    Bot.logger.info("User: " + event.getUser() + " verification Reaction removed");
                });
            });
        }));
    }

    private static int GetCurrentUserCount() {
        return BfServer.map(Server::getMemberCount).orElse(-1);
    }

    private static int GetCurrentOnlineCount() {
        int res = 0;
        Collection<User> users = BfServer.map(Server::getEveryoneRole).map(Role::getUsers).orElse(null);
        for (User user : users) {
            if (!user.getStatus().equals(UserStatus.OFFLINE)) {
                res++;
            }
        }
        return res;
    }

    private static int GetCurrentInVoiceCount() {
        int res = 0;
        List<ServerVoiceChannel> vcs = BfServer.map(Server::getVoiceChannels).orElse(null);
        if (vcs != null) {
            for (ServerVoiceChannel vc : vcs) {
                res += vc.getConnectedUsers().size();
            }
        } else {
            res = -1;
        }
        return res;
    }

    private void Disconnect() {
        api.disconnect();
        System.exit(0);
    }

    public static void main(String[] args) {
        logger.info("Bot Starting");

        if (args.length < 1) {
            System.out.print("Missing Bot Parameter");
            System.exit(0);
        } else {
            Bot b = new Bot(args[0]);
        }
    }

    public static void setTimeBasedRank(Collection<User> UserCollection) {
        for (User user:UserCollection) {
            if (user.isYourself()) {
                continue;
            }
            AtomicReference<Optional<Instant>> a;
            a = new AtomicReference<>();
            BfServer.ifPresent(server -> {
                a.set(user.getJoinedAtTimestamp(server));
            });
            if (a.get().isPresent()) {
                Duration Diff = Duration.between(a.get().get(), Instant.now());
                logger.info("User: " + user + " Time Since Join: " + Diff);
                double Months = (Diff.get(ChronoUnit.SECONDS) / 2.628E6);
                logger.info("User: " + user + "Time Since Join (monate): " + Months);

                for (Optional<Role> r:MembershipRoles) {
                    r.ifPresent(role -> {
                        if(role.getUsers().contains(user)){
                            role.removeUser(user).whenComplete((unused, throwable) -> {
                                logger.info("User: "+user+" removed from "+role);
                            });
                        }
                    });
                }

                if(Months>=3){
                    Month3Role.ifPresent(role -> {
                        role.addUser(user).whenComplete((unused, throwable) -> {
                            logger.info("User: "+user+" added to "+role);
                        });
                    });
                }else if (Months>=2){
                    Month2Role.ifPresent(role -> {
                        role.addUser(user).whenComplete((unused, throwable) -> {
                            logger.info("User: "+user+" added to "+role);
                        });
                    });
                }else if (Months>=1){
                    Month1Role.ifPresent(role -> {
                        role.addUser(user).whenComplete((unused, throwable) -> {
                            logger.info("User: "+user+" added to "+role);
                        });
                    });
                }
            }
        }


    }

    public void SetupRepeatedCode(){
        SetupDailyCode();
        Setup10MinCode();
        logger.info("Setup Repeated code");
    }

    private void Setup10MinCode(){
        TimerTask code = new TimerTask() {
            @Override
            public void run() {
                MemberChannel.flatMap(Channel::asServerVoiceChannel).ifPresent(serverVoiceChannel -> {
                    new ServerVoiceChannelUpdater(serverVoiceChannel).setName("╔ Mitglieder: "+GetCurrentUserCount()).update().whenComplete((unused, throwable) -> {
                        logger.info("UserCountChannel updated");
                    });
                });

                OnlineChannel.flatMap(Channel::asServerVoiceChannel).ifPresent(serverVoiceChannel -> {
                    new ServerVoiceChannelUpdater(serverVoiceChannel).setName("╠ Online: "+GetCurrentOnlineCount()).update().whenComplete((unused, throwable) -> {
                        logger.info("OnlineCountChannel updated");
                    });
                });

                InVoiceChannel.flatMap(Channel::asServerVoiceChannel).ifPresent(serverVoiceChannel -> {
                    new ServerVoiceChannelUpdater(serverVoiceChannel).setName("╚ In Voice: "+GetCurrentInVoiceCount()).update().whenComplete((unused, throwable) -> {
                        logger.info("InVoiceCountChannel updated");
                    });
                });
                logger.info("Channelname Code ran");
            }
        };
        Calendar c = Calendar.getInstance();
        int m = 10 - (c.get(Calendar.MINUTE) % 10);
        c.add(Calendar.MINUTE,m);
        long Period = 1000L * 60L * 10L; //10M in Milliseconds;
        Date d = c.getTime();
        Timer.scheduleAtFixedRate(code,d,Period);
        logger.info("Channelname Code will run in: "+ Duration.between(d.toInstant(), Instant.now()));
    }

    private void SetupDailyCode(){
        TimerTask code = new TimerTask() {
            @Override
            public void run() {
                BfServer.ifPresent(server -> {
                    setTimeBasedRank(server.getEveryoneRole().getUsers());
                });
                logger.info("Daily Code ran");
            }
        };

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR,1);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date tmrw = c.getTime();
        long Period = 24L * 60L * 60L * 1000L; //24H in Milliseconds
        Timer.scheduleAtFixedRate(code,tmrw,Period);
        logger.info("Daily Code will run in: "+ Duration.between(tmrw.toInstant(), Instant.now()));
    }
}
