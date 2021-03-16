package me.alexutzzu.botdiscord.commands.tickets;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import me.alexutzzu.botdiscord.utils.addons.AdditionalMethods;
import me.alexutzzu.botdiscord.utils.addons.UpgradedMenu;
import me.alexutzzu.botdiscord.utils.categories.Support;
import me.alexutzzu.botdiscord.utils.constants.Emoji;
import me.alexutzzu.botdiscord.utils.generators.Tickets;
import me.alexutzzu.botdiscord.utils.pojos.guild.GuildSettings;
import me.alexutzzu.botdiscord.utils.pojos.tickets.GuildTicket;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bson.conversions.Bson;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class NewTicket extends Command implements AdditionalMethods {
    private final MongoDatabase mongoDatabase;
    private final EventWaiter eventWaiter;
    public NewTicket(MongoDatabase mongoDatabase, EventWaiter eventWaiter) {
        this.eventWaiter = eventWaiter;
        this.category = new Support();
        this.mongoDatabase = mongoDatabase;
        this.cooldown = 15;
        this.name = "open";
        this.aliases = new String[]{"new"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_MANAGE, Permission.MANAGE_CHANNEL};
        this.guildOnly = true;
        this.arguments = "[message]";
        this.usesTopicTags = false;
        this.help = "Creates a new ticket. Maximum of 2 open tickets per user. If no message is specified, a menu will be opened where " +
                "the user can choose the category";
        this.helpBiConsumer = (event, command) -> {

        };
    }

    @Override
    protected void execute(CommandEvent event) {
        MongoCollection<GuildTicket> guildTickets = mongoDatabase.getCollection("Guild Tickets", GuildTicket.class);
        GuildSettings guildSettings = event.getClient().getSettingsFor(event.getGuild());
        if (guildSettings.getTicketSettings().getSupportTeamRoleID()==null || guildSettings.getTicketSettings().getSupportCategoryID()==null){
            event.getMessage().delete().queue();
            event.getClient().applyCooldown(getCooldownKey(event), 0);
            return;
        }
        if (!checkBlacklistedChannel(event, this)){
            return;
        }
        event.getMessage().delete().queue();
        if (cleanUpTickets(event.getAuthor().getId(), event.getGuild().getId(), event.getJDA()) > 1){
            event.replyError("You cannot have more than 2 open tickets at once!", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        net.dv8tion.jda.api.entities.Category supportCategory =event.getGuild().getCategoryById(guildSettings.getTicketSettings().getSupportCategoryID());
        Role supportRole = event.getGuild().getRoleById(guildSettings.getTicketSettings().getSupportTeamRoleID());
        if (supportCategory==null){
            event.replyError("No support category found! Please contact the server Admins!", m->m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        if (supportRole == null){
            event.replyError("No support role found! Please contact the server Admins!", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        String args;
        if (!event.getArgs().isEmpty()){
            args = event.getArgs();
            createTicket(Emoji.OTHER, args, supportCategory, event, supportRole);
        }else{
            args = "";
            ArrayList<Emoji> emojisList = new ArrayList<>();
            emojisList.add(Emoji.OTHER);
            emojisList.add(Emoji.STAFF_REPORT);
            emojisList.add(Emoji.USER_REPORT);
            UpgradedMenu.Builder ticketMenu = new UpgradedMenu.Builder();
            ticketMenu.setEventWaiter(eventWaiter);
            ticketMenu.addChoices(Emoji.OTHER.getUnicode(),Emoji.USER_REPORT.getUnicode(),
                    Emoji.STAFF_REPORT.getUnicode());
            if (event.getGuild().getId().equals("802509296603889665")){
                ticketMenu.addChoices(Emoji.REQUEST_FEATURE.getUnicode(),Emoji.BOT_ISSUES.getUnicode());
                emojisList.add(Emoji.REQUEST_FEATURE);
                emojisList.add(Emoji.BOT_ISSUES);
            }
            ticketMenu.setColor(Color.RED);
            ticketMenu.setTimeout(2, TimeUnit.MINUTES);
            ticketMenu.setFinalAction(message -> message.delete().queue());
            EmbedBuilder menuDescForTickets = new EmbedBuilder();
            menuDescForTickets.setTitle("New ticket");
            menuDescForTickets.setDescription("Hello," + event.getAuthor().getName() + ". It seems as you need help from Staff. Below are some possible " +
                    "categories you can choose from. If neither fit your request, you can select `Other`");
            for (Emoji e : emojisList){
                menuDescForTickets.addField(e.getUnicode() + " " +e.getName(), e.getDescription(), true);
            }
            ticketMenu.setDescription(menuDescForTickets);
            ticketMenu.setUsers(event.getAuthor());
            ticketMenu.setAction(reactionEmote ->
                    {
                        Emoji emoji = null;
                        Emoji[] emojis = Emoji.values();
                        for (Emoji e : emojis){
                            if (e.getUnicode().equalsIgnoreCase(reactionEmote.getEmoji())){
                                emoji = e;
                                break;
                            }
                        }
                        if (emoji==null){
                            emoji = Emoji.OTHER;
                        }
                        createTicket(emoji, args, supportCategory, event, supportRole);
                    }
            );
            UpgradedMenu upgradedMenu = ticketMenu.build();
            upgradedMenu.display(event.getTextChannel());
        }
    }
    private void createTicket(Emoji emoji, String args, net.dv8tion.jda.api.entities.Category supportCategory, CommandEvent event, Role supportRole){
        MongoCollection<GuildTicket> guildTickets = mongoDatabase.getCollection("Guild Tickets", GuildTicket.class);
        GuildSettings guildSettings = event.getClient().getSettingsFor(event.getGuild());


        supportCategory.createTextChannel(emoji.getUnicode() + Tickets.randomName())
                .addPermissionOverride(event.getSelfMember(), Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE), null)
                .addPermissionOverride(event.getMember(), Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE), null)
                .addPermissionOverride(event.getGuild().getPublicRole(), null, Collections.singleton(Permission.VIEW_CHANNEL))
                .addPermissionOverride(supportRole, Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE), null)
                .submit()
                .thenAccept(textChannel -> {
                    //update db
                    GuildTicket guildTicket = new GuildTicket(event.getGuild().getId());
                    guildTicket.setChannelID(textChannel.getId());
                    guildTicket.setUserID(event.getMember().getId());

                    guildTickets.insertOne(guildTicket);
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("Ticket " + textChannel.getName().substring(1));
                    eb.setColor(Color.RED);
                    eb.setDescription("Welcome to your Private channel with Staff. Here you can chat with them in order to solve " +
                            "your problems. Make sure you give as much information as possible!");
                    eb.addField("Staff", supportRole.getAsMention(), true);
                    eb.addField("Category", emoji.getUnicode() + emoji.getName(), true);
                    if (!args.isEmpty()){
                        eb.addField("Your Message", args, true);
                    }

                    textChannel.sendMessage(supportRole.getAsMention() + ", " + event.getAuthor().getAsMention() +
                            ", has opened ticket " + textChannel.getAsMention() + ". Please have a look!").queue(m -> m.delete().queue());
                    textChannel.sendMessage(eb.build()).queue();

                    TextChannel logsChannel = null;
                    if (guildSettings.getTicketSettings().getTicketLogsChannelID()!=null){
                        logsChannel = event.getGuild().getTextChannelById(guildSettings.getTicketSettings().getTicketLogsChannelID());
                    }
                    if (logsChannel!=null && event.getSelfMember().hasAccess(logsChannel)){
                        EmbedBuilder eb1 = new EmbedBuilder();
                        eb1.setTitle("Created Ticket with ID " + textChannel.getName().substring(1));
                        eb1.setColor(Color.RED);
                        eb1.addField("Channel",textChannel.getAsMention(), true);
                        eb1.addField("User", event.getAuthor().getAsMention(), true);
                        eb1.addField("Category", emoji.getName(), true);
                        eb1.setTimestamp(Instant.now());
                        eb1.setFooter("Autogenerated");
                        logsChannel.sendMessage(eb1.build()).queue();
                    }
                }).whenComplete((unused, throwable) -> {
            if (throwable != null){
                event.replyError("An error has occurred! Please contact staff!", m->m.delete().queueAfter(5, TimeUnit.SECONDS));
            }
        });

    }
    private long cleanUpTickets(String userID,String guildID, JDA jda){

        MongoCollection<GuildTicket> guildTickets = mongoDatabase.getCollection("Guild Tickets", GuildTicket.class);
        Bson query = Filters.and(Filters.eq("guildID", guildID), Filters.eq("userID", userID));
        MongoIterable<GuildTicket> userTickets = guildTickets.find(query);

        long tickets = guildTickets.countDocuments(query);

        for (GuildTicket ticket : userTickets){
            if (jda.getGuildChannelById(ticket.getChannelID())==null){
                tickets--;
                Bson deleteQ = Filters.and(query, Filters.eq("channelID", ticket.getChannelID()));
                guildTickets.deleteOne(deleteQ);
            }
        }
        return tickets;
    }
}
