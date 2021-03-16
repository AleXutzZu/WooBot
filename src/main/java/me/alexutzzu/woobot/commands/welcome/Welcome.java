package me.alexutzzu.woobot.commands.welcome;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import me.alexutzzu.woobot.utils.addons.AdditionalMethods;
import me.alexutzzu.woobot.utils.pojos.guild.GuildSettings;
import me.alexutzzu.woobot.utils.pojos.guild.WelcomeSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bson.conversions.Bson;
import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Welcome extends Command implements AdditionalMethods {
    public Welcome(MongoDatabase mongoDatabase){
        this.category = new me.alexutzzu.woobot.utils.categories.Welcome();
        this.cooldown = 10;
        this.name = "welcome";
        this.guildOnly = true;
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.help = "This command will display information about the currently configured Welcome settings.";
        this.children = new Command[]{new SetDefaultRole(mongoDatabase), new SetWelcomeChannel(mongoDatabase), new SetJoinMessage(mongoDatabase),
                new SetLeaveMessage(mongoDatabase)};
        this.helpBiConsumer = (commandEvent, command) -> {
            if (!checkBlacklistedChannel(commandEvent, this)){
                return;
            }
            EmbedBuilder eb = new EmbedBuilder();
            Command[] commands = command.getChildren();
            eb.setColor(Color.RED);
            eb.setTitle("welcome Command Help");
            eb.setDescription("This is the help panel for the welcome commands");
            eb.addField("`welcome help`", "Prints this help panel", false);
            eb.addField("`welcome`", command.getHelp(), false);
            for (Command value : commands) {
                eb.addField("`welcome " + value.getName() + " " + value.getArguments() + "`", value.getHelp(), false);
            }
            eb.addField("Permission", "Administrator", true);
            eb.addField("Cooldown", command.getCooldown() + " seconds", true);
            commandEvent.reply(eb.build());
        };

    }
    @Override
    protected void execute(CommandEvent event) {
        GuildSettings guildSettings = event.getClient().getSettingsFor(event.getGuild());
        WelcomeSettings welcomeSettings = guildSettings.getWelcomeSettings();
        if(!checkBlacklistedChannel(event, this)){
            return;
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Server Configured Settings");
        eb.setColor(Color.RED);
        eb.setDescription("This panel shows the configured settings for the Welcome Components");

        String messageChannelID = welcomeSettings.getMessageChannelID();
        String defaultRoleID = welcomeSettings.getDefaultRoleID();

        Role defaultRole = null;
        TextChannel messageChannel = null;

        if (messageChannelID!=null){
            messageChannel = event.getGuild().getTextChannelById(messageChannelID);
        }
        if (defaultRoleID!=null){
            defaultRole = event.getGuild().getRoleById(defaultRoleID);
        }

        eb.addField("Message channel",
                (messageChannel!=null ?
                        messageChannel.getAsMention() : "none"),
                true);
        eb.addField("Default Role",
                (defaultRole!=null ?
                        defaultRole.getAsMention() : "none"),
                true);
        eb.addField("Join Message", welcomeSettings.getJoinMessage(), false);
        eb.addField("Leave Message", welcomeSettings.getLeaveMessage(), false);
        event.reply(eb.build());
    }
}
class SetDefaultRole extends Command implements AdditionalMethods {
    private final MongoDatabase mongoDatabase;
    public SetDefaultRole(MongoDatabase mongoDatabase){
        this.mongoDatabase = mongoDatabase;
        this.name = "role";
        this.help = "This will set the role given to every user. " +
                "Set it to **none** to disable this feature.";
        this.guildOnly = true;
        this.arguments = "<@role or none>";
        this.usesTopicTags = false;
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.botPermissions = new Permission[]{Permission.MANAGE_ROLES};
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
        if (event.getArgs().equals("none")){
            MongoCollection<GuildSettings> guildInformation = mongoDatabase.getCollection("Guild Information", GuildSettings.class);
            Bson query = Filters.eq("guildID", event.getGuild().getId());
            Bson updateOp = Updates.set("welcomeSettings.defaultRoleID", null);
            guildInformation.findOneAndUpdate(query,updateOp);
            event.replySuccess("Default role was set to none meaning it was disabled.", message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        Message message = event.getMessage();
        if (message.getMentionedRoles().size() !=1){
            event.replyError("Please mention a role!", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        Role defaultRole = message.getMentionedRoles().get(0);
        if (!event.getSelfMember().canInteract(defaultRole)){
            event.replyWarning("I cannot interact with this role! Move my role higher or this one lower!", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        String defaultRoleID = defaultRole.getId();
        MongoCollection<GuildSettings> guildInformation = mongoDatabase.getCollection("Guild Information", GuildSettings.class);
        Bson query = Filters.eq("guildID", event.getGuild().getId());
        Bson updateOp = Updates.set("welcomeSettings.defaultRoleID", defaultRoleID);
        guildInformation.findOneAndUpdate(query,updateOp);
        event.replySuccess("Default role was successfully set to " + defaultRole.getAsMention(), m -> m.delete().queueAfter(7, TimeUnit.SECONDS));

    }
}
class SetWelcomeChannel extends Command implements AdditionalMethods {
    private final MongoDatabase mongoDatabase;
    public SetWelcomeChannel(MongoDatabase mongoDatabase){
        this.name = "channel";
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.botPermissions = new Permission[]{Permission.MANAGE_SERVER, Permission.MANAGE_CHANNEL};
        this.help = "This will set the **channel** in which **join/leave messages** will be sent. " +
                "Set this to **none** to disable this feature.";
        this.arguments = "<#channel or none>";
        this.guildOnly = true;
        this.usesTopicTags= false;
        this.mongoDatabase = mongoDatabase;
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
        if (event.getArgs().equals("none")){
            MongoCollection<GuildSettings> guildInformation = mongoDatabase.getCollection("Guild Information", GuildSettings.class);
            Bson query = Filters.eq("guildID", event.getGuild().getId());
            Bson updateOp = Updates.combine(Updates.set("welcomeSettings.useWelcome", false), Updates.set("welcomeSettings.messageChannelID", null));
            guildInformation.updateOne(query, updateOp);
            event.replySuccess("Welcome channel was set to none. No join/leave messages will be sent.");
            return;
        }
        if (event.getMessage().getMentionedChannels().size() !=1){
            event.replyError("Please mention a channel!", m -> m.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }
        TextChannel textChannel = event.getMessage().getMentionedChannels().get(0);
        if (!event.getSelfMember().hasAccess(textChannel)){
            event.replyWarning("I do not have access to this channel!", m -> m.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }
        MongoCollection<GuildSettings> guildInformation = mongoDatabase.getCollection("Guild Information", GuildSettings.class);
        Bson query = Filters.eq("guildID", event.getGuild().getId());
        Bson updateOp = Updates.combine(Updates.set("welcomeSettings.useWelcome", true), Updates.set("welcomeSettings.messageChannelID", textChannel.getId()));
        guildInformation.updateOne(query, updateOp);
        event.replySuccess("Welcome channel was set to " + textChannel.getAsMention() + ". All welcome messages will be sent" +
                " in that channel", m -> m.delete().queueAfter(7, TimeUnit.SECONDS));
    }
}
class SetJoinMessage extends Command implements AdditionalMethods {
    private final MongoDatabase mongoDatabase;
    public SetJoinMessage(MongoDatabase mongoDatabase){
        this.mongoDatabase = mongoDatabase;
        this.name = "join";
        this.arguments = "<message>";
        this.help = "This will set the message that will be sent when a user **joins** the server. " +
                "Use **{user}** to display the user's name and **{server}** for the Server's name.";
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.guildOnly = true;
        this.usesTopicTags = false;
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
        String welcomeMessage = event.getArgs();
        Bson query = Filters.eq("guildID", event.getGuild().getId());
        Bson updateOp = Updates.set("welcomeSettings.joinMessage", welcomeMessage);
        mongoDatabase.getCollection("Guild Information").updateOne(query, updateOp);
        event.replySuccess("Join message was set to " + welcomeMessage, m -> m.delete().queueAfter(7, TimeUnit.SECONDS));

    }
}
class SetLeaveMessage extends Command implements AdditionalMethods {
    private final MongoDatabase mongoDatabase;
    public SetLeaveMessage(MongoDatabase mongoDatabase){
        this.name = "leave";
        this.arguments = "<message>";
        this.help = "This will set the message that will be sent when a user **leaves** the server. " +
                "Use **{user}** to display the user's name and **{server}** for the Server's name.";
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.guildOnly = true;
        this.usesTopicTags = false;
        this.mongoDatabase = mongoDatabase;
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
        String leaveMessage = event.getArgs();
        Bson query = Filters.eq("guildID", event.getGuild().getId());
        Bson updateOp = Updates.set("welcomeSettings.leaveMessage", leaveMessage);
        mongoDatabase.getCollection("Guild Information").updateOne(query, updateOp);
        event.replySuccess("Leave message was set to " + leaveMessage, m -> m.delete().queueAfter(7, TimeUnit.SECONDS));
    }
}
