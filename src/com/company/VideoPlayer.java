package com.company;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

class VideoPlayer extends AbstractPlayer<ArrayList<File>> implements Runnable, ChangeListener {
    private final Queue<Integer> messageQueue = new ConcurrentLinkedQueue<>();
    private final Slider slider;
    private final JLabel canvas;
    private ArrayList<File> videoFrames;

    long lastSyncTime = -1;             // time of current frame rendered on screen
    long elapsedTimeActual = 0;         // actual elapsed time of the movie, depends on runtime environment
    int elapsedTimeStandard = 0;        // standard elapsed time of movie, calculated by pre-defined FPS
    int elapsedTimeReference = 0;       // target display time for every frame, regardless of I/O time or something
    int frameDurationOffset = 0;    // average offset between ideal and actual time to display a frame in target FPS

    /* DEBUG ONLY PARAMETERS */
    int fakeFrameIndex = 0;
    java.util.Timer timer = new Timer();

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
            // new implementation, for ordered playback only
            timer.schedule(new task(), 0);

            // old implementation, sync not supported
            // playbackThread = new Thread(this);
            // playbackThread.start();
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

    private class task extends TimerTask {

        @Override
        public void run() {
            int standard = getFrameDurationBiased(fakeFrameIndex); // i为即将展示帧的序号（当前帧）
            int cali = 0;
            if (lastSyncTime > 0) {
                BufferedImage newImage = ImageReader.getInstance().BImgFromFile(videoFrames.get(fakeFrameIndex));
                canvas.setIcon(new ImageIcon(newImage));
                long now = System.currentTimeMillis();
                long actual = now - lastSyncTime;   // 上一帧的实际展示时长
                lastSyncTime = now;

                elapsedTimeActual += actual;
                elapsedTimeReference += standard;
                elapsedTimeStandard += getFrameDurationStandard(fakeFrameIndex);
                fakeFrameIndex += 1;                         // i变成下一帧的序号
                frameDurationOffset = (int)(elapsedTimeActual - elapsedTimeReference) / fakeFrameIndex; // 实际值与睡眠值的差值，由此可见系统的响应速率
                cali = (int)(elapsedTimeActual - elapsedTimeStandard);       // 实际值与标准值的差值，由此可见视频播放的相对快慢

                if (standard - cali < 0) {
                    // 上一帧展示完后，延迟已经超过一帧，丢弃当前帧。（相当于当前帧已经完整展示一个标准周期）
                    // 为下一帧重置参数
                    cali = standard;
                    System.out.printf("DELAYED! SKIP CURRENT FRAME: %d\n", fakeFrameIndex);
                }
                else System.out.printf("Average FPS: %f, Accumulate Diff: %dms\n", (float) elapsedTimeActual / fakeFrameIndex, cali);
            }
            else lastSyncTime = System.currentTimeMillis();
            timer.schedule(new task(), standard - cali);
        }
    }

    private int getFrameDurationBiased(int frameIndex) {
        return ((frameIndex % 3 == 1) ? 34 : 33) - frameDurationOffset;
    }

    private int getFrameDurationStandard(int frameIndex) {
        return (frameIndex % 3 == 1) ? 34 : 33;
    }
}