package com.company;

import javax.sound.sampled.*;
import java.io.*;

public class WavePlayer extends AbstractPlayer {
    private BufferedInputStream inputStream;
    private Clip audioClip;

    private int videoFrameCount = 1; // for DEBUG Only

    public WavePlayer(Slider slider) {
        currentState = State.Stopped;
        slider.setManualChangeListener(() -> peek(slider.getValue()));
    }

    @Deprecated
    public void setVideoFrameCount(int frameCount) {
        videoFrameCount = frameCount;
    }

    public void open(String filename) {
        ImageReader reader = ImageReader.getInstance();
        inputStream = reader.BWavFromFile(filename);
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
            currentState = State.Stopped;
            e.printStackTrace();
            return;
        }

        if (currentState != State.Paused) {
            currentState = State.Paused;
        }
    }

    @Override
    void peek(long frameIndex) {
        System.out.println("PEEK!");
        int offset = audioClip.getFrameLength() / videoFrameCount;
        audioClip.setFramePosition(((int)frameIndex - 1) * offset);    // hard-coded approximately 30fps
    }
}
