package me.alexutzzu.woobot.commands.chat;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.alexutzzu.woobot.utils.categories.Chat;
import me.alexutzzu.woobot.utils.addons.AdditionalMethods;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PurgeCommand extends Command implements AdditionalMethods {

    public PurgeCommand(){
        this.category = new Chat();
        this.name = "purge";
        this.userPermissions = new Permission[]{Permission.MESSAGE_MANAGE};
        this.botPermissions = new Permission[]{Permission.MESSAGE_MANAGE};
        this.arguments = "<number>";
        this.guildOnly = true;
        this.usesTopicTags= false;
        this.aliases = new String[]{"delete", "clear"};
        this.cooldown = 5;
        this.help = "Deletes the specified number of messages from the text channel (between 2-100 messages). Some may not be " +
                "deleted if they are older than 2 weeks";
        this.helpBiConsumer = (commandEvent, command) -> {
            if (!checkBlacklistedChannel(commandEvent, this)){
                return;
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(Color.RED);
            eb.setTitle(command.getName() + "Command Help");
            eb.setDescription("This is the help panel for the purge command.");
            eb.addField("`" + command.getName() + " help`", "Prints this help panel", false);
            eb.addField("`" + command.getName() +" " + command.getArguments() + "`", command.getHelp(), false);
            eb.addField("Permission", "Manage Messages", true);
            eb.addField("Cooldown", command.getCooldown() + " seconds", true);
            eb.addField("Aliases", "`delete, clear`", true);
            commandEvent.reply(eb.build());
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getMessage().delete().queue();
        if (event.getArgs().isEmpty()){
            checkUnknownSyntax(event, this);
            return;
        }
        String[] args = event.getArgs().split(" ");
        if (args.length > 1){
            checkUnknownSyntax(event, this);
            return;
        }
        int integer;
        try{
            integer = Integer.parseInt(args[0]);
        }catch (Exception e){
            checkUnknownSyntax(event, this);
            return;
        }
        if (integer < 2 || integer > 100){
            checkUnknownSyntax(event, this);
            return;
        }
        event.async(() -> {
            OffsetDateTime twoWeeksAgo = OffsetDateTime.now().minus(2, ChronoUnit.WEEKS);
            List<Message> messageList = event.getChannel().getHistory().retrievePast(integer).complete();
            messageList.removeIf(m -> m.getTimeCreated().isBefore(twoWeeksAgo));
            if (messageList.isEmpty()){
                event.replyError("There were no messages to delete!",m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                event.getClient().applyCooldown(getCooldownKey(event), 5);
            }
            try{
                event.getTextChannel().deleteMessages(messageList).complete();
                event.replySuccess("Successfully deleted " + integer + " messages!", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            }catch (Exception e){
                event.replyError("Failed to delete messages!",m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                event.getClient().applyCooldown(getCooldownKey(event), 5);
                return;
            }

            event.getClient().applyCooldown(getCooldownKey(event), 5);
        });
    }
}
