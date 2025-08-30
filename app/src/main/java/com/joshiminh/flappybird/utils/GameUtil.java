package com.joshiminh.flappybird.utils;

import java.awt.Rectangle;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Utility methods shared across game modes.
 */
public final class GameUtil {

    private GameUtil() {}

    /**
     * Plays a sound from the given resource path at the specified volume.
     *
     * @param soundFilePath path to the sound resource
     * @param volume        volume between 0 and 1
     */
    public static void playSound(String soundFilePath, float volume) {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(ResourceUtil.getResource(soundFilePath))) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float gain = gainControl.getMinimum() + (gainControl.getMaximum() - gainControl.getMinimum()) * volume;
                gainControl.setValue(gain);
            }
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a pair of obstacles and adds them to the list.
     *
     * @param obstacles list to add obstacles to
     * @param random    source of randomness
     * @param space     vertical space between obstacles
     * @param width     width of each obstacle
     */
    public static void generateObstacle(List<Rectangle> obstacles, Random random, int space, int width) {
        int height = random.nextInt(300) + 50;
        obstacles.add(new Rectangle(800, 0, width, height));
        obstacles.add(new Rectangle(800, height + space, width, 600 - height - space));
    }
}
