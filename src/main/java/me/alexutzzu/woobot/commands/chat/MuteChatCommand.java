package me.alexutzzu.woobot.commands.chat;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.alexutzzu.woobot.utils.addons.AdditionalMethods;
import me.alexutzzu.woobot.utils.categories.Chat;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class MuteChatCommand extends Command implements AdditionalMethods {
    public MuteChatCommand(){
        this.category = new Chat();
        this.name = "mutechat";
        this.guildOnly = true;
        this.cooldown = 10;
        this.help = "Mutes the chat for @everyone. Use it again to unmute the chat.";
        this.helpBiConsumer = ((commandEvent, command) -> {
            if (!checkBlacklistedChannel(commandEvent, this)){
                return;
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(Color.RED);
            eb.setTitle(command.getName() + " Command Help");
            eb.setDescription("This is the the help panel for the mutechat command");
            eb.addField("`" + command.getName() + " help`", "Prints this help message", false);
            eb.addField("`" + command.getName() + "`", command.getHelp(), false);
            eb.addField("Permission", "Manage Messages", true);
            eb.addField("Cooldown", command.getCooldown() + " seconds", true);
            commandEvent.reply(eb.build());
        });
        this.userPermissions = new Permission[]{Permission.MESSAGE_MANAGE};
        this.botPermissions = new Permission[]{Permission.MANAGE_CHANNEL};
        this.usesTopicTags = false;
    }
    @Override
    protected void execute(CommandEvent event) {
        Role publicRole = event.getGuild().getPublicRole();
        TextChannel textChannel = event.getTextChannel();
        if (publicRole.hasPermission(textChannel, Permission.MESSAGE_WRITE)){
            textChannel.getManager().putPermissionOverride(publicRole, null, Collections.singleton(Permission.MESSAGE_WRITE)).queue();
            event.replySuccess("Channel was muted.", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
        }else{
            textChannel.getManager().putPermissionOverride(publicRole, Collections.singleton(Permission.MESSAGE_WRITE), null).queue();
            event.replySuccess("Channel was unmuted.", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
        }

    }
}
