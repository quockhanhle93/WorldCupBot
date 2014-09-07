/*
 * Created with IntelliJ IDEA.
 * User: taylor
 * Project: WorldCupBot
 * Date: 6/12/2014
 * Time: 8:47 PM
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class DataHandler {
    private final String MATCH_PAGE = "http://www.fifa.com/worldcup/matches/index.html";
    private final String LIVE_URL = "http://lup.fifa.com/live/common/competitions/worldcup/_feed/_listmachlive.js";

    private ArrayList<Match> matches;

    public DataHandler() {
        this.matches = new ArrayList<>();
        Timer timer = new Timer();
        ScoreChecker sc = new ScoreChecker();
        timer.schedule(sc, 0, 5000);
    }

    private String[] _getTeamNames(final String match_id) {
        String[] names = new String[4];
        names[0] = null;
        names[1] = null;
        names[2] = null;
        names[3] = null;
        Document doc;
        try {
            doc = Jsoup.connect(MATCH_PAGE).timeout(5000).get();
        } catch (HttpStatusException e) {
            System.out.println("URL returned bad HTTP status");
            return names;
        } catch (IOException e) {
            System.out.println("Input/output exception fetching match ID: " + match_id);
            return names;
        }
        if (match_id == null || match_id.isEmpty()) {
            return names;
        }
        Element teams = doc.getElementsByAttributeValue("data-id", match_id).get(0)
                .getElementsByClass("mu-m-link").get(0)
                .getElementsByClass("mu-m").get(0);
        Element home = teams.getElementsByClass("home").get(0);
        Element away = teams.getElementsByClass("away").get(0);
        names[0] = home.getElementsByClass("t-n").get(0).getElementsByClass("t-nText").get(0).text();
        names[1] = away.getElementsByClass("t-n").get(0).getElementsByClass("t-nText").get(0).text();
        names[2] = home.attr("data-team-id");
        names[3] = away.attr("data-team-id");
        return names;
    }

    public ArrayList<Match> getMatches() {
        return this.matches;
    }

    private ArrayList<Match> getLiveStats() {
        String jsonString;
        try {
            jsonString = Jsoup.connect(LIVE_URL).timeout(5000).ignoreContentType(true).execute().body();
        } catch (HttpStatusException e) {
            System.out.println("URL returned bad HTTP status");
            return null;
        } catch (IOException e) {
            System.out.println("Input/output exception fetching: " + LIVE_URL);
            return null;
        }
        try {
            jsonString = jsonString.substring(20, jsonString.length() - 4);
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("\tStringIndexOutOfBoundsException");
            System.out.println("\t" + jsonString);
        }
        JsonElement jsonElement;
        jsonElement = new JsonParser().parse(jsonString);
        JsonArray matches = jsonElement.getAsJsonObject().getAsJsonArray("matches");
        for (int i = 0; i < matches.size(); i++) {
            JsonObject match = matches.get(i).getAsJsonObject();
            String id = match.get("id").getAsString();
            Match tempMatch = hasMatch(id);
            if (tempMatch == null) {
                tempMatch = new Match();
            }
            tempMatch.match_id = match.get("id").getAsString();
            if (tempMatch.home_name == null || tempMatch.away_name == null) {
                String[] names = _getTeamNames(tempMatch.match_id);
                tempMatch.home_name = names[0];
                tempMatch.away_name = names[1];
                tempMatch.home_id = names[2];
                tempMatch.away_id = names[3];
            }
            tempMatch.winning_id = match.get("idWinTeam").getAsString();
            tempMatch.minutes = match.get("min").getAsString();
            boolean live = match.get("s").getAsString().equals("live");
            tempMatch.live_changed = tempMatch.live != live;
            tempMatch.live = live;
            if (!live) {
                updateMatch(tempMatch);
                continue;
            }
            boolean penalties = tempMatch.minutes.equals("fifa.penaltiesphase");
            tempMatch.penalties_changed = tempMatch.penalties != penalties;
            tempMatch.penalties = penalties;
            int tmp;
            if (penalties) {
                tmp = match.get("scorepenh").getAsInt();
                if (tmp > tempMatch.home_penalties) {
                    tempMatch.home_penalties_changed = true;
                    tempMatch.home_penalties = tmp;
                    tempMatch.home_penalties_decreased = false;
                } else if (tmp == tempMatch.home_penalties) {
                    tempMatch.home_penalties_error = 0;
                    tempMatch.home_penalties_decreased = false;
                } else {
                    tempMatch.home_penalties_error++;
                    if (tempMatch.home_penalties_error > 20) {
                        tempMatch.home_penalties_changed = true;
                        tempMatch.home_penalties = tmp;
                        tempMatch.home_penalties_error = 0;
                        tempMatch.home_penalties_decreased = true;
                    }
                }
                tmp = match.get("scorepena").getAsInt();
                if (tmp > tempMatch.away_penalties) {
                    tempMatch.away_penalties_changed = true;
                    tempMatch.away_penalties = tmp;
                    tempMatch.away_penalties_decreased = false;
                } else if (tmp == tempMatch.away_penalties) {
                    tempMatch.away_penalties_error = 0;
                    tempMatch.away_penalties_decreased = false;
                } else {
                    tempMatch.away_penalties_error++;
                    if (tempMatch.away_penalties_error > 20) {
                        tempMatch.away_penalties_changed = true;
                        tempMatch.away_penalties = tmp;
                        tempMatch.away_penalties_error = 0;
                        tempMatch.away_penalties_decreased = true;
                    }
                }
            }
            String[] score = match.get("r").getAsString().split("-");
            tmp = Integer.valueOf(score[0]);
            if (tmp > tempMatch.home_score) {
                tempMatch.home_score_changed = true;
                tempMatch.home_score = tmp;
                tempMatch.home_decreased = false;
            } else if (tmp == tempMatch.home_score) {
                tempMatch.home_error = 0;
                tempMatch.home_decreased = false;
            } else {
                tempMatch.home_error++;
                if (tempMatch.home_error > 20) {
                    tempMatch.home_score_changed = true;
                    tempMatch.home_score = tmp;
                    tempMatch.home_error = 0;
                    tempMatch.home_decreased = true;
                }
            }
            tmp = Integer.valueOf(score[1]);
            if (tmp > tempMatch.away_score) {
                tempMatch.away_score_changed = true;
                tempMatch.away_score = tmp;
                tempMatch.away_decreased = false;
            } else if (tmp == tempMatch.away_score) {
                tempMatch.away_error = 0;
                tempMatch.away_decreased = false;
            } else {
                tempMatch.away_error++;
                if (tempMatch.away_error > 20) {
                    tempMatch.away_score_changed = true;
                    tempMatch.away_score = tmp;
                    tempMatch.away_error = 0;
                    tempMatch.away_decreased = true;
                }
            }
            updateMatch(tempMatch);
        }
        return this.matches;
    }

    private Match hasMatch(String id) {
        for (Match match : this.matches) {
            if (match.match_id.equals(id)) {
                return match;
            }
        }
        return null;
    }

    private void updateMatch(Match match) {
        for (int i = 0; i < this.matches.size(); i++) {
            if (this.matches.get(i).match_id.equals(match.match_id)) {
                this.matches.set(i, match);
                return;
            }
        }
        this.matches.add(match);
    }

    class ScoreChecker extends TimerTask {
        @Override
        public void run() {
            getLiveStats();
        }
    }
}
