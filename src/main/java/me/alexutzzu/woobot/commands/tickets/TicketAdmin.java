package me.alexutzzu.woobot.commands.tickets;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import me.alexutzzu.woobot.utils.categories.Support;
import me.alexutzzu.woobot.utils.addons.AdditionalMethods;
import me.alexutzzu.woobot.utils.pojos.guild.GuildSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bson.conversions.Bson;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class TicketAdmin extends Command implements AdditionalMethods {

    public TicketAdmin(MongoDatabase mongoDatabase){
        this.category = new Support();
        this.name = "ticket";
        this.guildOnly = true;
        this.usesTopicTags = false;
        this.help = "Use this command to see/change settings of the ticket Component.";
        this.helpBiConsumer = (event, command) -> {
            if (!checkBlacklistedChannel(event, this)){
                return;
            }

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Ticket Command Help");
            eb.setColor(Color.RED);
            eb.setDescription("This is the help Panel for the ticket commands");
            eb.addField("`ticket`", command.getHelp(), false);
            eb.addField("`ticket help`", "Prints this help panel", false);
            for (Command value : command.getChildren()) {
                eb.addField("`ticket " + value.getName() + " " + value.getArguments() + "`", value.getHelp(), false);
            }
            eb.addField("Permission", "Administrator", true);
            eb.addField("Cooldown", command.getCooldown() + " seconds", true);
            event.reply(eb.build());
        };
        this.children = new Command[]{new SupportRole(mongoDatabase), new TicketCategory(mongoDatabase), new TicketLogChannel(mongoDatabase)};
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.cooldown = 5;
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildSettings guildSettings = event.getClient().getSettingsFor(event.getGuild());

        if (!checkBlacklistedChannel(event, this)){
            return;
        }
        if (!event.getArgs().isEmpty()){
            checkUnknownSyntax(event, this);
            return;
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.RED);
        eb.setTitle("Ticket Settings for " + event.getGuild().getName());
        eb.setDescription("These are the current Settings for the ticket component in this Server.");
        Role supportRole = null;
        if (guildSettings.getTicketSettings().getSupportTeamRoleID()!=null){
            supportRole = event.getGuild().getRoleById(guildSettings.getTicketSettings().getSupportTeamRoleID());
        }
        TextChannel ticketLogChannel = null;
        if (guildSettings.getTicketSettings().getTicketLogsChannelID()!=null){
            ticketLogChannel = event.getGuild().getTextChannelById(guildSettings.getTicketSettings().getTicketLogsChannelID());
        }
        net.dv8tion.jda.api.entities.Category supportCategory = null;
        if (guildSettings.getTicketSettings().getSupportCategoryID()!=null){
            supportCategory = event.getGuild().getCategoryById(guildSettings.getTicketSettings().getSupportCategoryID());
        }
        eb.addField("Category", (supportCategory==null ? "none" : supportCategory.getName()), true);
        eb.addField("Support Team Role", (supportRole==null ? "none" : supportRole.getAsMention()), true);
        eb.addField("Logs Channel", (ticketLogChannel==null ? "none" : ticketLogChannel.getAsMention()), true);
        if (supportCategory==null || supportRole==null){
            eb.setFooter("No role or category detected. You might want to run the setup or assign them, otherwise this feature won't work.");
        }
        event.reply(eb.build());
    }
}
class SupportRole extends Command implements AdditionalMethods {
    private final MongoDatabase mongoDatabase;

    public SupportRole(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
        this.guildOnly = true;
        this.usesTopicTags = false;
        this.name = "role";
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.cooldown = 10;
        this.help = "Use this to set a Custom Role for the Ticket Component. The role is a key part in this component, as it " +
                "allows the users to handle tickets by closing and answering them. If you want to disable the ticket component, you can" +
                "set this role to `none`, though it is not recommended.";
        this.botPermissions = new Permission[]{Permission.MANAGE_ROLES, Permission.MANAGE_CHANNEL};
        this.arguments = "<@role or none>";
    }

