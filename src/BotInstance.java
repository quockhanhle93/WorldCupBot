/*
 * Created with IntelliJ IDEA.
 * User: taylor
 * Project: WorldCupBot
 * Date: 6/12/2014
 * Time: 8:43 PM
 */

import org.jibble.pircbot.PircBot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

public class BotInstance extends PircBot {
    private ArrayList<Match> matches;
    private long last_sent_message;
    private LinkedHashMap<String, String> results;

    private String STREAM_URL = "http://stream.url/";
    private String COMCAST_URL = "http://comcast.url/";
    private String TOPIC_STRING = "SwiftIRC's Official World Cup Channel";
    private String MAIN_CHANNEL = "#WorldCup";
    private int RESULTS_LIMIT = 4;
    private boolean SCRAMBLE = true;

    public BotInstance() {
        this.setAutoNickChange(true);
        this.setName("GoalKeeper");
        this.setLogin("GoalKeep");
        this.setVersion("GoalKeeper/Taylor");
        _sendMessage("nickserv", "identify password");

        DataHandler dh = new DataHandler();
        matches = dh.getMatches();
        results = new LinkedHashMap<>();
        Timer timer = new Timer();
        timer.schedule(new ScoreChecker(), 0, 1500);
    }

    @Override
    public void onNickChange(String oldNick, String login, String hostname, String newNick) {
        if (isTaggedNick(newNick)) {
            voice(MAIN_CHANNEL, newNick);
            return;
        } else {
            if (isTaggedNick(oldNick)) {
                deVoice(MAIN_CHANNEL, newNick);
            }
            return;
        }
    }

    @Override
    public void onJoin(String channel, String sender, String login, String hostname) {
        if (sender.equals(this.getNick())) {
            boolean live_matches = false;
            for (Match match : matches) {
                if (match.live) {
                    live_matches = true;
                    _sendMessage(channel, "Hello! Current score: " + match.home_name + " " + match.home_score + ", " + match.away_name + " " + match.away_score);
                }
            }
            if (!live_matches) {
                _sendMessage(channel, "No game currently live. I'll let you know when one starts!");
            }
            return;
        }
        if (isTaggedNick(sender) && channel.equalsIgnoreCase(MAIN_CHANNEL)) {
            voice(MAIN_CHANNEL, sender);
            return;
        }
    }

    public ArrayList<Match> getLiveMatches() {
        ArrayList<Match> tempArrayList = new ArrayList<>();
        for (Match match : matches) {
            if (match.live) {
                tempArrayList.add(match);
            }
        }
        return tempArrayList;
    }

    public void _sendMessage(String target, String message) {
        System.out.println(target + ": " + message);
        sendMessage(target, message);
    }

    public void _sendNotice(String target, String message) {
        System.out.println(target + ": " + message);
        sendNotice(target, message);
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message) {
        long now = System.currentTimeMillis();
        if (now - last_sent_message < 2500L) {
            return;
        }
        if (!(message.startsWith("!") || message.startsWith("."))) {
            return;
        }
        message = message.toLowerCase();
        if (message.startsWith("!score") || message.startsWith(".score")) {
            last_sent_message = now;
            announceScore(channel);
            return;
        }
        if (message.startsWith("!minutes") || message.startsWith(".minutes") || message.startsWith("!time") || message.startsWith(".time")) {
            last_sent_message = now;
            announceMinutes(channel);
            return;
        }
        if (message.startsWith("!results") || message.startsWith(".results")) {
            last_sent_message = now;
            announceResults(channel, RESULTS_LIMIT);
            return;
        }
        if (message.startsWith("!last") || message.startsWith(".last") || message.startsWith("!result") || message.startsWith(".result")) {
            last_sent_message = now;
            announceResults(channel, 1);
            return;
        }

        if (message.startsWith("!commands") || message.startsWith(".commands")) {
            _sendNotice(sender, "Current commands are: !score, !time, !last, !results");
            return;
        }
    }

    @Override
    public void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {
        if (sourceNick.equals("Taylor") || sourceNick.equals("[GER]Taylor")) {
            joinChannel(channel);
        } else {
            _sendNotice(sourceNick, "I'm not currently accepting invites. Message Taylor if you'd like an instance in your channel.");
        }
    }

