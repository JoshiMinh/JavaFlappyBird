package joshiminh.flappybird;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Appends one CSV row per game to {@code scores.csv}: {@code timestamp,username,score}. */
public final class Score {

    private static final Path FILE = Paths.get("scores.csv");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Score() {}

    public static synchronized void save(String username, int score) {
        try {
            boolean exists = Files.exists(FILE);
            String entry = (exists ? "" : "timestamp,username,score\n")
                    + FMT.format(LocalDateTime.now()) + ","
                    + sanitize(username) + ","
                    + score + System.lineSeparator();
            Files.writeString(FILE, entry, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String sanitize(String s) {
        if (s == null || s.isBlank())
            return "Player";
        return s.trim().replaceAll("[,\r\n\"]+", "_");
    }
}