package com.company;

public abstract class AbstractPlayer {

    PlaybackStateChangeListener listener;
    volatile State currentState;

    enum State { Playing, Paused, Stopped }

    abstract void play();   // Stopped -> Playing
    abstract void pause();  // Playing -> Paused
    abstract void stop();   //       * -> Stopped
    abstract void reset();  // use this function to restore runtime variables

    final void setPlaybackStateChange(PlaybackStateChangeListener listener) {
        this.listener = listener;
    }

    public interface PlaybackStateChangeListener {
        void onPlaybackStateChange(AbstractPlayer.State state);
    }
}
