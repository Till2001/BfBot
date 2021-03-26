package de.bfbot.functions;

import de.bfbot.Bot;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;

public class VerefiedFunctionAdd implements ReactionAddListener {

    @Override
    public void onReactionAdd(ReactionAddEvent event) {
        Bot.logger.info("Verification Event received");
        event.getReaction().ifPresent(reaction ->{
           if(reaction.getEmoji().equalsEmoji("✅")){
               event.getUser().ifPresent(user -> {
                   Bot.Verified.ifPresent(user::addRole);
                   Bot.logger.info("User: "+user+" verified");
               });
           }
        });
    }
}
