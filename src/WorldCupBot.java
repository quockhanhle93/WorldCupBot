/*
 * Created with IntelliJ IDEA.
 * User: taylor
 * Project: WorldCupBot
 * Date: 6/12/2014
 * Time: 8:41 PM
 */

import org.jibble.pircbot.PircBot;

public class WorldCupBot extends PircBot {
    public static void main(String[] s) throws Exception {
        BotInstance bot = new BotInstance();
        bot.setMessageDelay(0L);
        bot.connect("irc.swiftirc.net");
    }
}