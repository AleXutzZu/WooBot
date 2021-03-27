package me.alexutzzu.woobot.commands.setup;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import me.alexutzzu.woobot.utils.addons.AdditionalMethods;
import me.alexutzzu.woobot.utils.pojos.guild.GuildSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.bson.conversions.Bson;

import java.awt.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Ticket extends Command implements AdditionalMethods {
    private final HashMap<Guild, Boolean> isWorking = new HashMap<>();
    private final MongoDatabase mongoDatabase;
    private final EventWaiter eventWaiter;
    public Ticket(MongoDatabase mongoDatabase, EventWaiter eventWaiter){
        this.eventWaiter = eventWaiter;
        this.mongoDatabase = mongoDatabase;
        this.name = "ticket";
        this.help = "This command sets up the ticket component for this server";
        this.cooldown = 20;
        this.guildOnly = true;
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.botPermissions = new Permission[]{Permission.MANAGE_CHANNEL, Permission.MANAGE_ROLES, Permission.MANAGE_PERMISSIONS};
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!checkBlacklistedChannel(event, this)){
            return;
        }

       if (!event.getArgs().isEmpty()){
           checkUnknownSyntax(event, this);
           return;
       }

       if (isWorking.get(event.getGuild())==null || isWorking.get(event.getGuild())){
           isWorking.put(event.getGuild(), false);
           EmbedBuilder eb = new EmbedBuilder();
           eb.setTitle("Ticket Setup Operation");
           eb.setColor(Color.RED);
           eb.setDescription("You are about to start the Setup for the ticket component. Please type `confirm` in the 2 " +
                   "minute window that you have. Not typing in that time frame or typing anything else will cancel the operation." +
                   " Below you have the changes that will be made. They can be customized later. Using a logs channel is not mandatory, " +
                   "it can be created and set up later.");
           eb.addField("Categories", "+ `TICKETS`", true);
           eb.addField("Channels", "`none`", true);
           eb.addField("Roles", "+ `@Support Team`", true);
           eb.setFooter("If these changes are not made, please check the bot's permissions. If that still doesn't work, feel free " +
                   "to join my Support Server.");
           event.reply(eb.build());
           eventWaiter.waitForEvent(GuildMessageReceivedEvent.class, e ->
               e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getMessage().equals(event.getMessage())
           ,e ->{
            if (e.getMessage().getContentRaw().equalsIgnoreCase("confirm")){
                event.replySuccess("Operation started.", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                setupTicket(event.getGuild(), event);
                isWorking.remove(event.getGuild());
            }else{
                isWorking.remove(event.getGuild());
                event.replyError("Operation was cancelled.", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            }
           }, 2, TimeUnit.MINUTES, () -> {
               event.replyError("Operation was cancelled due to timeout.",m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
               isWorking.remove(event.getGuild());
           });
       }else{
           event.replyError("Someone is already using this command!", message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
       }


    }
    private void setupTicket(Guild guild, CommandEvent event) throws NullPointerException{
        Role botRole = (guild.getBotRole() == null) ? event.getSelfMember().getRoles().get(0) : guild.getBotRole();

        MongoCollection<GuildSettings> guildInformation = mongoDatabase.getCollection("Guild Information", GuildSettings.class);
        Bson query = Filters.eq("guildID", guild.getId());
        GuildSettings guildSettings = guildInformation.find(query).first();
        if (guildSettings==null){
            throw new NullPointerException("Guild not found");
        }
        Role supportRole = guild.getRoles().stream().filter(role -> role.getName().equalsIgnoreCase("Support Team") && event.getSelfMember().canInteract(role)).findFirst().orElse(null);
        net.dv8tion.jda.api.entities.Category supportCategory = guild.getCategories().stream().filter(category1 -> category1.getName().equalsIgnoreCase("TICKETS") && event.getSelfMember().hasAccess(category1)).findFirst().orElse(null);


       if (supportCategory!=null){
           event.replySuccess("**CATEGORY** Found category *" + supportCategory.getName() + "*");
           //update db
           guildInformation.updateOne(query, Updates.set("ticketSettings.supportCategoryID", supportCategory.getId()));
           if (supportRole!=null){
               event.replySuccess("**ROLE** Found role " + supportRole.getAsMention());
               //update db
               guildInformation.updateOne(query, Updates.set("ticketSettings.supportTeamRoleID", supportRole.getId()));
               executeSetup(botRole, supportRole, supportCategory, event);
           }else{
               event.replyWarning("**ROLE** Not found. Creating new one..");
               guild.createRole().setName("Support Team").setColor(Color.CYAN).submit()
                       .thenAccept(role -> {
                           guildInformation.updateOne(query, Updates.set("ticketSettings.supportTeamRoleID", role.getId()));
                           event.replySuccess("**ROLE** Created role " + role.getAsMention());
                           executeSetup(botRole, role, supportCategory, event);
                       });
           }
       }else{
           event.replyWarning("**CATEGORY** Not found. Creating new one..");
           guild.createCategory("TICKETS")
                   .addPermissionOverride(botRole, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL, Permission.MESSAGE_WRITE),null)
                   .addPermissionOverride(event.getSelfMember(), EnumSet.of(Permission.VIEW_CHANNEL,Permission.MANAGE_CHANNEL, Permission.MESSAGE_WRITE), null)
                   .submit()
                   .thenAccept(category1 -> {
                       event.replySuccess("**CATEGORY** Created category");
                       //update db
                       guildInformation.updateOne(query, Updates.set("ticketSettings.supportCategoryID", category1.getId()));
                       if (supportRole!=null){
                           event.replySuccess("**ROLE** Found role " + supportRole.getAsMention());
                           //update db
                           guildInformation.updateOne(query, Updates.set("ticketSettings.supportTeamRoleID", supportRole.getId()));
                           executeSetup(botRole, supportRole, category1, event);
                       }else{
                           event.replyWarning("**ROLE** Not found. Creating new one...");
                           guild.createRole().setColor(Color.CYAN).setName("Support Team").submit()
                                   .thenAccept(role -> {
                                       event.replySuccess("**ROLE** Created role " + role.getAsMention());
                                       //update db
                                       guildInformation.updateOne(query, Updates.set("ticketSettings.supportTeamRoleID", role.getId()));
                                       executeSetup(botRole,role, category1,event);
                                   });
                       }
                   }).whenComplete((unused, throwable) -> {
                       if (throwable!=null){
                           event.replyError("Operation failed. Check my permissions and try again.");
                           guildInformation.updateOne(query, Updates.set("ticketSettings.supportTeamRoleID", null));
                           guildInformation.updateOne(query, Updates.set("ticketSettings.supportCategoryID", null));
                       }
           });
       }
    }
    private void executeSetup(Role botRole, Role supportRole, net.dv8tion.jda.api.entities.Category ticketCategory, CommandEvent event) throws NullPointerException{
        if (supportRole==null || ticketCategory == null){
            throw new NullPointerException();
        }
        MongoCollection<GuildSettings> guildInformation = mongoDatabase.getCollection("Guild Information", GuildSettings.class);
        Bson query = Filters.eq("guildID", event.getGuild().getId());
        ticketCategory
                .getManager()
                .putPermissionOverride(botRole, EnumSet.of(Permission.MANAGE_CHANNEL, Permission.MESSAGE_WRITE), null)
                .putPermissionOverride(supportRole, EnumSet.of(Permission.MESSAGE_WRITE, Permission.VIEW_CHANNEL),null)
                .putPermissionOverride(event.getSelfMember(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL, Permission.MESSAGE_WRITE), null)
                .putPermissionOverride(event.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .submit()
                .whenComplete((unused, throwable) -> {
                    if (throwable==null){
                        event.replySuccess("Operation completed.");
                    }else{
                        event.replyError("Operation failed. Check my permissions and try again.");
                        guildInformation.updateOne(query, Updates.set("ticketSettings.supportTeamRoleID", null));
                        guildInformation.updateOne(query, Updates.set("ticketSettings.supportCategoryID", null));
                    }
                });
    }
}
