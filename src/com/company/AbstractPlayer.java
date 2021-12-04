package com.company;

public interface AbstractPlayer {

    enum State { Playing, Paused, Stopped }

    void play();    // Stopped -> Playing
    void pause();   // Playing -> Paused
    void stop();    //       * -> Stopped
}