    @Override
    protected void execute(CommandEvent event) {
            MongoCollection<GuildSettings> guildInformation = mongoDatabase.getCollection("Guild Information", GuildSettings.class);
            Bson query = Filters.eq("guildID", event.getGuild().getId());
            GuildSettings guildSettings = event.getClient().getSettingsFor(event.getGuild());
            if (!checkBlacklistedChannel(event, this)){
                return;
            }
            if (event.getArgs().isEmpty()){
                checkUnknownSyntax(event, this);
                return;
            }
            String[] args = event.getArgs().split(" ");
            if (args.length > 1){
                checkUnknownSyntax(event, this);
                return;
            }
            if (args[0].equalsIgnoreCase("none")){
                Bson updateOp = Updates.set("ticketSettings.supportTeamRoleID", null);
                guildInformation.updateOne(query, updateOp);
                event.replySuccess("Successfully disabled the Ticket Component. You can re-enable it at any time by running the setup or by assigning a new Role.", m -> m.delete().queueAfter(7, TimeUnit.SECONDS));
            }else{
                if (event.getMessage().getMentionedRoles().size()!=1){
                    checkUnknownSyntax(event, this);
                    return;
                }
                Role newRole = event.getMessage().getMentionedRoles().get(0);
                if (!event.getSelfMember().canInteract(newRole)){
                    event.replyWarning("I cannot interact with this role! Please update my permissions!", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                    return;
                }
                Role oldRole = null;
                net.dv8tion.jda.api.entities.Category supportCategory = null;
                    if (guildSettings.getTicketSettings().getSupportTeamRoleID()!=null){
                        oldRole = event.getGuild().getRoleById(guildSettings.getTicketSettings().getSupportTeamRoleID());
                    }
                    if (guildSettings.getTicketSettings().getSupportCategoryID()!=null){
                        supportCategory = event.getGuild().getCategoryById(guildSettings.getTicketSettings().getSupportCategoryID());
                    }
                    if (supportCategory==null){
                        event.replyError("No ticket category exists. Operation stopped.");
                    }else{
                        if (oldRole==null){
                            supportCategory.getManager()
                                    .putPermissionOverride(newRole, Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE), null)
                                    .submit().thenAccept(x ->{
                                        event.replySuccess("Successfully updated permissions!");
                                        guildInformation.updateOne(query, Updates.set("ticketSettings.supportTeamRoleID", newRole.getId()));
                            })
                                    .whenComplete((v, t) ->{
                                if (t!=null){
                                    event.replyError("An error occurred. Please check my permissions and try again");
                            }else{
                                    event.replySuccess("Successfully updated the Support Team Role!");
                                }
                                    });

                        }else{
                            supportCategory.getManager()
                                    .removePermissionOverride(oldRole)
                                    .putPermissionOverride(newRole, Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE), null)
                                    .submit()
                                    .thenAccept(unused -> {
                                        event.replySuccess("Successfully updated permissions!");
                                        guildInformation.updateOne(query,Updates.set("ticketSettings.supportTeamRoleID", newRole.getId()));
                                 }).whenComplete(((unused, throwable) -> {
                                       if (throwable!=null){
                                            event.replyWarning("An error occurred. Please check my permissions and try again");
                                     }else{
                                           event.replySuccess("Successfully updated the Support Team Role!");
                                        }
                                 }));
                        }
                    }
            }
    }
}
class TicketCategory extends Command implements AdditionalMethods {
    private final MongoDatabase mongoDatabase;

    public TicketCategory(MongoDatabase mongoDatabase) {
        this.name = "category";
        this.guildOnly = true;
        this.usesTopicTags = false;
        this.mongoDatabase = mongoDatabase;
        this.arguments = "<category or none>";
        this.botPermissions = new Permission[]{Permission.MANAGE_CHANNEL, Permission.MANAGE_ROLES};
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.help = "Use this command to set the Ticket Category. This is where tickets will be appended when created. If a Support Role exists, its " +
                "permissions will be updated. Set this to none to disable the ticket component.";
    }

