package com.company;

public abstract class AbstractPlayer<T>  {

    PlaybackStateChangeListener listener;
    volatile State currentState;

    enum State { Playing, Paused, Stopped }

    abstract void open(T mediaSource);      // Open a media file
    abstract void close();  // Closed any resources
    abstract void play();   // Stopped -> Playing
    abstract void pause();  // Playing -> Paused
    abstract void stop();   //       * -> Stopped
    abstract void reset();  // use this function to restore runtime variables
    abstract void peek(long frameIndex);   // relocate current frame to frameIndex

    final void setPlaybackStateChange(PlaybackStateChangeListener listener) {
        this.listener = listener;
    }

    final void notifyStateChanged() {
        if (listener != null) {
            listener.onPlaybackStateChange(currentState);
        }
    }

    public interface PlaybackStateChangeListener {
        void onPlaybackStateChange(AbstractPlayer.State state);
    }
}
