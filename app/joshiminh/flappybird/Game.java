package joshiminh.flappybird;

import joshiminh.flappybird.components.GamePhysics;
import joshiminh.flappybird.components.GameRenderer;
import joshiminh.flappybird.components.SoundPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Thin JPanel coordinator: owns the timer, key listener, and dialog flows.
 * All physics live in {@link GamePhysics}; all rendering in {@link GameRenderer}.
 */
public class Game extends JPanel implements ActionListener, KeyListener {

    private static final float VOL = 0.8f;

    private final GamePhysics physics;
    private final GameRenderer renderer;
    private final String playerName;
    private final Timer timer;

    public Game(String theme, int difficulty, String playerName) {
        this.playerName = playerName;
        this.physics = new GamePhysics(difficulty);
        this.renderer = new GameRenderer(theme);

        addKeyListener(this);
        setFocusable(true);
        timer = new Timer(150, this);
        timer.start();
    }

    // ─── Timer tick ───────────────────────────────────────────────────────────

    @Override
    public void actionPerformed(ActionEvent e) {
        GamePhysics.TickResult result = physics.tick();
        timer.setDelay(physics.getTick());   // applies on level-up (no-op otherwise)
        switch (result) {
            case SCORED -> SoundPlayer.play("resources/sound/point.wav",    VOL);
            case HIT    -> SoundPlayer.play("resources/sound/bird-hit.wav", VOL);
            case DEAD   -> gameOver();
            default     -> {}
        }
        repaint();
    }

    // ─── Rendering ────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.render(g, physics, this);
    }

    // ─── Input ────────────────────────────────────────────────────────────────

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if ((key == KeyEvent.VK_SPACE || key == KeyEvent.VK_UP) && !physics.isEndGame()) {
            physics.jump();
            timer.setDelay(physics.getTick());
            SoundPlayer.play("resources/sound/flap.wav", VOL);
        } else if (key == KeyEvent.VK_ESCAPE) {
            pauseGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE)
            physics.setRotationAngle(30);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // ─── Game-control dialogs ─────────────────────────────────────────────────

    private void closeGame() {
        SwingUtilities.getWindowAncestor(this).dispose();
    }

    private void pauseGame() {
        timer.stop();
        int choice = JOptionPane.showOptionDialog(this, "Game Paused", "Pause",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
                renderer.pauseIcon(),
                new Object[] { "Continue", "Restart", "Quit" }, "Continue");
        if (choice == JOptionPane.YES_OPTION) {
            timer.start();
        } else if (choice == JOptionPane.NO_OPTION) {
            restartGame();
        } else {
            closeGame();
            Launcher.restartLauncher();
        }
    }

    private void gameOver() {
        timer.stop();
        int finalScore = physics.computeFinalScore();
        Score.save(playerName, finalScore);

        int choice = JOptionPane.showOptionDialog(this,
                "Score: " + finalScore, "Game Over",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                renderer.deadBirdIcon(),
                new Object[] { "Retry", "Quit" }, "Retry");

        if (choice == JOptionPane.YES_OPTION) {
            restartGame();
        } else {
            closeGame();
            Launcher.restartLauncher();
        }
    }

    private void restartGame() {
        physics.reset();
        timer.setDelay(150);
        timer.start();
    }
}