    @Override
    protected void execute(CommandEvent event) {
        MongoCollection<GuildSettings> guildInformation = mongoDatabase.getCollection("Guild Information", GuildSettings.class);
        Bson query = Filters.eq("guildID", event.getGuild().getId());
        GuildSettings guildSettings = event.getClient().getSettingsFor(event.getGuild());
        if (!checkBlacklistedChannel(event, this)){
            return;
        }
        if (event.getArgs().isEmpty()){
            checkUnknownSyntax(event, this);
            return;
        }
        String[] args = event.getArgs().split(" ");

        if (args[0].equalsIgnoreCase("none") && args.length==1){
            Bson updateOp = Updates.set("ticketSettings.supportCategoryID", null);
            guildInformation.updateOne(query, updateOp);
            event.replySuccess("Successfully disabled the ticket Component. You can re-enable it at any time by running the setup or by setting the " +
                    "ticket category", m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
        }else{
            String categoryName = "";
            for (int i=0;i<args.length;i++){
                categoryName = categoryName.concat(args[i]);
                if (i < args.length-1){
                    categoryName = categoryName.concat(" ");
                }
            }
            if (event.getGuild().getCategoriesByName(categoryName, true).isEmpty()){
                event.replyWarning("The provided category does not exist!", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                return;
            }
            net.dv8tion.jda.api.entities.Category newCategory = event.getGuild().getCategoriesByName(categoryName, true).get(0);
            if (!event.getSelfMember().hasAccess(newCategory)){
                event.replyError("I do not have access to that Category! Update my permissions and try again!", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                return;
            }
            Role supportRole = null;
            if (guildSettings.getTicketSettings().getSupportTeamRoleID()!=null){
                supportRole = event.getGuild().getRoleById(guildSettings.getTicketSettings().getSupportTeamRoleID());
            }
            guildInformation.updateOne(query, Updates.set("ticketSettings.supportCategoryID", newCategory.getId()));
            if (supportRole==null){
                newCategory.getManager()
                        .putPermissionOverride(event.getGuild().getPublicRole(), null, Collections.singletonList(Permission.VIEW_CHANNEL))
                        .submit().thenAccept(unused -> event.replySuccess("Successfully updated permissions!")).whenComplete((unused, throwable) -> {
                    if (throwable==null) {
                        event.replySuccess("Operation completed.");
                    }else{
                        event.replyError("An error occurred. Please check my permissions and try again!");
                    }
                });
            }else{
                newCategory.getManager()
                        .putPermissionOverride(event.getGuild().getPublicRole(), null, Collections.singletonList(Permission.VIEW_CHANNEL))
                        .putPermissionOverride(supportRole, Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE), null)
                        .submit().thenAccept(unused -> event.replySuccess("Successfully updated permissions!")).whenComplete((unused, throwable) -> {
                    if (throwable==null) {
                        event.replySuccess("Operation completed.");
                    }else{
                        event.replyError("An error occurred. Please check my permissions and try again!");
                    }
                });
            }

        }
    }
}
class TicketLogChannel extends Command implements AdditionalMethods {
    private final MongoDatabase mongoDatabase;
    public TicketLogChannel(MongoDatabase mongoDatabase){
        this.mongoDatabase = mongoDatabase;
        this.name = "log";
        this.arguments = "<#channel or none>";
        this.help = "Use this command to set the logs Channel for the Ticket Component. It is not mandatory to have one, but all created " +
                "or deleted tickets will be logged here.";
        this.aliases = new String[]{"logs", "channel"};
        this.guildOnly = true;
        this.usesTopicTags = false;
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.botPermissions =  new Permission[]{Permission.MESSAGE_MANAGE, Permission.MANAGE_CHANNEL, Permission.MANAGE_ROLES};
    }
    @Override
    protected void execute(CommandEvent event) {
        MongoCollection<GuildSettings> guildInformation = mongoDatabase.getCollection("Guild Information", GuildSettings.class);
        Bson query = Filters.eq("guildID", event.getGuild().getId());
        GuildSettings guildSettings = event.getClient().getSettingsFor(event.getGuild());
        if (!checkBlacklistedChannel(event, this)){
            return;
        }
        if (event.getArgs().isEmpty()){
            checkUnknownSyntax(event, this);
            return;
        }
        String[] args = event.getArgs().split(" ");
        if (args.length !=1){
            checkUnknownSyntax(event, this);
            return;
        }
        if (args[0].equalsIgnoreCase("none")){
            if (guildSettings.getTicketSettings().getTicketLogsChannelID()==null){
                event.replyWarning("You already unset the ticket logs channel!", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                return;
            }
            guildInformation.updateOne(query, Updates.set("ticketSettings.ticketLogsChannelID", null));
            event.replySuccess("Successfully disabled ticket logging!", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        if (event.getMessage().getMentionedChannels().size()!=1){
            event.replyWarning("Unknown syntax! Use `ticket help` for help!", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        TextChannel newTextChannel = event.getMessage().getMentionedChannels().get(0);
        TextChannel oldTextChannel = null;
        if (guildSettings.getTicketSettings().getTicketLogsChannelID()!=null){
            oldTextChannel = event.getGuild().getTextChannelById(guildSettings.getTicketSettings().getTicketLogsChannelID());
        }
        if (oldTextChannel!=null){
            if (oldTextChannel.equals(newTextChannel)){
                event.replyWarning("You already use this channel as the logs channel!", m-> m.delete().queueAfter(5, TimeUnit.SECONDS));
                return;
            }
        }
        if (!event.getSelfMember().hasAccess(newTextChannel)){
            event.replyWarning("I do not have access to that channel! Please change my permissions and try again!", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        guildInformation.updateOne(query, Updates.set("ticketSettings.ticketLogsChannelID", newTextChannel.getId()));
        event.replySuccess("Successfully set the ticket logs channel!", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
    }
}
