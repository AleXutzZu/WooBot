package me.alexutzzu.botdiscord.commands.tickets;

public class CloseTicketCommand {
    /**public void closeTicket(TextChannel channel, Member member){
        JSONReader jr = new JSONReader(member.getGuild().getId() + ".json");
        try{
            if (member.getRoles().contains(member.getGuild().getRoleById((String) jr.readGuildInfo("SupportTeamID"))) || member.hasPermission(Permission.ADMINISTRATOR)){
                Category ticketCategory = member.getGuild().getCategoryById((String) jr.readGuildInfo("SupportTicketCategoryID"));
                List<TextChannel> textChannelList = ticketCategory.getTextChannels();
                if (textChannelList.contains(channel)){
                    channel.delete().queue();
                    return;
                }
            }else{
                channel.sendMessage("You may not use this command!").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            }
        }catch(Exception e){
            channel.sendMessage("It seems like the Ticket System was not properly set-up.").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
    }*/
}
