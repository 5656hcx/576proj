package com.company;

import javax.sound.sampled.*;
import java.io.*;

public class WavePlayer extends AbstractPlayer<String> {
    private BufferedInputStream inputStream;
    private Clip audioClip;

    private int frameOffsetMicros = 0;

    public WavePlayer(Slider slider) {
        slider.setManualChangeListener(() -> peek(slider.getValue()));
    }

    public void setVideoFrameLength(int frameLength) {
        if (audioClip != null && frameLength > 0) {
            System.out.println(audioClip.getMicrosecondLength());
            frameOffsetMicros = (int) (audioClip.getMicrosecondLength() / frameLength);
            while ((long) frameOffsetMicros * frameLength < audioClip.getMicrosecondLength()) {
                frameOffsetMicros += 1;
            }
        }
    }

    public void open(String mediaSource) {
        ImageReader reader = ImageReader.getInstance();
        inputStream = reader.BWavFromFile(mediaSource);
        reset();
    }

    public void close() {
        if (audioClip != null && audioClip.isOpen()) {
            audioClip.stop();
            audioClip.close();
        }
    }

    @Override
    public void play() {
        audioClip.start();
    }

    @Override
    public void pause() {
        audioClip.stop();
    }

    @Override
    public void stop() {

    }

    @Override
    void reset() {
        try {
            close();
            audioClip = AudioSystem.getClip();
            audioClip.open(AudioSystem.getAudioInputStream(inputStream));
        }
        catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    void peek(long frameIndex) {
        if (audioClip != null) {
            audioClip.setMicrosecondPosition(frameIndex * frameOffsetMicros);
        }
    }
}
