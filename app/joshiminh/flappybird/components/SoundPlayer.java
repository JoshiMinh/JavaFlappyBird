package joshiminh.flappybird.components;

import java.io.File;
import javax.sound.sampled.*;

/** Fire-and-forget sound utility. Each call runs on a daemon thread. */
public final class SoundPlayer {

    private SoundPlayer() {}

    public static void play(String path, float volume) {
        Thread t = new Thread(() -> {
            try (AudioInputStream audio = AudioSystem.getAudioInputStream(new File(path))) {
                Clip clip = AudioSystem.getClip();
                clip.open(audio);
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    gc.setValue(gc.getMinimum() + (gc.getMaximum() - gc.getMinimum()) * volume);
                }
                clip.start();
                clip.drain();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }
}