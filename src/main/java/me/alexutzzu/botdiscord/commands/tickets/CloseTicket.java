package me.alexutzzu.botdiscord.commands.tickets;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.alexutzzu.botdiscord.utils.addons.AdditionalMethods;
import me.alexutzzu.botdiscord.utils.categories.Support;
import me.alexutzzu.botdiscord.utils.pojos.guild.GuildSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class CloseTicket extends Command implements AdditionalMethods {
    private final EventWaiter eventWaiter;
    public CloseTicket(EventWaiter eventWaiter){
        this.category = new Support();
        this.eventWaiter = eventWaiter;
        this.name = "close";
        this.usesTopicTags = false;
        this.guildOnly = true;
        this.help = "Use this to close a ticket. Either mention it or be in the channel.";
        this.arguments = "[#channel] [reason]";
        this.cooldown = 5;
        this.botPermissions = new Permission[]{Permission.MANAGE_CHANNEL};
        this.helpBiConsumer = (event, command) -> {

        };
    }
    @Override
    protected void execute(CommandEvent event) {
        if (!checkBlacklistedChannel(event, this)){
            return;
        }
        GuildSettings guildSettings = event.getClient().getSettingsFor(event.getGuild());
        if (guildSettings.getTicketSettings().getSupportCategoryID()==null || guildSettings.getTicketSettings().getSupportTeamRoleID()==null){
            return;
        }
        Role supportRole = event.getGuild().getRoleById(guildSettings.getTicketSettings().getSupportTeamRoleID());
        net.dv8tion.jda.api.entities.Category supportCategory = event.getGuild().getCategoryById(guildSettings.getTicketSettings().getSupportCategoryID());
        TextChannel ticketLogsChannel = null;
        if (guildSettings.getTicketSettings().getTicketLogsChannelID()!=null){
            ticketLogsChannel = event.getGuild().getTextChannelById(guildSettings.getTicketSettings().getTicketLogsChannelID());
        }
        if (supportCategory==null) {
            return;
        }
        if (supportRole==null){
            return;
        }
        if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) && !event.getMember().getRoles().contains(supportRole)) {
            event.replyError("You may not use this command!", m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        String reason = "No reason provided";
        TextChannel ticketChannel;
        Member member = event.getMember();
        if (event.getMessage().getMentionedChannels().isEmpty()){
            ticketChannel = event.getTextChannel();

            if (!event.getArgs().isEmpty()){
                reason = event.getArgs();
            }
        }else{
            if (event.getMessage().getMentionedChannels().size() > 1){
                event.replyError("You mentioned too many channels!", m->m.delete().queueAfter(5, TimeUnit.SECONDS));
                return;
            }
            ticketChannel = event.getMessage().getMentionedChannels().get(0);
            String args = event.getArgs();
            String getReason = args.replace("<#" + ticketChannel.getId() + ">", "");
            if (!getReason.isEmpty()){
                reason = getReason;
            }
        }
        if (!supportCategory.getTextChannels().contains(ticketChannel)){
            event.replyError("This is not a ticket!", m->m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        TextChannel finalTicketChannel = ticketChannel, finalTicketLogsChannel = ticketLogsChannel;
        String finalReason = reason;
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Close Ticket?");
        eb.setDescription("You are about to close this ticket. Please type `confirm` to confirm this action or anything else to cancel. Not answering in 2 " +
                "minuted will cancel this operation. Below are the inputs you provided.");
        eb.addField("Ticket", ticketChannel.getAsMention(), true);
        eb.addField("Reason", reason, true);
        eb.setColor(Color.RED);
        event.reply(eb.build());
        eventWaiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getMessage().equals(event.getMessage()),
                e -> {
                    if (e.getMessage().getContentRaw().equalsIgnoreCase("confirm")){
                        event.replySuccess("Operation started..");
                        deleteTicket(finalTicketChannel.getId(), finalTicketLogsChannel, finalReason, member, event);
                    }else{
                        event.replyError("Operation cancelled.");
                    }
                },
                2, TimeUnit.MINUTES, () -> event.replyError("Operation was cancelled due to timeout.",m -> m.delete().queueAfter(5, TimeUnit.SECONDS))
        );
    }

    private void deleteTicket(String ticketChannelID, TextChannel logsChannel, String reason, Member member, CommandEvent event){
        TextChannel ticketChannel =event.getGuild().getTextChannelById(ticketChannelID);
        if (ticketChannel == null){
            event.replyError("This ticket was already deleted or does not exist!", message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        ticketChannel.delete().reason(reason).submit().whenComplete((unused, throwable) -> {
            if (throwable==null){
                if (logsChannel!=null){
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("Deleted Ticket");
                    eb.setColor(Color.RED);
                    eb.addField("ID", ticketChannel.getName(), true);
                    eb.addField("Deleted By", member.getAsMention(), true);
                    eb.addField("Reason", reason, true);
                    eb.setTimestamp(Instant.now());
                    eb.setFooter("Autogenerated");
                    logsChannel.sendMessage(eb.build()).queue();
                }
            }
        });
    }
}
