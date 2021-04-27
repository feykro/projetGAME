package utils;

import javax.sound.sampled.*;
import java.io.*;

/**
 * Manage sound
 */
public class NotificationSound {

    private AudioFormat format;
    private byte[] samples;

    private AudioInputStream sound_force;
    private AudioInputStream sound_new_message;


    private byte[] getSamples() {
        return samples;
    }

    private byte[] getSamples(AudioInputStream stream) {
        int length = (int) (stream.getFrameLength() * format.getFrameSize());
        byte[] samples = new byte[length];
        DataInputStream in = new DataInputStream(stream);
        try {
            in.readFully(samples);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return samples;
    }

    /**
     * play fail notification
     */
    public void notification_fail() {
        try {
            sound_force = AudioSystem.getAudioInputStream(new File("resources/sound/BONK.wav"));
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
        format = sound_force.getFormat();
        samples = getSamples(sound_force);
        play();
    }

    /**
     * play new message notification
     */
    public void notification_new_message() {
        try {
            sound_new_message = AudioSystem.getAudioInputStream(new File("resources/sound/new_message.wav"));
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
        format = sound_new_message.getFormat();
        samples = getSamples(sound_new_message);
        play();
    }

    /**
     * play the current sample
     */
    private void play() {
        InputStream source = new ByteArrayInputStream(getSamples());
        // 100 ms buffer for real time change to the sound stream
        new Thread(() -> {
            int bufferSize = format.getFrameSize() * Math.round(format.getSampleRate() / 10);
            byte[] buffer = new byte[bufferSize];
            SourceDataLine line;
            try {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, bufferSize);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
                return;
            }
            line.start();
            try {
                int numBytesRead = 0;
                while (numBytesRead != -1) {
                    numBytesRead = source.read(buffer, 0, buffer.length);
                    if (numBytesRead != -1)
                        line.write(buffer, 0, numBytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            line.drain();
            line.close();
        }).start();
    }
}
