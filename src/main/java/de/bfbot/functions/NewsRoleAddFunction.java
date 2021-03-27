package de.bfbot.functions;

import de.bfbot.Bot;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;

import java.util.Optional;

public class NewsRoleAddFunction implements ReactionAddListener {

    @Override
    public void onReactionAdd(ReactionAddEvent event) {
        Optional<Role> ToBeChangedRole = Optional.empty();
        CustomEmoji e = null;
        if(event.getEmoji().asCustomEmoji().isPresent()){
            e = event.getEmoji().asCustomEmoji().get();
        }else{
            return;
        }

        if(e.getReactionTag().equals("1684_Pepe_Spasm2:810141707911954472")){
            ToBeChangedRole = Bot.HotNewsRole;
        }else if(e.getReactionTag().equals("2626_PepeYAYRunning:810143023844294688")){
            ToBeChangedRole = Bot.SocialMediaNewsRole;
        }else if(e.getReactionTag().equals("2709_pepehype:810141710125629468")){
            ToBeChangedRole = Bot.MatchdayNewsRole;
        }

        if(ToBeChangedRole.isPresent()){
            Optional<Role> finalToBeChangedRole = ToBeChangedRole;
            event.getUser().ifPresent(user -> {
                finalToBeChangedRole.ifPresent(role -> {
                    role.addUser(user);
                    Bot.logger.info("User: "+user+" added to Role: "+role);
                });
            });
        }
    }
}
