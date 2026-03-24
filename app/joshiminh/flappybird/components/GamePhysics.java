package joshiminh.flappybird.components;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Owns all mutable game state and advances the simulation by one frame.
 * No Swing dependency — pure logic only.
 */
public class GamePhysics {

    public enum TickResult { NORMAL, SCORED, HIT, DEAD }

    // Panel constants
    public static final int BIRD_X  = 370;
    public static final int PANEL_W = 800;
    public static final int PANEL_H = 600;
    public static final int GROUND_Y = 475;

    // Difficulty table: {timerDelay, pipeGap, obstacleDistance, speed, gravity}
    private static final int[][] DIFFICULTY = {
            { 20, 220, 580, 3, 1 },
            { 16, 190, 530, 4, 1 },
            { 12, 150, 470, 6, 1 },
            {  8, 110, 110, 8, 2 }
    };

    // Difficulty-driven params
    private int tick, space, distance, velocity, gravity;
    private int diff, defaultDiff, prevScore;

    // Bird state
    private int birdY = 225, birdVelocity, rotationAngle;
    private boolean down = true;

    // Game flags / score
    private float score;
    private boolean startGame, endGame;

    private final List<Rectangle> obstacles = new ArrayList<>();
    private final Random random = new Random();

    public GamePhysics(int difficulty) {
        this.diff = difficulty;
        this.defaultDiff = difficulty;
        setDifficulty(difficulty);
        generateObstacle();
    }

    // ─── Main tick ───────────────────────────────────────────────────────────

    public TickResult tick() {
        if (endGame) {
            if (startGame) {
                birdVelocity += gravity;
                birdY += birdVelocity;
                if (birdY > GROUND_Y)
                    return TickResult.DEAD;
            }
            pruneAndRefill();
            return TickResult.NORMAL;
        }

        if (startGame) {
            birdVelocity += gravity;
            birdY += birdVelocity;
            obstacles.forEach(o -> o.x -= velocity);

            TickResult result = TickResult.NORMAL;
            Rectangle bird = new Rectangle(BIRD_X, birdY, 50, 40);
            for (Rectangle obs : obstacles) {
                if (obs.intersects(bird) || birdY < 0 || birdY > GROUND_Y) {
                    endGame = true;
                    return TickResult.HIT;
                } else if (BIRD_X > obs.x && BIRD_X < obs.x + velocity) {
                    score += 0.5f;
                    result = TickResult.SCORED;
                }
            }
            pruneAndRefill();
            levelUp();
            return result;
        }

        // Idle bob before first jump
        birdY += down ? 10 : -10;
        down = !down;
        return TickResult.NORMAL;
    }

    /** Called when the player presses Space / Up. */
    public void jump() {
        startGame = true;
        birdVelocity = -13;
        rotationAngle = -20;
    }

    /** Fully resets state for a new game. */
    public void reset() {
        score = 0;
        prevScore = 0;
        birdY = 225;
        birdVelocity = 0;
        rotationAngle = 0;
        startGame = false;
        endGame = false;
        down = true;
        diff = defaultDiff;
        setDifficulty(defaultDiff);
        obstacles.clear();
        generateObstacle();
    }

    // ─── Internal helpers ─────────────────────────────────────────────────────

    private void setDifficulty(int d) {
        if (d < 0 || d >= DIFFICULTY.length)
            return;
        int[] s = DIFFICULTY[d];
        tick = s[0]; space = s[1]; distance = s[2]; velocity = s[3]; gravity = s[4];
    }

    private void levelUp() {
        if (getScoreInt() >= prevScore + 4 * (diff + 1) && diff < 3) {
            prevScore = getScoreInt();
            setDifficulty(++diff);
        }
    }

    private void generateObstacle() {
        int w = 55, h = random.nextInt(300) + 50;
        obstacles.add(new Rectangle(PANEL_W, 0, w, h));
        obstacles.add(new Rectangle(PANEL_W, h + space, w, PANEL_H - h - space));
    }

    private void pruneAndRefill() {
        obstacles.removeIf(o -> o.x + o.width < 0);
        if (!obstacles.isEmpty() && obstacles.get(obstacles.size() - 1).x < distance)
            generateObstacle();
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public int getBirdY()           { return birdY; }
    public int getRotationAngle()   { return rotationAngle; }
    public void setRotationAngle(int a) { rotationAngle = a; }
    public int getScoreInt()        { return (int) score; }
    public boolean isStartGame()    { return startGame; }
    public boolean isEndGame()      { return endGame; }
    public List<Rectangle> getObstacles() { return obstacles; }
    public int getSpace()           { return space; }
    public int getTick()            { return tick; }

    public int computeFinalScore() {
        return (int) (score * (diff > 0 ? Math.pow(diff, diff - 1) : 1));
    }
}