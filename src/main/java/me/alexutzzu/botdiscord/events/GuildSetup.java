package me.alexutzzu.botdiscord.events;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import me.alexutzzu.botdiscord.utils.pojos.guild.GuildSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.conversions.Bson;

import java.awt.*;


public class GuildSetup extends ListenerAdapter {
    private final MongoDatabase mongoDatabase;
    public GuildSetup(MongoDatabase mongoDatabase){
        this.mongoDatabase = mongoDatabase;
    }
    @Override
    public void onGuildJoin(GuildJoinEvent event){
        Guild guild = event.getGuild();
        TextChannel channel = guild.getDefaultChannel();
        GuildSettings newGuild = new GuildSettings(guild.getId());
        EmbedBuilder eb = new EmbedBuilder();
        MongoCollection<GuildSettings> guildInformation = mongoDatabase.getCollection("Guild Information", GuildSettings.class);
        FindOneAndReplaceOptions options = new FindOneAndReplaceOptions().upsert(true).returnDocument(ReturnDocument.AFTER);
        Bson query = Filters.eq("guildID", guild.getId());

        GuildSettings guildSettings = guildInformation.findOneAndReplace(query, newGuild, options);

        eb.setColor(Color.RED);
        eb.setTitle("Thanks for inviting me!");
        eb.setDescription("I am " + event.getJDA().getSelfUser().getName() + "! I am a moderation and Support bot with " +
                "some useful commands.");
        eb.addField("Default Prefix", guildSettings.getGeneralSettings().getCommandPrefix(), true);
        eb.addField("Need help?", "Use `" + guildSettings.getGeneralSettings().getCommandPrefix() + "help`", true);
        eb.addField("Support the Developer", "not yet", true);
        channel.sendMessage(eb.build()).queue();
    }
}
