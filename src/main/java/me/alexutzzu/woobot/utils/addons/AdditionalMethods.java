package me.alexutzzu.woobot.utils.addons;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.alexutzzu.woobot.utils.pojos.guild.GuildSettings;
import net.dv8tion.jda.api.Permission;

import java.util.concurrent.TimeUnit;

public interface AdditionalMethods {
    default boolean checkBlacklistedChannel(CommandEvent event, Command command){
        GuildSettings guildSettings = event.getClient().getSettingsFor(event.getGuild());
        if (guildSettings.getGeneralSettings().getBlacklistedChannelsID().contains(event.getChannel().getId())){
            if (event.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)){
                event.getMessage().delete().queue();
            }
            event.replyWarning("You may not use this command here!", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            event.getClient().applyCooldown(command.getCooldownKey(event), 0);
            return false;
        }
        return true;
    }
    default void checkUnknownSyntax(CommandEvent event, Command command){
     event.replyWarning("Unknown syntax! Please use `" + command.getName() + " help` for help!",
             m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
     event.getClient().applyCooldown(command.getCooldownKey(event), 0);
    }
}