    @Override
    public void onPrivateMessage(String sender, String login, String hostname, String message) {
        if (sender.equals("Taylor") || sender.equals("[GER]Taylor")) {
            String[] messageArrayLowerCase = message.toLowerCase().split(" ");
            String[] messageArray = message.split(" ");
            if (messageArrayLowerCase[0].equals("join")) {
                joinChannel(messageArray[1]);
                return;
            }
            if (messageArrayLowerCase[0].equals("raw")) {
                sendRawLine(join(messageArray, 1, " "));
                return;
            }
            if (messageArrayLowerCase[0].equals("part")) {
                partChannel(messageArray[1]);
                return;
            }
            if (messageArrayLowerCase[0].equals("addresult")) {
                addResults(messageArray[1], join(messageArray, 2, " "));
                return;
            }
            if (messageArrayLowerCase[0].equals("amsg")) {
                sendToAllChannels(join(messageArray, 1, " "));
                return;
            }
            if (messageArrayLowerCase[0].equals("say")) {
                _sendMessage(messageArray[1], join(messageArray, 2, " "));
                return;
            }
            if (messageArrayLowerCase[0].equals("updatetopic")) {
                setTopic();
                return;
            }
            if (messageArrayLowerCase[0].equals("random")) {
                SCRAMBLE = messageArrayLowerCase[1].equals("true");
                return;
            }
            if (messageArrayLowerCase[0].equals("settopic")) {
                TOPIC_STRING = messageArray[1];
                return;
            }
            if (messageArrayLowerCase[0].equals("setcomcast")) {
                COMCAST_URL = messageArray[1];
                return;
            }
            if (messageArrayLowerCase[0].equals("setstream")) {
                STREAM_URL = messageArray[1];
                return;
            }
            if (messageArrayLowerCase[0].equals("setresultslimit")) {
                RESULTS_LIMIT = Integer.parseInt(messageArray[1]);
                return;
            }
            if (messageArrayLowerCase[0].equals("setmainchannel")) {
                MAIN_CHANNEL = messageArray[1];
                return;
            }
        }
    }

