/*
 * Created with IntelliJ IDEA.
 * User: taylor
 * Project: WorldCupBot
 * Date: 6/22/2014
 * Time: 5:17 PM
 */

public class Match {

    public String match_id;
    public String home_name;
    public String away_name;
    public String home_id;
    public String away_id;
    public String winning_id;
    public String minutes;
    public int home_error;
    public int home_penalties_error;
    public int away_error;
    public int away_penalties_error;
    public int home_score;
    public boolean home_score_changed;
    public boolean home_decreased;
    public int away_score;
    public boolean away_score_changed;
    public boolean away_decreased;
    public boolean penalties;
    public boolean penalties_changed;
    public int home_penalties;
    public boolean home_penalties_changed;
    public boolean home_penalties_decreased;
    public int away_penalties;
    public boolean away_penalties_changed;
    public boolean away_penalties_decreased;
    public boolean live;
    public boolean live_changed;

    public Match() {

    }
}