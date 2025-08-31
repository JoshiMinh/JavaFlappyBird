package com.joshiminh.flappybird;
import com.joshiminh.flappybird.game.FlappyBird;
import com.joshiminh.flappybird.game.FlappyBirdDuoGame;
import com.joshiminh.flappybird.score.ScoreBoard;
import com.joshiminh.flappybird.utils.ResourceUtil;
import java.awt.GridLayout;
import java.awt.Image;
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
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
          .toArray(String[] ::new);
    } catch (IOException e) {
      e.printStackTrace();
      return new String[0];
    }
  }

  public static void restartLauncher() {
    main(new String[0]);
  }

  public static void main(String[] args) {
    JTextField nameField = new JTextField(10);
    String[] themes = loadThemes();
    JComboBox<String> themesComboBox = new JComboBox<>(themes);
    if (Arrays.stream(themes).anyMatch(DEFAULT_THEME::equals)) {
      themesComboBox.setSelectedItem(DEFAULT_THEME);
    } else if (themes.length > 0) {
      themesComboBox.setSelectedIndex(0);
    }
    JComboBox<String> difficulty =
        new JComboBox<>(new String[] {"Easy", "Normal", "Hard", "Impossible"});
    difficulty.setSelectedItem("Normal");

    JPanel panel = new JPanel(new GridLayout(5, 2));
    panel.add(new JLabel("Player Name:"));
    panel.add(nameField);
    panel.add(new JLabel("Select Theme:"));
    panel.add(themesComboBox);
    panel.add(new JLabel("Select Difficulty:"));
    panel.add(difficulty);
    panel.add(new JLabel("Copyright@JoshiMinh"));

    try (InputStream in = Launcher.class.getResourceAsStream(FILE_PATH)) {
      if (in != null) {
        try (BufferedReader reader =
                 new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
          String firstLine = reader.readLine();
          if (firstLine != null)
            nameField.setText(firstLine);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    ImageIcon birdIcon = new ImageIcon(ResourceUtil.getResource("/images/icon.png"));
    birdIcon = new ImageIcon(birdIcon.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));

    JFrame parentFrame = new JFrame();
    parentFrame.setUndecorated(true);
    parentFrame.setLocationRelativeTo(null);
    parentFrame.setIconImage(birdIcon.getImage());
    parentFrame.setVisible(true);

    int result = JOptionPane.showOptionDialog(parentFrame, panel, "Flappy Bird",
        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, birdIcon,
        new String[] {"PLAY", "2-Player", "ScoreBoard", "EXIT"}, "PLAY");

    parentFrame.dispose();

    String selectedTheme = (String) themesComboBox.getSelectedItem();
    int selectedDifficulty = difficulty.getSelectedIndex();

    if (result == 0) {
      String playerName = nameField.getText().trim();

      JFrame frame = new JFrame("Flappy Bird");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setSize(800, 600);
      frame.setResizable(false);
      frame.add(new FlappyBird(selectedTheme, selectedDifficulty, playerName));
      frame.setVisible(true);
      frame.setLocationRelativeTo(null);
      frame.setIconImage(birdIcon.getImage());

      try (
          InputStream out = new ByteArrayInputStream(playerName.getBytes(StandardCharsets.UTF_8))) {
        Files.copy(out, Paths.get(ResourceUtil.getResource(FILE_PATH).toURI()),
            StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException | URISyntaxException e) {
        e.printStackTrace();
      }
    } else if (result == 1) {
      JTextField playerOneField = new JTextField(10);
      JTextField playerTwoField = new JTextField(10);

      try (InputStream in = Launcher.class.getResourceAsStream(FILE_PATH)) {
        if (in != null) {
          try (BufferedReader reader =
                   new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine();
            String secondLine = reader.readLine();
            if (firstLine != null)
              playerOneField.setText(firstLine);
            if (secondLine != null)
              playerTwoField.setText(secondLine);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      JPanel namesPanel = new JPanel(new GridLayout(2, 2));
      namesPanel.add(new JLabel("Player 1 Name:"));
      namesPanel.add(playerOneField);
      namesPanel.add(new JLabel("Player 2 Name:"));
      namesPanel.add(playerTwoField);

      int option = JOptionPane.showConfirmDialog(
          null, namesPanel, "Enter Player Names", JOptionPane.OK_CANCEL_OPTION);
      if (option == JOptionPane.OK_OPTION) {
        String playerOneName = playerOneField.getText().trim();
        String playerTwoName = playerTwoField.getText().trim();
        startTwoPlayerGame(
            playerOneName, playerTwoName, birdIcon, selectedTheme, selectedDifficulty);
      }
    } else if (result == 3) {
      System.exit(0);
    } else {
      new ScoreBoard();
      main(new String[0]);
    }
  }

  private static void startTwoPlayerGame(String playerOneName, String playerTwoName,
      ImageIcon birdIcon, String theme, int difficulty) {
    JFrame frame = new JFrame("Flappy Bird");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(800, 600);
    frame.setResizable(false);
    frame.add(new FlappyBirdDuoGame(theme, difficulty));
    frame.setVisible(true);
    frame.setLocationRelativeTo(null);
    frame.setIconImage(birdIcon.getImage());

    String content = playerOneName + System.lineSeparator() + playerTwoName;
    try (InputStream out = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
      Files.copy(out, Paths.get(ResourceUtil.getResource(FILE_PATH).toURI()),
          StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
    }
  }
}
