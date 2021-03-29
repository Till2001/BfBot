package de.bfbot.functions;

import de.bfbot.Bot;
import de.bfbot.IDs;
import org.javacord.api.event.user.UserChangeNameEvent;
import org.javacord.api.listener.user.UserChangeNameListener;

public class BFClanTagUserRenameFunction implements UserChangeNameListener {

    @Override
    public void onUserChangeName(UserChangeNameEvent event) {
        Bot.BfServer.ifPresent(server -> {
            event.getUser().updateNickname(server,"BF| "+event.getUser().getDisplayName(server)).whenComplete((unused, throwable) -> {
                Bot.logger.info("User: "+event.getUser()+" nickname updated");
                Bot.logger.info(throwable.getMessage());
            });
        });
    }
}
