package me.alexutzzu.woobot.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.alexutzzu.woobot.utils.addons.AdditionalMethods;
import me.alexutzzu.woobot.utils.categories.Misc;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.time.temporal.ChronoUnit;

public class PingCommand extends Command implements AdditionalMethods {
    public PingCommand(){
        this.category =new Misc();
        this.name = "ping";
        this.guildOnly= true;
        this.help = "Use this command to get your ping.";
        this.usesTopicTags = false;
        this.cooldown = 10;
        this.helpBiConsumer = ((commandEvent, command) -> {
            if (!checkBlacklistedChannel(commandEvent, this)){
                return;
            }
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Color.RED);
            embedBuilder.setTitle("ping Command Help");
            embedBuilder.setDescription(command.getHelp());
            embedBuilder.addField("`ping help`","Prints this help message", false);
            embedBuilder.addField("`ping`", "This will return your ping", false);
            embedBuilder.addField("Permission", "everyone", true);
            embedBuilder.addField("Cooldown", command.getCooldown() + " seconds", true);
            commandEvent.reply(embedBuilder.build());
            });
    }
    @Override
    protected void execute(CommandEvent event) {

         if(!checkBlacklistedChannel(event, this)){
             return;
         }

        event.reply("Ping...", m -> {
            long ping = event.getMessage().getTimeCreated().until(m.getTimeCreated(), ChronoUnit.MILLIS);
            m.editMessage("Ping: " + ping + "ms | Websocket: " + event.getJDA().getGatewayPing() + "ms").queue();
        });


    }
}
