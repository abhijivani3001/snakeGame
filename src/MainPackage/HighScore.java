package MainPackage;

import java.io.*;
import java.util.*;

public class HighScore implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String HIGH_SCORES_FILE = "src/MainPackage/NewScore.txt";

    private String playerName;
    private int score;

    public HighScore(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    // Method to retrieve high scores from a file
    public static List<HighScore> getHighScores() {
        List<HighScore> highScores = new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(HIGH_SCORES_FILE))) {
            highScores = (List<HighScore>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Handle exceptions appropriately
            System.out.println("not found");
        }

        return highScores;
    }

    // Method to update and save high scores to a file
    public static void updateHighScores(HighScore newScore) {
        List<HighScore> highScores = getHighScores();
        highScores.add(newScore);
        Collections.sort(highScores, Comparator.comparingInt(HighScore::getScore).reversed());

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HIGH_SCORES_FILE))) {
            oos.writeObject(highScores);
        } catch (IOException e) {
            // Handle exceptions appropriately
//            e.printStackTrace();
            System.out.println("...");

        }
    }
}
