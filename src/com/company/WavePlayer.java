package com.company;

import org.wikijava.sound.playWave.PlaySound;
import org.wikijava.sound.playWave.PlayWaveException;

import java.io.*;

public class WavePlayer extends AbstractPlayer implements Runnable {

    private InputStream waveStream;
    private final int EXTERNAL_BUFFER_SIZE = 524288;

    public WavePlayer() {

    }

    public void load(String filename) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(filename);
            PlaySound playSound = new PlaySound(inputStream);
            playSound.play();
        } catch (FileNotFoundException | PlayWaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        load("/Users/5656hcx/Documents/CSCI 576/Project/USC/USCOne/USCOne.wav");
    }

    @Override
    public void play() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    void reset() {

    }
}
