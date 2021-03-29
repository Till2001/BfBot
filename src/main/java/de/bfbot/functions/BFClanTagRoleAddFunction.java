package de.bfbot.functions;

import de.bfbot.Bot;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.listener.server.role.UserRoleAddListener;

public class BFClanTagRoleAddFunction implements UserRoleAddListener {

    @Override
    public void onUserRoleAdd(UserRoleAddEvent event) {
        Bot.BfServer.ifPresent(server -> {
            event.getUser().updateNickname(server, "BF| "+event.getUser().getDisplayName(server)).whenComplete((unused, throwable) -> {
                event.getUser().addUserChangeNameListener(new BFClanTagUserRenameFunction());
                Bot.logger.info("User: "+event.getUser()+" nickname updated");
                Bot.logger.info(throwable.getMessage());
            });
        });
    }
}
