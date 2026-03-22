package joshiminh.flappybird;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;
import java.io.File;

public class Launcher {

    public static void restartLauncher() {
        main(new String[0]);
    }

    public static void main(String[] args) {
        JTextField nameField = new JTextField(10);
        File themesDir = new File("resources/themes");
        String[] themes = themesDir.isDirectory() ? themesDir.list((dir, name) -> new File(dir, name).isDirectory()) : new String[0];
        JComboBox<String> themesComboBox = new JComboBox<>(themes);
        JComboBox<String> difficulty = new JComboBox<>(new String[]{"Easy", "Normal", "Hard", "Impossible"});

        themesComboBox.setSelectedItem("Original");
        difficulty.setSelectedItem("Normal");

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Player Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Select Theme:"));
        panel.add(themesComboBox);
        panel.add(new JLabel("Select Difficulty:"));
        panel.add(difficulty);
        panel.add(new JLabel("Copyright@JoshiMinh"));

        Preferences prefs = Preferences.userNodeForPackage(Launcher.class);
        String lastPlayer = prefs.get("LastPlayer", "Player");
        nameField.setText(lastPlayer);

        ImageIcon birdIcon = new ImageIcon("icon.png");
        birdIcon = new ImageIcon(birdIcon.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));

        JFrame parentFrame = new JFrame();
        parentFrame.setUndecorated(true);
        parentFrame.setLocationRelativeTo(null);
        parentFrame.setIconImage(birdIcon.getImage());
        parentFrame.setVisible(true);

        int result = JOptionPane.showOptionDialog(
            parentFrame,
            panel,
            "Flappy Bird",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            birdIcon,
            new String[]{"PLAY", "ScoreBoard", "EXIT"},
            "PLAY"
        );

        parentFrame.dispose();

        if (result == 0) {
            String playerName = nameField.getText().trim();
            String selectedTheme = (String) themesComboBox.getSelectedItem();
            int selectedDifficulty = difficulty.getSelectedIndex();

            JFrame frame = new JFrame("Flappy Bird");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setResizable(false);
            frame.add(new FlappyBird(selectedTheme, selectedDifficulty, playerName));
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);
            frame.setIconImage(birdIcon.getImage());

            prefs.put("LastPlayer", playerName);
        } else if (result == 2) {
            System.exit(0);
        } else {
            new ScoreBoard();
            main(new String[0]);
        }
    }
}