package me.alexutzzu.woobot.utils.pojos.guild;

import com.jagrosh.jdautilities.command.GuildSettingsProvider;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class GuildSettings implements GuildSettingsProvider {
    public GuildSettings(){

    }
    public GuildSettings(String guildID){
        this.guildID = guildID;
    }

    @BsonProperty("_id")
    private ObjectId id = new ObjectId();

    @BsonProperty("guildID")
    private String guildID;


    @BsonProperty("welcomeSettings")
    private WelcomeSettings welcomeSettings = new WelcomeSettings();

    @BsonProperty("generalSettings")
    private GeneralSettings generalSettings = new GeneralSettings();

    @BsonProperty("ticketSettings")
    private TicketSettings ticketSettings = new TicketSettings();

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getGuildID() {
        return guildID;
    }

    public void setGuildID(String guildID) {
        this.guildID = guildID;
    }

    public GeneralSettings getGeneralSettings() {
        return generalSettings;
    }

    public void setGeneralSettings(GeneralSettings generalSettings) {
        this.generalSettings = generalSettings;
    }
    @BsonIgnore
    private final Collection<String> prefixes =Collections.singleton(generalSettings.getCommandPrefix());

    @Override
    @BsonIgnore
    public Collection<String> getPrefixes(){
        return Collections.singleton(generalSettings.getCommandPrefix());
    }

    public WelcomeSettings getWelcomeSettings() {
        return welcomeSettings;
    }

    public void setWelcomeSettings(WelcomeSettings welcomeSettings) {
        this.welcomeSettings = welcomeSettings;
    }



    public TicketSettings getTicketSettings() {
        return ticketSettings;
    }

    public void setTicketSettings(TicketSettings ticketSettings) {
        this.ticketSettings = ticketSettings;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuildSettings that = (GuildSettings) o;
        return getId().equals(that.getId()) && getGuildID().equals(that.getGuildID()) && Objects.equals(getWelcomeSettings(), that.getWelcomeSettings()) && Objects.equals(getGeneralSettings(), that.getGeneralSettings()) && Objects.equals(getTicketSettings(), that.getTicketSettings());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getGuildID(), getWelcomeSettings(), getGeneralSettings(), getTicketSettings());
    }
}
