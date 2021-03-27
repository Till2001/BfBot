package de.bfbot.functions;

import de.bfbot.Bot;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerTextChannelUpdater;
import org.javacord.api.entity.channel.ServerVoiceChannelUpdater;

import java.util.Calendar;

public class ChannelNameTimer extends Thread{


    public void run(){
        try{
            while(true){
                UpdateChannelNames();
                Thread.sleep(60*10*1000);
            }
        }catch (Exception e){
            Bot.logger.error("ChannelNameTimerThread Error Occured");
            Bot.logger.error(e.getMessage());
            Bot.logger.error(e.getStackTrace());
        }

    }

    public void UpdateChannelNames(){

        Bot.MemberChannel.flatMap(Channel::asServerVoiceChannel).ifPresent(serverVoiceChannel -> {
            new ServerVoiceChannelUpdater(serverVoiceChannel).setName("╔ Mitglieder: "+Bot.GetCurrentUserCount()).update().whenComplete((unused, throwable) -> {
                Bot.logger.info("UserCountChannel updated");
            });
        });

        Bot.OnlineChannel.flatMap(Channel::asServerVoiceChannel).ifPresent(serverVoiceChannel -> {
            new ServerVoiceChannelUpdater(serverVoiceChannel).setName("╠ Online: "+Bot.GetCurrentOnlineCount()).update().whenComplete((unused, throwable) -> {
                Bot.logger.info("OnlineCountChannel updated");
            });
        });

        Bot.InVoiceChannel.flatMap(Channel::asServerVoiceChannel).ifPresent(serverVoiceChannel -> {
            new ServerVoiceChannelUpdater(serverVoiceChannel).setName("╚ In Voice: "+Bot.GetCurrentInVoiceCount()).update().whenComplete((unused, throwable) -> {
                Bot.logger.info("InVoiceCountChannel updated");
            });
        });
    }
}
