package me.alexutzzu.woobot.commands.help;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.alexutzzu.woobot.utils.addons.AdditionalMethods;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class HelpCommand extends Command implements AdditionalMethods {
    public HelpCommand(){
        this.name = "help";
        this.usesTopicTags = false;
        this.guildOnly = true;
        this.cooldown = 60;
        this.aliases = new String[]{"helpMe"};
    }
    @Override
    protected void execute(CommandEvent event) {
        if(!checkBlacklistedChannel(event, this)){
            return;
        }
        HashMap<Category, ArrayList<Command>> categoryListHashMap = new HashMap<>();
        for (Command cmd : event.getClient().getCommands()){
            ArrayList<Command> cmdList = categoryListHashMap.get(cmd.getCategory());
            if (cmdList==null){
                categoryListHashMap.put(cmd.getCategory(), new ArrayList<>(Collections.singleton(cmd)));
            }else{
                cmdList.add(cmd);
                categoryListHashMap.put(cmd.getCategory(), cmdList);
            }
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.RED);
        eb.setTitle("Help Panel");
        eb.setDescription("This is the help panel for my commands!" + '\n' +
                "Still need help? Join my [Support Server](" + event.getClient().getServerInvite() + ")");
        eb.setFooter("Use `[command] help` for specific help");
        for (Category category : categoryListHashMap.keySet()){
            String commandList = "`";
            ArrayList<Command> commandArrayList = categoryListHashMap.get(category);
            if (commandArrayList.size() >= 1){
                for (int i=0;i<commandArrayList.size(); i++){
                    if (commandArrayList.get(i).getUserPermissions().length==0){
                        if (i >=1){
                            commandList = commandList.concat(", " + commandArrayList.get(i).getName());
                        }else{
                            commandList = commandList.concat(commandArrayList.get(i).getName());
                        }
                    }
                    for (Permission perm : commandArrayList.get(i).getUserPermissions()){
                        if (event.getMember().hasPermission(perm)){
                            if (i >=1){
                                commandList = commandList.concat(", " + commandArrayList.get(i).getName());
                            }else{
                                commandList = commandList.concat(commandArrayList.get(i).getName());
                            }
                        }
                    }
                }
            }
            if (category!=null && !commandList.equals("`")){
                eb.addField(category.getName(), commandList + '`', true);
            }
        }
        event.reply(eb.build());
    }
}
