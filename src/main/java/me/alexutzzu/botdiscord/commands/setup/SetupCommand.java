package me.alexutzzu.botdiscord.commands.setup;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.mongodb.client.MongoDatabase;
import me.alexutzzu.botdiscord.utils.categories.Setup;
import me.alexutzzu.botdiscord.utils.addons.AdditionalMethods;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;

public class SetupCommand extends Command implements AdditionalMethods {
    public SetupCommand(MongoDatabase mongoDatabase, EventWaiter eventWaiter) {
        this.category = new Setup();
        this.name = "setup";
        this.children = new Command[]{new Ticket(mongoDatabase, eventWaiter)};
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.botPermissions = new Permission[]{Permission.MANAGE_ROLES, Permission.MANAGE_SERVER};
        this.help = "Use this command to setup some of the more important components of the bot.";
        this.helpBiConsumer = (commandEvent, command) -> {
            if (!checkBlacklistedChannel(commandEvent, this)){
                return;
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(Color.RED);
            eb.setTitle("Setup Command Help");
            eb.setDescription("This is the help panel for the `setup` command.");
            eb.addField("`setup help`", "Prints this help panel", false);
            for (Command value : command.getChildren()) {
                eb.addField("`setup " + value.getName() +"`", value.getHelp(), false);
            }
            eb.addField("Permission", "Administrator", true);
            eb.addField("Cooldown", command.getCooldown() + " seconds", true);
            commandEvent.reply(eb.build());
        };
        this.cooldown = 10;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!checkBlacklistedChannel(event, this)){
            return;
        }
        if (event.getArgs().isEmpty()){
            checkUnknownSyntax(event, this);
        }
    }
}
