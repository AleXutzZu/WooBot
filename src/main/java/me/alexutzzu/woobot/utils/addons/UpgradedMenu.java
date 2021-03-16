package me.alexutzzu.woobot.utils.addons;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Menu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class UpgradedMenu extends Menu {
    private final Color color;
    private final EmbedBuilder description;
    private final List<String> choices;
    private final Consumer<MessageReaction.ReactionEmote> action;
    private final Consumer<Message> finalAction;

    UpgradedMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
               Color color, EmbedBuilder description, List<String> choices, Consumer<MessageReaction.ReactionEmote> action, Consumer<Message> finalAction)
    {
        super(waiter, users, roles, timeout, unit);
        this.color = color;
        this.description = description;
        this.choices = choices;
        this.action = action;
        this.finalAction = finalAction;
    }

    @Override
    public void display(MessageChannel channel)
    {
        initialize(channel.sendMessage(getMessage()));
    }


    @Override
    public void display(Message message)
    {
        initialize(message.editMessage(getMessage()));
    }

    private Message getMessage()
    {
        MessageBuilder mBuilder = new MessageBuilder();
        if(description!=null)
            mBuilder.setEmbed(description.setColor(color).build());
        return mBuilder.build();
    }


    private void initialize(RestAction<Message> ra){
        ra.queue(m -> {
            for(int i=0; i<choices.size(); i++)
            {
                // Get the emote to display.
                Emote emote;
                try {
                    emote = m.getJDA().getEmoteById(choices.get(i));
                } catch(Exception e) {
                    emote = null;
                }
                // If the emote is null that means that it might be an emoji.
                // If it's neither, that's on the developer and we'll let it
                // throw an error when we queue a rest action.
                RestAction<Void> r = emote==null ? m.addReaction(choices.get(i)) : m.addReaction(emote);
                if(i+1<choices.size())
                    r.queue(); // If there is still more reactions to add we delay using the EventWaiter
                else
                {
                    // This is the last reaction added.
                    r.queue(v -> {
                        waiter.waitForEvent(MessageReactionAddEvent.class, event -> {
                            // If the message is not the same as the ButtonMenu
                            // currently being displayed.
                            if(!event.getMessageId().equals(m.getId()))
                                return false;

                            // If the reaction is an Emote we get the Snowflake,
                            // otherwise we get the unicode value.
                            String re = event.getReaction().getReactionEmote().isEmote()
                                    ? event.getReaction().getReactionEmote().getId()
                                    : event.getReaction().getReactionEmote().getName();

                            // If the value we got is not registered as a button to
                            // the ButtonMenu being displayed we return false.
                            if(!choices.contains(re))
                                return false;

                            // Last check is that the person who added the reaction
                            // is a valid user.
                            return isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
                        }, (MessageReactionAddEvent event) -> {
                            // What happens next is after a valid event
                            // is fired and processed above.

                            // Preform the specified action with the ReactionEmote
                            action.accept(event.getReaction().getReactionEmote());
                            finalAction.accept(m);
                        }, timeout, unit, () -> finalAction.accept(m));
                    });
                }
            }
        });
    }

    public static class Builder extends Menu.Builder<Builder, UpgradedMenu>{

        private Color color;
        private EmbedBuilder description;
        private final List<String> choices = new LinkedList<>();
        private Consumer<MessageReaction.ReactionEmote> action;
        private Consumer<Message> finalAction = (m) -> {};


        @Override
        public UpgradedMenu build()
        {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!choices.isEmpty(), "Must have at least one choice");
            Checks.check(action != null, "Must provide an action consumer");
            Checks.check( description != null, "Either text or description must be set");

            return new UpgradedMenu(waiter, users, roles, timeout, unit, color, description, choices, action, finalAction);
        }


        public UpgradedMenu.Builder setColor(Color color)
        {
            this.color = color;
            return this;
        }



        public UpgradedMenu.Builder setDescription(EmbedBuilder description)
        {
            this.description = description;
            return this;
        }


        public UpgradedMenu.Builder setAction(Consumer<MessageReaction.ReactionEmote> action)
        {
            this.action = action;
            return this;
        }


        public UpgradedMenu.Builder setFinalAction(Consumer<Message> finalAction)
        {
            this.finalAction = finalAction;
            return this;
        }


        public UpgradedMenu.Builder addChoice(String emoji)
        {
            this.choices.add(emoji);
            return this;
        }


        public UpgradedMenu.Builder addChoice(Emote emote)
        {
            return addChoice(emote.getId());
        }


        public UpgradedMenu.Builder addChoices(String... emojis)
        {
            for(String emoji : emojis)
                addChoice(emoji);
            return this;
        }


        public UpgradedMenu.Builder addChoices(Emote... emotes)
        {
            for(Emote emote : emotes)
                addChoice(emote);
            return this;
        }

    }
}
