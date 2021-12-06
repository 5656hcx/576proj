package com.company;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class JPlayer extends JPanel implements AbstractPlayer.PlaybackStateChangeListener {

    private final JButton button_play;
    private final JLabel video;
    private final Slider slider;

    private ArrayList<File> videoFrames;
    private final WavePlayer audioPlayer;
    private final AbstractPlayer videoPlayer;

    public JPlayer() {
        setLayout(new BorderLayout());

        video = new JLabel("\\(^o^)/", JLabel.CENTER);
        video.setAlignmentX(CENTER_ALIGNMENT);
        add(video, BorderLayout.CENTER);

        JLabel status = new JLabel("Playlist is empty", JLabel.CENTER);
        slider = new Slider(status, "Now playing the %dth frame", videoFrames);
        slider.setCanvas(video);

        audioPlayer = new WavePlayer();
        audioPlayer.setPlaybackStateChange(this);
        videoPlayer = new VideoPlayer(slider);
        videoPlayer.setPlaybackStateChange(this);

        JButton button_load = new JButton("load");
        button_load.addActionListener(new FileSelector("Select video directory...", JPlayer.this) {
            @Override
            void onFileSelected(String path) {
                ImageReader reader = ImageReader.getInstance();
                videoFrames = reader.FolderConfig(path);
                if (!videoFrames.isEmpty()) {
                    audioPlayer.load(path);
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
                case Paused -> {
                    // tell playback thread to resume
                    audioPlayer.play();
                    videoPlayer.play();
                }
                case Playing -> {
                    // tell playback thread to pause
                    audioPlayer.pause();
                    videoPlayer.pause();
                }
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
                button_play.setText("play");
                // System.out.println("Video Playback has paused");
            case Playing ->
                button_play.setText("pause");
                // System.out.println("Video Playback has resumed");
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
