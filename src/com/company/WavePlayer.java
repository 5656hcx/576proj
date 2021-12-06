package com.company;

import javax.sound.sampled.*;
import java.io.*;

public class WavePlayer extends AbstractPlayer implements Runnable {
    private BufferedInputStream inputStream;
    private AudioInputStream audioInputStream;

    private final int EXTERNAL_BUFFER_SIZE = 524288;
    private boolean isReloaded = false;

    private Thread playbackThread;
    private SourceDataLine dataLine;

    public WavePlayer() {
        currentState = State.Stopped;
    }

    public void load(String filename) {
        isReloaded = true;
        ImageReader reader = ImageReader.getInstance();
        inputStream = reader.BWavFromFile(filename);

        try {
            audioInputStream = AudioSystem.getAudioInputStream(inputStream);
            if (dataLine != null) {
                dataLine.flush();
            }
            else {
                AudioFormat audioFormat = audioInputStream.getFormat();
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
                dataLine = (SourceDataLine) AudioSystem.getLine(info);
                dataLine.open(audioFormat, EXTERNAL_BUFFER_SIZE);
            }
        }
        catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
            currentState = State.Stopped;
            return;
        }

        if (currentState != State.Paused) {
            currentState = State.Paused;
        }
        if (playbackThread == null) {
            playbackThread = new Thread(this);
            playbackThread.start();
        }
    }

    @Override
    public void run() {
        // for now the audio playback thread never quit
        while (currentState != State.Stopped) {
            try {
                byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
                int readBytes = audioInputStream.read(audioBuffer, 0, EXTERNAL_BUFFER_SIZE);
                if (readBytes >= 0) {
                    int writeBytes = 0;
                    while (writeBytes < EXTERNAL_BUFFER_SIZE) {
                        synchronized (this) {
                            if (currentState == State.Paused) {
                                notifyStateChanged();
                                this.wait();
                                dataLine.start();
                                currentState = State.Playing;
                                notifyStateChanged();
                            }
                        }
                        writeBytes += dataLine.write(audioBuffer, writeBytes, EXTERNAL_BUFFER_SIZE - writeBytes);
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (dataLine != null) {
            dataLine.drain();
            dataLine.close();
        }
        System.out.println("Audio Playback Thread : exits");
    }

    @Override
    public void play() {
        if (currentState != State.Playing) {
            synchronized (this) {
                this.notify();
            }
        }
    }

    @Override
    public void pause() {
        if (currentState != State.Paused) {
            currentState = State.Paused;
            dataLine.stop();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    void reset() {

    }
}
