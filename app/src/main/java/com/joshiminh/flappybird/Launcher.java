package com.joshiminh.flappybird;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.joshiminh.flappybird.game.FlappyBird;
import com.joshiminh.flappybird.game.FlappyBirdDuoGame;
import com.joshiminh.flappybird.score.ScoreBoard;
import com.joshiminh.flappybird.utils.ResourceUtil;

import java.awt.*;
import java.io.*;

public class Launcher {
    private static final String FILE_PATH = "LastPlay.txt";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Launcher::showLauncher);
    }

    public static void restartLauncher() {
        SwingUtilities.invokeLater(Launcher::showLauncher);
    }

    private static void showLauncher() {
        JTextField nameField = new JTextField(10);
        String[] themes = {"Original", "Red Night", "9-11", "Under Water"};
        JComboBox<String> themesComboBox = new JComboBox<>(themes);
        JComboBox<String> difficultyComboBox = new JComboBox<>(new String[]{"Easy", "Normal", "Hard", "Impossible"});

        themesComboBox.setSelectedItem("Original");
        difficultyComboBox.setSelectedItem("Normal");

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String firstLine = reader.readLine();
            if (firstLine != null) nameField.setText(firstLine);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JButton playButton = new JButton("Play");
        JButton duoButton = new JButton("Play Duo");
        JButton scoreButton = new JButton("ScoreBoard");
        JButton exitButton = new JButton("Exit");

        JFrame frame = new JFrame("Flappy Bird Launcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(ResourceUtil.loadAppIcon());

        JPanel content = new JPanel();
        content.setBorder(new EmptyBorder(10, 10, 10, 10));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel fieldPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        fieldPanel.add(new JLabel("Player Name:"));
        fieldPanel.add(nameField);
        fieldPanel.add(new JLabel("Select Theme:"));
        fieldPanel.add(themesComboBox);
        fieldPanel.add(new JLabel("Select Difficulty:"));
        fieldPanel.add(difficultyComboBox);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(playButton);
        buttonPanel.add(duoButton);
        buttonPanel.add(scoreButton);
        buttonPanel.add(exitButton);

        JLabel copyright = new JLabel("Copyright@JoshiMinh", SwingConstants.CENTER);

        content.add(fieldPanel);
        content.add(Box.createVerticalStrut(10));
        content.add(buttonPanel);
        content.add(Box.createVerticalStrut(10));
        content.add(copyright);
        frame.setContentPane(content);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        playButton.addActionListener(e -> {
            frame.dispose();
            String playerName = nameField.getText().trim();
            String selectedTheme = (String) themesComboBox.getSelectedItem();
            int selectedDifficulty = difficultyComboBox.getSelectedIndex();

            JFrame gameFrame = new JFrame("Flappy Bird");
            gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameFrame.setSize(800, 600);
            gameFrame.setResizable(false);
            gameFrame.add(new FlappyBird(selectedTheme, selectedDifficulty, playerName));
            gameFrame.setIconImage(ResourceUtil.loadAppIcon());
            gameFrame.setLocationRelativeTo(null);
            gameFrame.setVisible(true);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
                writer.write(playerName);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        duoButton.addActionListener(e -> {
            frame.dispose();
            int diff = Math.min(difficultyComboBox.getSelectedIndex() + 1, 3);
            JFrame gameFrame = new JFrame("Flappy Bird Duo");
            gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameFrame.setSize(800, 600);
            gameFrame.setResizable(false);
            gameFrame.add(new FlappyBirdDuoGame(diff));
            gameFrame.setIconImage(ResourceUtil.loadAppIcon());
            gameFrame.setLocationRelativeTo(null);
            gameFrame.setVisible(true);
        });

        scoreButton.addActionListener(e -> new ScoreBoard());
        exitButton.addActionListener(e -> System.exit(0));
    }
}

