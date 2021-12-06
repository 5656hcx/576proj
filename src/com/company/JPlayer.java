package com.company;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

interface onPlaybackStateChangeListener {
    void onPlaybackStateChange(AbstractPlayer.State state);
}

public class JPlayer extends JPanel implements onPlaybackStateChangeListener {

    private final JButton button_play;
    private final JLabel video;
    private final Slider slider;

    private ArrayList<File> videoFrames;
    private final WavePlayer wavePlayer;
    private final VideoPlayer videoPlayer;

    public JPlayer() {
        setLayout(new BorderLayout());

        wavePlayer = new WavePlayer();
        videoPlayer = new VideoPlayer();
        videoPlayer.setPlaybackStateChange(this);

        video = new JLabel("\\(^o^)/", JLabel.CENTER);
        video.setAlignmentX(CENTER_ALIGNMENT);
        add(video, BorderLayout.CENTER);

        JLabel status = new JLabel("Playlist is empty", JLabel.CENTER);
        slider = new Slider(status, "Now playing the %dth frame", videoFrames);
        slider.setCanvas(video);

        JButton button_load = new JButton("load");
        button_load.addActionListener(new FileSelector("Select video directory...", JPlayer.this) {
            @Override
            void onFileSelected(String path) {
                ImageReader reader = ImageReader.getInstance();
                videoFrames = reader.FolderConfig(path);
                if (!videoFrames.isEmpty()) {
                    video.setText(null);
                    video.setIcon(new ImageIcon(reader.BImgFromFile(videoFrames.get(0))));
                    videoPlayer.reset();
                    slider.reset(videoFrames);
                }
            }
        });

        button_play = new JButton("play");
        button_play.addActionListener(e -> {
            // for now, do nothing in Stopped state
            switch (videoPlayer.currentState) {
                case Paused ->
                    // tell playback thread to resume
                    videoPlayer.play();
                case Playing ->
                    // tell playback thread to pause
                    videoPlayer.pause();
            }
        });

        JPanel buttons = new JPanel(new GridLayout(1, 2));
        buttons.add(button_load);
        buttons.add(button_play);

        JPanel widget = new JPanel(new GridLayout(3, 1));
        widget.add(status);
        widget.add(slider);
        widget.add(buttons);

        add(widget, BorderLayout.SOUTH);
    }

    @Override
    public void onPlaybackStateChange(AbstractPlayer.State state) {
        switch (state) {
            case Paused ->
                button_play.setText("resume");
                // System.out.println("Video Playback has paused");
            case Playing ->
                button_play.setText("pause");
                // System.out.println("Video Playback has resumed");
        }
    }

    private class VideoPlayer implements Runnable, AbstractPlayer {
        final Queue<Integer> messageQueue = new ConcurrentLinkedQueue<>();
        onPlaybackStateChangeListener listener;

        volatile State currentState;
        Thread playbackThread;

        public VideoPlayer() {
            currentState = State.Stopped;
        }

        void setPlaybackStateChange(onPlaybackStateChangeListener observer) {
            this.listener = observer;
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
                messageQueue.offer(1);
                synchronized (this) {
                    notify();
                }
                notifyStateChanged();
            }
        }

        @Override
        public void pause() {
            if (currentState != State.Paused) {
                currentState = State.Paused;
                messageQueue.clear();
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
                if (!messageQueue.isEmpty()) {
                    Integer message = messageQueue.poll();
                    if (message == 1) {
                        slider.forward();
                        if (slider.getValue() < slider.getMaximum()) {
                            if (currentState == State.Playing) {
                                messageQueue.offer(1);
                                try {
                                    synchronized (this) {
                                        wait(16);   // hard-coded approximate 60fps
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        else {
                            currentState = State.Paused;
                            notifyStateChanged();
                        }
                    }
                }
                else {
                    System.out.println("Video Playback Thread : waiting");
                    try {
                        synchronized (this) {
                            wait();
                            System.out.println("Video Playback Thread : awaking");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("Video Playback Thread : exits");
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Media Player");
        frame.add(new JPlayer());
        frame.setSize(360, 440);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
