package me.alexutzzu.woobot.utils.generators;

import java.util.Random;

public class Tickets {
    private static final String lower = "abcdefghijklmnopqrstuvwxyz";
    private static final String digits = "0123456789";
    private static final String alphaNum = lower + digits;

    static Random random = new Random();
    public static String randomName(){
        StringBuilder stringBuilder = new StringBuilder(8);
        for (int i=0;i<8;i++){
            stringBuilder.append(alphaNum.charAt(random.nextInt(alphaNum.length())));
        }
        return stringBuilder.toString();
    }
}
