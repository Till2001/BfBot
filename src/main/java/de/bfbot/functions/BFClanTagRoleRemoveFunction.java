package de.bfbot.functions;

import de.bfbot.Bot;
import de.bfbot.IDs;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;

public class BFClanTagRoleRemoveFunction implements UserRoleRemoveListener {

    @Override
    public void onUserRoleRemove(UserRoleRemoveEvent event) {
        Bot.BfServer.ifPresent(server -> {
            event.getUser().updateNickname(server,event.getUser().getDisplayName(server).substring(4)).whenComplete((unused, throwable) -> {
                Bot.logger.info("User: "+event.getUser()+" nickname reverted");
                Bot.logger.info(throwable.getMessage());
                event.getUser().removeUserAttachableListener(event.getUser().getUserChangeNameListeners().get(0));
            });
        });
    }
}
