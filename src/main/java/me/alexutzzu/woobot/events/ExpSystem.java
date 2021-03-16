package me.alexutzzu.woobot.events;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ExpSystem extends ListenerAdapter {
   /** public HashMap<Member, LocalDateTime> userLastMessage = new HashMap<>();

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event){
        Member member = event.getMember();
        JSONReader jr = new JSONReader(member.getGuild().getId() + ".json");
        if (event.getMessage().getContentDisplay().equalsIgnoreCase(jr.readGuildInfo("CommandPrefix") + "level")){
            event.getMessage().delete().queue();
            int userXP = Integer.parseInt(getUserXP(member.getGuild().getId() + ".json", member.getId()).toString());
            int userLevel = getUserLevel(userXP);
            int nextUserLevelXP = userLevel * 250 + 100;
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(member.getUser().getName() + "`s Level");
            double percentage = (userXP*100.0)/nextUserLevelXP;
            int percentageInt = (int) percentage;
            String progressBar = "";
            int p=0;
            while (p <= percentageInt-20){
               progressBar = progressBar.concat("▮");
                p+=20;
            }
            int l = progressBar.length();
            for (int i=1; i<=5-l;i++){
                progressBar = progressBar.concat("▯");
            }
            eb.addField("Progress" + "(" + percentageInt + "%)", userLevel + " " + progressBar + " " + (userLevel+ 1), true);
            eb.addField("XP", userXP + "/" + nextUserLevelXP, true);
            eb.setColor(Color.RED);
            event.getChannel().sendMessage(eb.build()).queue();
            return;
        }

        if (!member.equals(event.getGuild().getSelfMember()) && !member.getUser().isBot()){
            if (userLastMessage.get(member)!=null){
                LocalDateTime ldt = userLastMessage.get(member);
                if (!ldt.plus(1, ChronoUnit.MINUTES).isAfter(LocalDateTime.now())){
                    userLastMessage.put(member, LocalDateTime.now());
                    setUserXP(member.getId(), member.getGuild().getId() + ".json", event.getChannel());
                    //System.out.println("That fired at " + userLastMessage.get(member) + " in guild " + event.getGuild().getName() + " from " +member.getEffectiveName());
                    return;
                }
            }else{
                userLastMessage.put(member, LocalDateTime.now());
                setUserXP(member.getId(), member.getGuild().getId() + ".json", event.getChannel());
                //System.out.println("This fired at " + userLastMessage.get(member) + " in guild " + event.getGuild().getName() + " from " +member.getEffectiveName());
                return;
            }
            return;
        }

    }
    private int getRandomXP(){
        return new Random().nextInt(25)+1;
    }

    private Object getUserXP(String filename, String userID){
        JSONParser jp = new JSONParser();
        try (FileReader r = new FileReader(filename)){
            JSONArray infoArr = (JSONArray) jp.parse(r);

            if (infoArr.size() < 3){
                setUpUserList(filename);
                return getUserXP(filename, userID);
            }
            JSONObject lvlInfo = (JSONObject) infoArr.get(2);
            JSONArray lvlList = (JSONArray) lvlInfo.get("levels");

            for (Object i : lvlList){
                JSONObject iObj = (JSONObject) i;
                if (iObj.get("ID").equals(userID)){
                    return (Object) iObj.get("XP");
                }
            }
            setUpUser(userID, filename);
            return getUserXP(filename, userID);

        }catch (IOException | ParseException e){
            e.printStackTrace();
        }
        return 0;
    }

    private void setUserXP(String userID, String filename, TextChannel channel){
            JSONParser jp = new JSONParser();
            String currentXp = getUserXP(filename, userID).toString();
            int newXP = Integer.parseInt(currentXp) + getRandomXP();

            ///new level
            int currentLvl = getUserLevel( Integer.parseInt(currentXp));
            int possbileNewLvl = getUserLevel(newXP);
            if (possbileNewLvl == currentLvl + 1){
                channel.sendMessage("Congratulations! You **leveled** up to level **" + possbileNewLvl + "**!").queue();
            }

            try (FileReader r = new FileReader(filename)){
                JSONArray infoArr = (JSONArray) jp.parse(r);
                if (infoArr.size() < 3){
                    setUpUserList(filename);
                    setUserXP(userID, filename, channel);
                }
                JSONObject lvlObject = (JSONObject) infoArr.get(2);
                JSONArray lvlList = (JSONArray) lvlObject.get("levels");

                for (Object i : lvlList){
                    JSONObject iObj = (JSONObject) i;
                    if (iObj.get("ID").equals(userID)){
                        iObj.put("XP", newXP);
                        break;
                    }
                }
                lvlObject.put("levels", lvlList);
                infoArr.set(2, lvlObject);
                try(FileWriter w = new FileWriter(filename)){
                    w.write(infoArr.toJSONString());
                    w.flush();
                }

            }catch (IOException | ParseException e){
                e.printStackTrace();
            }
    }

    private void setUpUser(String userID, String filename){
        JSONObject userInfo = new JSONObject();
        userInfo.put("ID", userID);
        userInfo.put("XP", 0);

        JSONParser jp = new JSONParser();
        try (FileReader r = new FileReader(filename)){
            JSONArray infoArr =(JSONArray) jp.parse(r);

            JSONObject lvlInfo = (JSONObject) infoArr.get(2);
            JSONArray lvlList = (JSONArray) lvlInfo.get("levels");

            lvlList.add(userInfo);

            lvlInfo.put("levels", lvlList);
            infoArr.set(2, lvlInfo);
            try (FileWriter w = new FileWriter(filename)){
                w.write(infoArr.toJSONString());
                w.flush();
            }

        }catch (IOException | ParseException e){
            e.printStackTrace();
        }
    }
    private void setUpUserList(String filename){
        JSONParser jp = new JSONParser();

        try (FileReader r = new FileReader(filename)){
            JSONArray arrInfo = (JSONArray) jp.parse(r);

            JSONObject listInfo = new JSONObject();
            listInfo.put("levels", new JSONArray());
            arrInfo.add(2, listInfo);

            try (FileWriter w = new FileWriter(filename)){
                w.write(arrInfo.toJSONString());
                w.flush();
            }

        }catch (IOException | ParseException e){
            e.printStackTrace();
        }
    }
    private int getUserLevel(int userXP){
        int l1 = 100;
        int ratio = 250;
        if (userXP < l1){
            return 0;
        }
        return (int) (Math.floor((userXP - l1)/ratio)+1);
    }*/
}
