package me.alexutzzu.woobot.events;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import me.alexutzzu.woobot.utils.constants.Links;
import me.alexutzzu.woobot.utils.pojos.guild.GuildSettings;
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
        EmbedBuilder eb = new EmbedBuilder();
        GuildSettings guildSettings = createSettings(guild.getId());

        eb.setColor(Color.RED);
        eb.setTitle("Thanks for inviting me!");
        eb.setDescription("I am " + event.getJDA().getSelfUser().getName() + "! I am a moderation and Support bot with " +
                "some useful commands.");
        eb.addField("Need help?", "Use `" + guildSettings.getGeneralSettings().getCommandPrefix() + "help`", true);
        eb.addField("Join our Support Server","Need even more help? We're happy to help! You can join [here](" + Links.SUPPORT_SERVER.getLink()+")", true);
        eb.addField("Visit our Official Website","Gain full control over your bot from the web! No need for commands." + Links.CONTROL_PANEL.getLink(), true);
        if (channel!=null){
            channel.sendMessage(eb.build()).queue();
        }
    }
    private GuildSettings createSettings(String guildID){
        MongoCollection<GuildSettings> guildInformation = mongoDatabase.getCollection("Guild Information", GuildSettings.class);
        Bson query = Filters.eq("guildID", guildID);
        GuildSettings guildSettings = guildInformation.find(query).first();
        if (guildSettings==null){
            GuildSettings settings = new GuildSettings(guildID);
            guildInformation.insertOne(settings);
            return settings;
        }else{
            guildInformation.deleteMany(query);
            GuildSettings settings = new GuildSettings(guildID);
            guildInformation.insertOne(settings);
            return settings;
        }
    }
}
