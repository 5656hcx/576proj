package com.company;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class VideoPlayer extends AbstractPlayer<ArrayList<File>> implements Runnable, ChangeListener {
    private final Queue<Integer> messageQueue = new ConcurrentLinkedQueue<>();
    private final Slider slider;
    private final JLabel canvas;
    private ArrayList<File> videoFrames;

    private Thread playbackThread;

    public VideoPlayer(Slider slider, JLabel canvas) {
        currentState = State.Stopped;
        this.canvas = canvas;
        this.slider = slider;
        this.slider.addChangeListener(this);
    }

    private void setAndNotifyStateChanged(State newState) {
        if (currentState != newState) {
            currentState = newState;
            notifyStateChanged();
        }
    }

    @Override
    void open(ArrayList<File> mediaSource) {
        videoFrames = mediaSource;
        slider.reset(videoFrames);
    }

    @Override
    void close() {

    }

    @Override
    public void reset() {
        synchronized (messageQueue) {
            messageQueue.clear();
        }
        setAndNotifyStateChanged(State.Paused);
        if (playbackThread == null) {
            playbackThread = new Thread(this);
            playbackThread.start();
        }
    }

    @Override
    protected void peek(long frameIndex) {
        if (canvas != null) {
            BufferedImage newImage = ImageReader.getInstance().BImgFromFile(videoFrames.get((int) frameIndex));
            canvas.setIcon(new ImageIcon(newImage));
        }
        if (slider != null && slider.getValue() != frameIndex) {
            slider.setValue((int) frameIndex);
        }
    }

    @Override
    public void play() {
        if (currentState != State.Playing) {
            setAndNotifyStateChanged(State.Playing);
            synchronized (messageQueue) {
                messageQueue.offer(30);
                messageQueue.notify();
            }
        }
    }

    @Override
    public void pause() {
        if (currentState != State.Paused) {
            setAndNotifyStateChanged(State.Paused);
            synchronized (messageQueue) {
                messageQueue.clear();
            }
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
                    if (slider.getValue() < slider.getMaximum()) {
                        if (currentState == State.Playing) {
                            try {
                                messageQueue.wait(frameDuration);
                                slider.forward();
                                if (currentState == State.Playing) {
                                    messageQueue.offer(frameDuration);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else setAndNotifyStateChanged(State.Paused);
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

    @Override
    public void stateChanged(ChangeEvent e) {
        if (canvas != null) {
            BufferedImage newImage = ImageReader.getInstance().BImgFromFile(videoFrames.get(slider.getValue()));
            canvas.setIcon(new ImageIcon(newImage));
        }
    }
}