package me.alexutzzu.woobot.utils.pojos;

import com.jagrosh.jdautilities.command.GuildSettingsManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import me.alexutzzu.woobot.utils.pojos.guild.GuildSettings;
import net.dv8tion.jda.api.entities.Guild;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

public class GuildData implements GuildSettingsManager<GuildSettings> {
    private final MongoDatabase mongoDatabase;

    public GuildData(MongoDatabase mongoDatabase){
        this.mongoDatabase = mongoDatabase;
    }

    @NotNull
    @Override
    public GuildSettings getSettings(Guild guild) {
        String guildID = guild.getId();
        MongoCollection<GuildSettings> guildInformation = mongoDatabase.getCollection("Guild Information", GuildSettings.class);
        Bson query = Filters.eq("guildID", guildID);
        GuildSettings guildSettings = guildInformation.find(query).first();
        if (guildSettings == null){
            GuildSettings newGuildSettings = new GuildSettings(guildID);
            guildInformation.insertOne(newGuildSettings);
            return newGuildSettings;
        }else{
            return guildSettings;
        }
    }
}

