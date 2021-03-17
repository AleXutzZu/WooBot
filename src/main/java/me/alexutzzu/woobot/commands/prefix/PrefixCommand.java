package me.alexutzzu.woobot.commands.prefix;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import me.alexutzzu.woobot.utils.addons.AdditionalMethods;
import me.alexutzzu.woobot.utils.categories.Admin;
import me.alexutzzu.woobot.utils.pojos.guild.GuildSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.bson.conversions.Bson;

import java.awt.*;
import java.util.concurrent.TimeUnit;


public class PrefixCommand extends Command implements AdditionalMethods {

    public PrefixCommand(MongoDatabase mongoDatabase){
        this.category = new Admin();
        this.name = "prefix";
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.guildOnly = true;
        this.cooldown = 5;
        this.help = "Use this command to change the command prefix for the bot";
        this.usesTopicTags = false;
        this.helpBiConsumer = ((commandEvent, command) -> {
            if (!checkBlacklistedChannel(commandEvent, this)){
                return;
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("prefix Command Help");
            eb.setColor(Color.RED);
            eb.setDescription(command.getHelp());
            eb.addField("`prefix set <new prefix>`", "Use this command to se the new prefix", false);
            eb.addField("`prefix help`", "Prints this help panel", false);
            eb.addField("Permission", "Administrator", true);
            eb.addField("Example", "`prefix set ~`", true);
            eb.addField("Cooldown", command.getCooldown() + " seconds", true);
            commandEvent.reply(eb.build());
        });
        this.children = new Command[]{new SetPrefix(mongoDatabase)};
    }
    @Override
    protected void execute(CommandEvent event) {
        if(!checkBlacklistedChannel(event, this)){
            return;
        }
        checkUnknownSyntax(event, this);
    }
}
class SetPrefix extends Command implements AdditionalMethods {
    private final MongoDatabase mongoDatabase;
    public SetPrefix(MongoDatabase mongoDatabase){
        this.name = "set";
        this.arguments = "<new prefix>";
        this.mongoDatabase = mongoDatabase;
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
    }
    @Override
    protected void execute(CommandEvent event) {
        if(!checkBlacklistedChannel(event, this)){
            return;
        }
        if (event.getArgs().isEmpty()){
            checkUnknownSyntax(event, this);
            return;
        }
        String[] args = event.getArgs().split(" ");
        if (args.length > 1){
            checkUnknownSyntax(event, this);
            return;
        }
        String newPrefix = args[0];
        MongoCollection<GuildSettings> guildInformation = mongoDatabase.getCollection("Guild Information", GuildSettings.class);
        Bson query = Filters.eq("guildID", event.getGuild().getId());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
        Bson updateOp = Updates.set("generalSettings.commandPrefix", newPrefix);
        GuildSettings newGuildSettings =guildInformation.findOneAndUpdate(query,updateOp,options);
        event.replySuccess("Prefix was updated to " + newGuildSettings.getGeneralSettings().getCommandPrefix(), m -> m.delete().queueAfter(5, TimeUnit.SECONDS));

    }
}
