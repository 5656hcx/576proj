package com.company;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class VideoPlayer extends AbstractPlayer implements Runnable {
    private final Queue<Integer> messageQueue = new ConcurrentLinkedQueue<>();
    private final Slider frameManager;
    private final JLabel videoCanvas;
    private ArrayList<File> videoFrames;

    private Thread playbackThread;

    public VideoPlayer(Slider slider, JLabel canvas) {
        currentState = State.Stopped;
        videoCanvas = canvas;
        frameManager = slider;
        frameManager.addChangeListener(e -> {
            if (canvas != null) {
                BufferedImage newImage = ImageReader.getInstance().BImgFromFile(videoFrames.get(slider.getValue()));
                canvas.setIcon(new ImageIcon(newImage));
            }
        });
    }

    public void addMessage(int frameDuration) {
        synchronized (messageQueue) {
            messageQueue.offer(frameDuration);
        }
    }

    public void load(ArrayList<File> videoFrames) {
        this.videoFrames = videoFrames;
        frameManager.reset(videoFrames);
    }

    public void reset() {
        synchronized (messageQueue) {
            messageQueue.clear();
        }
        if (currentState != State.Paused) {
            currentState = State.Paused;
            notifyStateChanged();
        }
        if (playbackThread == null) {
            playbackThread = new Thread(this);
            playbackThread.start();
        }
    }

    @Override
    public void play() {
        if (currentState != State.Playing) {
            currentState = State.Playing;
            synchronized (messageQueue) {
                messageQueue.offer(16);     // hard-coded approximately 60fps
                messageQueue.notify();
            }
            notifyStateChanged();
        }
    }

    @Override
    public void pause() {
        if (currentState != State.Paused) {
            currentState = State.Paused;
            synchronized (messageQueue) {
                messageQueue.clear();
            }
            notifyStateChanged();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void run() {
        // for now the thread never quit, since it
        // won't reach Stopped state after execution
        // to DEBUG, call stop() to exit the thread
        while (currentState != State.Stopped) {
            synchronized (messageQueue) {
                if (!messageQueue.isEmpty()) {
                    // message is the waiting time for next frame
                    int frameDuration = messageQueue.poll();
                    frameManager.forward();
                    if (frameManager.getValue() < frameManager.getMaximum()) {
                        if (currentState == State.Playing) {
                            messageQueue.offer(frameDuration);
                            try {
                                messageQueue.wait(frameDuration);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        currentState = State.Paused;
                        notifyStateChanged();
                    }
                } else {
                    System.out.println("Video Playback Thread : waiting");
                    try {
                        messageQueue.wait();
                        System.out.println("Video Playback Thread : awaking");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        System.out.println("Video Playback Thread : exits");
    }
}