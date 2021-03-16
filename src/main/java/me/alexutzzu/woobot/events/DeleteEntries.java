package me.alexutzzu.woobot.events;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import me.alexutzzu.woobot.utils.pojos.guild.GuildSettings;
import me.alexutzzu.woobot.utils.pojos.tickets.GuildTicket;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.conversions.Bson;

public class DeleteEntries extends ListenerAdapter {
    private final MongoDatabase mongoDatabase;

    public DeleteEntries(MongoDatabase mongoDatabase){
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        String deletedChannelID = event.getChannel().getId();
        MongoCollection<GuildTicket> guildTickets = mongoDatabase.getCollection("Guild Tickets", GuildTicket.class);
        Bson query = Filters.eq("channelID", deletedChannelID);
        guildTickets.deleteOne(query);
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event){
        deleteGuildInfo(event.getGuild().getId());
    }
    @Override
    public void onUnavailableGuildLeave(UnavailableGuildLeaveEvent event){
        deleteGuildInfo(event.getGuildId());
    }


    /* TODO>> delete entries from every database */
    private void deleteGuildInfo(String guildID){
        Bson query = Filters.eq("guildID", guildID);
        MongoCollection<GuildSettings> guildInformation = mongoDatabase.getCollection("Guild Information", GuildSettings.class);
        guildInformation.deleteMany(query);
    }
}
