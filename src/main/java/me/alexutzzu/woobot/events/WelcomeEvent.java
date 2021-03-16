package me.alexutzzu.woobot.events;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import me.alexutzzu.woobot.utils.pojos.guild.GuildSettings;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.conversions.Bson;


public class WelcomeEvent extends ListenerAdapter {
    private final MongoDatabase mongoDatabase;
    public WelcomeEvent(MongoDatabase mongoDatabase){
        this.mongoDatabase = mongoDatabase;
    }


    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event){
        Guild guild = event.getGuild();
        GuildSettings guildSettings = getGuildInfo(guild.getId());

        if (guildSettings.getWelcomeSettings().getDefaultRoleID()!=null){
            Role defaultRole = guild.getRoleById(guildSettings.getWelcomeSettings().getDefaultRoleID());
            if (defaultRole!=null){
                guild.addRoleToMember(event.getMember(), defaultRole).queue();
            }
        }
        if (!guildSettings.getWelcomeSettings().isUseWelcome()){
            return;
        }
        String welcomeMessage = guildSettings.getWelcomeSettings().getJoinMessage();
        if (welcomeMessage.contains("{user}")){
            welcomeMessage = welcomeMessage.replaceAll("(\\{user})",event.getMember().getAsMention());
        }
        if (welcomeMessage.contains("{server}")){
            welcomeMessage = welcomeMessage.replaceAll("(\\{server})", guild.getName());
        }
        if (guildSettings.getWelcomeSettings().getMessageChannelID()==null){
            return;
        }
        TextChannel welcomeChannel = guild.getTextChannelById(guildSettings.getWelcomeSettings().getMessageChannelID());
        if (welcomeChannel == null){
            return;
        }
        if (event.getGuild().getSelfMember().hasAccess(welcomeChannel)){
            welcomeChannel.sendMessage(welcomeMessage).queue();
        }
    }
    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event){
        GuildSettings guildSettings = getGuildInfo(event.getGuild().getId());
        if (!guildSettings.getWelcomeSettings().isUseWelcome()){
            return;
        }
        String leaveMessage = guildSettings.getWelcomeSettings().getLeaveMessage();
        if (leaveMessage.contains("{user}")){
            leaveMessage = leaveMessage.replaceAll("(\\{user})", event.getUser().getName());
        }
        if (leaveMessage.contains("{server}")){
            leaveMessage = leaveMessage.replaceAll("(\\{server})", event.getGuild().getName());
        }
        if (guildSettings.getWelcomeSettings().getMessageChannelID()==null){
            return;
        }
        TextChannel welcomeChannel = event.getGuild().getTextChannelById(guildSettings.getWelcomeSettings().getMessageChannelID());
        if (welcomeChannel == null){
            return;
        }
        if (event.getGuild().getSelfMember().hasAccess(welcomeChannel)){
            welcomeChannel.sendMessage(leaveMessage).queue();
        }
    }

    private GuildSettings getGuildInfo(String guild){
        MongoCollection<GuildSettings> guildInformation = mongoDatabase.getCollection("Guild Information", GuildSettings.class);
        Bson query = Filters.eq("guildID", guild);
        return guildInformation.find(query).first();
    }
}