    private String join(String[] aArr, int element, String sSep) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = element, il = aArr.length; i < il; i++) {
            if (i > element)
                sbStr.append(sSep);
            sbStr.append(aArr[i]);
        }
        return sbStr.toString();
    }

    private boolean isTaggedNick(String nick) {
        String[] countrycodes = {"USA", "MEX", "HON", "CAN", "CRC", "COL", "ECU", "BRA", "CHI", "ARG", "URU", "ENG", "BEL", "BE", "NED", "NL", "NE", "GER", "FRA", "SUI", "CRO", "CR", "BIH", "BOS", "ESP", "SPA", "POR", "ITA", "IT", "GRE", "GR", "RUS", "RU", "GER", "GE", "DE", "AL", "ALG", "GH", "GHA", "NIG", "NI", "NGA", "IV", "CIV", "IVO", "CA", "CAM", "CMR", "IR", "IRN", "KO", "KOR", "JAP", "JA", "JP", "AU", "AUS"};
        int start = nick.indexOf('[') + 1;
        int end = nick.indexOf(']');
        if (start == -1 || end == -1) {
            return false;
        }
        String tag = nick.substring(start, end);
        for (String countrycode : countrycodes) {
            if (tag.toUpperCase().equals(countrycode)) {
                return true;
            }
        }
        return false;
    }

    private void announceScore(String channel) {
        boolean live_matches = false;
        for (Match match : matches) {
            if (match.live) {
                live_matches = true;
                if (match.penalties) {
                    _sendMessage(channel, "The current score is " + match.home_name + " (" + match.home_score + ")(" + match.home_penalties + "), " + match.away_name + " (" + match.away_score + ")(" + match.away_penalties + ")");
                } else {
                    _sendMessage(channel, "The current score is " + match.home_name + " (" + match.home_score + "), " + match.away_name + " (" + match.away_score + ")");
                }
            }
        }
        if (!live_matches) {
            _sendMessage(channel, "No game currently live. I'll let you know when one starts!");
        }
    }

    private void announceMinutes(String channel) {
        boolean live_matches = false;
        for (Match match : matches) {
            if (match.live) {
                live_matches = true;
                String minutes = match.minutes;
                if (minutes.contains("'")) {
                    _sendMessage(channel, "Current time for " + match.home_name + " vs. " + match.away_name + ": " + minutes);
                    continue;
                }
                if (minutes.equals("fifa.half-time")) {
                    _sendMessage(channel, "It's currently half-time in the " + match.home_name + " vs. " + match.away_name + " match.");
                    continue;
                }
                if (minutes.equals("fifa.full-time")) {
                    _sendMessage(channel, "The " + match.home_name + " vs. " + match.away_name + " match has now ended.");
                    continue;
                }
                if (minutes.equals("fifa.postponedStatus")) {
                    _sendMessage(channel, "The " + match.home_name + " vs. " + match.away_name + " match has been postponed.");
                    continue;
                }
                if (minutes.equals("fifa.lineups")) {
                    _sendMessage(channel, "The " + match.home_name + " vs. " + match.away_name + " match is in the line-ups phase.");
                    continue;
                }
                if (minutes.equals("fifa.end2ndhalf")) {
                    _sendMessage(channel, "The second half just ended in the " + match.home_name + " vs. " + match.away_name + " match.");
                    continue;
                }
                if (minutes.equals("fifa.endfirstextra")) {
                    _sendMessage(channel, "The first 15 minute extension just ended in the " + match.home_name + " vs. " + match.away_name + " match.");
                    continue;
                }
                if (minutes.equals("fifa.endsecondextra")) {
                    _sendMessage(channel, "The second 15 minute extension just ended in the " + match.home_name + " vs. " + match.away_name + " match.");
                    continue;
                }
                if (minutes.equals("fifa.penaltiesphase")) {
                    _sendMessage(channel, "The match ended in a " + match.home_score + " - " + match.away_score + " tie, it's sudden-death penalty kick time for " + match.home_name + " vs. " + match.away_name + "!");
                    continue;
                }
                if (minutes.equals("fifa.endpenaltiesphase")) {
                    _sendMessage(channel, "The penalty phase just ended in the " + match.home_name + " vs. " + match.away_name + " match.");
                    continue;
                }
                _sendMessage(channel, "I'm not sure what this message means: " + minutes + " for the " + match.home_name + " vs. " + match.away_name + " match.");
            }
        }
        if (!live_matches) {
            _sendMessage(channel, "No game currently live. I'll let you know when one starts!");
        }
    }

    private void announceResults(String channel, int amount) {
        int size = results.size();
        if (size < 1) {
            sendMessage(channel, "I have no records available. Wait for a game to finish!");
        } else {
            if (size > amount) {
                size = amount;
            }
            Object[] keys = results.keySet().toArray();
            for (int i = 0; i < size; i++) {
                String result = results.get(keys[i].toString());
                _sendMessage(channel, ("\u00030,1[\u00034Results\u00030,1]\u000F: ") + result);
            }
        }
    }

    public void sendToAllChannels(String message) {
        String[] channels = this.getChannels();
        for (String channel : channels) {
            _sendMessage(channel, message);
        }
    }

    private void setTopic() {
        StringBuilder topic = new StringBuilder();
        ArrayList<Match> matches = getLiveMatches();
        String red = " \u000314";
        String grey = " \u00034";
        if (SCRAMBLE) {
            if (Math.random() * 10 > 5) {
                if (Math.random() * 10 > 5) {
                    red = " \u000314";
                } else {
                    red = " \u000314\u000314";
                }
            } else {
                if (Math.random() * 10 > 5) {
                    red = "\u000314 ";
                } else {
                    red = "\u000314\u000314 ";
                }
            }
            if (Math.random() * 10 > 5) {
                if (Math.random() * 10 > 5) {
                    grey = " \u00034";
                } else {
                    grey = " \u00034\u00034";
                }
            } else {
                if (Math.random() * 10 > 5) {
                    grey = "\u00034 ";
                } else {
                    grey = "\u00034\u00034 ";
                }
            }
        }
        if (matches.size() > 0) {
            if (matches.size() > 1) {
                topic.append("\u00034[").append(MAIN_CHANNEL).append("]").append(red).append(TOPIC_STRING).append(grey).append("||").append(red).append("Current matches:").append(grey);
                for (int i = 0; i < matches.size(); i++) {
                    Match match = matches.get(i);
                    topic.append(match.home_name).append(" (").append(match.home_score).append(")").append(red).append("vs.").append(grey).append(match.away_name).append(" (").append(match.away_score).append(")");
                    if (i < matches.size() - 1) {
                        topic.append(" | ");
                    }
                }
                topic.append(grey).append("||").append(red).append("Live stream:").append(grey).append(STREAM_URL).append(red).append("Comcast users:").append(grey).append(COMCAST_URL);
            } else {
                Match match = matches.get(0);
                topic.append("\u00034[").append(MAIN_CHANNEL).append("]").append(red).append(TOPIC_STRING).append(grey).append("||").append(red).append("Current match:").append(grey).append(match.home_name).append(" (").append(match.home_score).append(")").append(red).append("vs.").append(grey).append(match.away_name).append(" (").append(match.away_score).append(")").append(grey).append("||").append(red).append("Live stream:").append(grey).append(STREAM_URL).append(red).append("Comcast users:").append(grey).append(COMCAST_URL);
            }
        } else {
            topic.append("\u00034[").append(MAIN_CHANNEL).append("]").append(red).append(TOPIC_STRING).append(grey).append("||").append(red).append("No matches currently live").append(grey).append("||").append(red).append("Live stream:").append(grey).append(STREAM_URL).append(red).append("Comcast users:").append(grey).append(COMCAST_URL);
        }
        setTopic(MAIN_CHANNEL, topic.toString());
    }

    private void addResults(String match_id, String result) {
        if (results.containsKey(match_id)) {
            results.put(match_id, result);
            return;
        }
        LinkedHashMap<String, String> tempResults = new LinkedHashMap<>();
        tempResults.put(match_id, result);
        tempResults.putAll(results);
        results = tempResults;
    }

    class ScoreChecker extends TimerTask {
        @Override
        public void run() {
            checkForChanges();
        }

        private void checkForChanges() {
            for (int i = 0; i < matches.size(); i++) {
                Match match = matches.get(i);
                if (match.live_changed) {
                    if (match.live) {
                        sendToAllChannels(match.home_name + " vs. " + match.away_name + " starts now!");
                        setTopic();
                    } else {
                        String finalScore;
                        if (match.home_score == match.away_score) {
                            if (match.winning_id != null && !match.winning_id.isEmpty()) {
                                String winner;
                                if (match.winning_id.equals(match.home_id)) {
                                    winner = match.home_name;
                                } else if (match.winning_id.equals(match.away_id)) {
                                    winner = match.away_name;
                                } else {
                                    winner = "Someone";
                                }
                                finalScore = match.home_name + " " + match.home_score + ", " + match.away_name + " " + match.away_score + " (" + winner + " won on penalties!)";
                            } else {
                                finalScore = match.home_name + " " + match.home_score + ", " + match.away_name + " " + match.away_score;
                            }
                        } else {
                            finalScore = match.home_name + " " + match.home_score + ", " + match.away_name + " " + match.away_score;
                        }
                        sendToAllChannels("The game has now ended, final score " + finalScore);
                        addResults(match.match_id, finalScore);
                        setTopic();
                    }
                    match.live_changed = false;
                    matches.set(i, match);
                    return;
                }
                if (match.live) {
                    if (match.away_score_changed) {
                        if(match.away_decreased) {
                            match.away_decreased = false;
                            sendToAllChannels("\u0002Hmmm... \u0002" + match.away_name + " lost a point. Current score: " + match.home_name + " " + match.home_score + ", " + match.away_name + " " + match.away_score);
                        } else {
                            sendToAllChannels("\u0002GOOOAAAL! \u0002" + match.away_name + " has scored! Current score: " + match.home_name + " " + match.home_score + ", " + match.away_name + " " + match.away_score);
                        }
                        setTopic();
                        match.away_score_changed = false;
                        matches.set(i, match);
                        return;
                    }
                    if (match.home_score_changed) {
                        if(match.home_decreased) {
                            match.home_decreased = false;
                            sendToAllChannels("\u0002Hmmm... \u0002" + match.home_name + " lost a point. Current score: " + match.home_name + " " + match.home_score + ", " + match.away_name + " " + match.away_score);
                        } else {
                            sendToAllChannels("\u0002GOOOAAAL! \u0002" + match.home_name + " has scored! Current score: " + match.home_name + " " + match.home_score + ", " + match.away_name + " " + match.away_score);
                        }
                        setTopic();
                        match.home_score_changed = false;
                        matches.set(i, match);
                        return;
                    }
                }
                if (match.penalties_changed) {
                    if (match.penalties) {
                        sendToAllChannels(match.home_name + " vs. " + match.away_name + " are starting sudden-death penalties!");
                    } else {
                        sendToAllChannels(match.home_name + " vs. " + match.away_name + " sudden-death penalties have now ended.");
                    }
                    match.penalties_changed = false;
                    matches.set(i, match);
                    return;
                }
                if (match.penalties) {
                    if (match.away_penalties_changed) {
                        sendToAllChannels("GOOOAAAL! " + match.away_name + " scored a penalty kick! Current penalty score: " + match.home_name + " " + match.home_penalties + ", " + match.away_name + " " + match.away_penalties);
                        match.away_penalties_changed = false;
                        matches.set(i, match);
                        return;
                    }
                    if (match.home_penalties_changed) {
                        sendToAllChannels("GOOOAAAL! " + match.home_name + " scored a penalty kick! Current penalty score: " + match.home_name + " " + match.home_penalties + ", " + match.away_name + " " + match.away_penalties);
                        match.home_penalties_changed = false;
                        matches.set(i, match);
                        return;
                    }
                }
            }
        }
    }
}