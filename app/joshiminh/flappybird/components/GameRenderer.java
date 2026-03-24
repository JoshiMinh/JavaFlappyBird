package joshiminh.flappybird.components;

import javax.swing.*;
import java.awt.*;

/**
 * Loads theme assets once and renders a complete game frame onto any Graphics context.
 * No mutable state — purely a drawing service.
 */
public class GameRenderer {

    private final ImageIcon background, bird, upperPipe, lowerPipe, base;
    private final ImageIcon deadBirdSrc, pauseIconSrc;

    public GameRenderer(String theme) {
        String p = "resources/themes/" + theme + "/";
        bird        = scaled(new ImageIcon(p + "bird.png"),       50,  50);
        background  = scaled(new ImageIcon(p + "background.png"), 800, 600);
        base        = scaled(new ImageIcon(p + "base.png"),       800, 100);
        upperPipe   = new ImageIcon(p + "obsdown.png");
        lowerPipe   = new ImageIcon(p + "obs.png");
        deadBirdSrc = new ImageIcon(p + "dead_bird.png");
        pauseIconSrc = new ImageIcon("resources/images/pause.png");
    }

    // ─── Public icons (used by dialog helpers in Game.java) ───────────────────

    public ImageIcon deadBirdIcon() { return scaled(deadBirdSrc, 45, 45); }
    public ImageIcon pauseIcon()    { return scaled(pauseIconSrc, 30, 30); }

    // ─── Main render ──────────────────────────────────────────────────────────

    public void render(Graphics g, GamePhysics p, JComponent host) {
        background.paintIcon(host, g, 0, 0);
        drawPipes(g, p, host);
        base.paintIcon(host, g, 0, 520);
        drawBird(g, p, host);
        drawScore(g, p.getScoreInt(), host.getWidth());
        if (!p.isEndGame() && !p.isStartGame())
            drawStartHint(g, host);
    }

    // ─── Private drawing helpers ──────────────────────────────────────────────

    private void drawPipes(Graphics g, GamePhysics p, JComponent host) {
        int gap = p.getSpace();
        for (Rectangle obs : p.getObstacles()) {
            int lowerH = GamePhysics.PANEL_H - obs.height - gap;
            g.drawImage(lowerPipe.getImage(), obs.x, obs.y + obs.height + gap, obs.width, lowerH, host);
            g.drawImage(upperPipe.getImage(), obs.x, obs.y, obs.width, obs.height, host);
        }
    }

    private void drawBird(Graphics g, GamePhysics p, JComponent host) {
        int bx = GamePhysics.BIRD_X, by = p.getBirdY();
        Graphics2D g2 = (Graphics2D) g;
        double angle = Math.toRadians(p.getRotationAngle());
        g2.rotate(angle, bx + 20, by + 22);
        bird.paintIcon(host, g2, bx, by);
        g2.rotate(-angle, bx + 20, by + 22);
    }

    private void drawScore(Graphics g, int score, int panelWidth) {
        String text = Integer.toString(score);
        g.setFont(new Font("Arial", Font.PLAIN, 60));
        FontMetrics fm = g.getFontMetrics();
        int x = (panelWidth - fm.stringWidth(text)) / 2;
        int y = 100, sw = 2;
        ((Graphics2D) g).setStroke(new BasicStroke(sw));
        g.setColor(Color.BLACK);
        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                g.drawString(text, x + i * sw, y + j * sw);
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
    }

    private void drawStartHint(Graphics g, JComponent host) {
        ImageIcon start = scaled(new ImageIcon("resources/images/start.png"), 150, 60);
        start.paintIcon(host, g, GamePhysics.BIRD_X - 55, 400);
    }

    // ─── Static helper ────────────────────────────────────────────────────────

    public static ImageIcon scaled(ImageIcon src, int w, int h) {
        return new ImageIcon(src.getImage().getScaledInstance(w, h, Image.SCALE_DEFAULT));
    }
}