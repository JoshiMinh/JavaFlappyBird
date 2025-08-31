package com.joshiminh.flappybird;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.joshiminh.flappybird.game.FlappyBird;
import com.joshiminh.flappybird.game.FlappyBirdDuoGame;
import com.joshiminh.flappybird.score.ScoreBoard;
import com.joshiminh.flappybird.utils.ResourceUtil;

import java.awt.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class Launcher {
    private static final String FILE_PATH = "/data/LastPlay.txt";
    private static final String MANIFEST_PATH = "/themes/themes.json";
    private static final String DEFAULT_THEME = "Original";

    /**
     * Loads available theme names from the themes manifest.
     *
     * @return an array of theme names defined in <code>themes.json</code>
     */
    private static String[] loadThemes() {
        URL resource = ResourceUtil.getResource(MANIFEST_PATH);
        if (resource == null) {
            return new String[0];
        }
        try (InputStream in = resource.openStream()) {
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
            if (json.length() <= 2) {
                return new String[0];
            }
            json = json.substring(1, json.length() - 1);
            return Arrays.stream(json.split(","))
                    .map(s -> s.trim().replaceAll("^\\\"|\\\"$", ""))
                    .toArray(String[]::new);
        } catch (IOException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    public static void restartLauncher() {
        main(new String[0]);
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(LauncherFrame::new);
    }

    private static class LauncherFrame extends JFrame {
        private final JTextField playerOneField = new JTextField(15);
        private final JTextField playerTwoField = new JTextField(15);
        private final JComboBox<String> themesComboBox;
        private final JComboBox<String> difficulty;
        private final ImageIcon birdIcon;
        private final JPanel playerTwoPanel;

        LauncherFrame() {
            birdIcon = new ImageIcon(ResourceUtil.getResource("/images/icon.png"));
            setIconImage(birdIcon.getImage());
            setTitle("Flappy Bird");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(new EmptyBorder(15, 15, 15, 15));
            content.setBackground(new Color(30, 30, 30));

            Font font = new Font("SansSerif", Font.PLAIN, 16);

            JLabel title = new JLabel("Flappy Bird", birdIcon, JLabel.CENTER);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            title.setFont(font.deriveFont(Font.BOLD, 24f));
            title.setForeground(Color.WHITE);
            content.add(title);
            content.add(Box.createRigidArea(new Dimension(0, 10)));

            loadLastPlayed();

            addLabeledField(content, "Player 1:", playerOneField, font);
            playerTwoPanel = addLabeledField(content, "Player 2:", playerTwoField, font);
            playerTwoPanel.setVisible(false);

            String[] themes = loadThemes();
            themesComboBox = new JComboBox<>(themes);
            if (Arrays.stream(themes).anyMatch(DEFAULT_THEME::equals)) {
                themesComboBox.setSelectedItem(DEFAULT_THEME);
            } else if (themes.length > 0) {
                themesComboBox.setSelectedIndex(0);
            }
            addLabeledField(content, "Theme:", themesComboBox, font);

            difficulty = new JComboBox<>(new String[]{"Easy", "Normal", "Hard", "Impossible"});
            difficulty.setSelectedItem("Normal");
            addLabeledField(content, "Difficulty:", difficulty, font);

            content.add(Box.createRigidArea(new Dimension(0, 10)));

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            buttons.setOpaque(false);

            JButton playBtn = new JButton("Play", new ImageIcon(ResourceUtil.getResource("/images/StartButton.png")));
            playBtn.setFont(font);
            JButton twoBtn = new JButton("2-Player", new ImageIcon(ResourceUtil.getResource("/images/bird.png")));
            twoBtn.setFont(font);
            JButton scoreBtn = new JButton("ScoreBoard", new ImageIcon(ResourceUtil.getResource("/images/Shift.png")));
            scoreBtn.setFont(font);
            JButton exitBtn = new JButton("Exit", new ImageIcon(ResourceUtil.getResource("/images/pause.png")));
            exitBtn.setFont(font);

            buttons.add(playBtn);
            buttons.add(twoBtn);
            buttons.add(scoreBtn);
            buttons.add(exitBtn);
            content.add(buttons);

            setContentPane(content);
            pack();
            setResizable(false);
            setLocationRelativeTo(null);
            setVisible(true);

            playBtn.addActionListener(e -> startSinglePlayer());
            twoBtn.addActionListener(e -> startTwoPlayer());
            scoreBtn.addActionListener(e -> new ScoreBoard());
            exitBtn.addActionListener(e -> System.exit(0));
        }

        private void startSinglePlayer() {
            String playerName = playerOneField.getText().trim();
            String selectedTheme = (String) themesComboBox.getSelectedItem();
            int selectedDifficulty = difficulty.getSelectedIndex();

            JFrame frame = new JFrame("Flappy Bird");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setResizable(false);
            frame.add(new FlappyBird(selectedTheme, selectedDifficulty, playerName));
            frame.setLocationRelativeTo(null);
            frame.setIconImage(birdIcon.getImage());
            frame.setVisible(true);

            saveNames(playerOneField.getText(), playerTwoField.getText());
            dispose();
        }

        private void startTwoPlayer() {
            if (!playerTwoPanel.isVisible()) {
                playerTwoPanel.setVisible(true);
                pack();
                return;
            }
            String playerOneName = playerOneField.getText().trim();
            String playerTwoName = playerTwoField.getText().trim();
            String selectedTheme = (String) themesComboBox.getSelectedItem();
            int selectedDifficulty = difficulty.getSelectedIndex();
            startTwoPlayerGame(playerOneName, playerTwoName, selectedTheme, selectedDifficulty, birdIcon);
            saveNames(playerOneField.getText(), playerTwoField.getText());
            dispose();
        }

        private void loadLastPlayed() {
            try (InputStream in = Launcher.class.getResourceAsStream(FILE_PATH)) {
                if (in != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                        String firstLine = reader.readLine();
                        String secondLine = reader.readLine();
                        if (firstLine != null) playerOneField.setText(firstLine);
                        if (secondLine != null) playerTwoField.setText(secondLine);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private JPanel addLabeledField(JPanel parent, String labelText, JComponent field, Font font) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            panel.setOpaque(false);
            JLabel label = new JLabel(labelText);
            label.setFont(font);
            label.setForeground(Color.WHITE);
            field.setFont(font);
            panel.add(label);
            panel.add(field);
            parent.add(panel);
            return panel;
        }

        private void saveNames(String name1, String name2) {
            String content = name1 + System.lineSeparator() + name2;
            try (InputStream out = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
                Files.copy(out, Paths.get(ResourceUtil.getResource(FILE_PATH).toURI()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    private static void startTwoPlayerGame(String playerOneName, String playerTwoName, String theme, int diff, ImageIcon birdIcon) {
        JFrame frame = new JFrame("Flappy Bird");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setResizable(false);
        frame.add(new FlappyBirdDuoGame(theme, diff));
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setIconImage(birdIcon.getImage());

        String content = playerOneName + System.lineSeparator() + playerTwoName;
        try (InputStream out = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            Files.copy(out, Paths.get(ResourceUtil.getResource(FILE_PATH).toURI()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
