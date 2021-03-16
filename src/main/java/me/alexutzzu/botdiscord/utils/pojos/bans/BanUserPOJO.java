package me.alexutzzu.botdiscord.utils.pojos.bans;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.List;

public class BanUserPOJO {
    public BanUserPOJO(){

    }

    @BsonProperty("_id")
    private ObjectId id;

    @BsonProperty("guildID")
    private String guildID;

    @BsonProperty("banLogChannelID")
    private String banLogChannelID;

    @BsonProperty("banWave")
    private List<AddUserPOJO> banWave;



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

    public String getBanLogChannelID() {
        return banLogChannelID;
    }

    public void setBanLogChannelID(String banLogChannelID) {
        this.banLogChannelID = banLogChannelID;
    }

    public List<AddUserPOJO> getBanWave() {
        return banWave;
    }

    public void setBanWave(List<AddUserPOJO> banWave) {
        this.banWave = banWave;
    }
}
