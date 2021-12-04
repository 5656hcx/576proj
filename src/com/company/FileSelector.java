package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class FileSelector implements ActionListener {

    private final String title;
    private final Component parent;

    FileSelector(String title, Component parent) {
        this.title = title;
        this.parent = parent;
    }

    abstract void onFileSelected(String path);

    @Override
    public final void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle(title);
        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(parent)) {
            onFileSelected(fileChooser.getSelectedFile().getPath());
        }
    }

}
