package me.alexutzzu.woobot.commands.blacklist;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import me.alexutzzu.woobot.utils.categories.Admin;
import me.alexutzzu.woobot.utils.addons.AdditionalMethods;
import me.alexutzzu.woobot.utils.pojos.guild.GuildSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bson.conversions.Bson;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Updates.pull;
import static com.mongodb.client.model.Updates.push;

public class BlacklistCommand extends Command implements AdditionalMethods {
    private final MongoDatabase mongoDatabase;
    public BlacklistCommand(MongoDatabase mongoDatabase){
        this.category = new Admin();
        this.mongoDatabase = mongoDatabase;
        this.name = "blacklist";
        this.aliases = new String[]{"bl"};
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.cooldown = 10;
        this.guildOnly = true;
        this.arguments = "[#channel]";
        this.usesTopicTags = false;
        this.help = "Use this command to allow/prevent commands in a channel. Some commands (such as Chat " +
                "Moderation Commands) will work anyway. Not specifying a channel will print all the currently blacklisted channels.";
        this.helpBiConsumer= (commandEvent, command) -> {
            if (!checkBlacklistedChannel(commandEvent, this)){
                return;
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(Color.RED);
            eb.setTitle("Blacklist Command Help");
            eb.setDescription("This is the help panel for the blacklist command.");
            eb.addField("`blacklist " + command.getArguments() + "`", command.getHelp(), false);
            eb.addField("`blacklist help`", "Prints this help Panel", false);
            eb.addField("Permission", "Administrator", true);
            eb.addField("Cooldown", command.getCooldown() + " seconds", true);
            commandEvent.reply(eb.build());
        };
    }
    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()){
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Blacklist Panel");
            GuildSettings guildSettings = event.getClient().getSettingsFor(event.getGuild());
            eb.setColor(Color.RED);
            eb.setDescription("This will print all of the channels that are currently blacklisted.");
            String channelList = "";
            if (guildSettings.getGeneralSettings().getBlacklistedChannelsID().isEmpty()){
                channelList="none";
            }
            List<String> blacklistedChannels = guildSettings.getGeneralSettings().getBlacklistedChannelsID();
            for (int i=0; i<blacklistedChannels.size(); i++){
                channelList = channelList.concat("<#" + blacklistedChannels.get(i) + ">");
                if (i < blacklistedChannels.size()-1){
                    channelList = channelList.concat(", ");
                }
            }
            eb.addField("Blacklisted Channels", channelList, true);
            event.reply(eb.build());
        }else{
            String[] args = event.getArgs().split(" ");
            if (args.length > 1){
                checkUnknownSyntax(event, this);
                return;
            }
            if (event.getMessage().getMentionedChannels().size()!=1){
                checkUnknownSyntax(event, this);
                return;
            }
            TextChannel textChannel = event.getMessage().getMentionedChannels().get(0);
            if (updateBlacklistedChannelsID(event.getGuild().getId(), textChannel.getId())) {
                event.replySuccess("Channel " + textChannel.getAsMention() + " was successfully blacklisted. Most commands will no longer work in that channel.", message -> message.delete().queueAfter(5, TimeUnit.SECONDS));

            } else {
                event.replySuccess("Channel " + textChannel.getAsMention() + " was successfully un-blacklisted. All commands will work.", message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
            }
        }
    }
    private boolean updateBlacklistedChannelsID (String guild, String channelID) throws NullPointerException{
        MongoCollection<GuildSettings> guildInformation = mongoDatabase.getCollection("Guild Information", GuildSettings.class);
        Bson query = Filters.eq("guildID", guild);
        GuildSettings guildInfo = guildInformation.find(query).first();
        if (guildInfo==null){
            throw new NullPointerException();
        }
        if (guildInfo.getGeneralSettings().getBlacklistedChannelsID().contains(channelID)){
            Bson updateOp = pull("generalSettings.blacklistedChannelsID", channelID);
            guildInformation.updateOne(query, updateOp);
            return false;
        }
        else{
            Bson updateOp = push("generalSettings.blacklistedChannelsID", channelID);
            guildInformation.updateOne(query, updateOp);
            return true;
        }
    }
}
