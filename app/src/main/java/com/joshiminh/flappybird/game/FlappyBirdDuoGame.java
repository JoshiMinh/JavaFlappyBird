package com.joshiminh.flappybird.game;

import com.joshiminh.flappybird.utils.AudioUtil;
import com.joshiminh.flappybird.utils.GameUtil;
import com.joshiminh.flappybird.utils.ResourceUtil;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class FlappyBirdDuoGame extends JPanel implements ActionListener, KeyListener {
  private Timer timer;
  private int birdX, birdY, bird2X, bird2Y, loser = 0;
  private int birdVelocity, birdVelocityX, bird2Velocity, bird2VelocityX,
      hit; // velocities for birds
  private int rotationAngle, rotationAngle2, obstacleCount = 0;
  private int Diff, DefaultDiff, prevScore;
  private String theme;
  private List<Rectangle> obstacles = new ArrayList<>();
  private Random random = new Random();
  private float gameVol = 0.8f;
  private boolean endGame = false, startGame = false, Down = true, SHIFTED = false;

  // Game settings
  private int Tick, space, distance, velocity, gravity;

  // Game assets
  private ImageIcon base, deadBird, flappyBirdIcon, flappyBird2Icon, backgroundImage, upperPipeIcon,
      lowerPipeIcon;

  public static void main(String[] args) {
    // Setup main game window
    JFrame frame = new JFrame("Flappy Bird Duo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setIconImage(new ImageIcon(ResourceUtil.getResource("/images/icon.png")).getImage());
    frame.setSize(800, 600);
    frame.setResizable(false);
    frame.add(new FlappyBirdDuoGame("Original", 0));
    frame.setVisible(true);
    frame.setLocationRelativeTo(null);
  }

  public FlappyBirdDuoGame(String theme, int difficulty) {
    this.theme = theme;
    this.Diff = difficulty;
    DefaultDiff = Diff;
    setDifficulty(Diff);

    // Load and scale images
    flappyBirdIcon = new ImageIcon(ResourceUtil.getResource("/themes/" + theme + "/bird.png"));
    flappyBird2Icon = new ImageIcon(ResourceUtil.getResource("/images/bird2.png"));
    backgroundImage =
        new ImageIcon(ResourceUtil.getResource("/themes/" + theme + "/background.png"));
    upperPipeIcon = new ImageIcon(ResourceUtil.getResource("/themes/" + theme + "/obsdown.png"));
    lowerPipeIcon = new ImageIcon(ResourceUtil.getResource("/themes/" + theme + "/obs.png"));
    base = new ImageIcon(ResourceUtil.getResource("/themes/" + theme + "/base.png"));

    // Scale images
    backgroundImage =
        new ImageIcon(backgroundImage.getImage().getScaledInstance(800, 600, Image.SCALE_DEFAULT));
    flappyBirdIcon =
        new ImageIcon(flappyBirdIcon.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
    flappyBird2Icon = new ImageIcon(flappyBird2Icon.getImage().getScaledInstance(
        50, 50, Image.SCALE_DEFAULT)); // Scale second bird
    base = new ImageIcon(base.getImage().getScaledInstance(800, 100, Image.SCALE_DEFAULT));

    // Initialize game variables
    birdX = 200;
    birdY = 200;
    bird2X = 550; // Initial position for second bird
    bird2Y = 200;
    birdVelocity = birdVelocityX = 0;
    bird2Velocity = bird2VelocityX = 0; // Initialize second bird's velocities
    rotationAngle = rotationAngle2 = 0;
    obstacles = new ArrayList<>();
    random = new Random();

    // Setup game
    addKeyListener(this);
    setFocusable(true);

    timer = new Timer(150, this);
    timer.start();

    addObstacle(); // Create the first obstacle
  }

  public void setDifficulty(int difficulty) {
    int[][] settings = {
        {20, 220, 580, 3, 1}, {16, 190, 530, 4, 1}, {12, 150, 470, 6, 1}, {8, 110, 110, 8, 2}};
    if (difficulty >= 0 && difficulty < settings.length) {
      int[] s = settings[difficulty];
      Tick = s[0];
      space = s[1];
      distance = s[2];
      velocity = s[3];
      gravity = s[4];
    }
  }

  public void getLevel(int level) {
    if (level >= prevScore + 4 * (Diff + 1) && Diff < 3) {
      prevScore = level;
      setDifficulty(++Diff);
      timer.setDelay(Tick);
    }
  }

  private void addObstacle() {
    GameUtil.generateObstacle(obstacles, random, space, 60);
    obstacleCount++;
  }

  public void move() {
    if (!endGame && startGame) {
      // Apply gravity to both birds
      if (!SHIFTED) {
        birdVelocityX += (birdVelocityX > 0) ? -gravity : gravity;
        bird2VelocityX +=
            (bird2VelocityX > 0) ? -gravity : gravity; // Second bird's velocity adjustment
      }
      birdVelocity += gravity;
      bird2Velocity += gravity;

      birdY += birdVelocity;
      birdX += birdVelocityX;
      bird2Y += bird2Velocity; // Move second bird
      bird2X += bird2VelocityX; // Move second bird

      // Move obstacles
      for (Rectangle obstacle : obstacles) {
        obstacle.x -= velocity;
      }

      // Check for collisions for both birds
      Rectangle bird1 = new Rectangle(birdX, birdY, 50, 40);
      Rectangle bird2 = new Rectangle(bird2X, bird2Y, 50, 40); // Second bird's rectangle
      for (Rectangle obstacle : obstacles) {
        if (obstacle.intersects(bird1) || birdY < 0 || birdY > 475 || birdX < 0 || birdX > 800) {
          AudioUtil.playSound("/audio/bird-hit.wav", gameVol);
          loser = 1;
          endGame = true;
          return;
        }

        else if (obstacle.intersects(bird2) || bird2Y < 0 || bird2Y > 475 || bird2X < 0
            || bird2X > 800) {
          AudioUtil.playSound("/audio/bird-hit.wav", gameVol);
          loser = 2;
          endGame = true;
          return;
        }

        else if (bird1.intersects(bird2)) { // Check for bird-to-bird collision
          AudioUtil.playSound("/audio/bird-hit.wav", gameVol);
          hit = 15;
          if (Math.abs(birdVelocityX) < Math.abs(bird2VelocityX)) {
            birdVelocityX += (birdX > bird2X) ? hit : -hit;
          }

          else if (Math.abs(birdVelocityX) > Math.abs(bird2VelocityX)) {
            bird2VelocityX += (bird2X > birdX) ? hit : -hit;
          }
          return;
        }
      }

      // Generate new obstacles
      if (obstacles.isEmpty() || obstacles.get(obstacles.size() - 1).x < distance) {
        addObstacle();
      }

      // Remove off-screen obstacles
      obstacles.removeIf(obstacle -> obstacle.x + obstacle.width < 0);

    } else if (!endGame && !startGame) {
      // Apply gravity to both birds in a different way when the game isn't started or ended
      birdY += (Down) ? 10 : -10;
      bird2Y += (Down) ? 10 : -10; // Apply gravity effect to second bird
      Down = !Down;

      // Move obstacles (no movement when the game isn't started)
      obstacles.forEach(obstacle -> obstacle.x -= 0);

    } else if (endGame && startGame) {
      // Apply gravity to both birds during end game
      birdVelocity += gravity;
      birdVelocityX = 0;
      birdY += birdVelocity;
      birdX += birdVelocityX;

      bird2Velocity += gravity; // Gravity for second bird
      bird2VelocityX = 0;
      bird2Y += bird2Velocity;
      bird2X += bird2VelocityX;

      // Move obstacles (no movement during end game)
      for (Rectangle obstacle : obstacles) {
        obstacle.x -= 0;
        if (birdY > 475 || bird2Y > 475) { // Check for both birds
          gameOver();
        }
      }

      // Generate new obstacles
      if (obstacles.isEmpty() || obstacles.get(obstacles.size() - 1).x < distance) {
        addObstacle();
      }

      // Remove off-screen obstacles
      obstacles.removeIf(obstacle -> obstacle.x + obstacle.width < 0);
    }
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    // Draw background image
    backgroundImage.paintIcon(this, g, 0, 0);

    // Draw obstacles
    for (int i = 0; i < obstacles.size(); i += 2) {
      Rectangle upper = obstacles.get(i);
      Rectangle lower = obstacles.get(i + 1);
      g.drawImage(upperPipeIcon.getImage(), upper.x, upper.y, upper.width, upper.height, this);
      g.drawImage(lowerPipeIcon.getImage(), lower.x, lower.y, lower.width, lower.height, this);
    }

    // Draw ground
    base.paintIcon(this, g, 0, 520);

    // Rotate and draw first Flappy Bird image
    Graphics2D g2d = (Graphics2D) g;
    g2d.rotate(Math.toRadians(rotationAngle), birdX + 20, birdY + 22);
    flappyBirdIcon.paintIcon(this, g2d, birdX, birdY);
    g2d.rotate(-Math.toRadians(rotationAngle), birdX + 20, birdY + 22); // Reset rotation

    // Draw second Flappy Bird image (no rotation needed if similar behavior)
    g2d.rotate(Math.toRadians(rotationAngle2), bird2X + 20, bird2Y + 22);
    flappyBird2Icon.paintIcon(this, g2d, bird2X, bird2Y);
    g2d.rotate(-Math.toRadians(rotationAngle2), bird2X + 20, bird2Y + 22);

    // Draw start button if the game hasn't started or ended
    if (!endGame && !startGame) {
      ImageIcon startButtonIcon =
          new ImageIcon(ResourceUtil.getResource("/images/StartButton.png"));
      startButtonIcon =
          new ImageIcon(startButtonIcon.getImage().getScaledInstance(150, 60, Image.SCALE_DEFAULT));

      int centerX = (getWidth() - startButtonIcon.getIconWidth()) / 2;
      startButtonIcon.paintIcon(this, g, centerX, 400);
    }

    else if (startGame) {
      ImageIcon shiftButton;
      if (SHIFTED) {
        shiftButton = new ImageIcon(ResourceUtil.getResource("/images/Shifted.png"));
      } else {
        shiftButton = new ImageIcon(ResourceUtil.getResource("/images/Shift.png"));
      }

      shiftButton =
          new ImageIcon(shiftButton.getImage().getScaledInstance(150, 50, Image.SCALE_DEFAULT));
      shiftButton.paintIcon(this, g, 10, 10);
    }
    getLevel(obstacleCount);
  }

  public void actionPerformed(ActionEvent e) {
    move(); // Move game elements
    repaint(); // Refresh screen
  }

  public void keyPressed(KeyEvent e) {
    int keyCode = e.getKeyCode();
    if (loser == 2 || loser == 0) {
      switch (keyCode) {
        // Controls for the first bird
        case KeyEvent.VK_SPACE:
          startGame = true;
          timer.setDelay(Tick);
          break;
        case KeyEvent.VK_W:
          startGame = true;
          timer.setDelay(Tick);
          birdVelocity = -13;
          rotationAngle = -20;
          AudioUtil.playSound("/audio/flap.wav", gameVol);
          break;
        case KeyEvent.VK_S:
          birdVelocity = 10;
          rotationAngle = 20;
          AudioUtil.playSound("/audio/flap.wav", gameVol);
          break;
        case KeyEvent.VK_D:
          birdVelocityX = 15;
          rotationAngle = -20;
          AudioUtil.playSound("/audio/flap.wav", gameVol);
          break;
        case KeyEvent.VK_A:
          birdVelocityX = -15;
          rotationAngle = -20;
          AudioUtil.playSound("/audio/flap.wav", gameVol);
          break;
      }
    }
    if (loser == 1 || loser == 0) {
      switch (keyCode) {
        // Controls for the second bird
        case KeyEvent.VK_UP:
          startGame = true;
          timer.setDelay(Tick);
          bird2Velocity = -13;
          rotationAngle2 = -20;
          AudioUtil.playSound("/audio/flap.wav", gameVol);
          break;
        case KeyEvent.VK_DOWN:
          bird2Velocity = 10;
          rotationAngle2 = 20;
          AudioUtil.playSound("/audio/flap.wav", gameVol);
          break;
        case KeyEvent.VK_RIGHT:
          bird2VelocityX = 15;
          rotationAngle2 = -20;
          AudioUtil.playSound("/audio/flap.wav", gameVol);
          break;
        case KeyEvent.VK_LEFT:
          bird2VelocityX = -15;
          rotationAngle2 = -20;
          AudioUtil.playSound("/audio/flap.wav", gameVol);
          break;
      }
    }

    if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
      SHIFTED = true;
    }

    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
      timer.stop();

      ImageIcon pauseIcon = new ImageIcon(ResourceUtil.getResource("/images/pause.png"));
      pauseIcon =
          new ImageIcon(pauseIcon.getImage().getScaledInstance(30, 30, Image.SCALE_DEFAULT));

      // Pause dialog with three options
      int choice = JOptionPane.showOptionDialog(this, "Game Paused", "Pause",
          JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, pauseIcon,
          new Object[] {"Continue", "Restart", "Quit"}, "Continue");

      // Handle user choice
      if (choice == JOptionPane.YES_OPTION) {
        timer.start();
      } else if (choice == JOptionPane.NO_OPTION) {
        restartGame();
      } else {
        System.exit(0);
      }
    }
  }

  public void keyReleased(KeyEvent e) {
    int keyCode = e.getKeyCode();

    // Common action for all birds
    if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
      rotationAngle = 30; // Set rotation angle when Space, W, or Up is released
      rotationAngle2 = 30;
    }

    // Reset flags and velocities
    if (keyCode == KeyEvent.VK_SHIFT) {
      SHIFTED = false; // Reset SHIFTED flag
    }
  }

  public void keyTyped(KeyEvent e) {} // No action required

  public void gameOver() {
    timer.stop(); // Stop the game timer

    // Resize the dead bird image
    if (loser == 1) {
      deadBird = new ImageIcon(ResourceUtil.getResource("/themes/" + theme + "/dead_bird.png"));
    } else if (loser == 2) {
      deadBird = new ImageIcon(ResourceUtil.getResource("/images/dead_bird2.png"));
    }

    deadBird = new ImageIcon(deadBird.getImage().getScaledInstance(45, 45, Image.SCALE_DEFAULT));

    // Display game over dialog
    int choice = JOptionPane.showOptionDialog(this, "Player " + loser + " Lose", "Game Over",
        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, deadBird,
        new Object[] {"Retry", "Quit"}, "Retry");

    // Handle the user's choice
    if (choice == JOptionPane.YES_OPTION) {
      restartGame(); // Restart the game
    } else {
      System.exit(0); // Exit the game
    }
  }

  public void restartGame() {
    // Reset game variables for the first bird
    birdX = 200;
    birdY = 200;
    birdVelocity = birdVelocityX = 0;

    // Reset game variables for the second bird
    bird2X = 550; // Reset position for second bird
    bird2Y = 200;
    bird2Velocity = bird2VelocityX = 0;

    rotationAngle = 0;
    rotationAngle2 = 0;
    Diff = DefaultDiff;
    prevScore = 0;
    obstacleCount = 0;
    loser = 0;
    SHIFTED = false;
    startGame = endGame = false;

    // Reset difficulty and obstacles
    setDifficulty(Diff);
    obstacles.clear();
    addObstacle();

    // Restart game timer
    timer.setDelay(150);
    timer.start();
  }
}