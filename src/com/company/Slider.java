package com.company;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

class Slider extends JSlider {

    private final JLabel status;
    private final String format;

    Slider(JLabel status, String format) {
        super();
        this.status = status;
        this.format = format;
        reset(null);
    }

    @Override
    public void setValue(int n) {
        super.setValue(n);
        status.setText(String.format(format, getValue() + 1));
    }

    public void forward() {
        if (isEnabled() && getMaximum() > getValue()) {
            setValue(getValue() + 1);
        }
    }

    public void back() {
        if (isEnabled() && getMinimum() < getValue()) {
            setValue(getValue() - 1);
        }
    }

    public void reset(ArrayList<File> video) {
        if (video == null || video.isEmpty()) {
            setMaximum(0);
            setEnabled(false);
        }
        else {
            setMaximum(video.size() - 1);
            setEnabled(true);
            setValue(0);
        }
        setMinimum(0);
        setPaintTicks(true);
        setPaintLabels(true);
    }
}