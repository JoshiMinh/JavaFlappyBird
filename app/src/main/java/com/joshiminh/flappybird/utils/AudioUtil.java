package com.joshiminh.flappybird.utils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/** Utility class for audio-related helpers. */
public final class AudioUtil {
  private AudioUtil() {}

  /**
   * Plays a sound from the given resource path at the specified volume.
   *
   * @param path path to the sound resource
   * @param volume volume level between 0 and 1
   */
  public static void playSound(String path, float volume) {
    try (AudioInputStream audioInputStream =
             AudioSystem.getAudioInputStream(ResourceUtil.getResource(path))) {
      Clip clip = AudioSystem.getClip();
      clip.open(audioInputStream);
      if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float gain = gainControl.getMinimum()
            + (gainControl.getMaximum() - gainControl.getMinimum()) * volume;
        gainControl.setValue(gain);
      }
      clip.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
