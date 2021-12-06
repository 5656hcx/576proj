package com.company;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class VideoPlayer extends AbstractPlayer implements Runnable {
    private final Queue<Integer> messageQueue = new ConcurrentLinkedQueue<>();
    private final Slider frameManager;

    private Thread playbackThread;

    public VideoPlayer(Slider slider) {
        frameManager = slider;
        currentState = State.Stopped;
    }

    void notifyStateChanged() {
        if (listener != null) {
            listener.onPlaybackStateChange(currentState);
        }
    }

    public void reset() {
        messageQueue.clear();
        if (playbackThread == null) {
            playbackThread = new Thread(this);
            playbackThread.start();
        }
        if (currentState != State.Paused) {
            currentState = State.Paused;
            notifyStateChanged();
        }
    }

    @Override
    public void play() {
        if (currentState != State.Playing) {
            currentState = State.Playing;
            synchronized (messageQueue) {
                messageQueue.offer(16);
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
                    Integer message = messageQueue.poll();
                    frameManager.forward();
                    if (frameManager.getValue() < frameManager.getMaximum()) {
                        if (currentState == State.Playing) {
                            messageQueue.offer(message);
                            try {
                                messageQueue.wait(message);   // hard-coded approximate 60fps
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