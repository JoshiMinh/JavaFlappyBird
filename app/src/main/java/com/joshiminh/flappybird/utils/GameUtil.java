package com.joshiminh.flappybird.utils;

import java.awt.Rectangle;
import java.util.List;
import java.util.Random;
/** Utility methods shared across game modes. */
public final class GameUtil {
  private GameUtil() {}

  /**
   * Generates a pair of obstacles and adds them to the list.
   *
   * @param obstacles list to add obstacles to
   * @param random source of randomness
   * @param space vertical space between obstacles
   * @param width width of each obstacle
   */
  public static void generateObstacle(
      List<Rectangle> obstacles, Random random, int space, int width) {
    int height = random.nextInt(300) + 50;
    obstacles.add(new Rectangle(800, 0, width, height));
    obstacles.add(new Rectangle(800, height + space, width, 600 - height - space));
  }
}
