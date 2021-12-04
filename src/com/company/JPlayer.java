package com.company;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class JPlayer extends JPanel {

    private final JButton button_play;
    private final JLabel video;
    private final Slider slider;

    private boolean isLoaded;
    private ArrayList<File> videoFrames;
    private WavePlayer wavePlayer;
    private VideoPlayer videoPlayer;

    public JPlayer() {
        setLayout(new BorderLayout());

        video = new JLabel("\\(^o^)/", JLabel.CENTER);
        video.setAlignmentX(CENTER_ALIGNMENT);
        add(video, BorderLayout.CENTER);

        JButton button_load = new JButton("load");
        button_load.addActionListener(new FileSelector("Select video directory...", JPlayer.this) {
            @Override
            void onFileSelected(String path) {
                ImageReader reader = ImageReader.getInstance();
                videoFrames = reader.FolderConfig(path);
                if (!videoFrames.isEmpty()) {
                    slider.reset(videoFrames);
                    video.setText(null);
                    video.setIcon(new ImageIcon(reader.BImgFromFile(videoFrames.get(0))));
                }
            }
        });

        button_play = new JButton("play");
        button_play.addActionListener(e -> {
            Thread thread = new Thread(new VideoPlayer());
            thread.start();
        });

        JLabel status = new JLabel("Playlist is empty", JLabel.CENTER);
        slider = new Slider(status, "Now playing the %dth frame", videoFrames);
        slider.setCanvas(video);

        JPanel buttons = new JPanel(new GridLayout(1, 2));
        buttons.add(button_load);
        buttons.add(button_play);

        JPanel widget = new JPanel(new GridLayout(3, 1));
        widget.add(status);
        widget.add(slider);
        widget.add(buttons);

        add(widget, BorderLayout.SOUTH);

        wavePlayer = new WavePlayer();
        videoPlayer = new VideoPlayer();
    }

    private class VideoPlayer implements Runnable, AbstractPlayer {

        State currentState;
        Queue<Integer> messageQueue = new LinkedList<Integer>();

        public VideoPlayer() {
            currentState = State.Stopped;
        }

        @Override
        public void play() {
            messageQueue.offer(1);
        }

        @Override
        public void pause() {
            messageQueue.offer(0);
        }

        @Override
        public void stop() {

        }

        @Override
        public void run() {
            messageQueue.offer(1);
            while (!messageQueue.isEmpty()) {
                Integer message = messageQueue.poll();
                if (message == 1) {
                    slider.forward();
                    if (slider.getValue() < slider.getMaximum()) {
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
            }
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
