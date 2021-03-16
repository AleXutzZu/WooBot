package me.alexutzzu.woobot.utils.pojos.levels;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class LevelPOJO {
    public LevelPOJO(){

    }

    @BsonProperty("_id")
    private ObjectId id;

    @BsonProperty("guildID")
    private String guildID;

    @BsonProperty("totalUsers")
    private long totalUsers;

    @BsonProperty("customLevels")
    private List<RoleRewardPOJO> customLevels = new ArrayList<>();

    @BsonProperty("users")
    private List<UserPOJO> users = new ArrayList<>();

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

    public List<RoleRewardPOJO> getCustomLevels() {
        return customLevels;
    }

    public void setCustomLevels(List<RoleRewardPOJO> customLevels) {
        this.customLevels = customLevels;
    }

    public List<UserPOJO> getUsers() {
        return users;
    }

    public void setUsers(List<UserPOJO> users) {
        this.users = users;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }
}
