package com.github.MadyarovGleb;

import com.github.MadyarovGleb.controller.HexEditorController;
import com.github.MadyarovGleb.model.ByteBufferFileModel;
import com.github.MadyarovGleb.view.HexEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;

public class App extends JFrame {
    private HexEditorController controller;
    private HexEditorPanel editorPanel;

    public App() {
        initUI();
        setTitle("HEX Editor");
        setSize(800, 600);
        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmAndExit();
            }
        });
    }

    private void confirmAndExit() {
        if (controller != null && controller.hasUnsavedChanges()) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Save changes before exiting?",
                    "Exit",
                    JOptionPane.YES_NO_CANCEL_OPTION
            );

            if (option == JOptionPane.YES_OPTION) {
                try {
                    controller.handleSave();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error saving file: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if (option == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }

        if (controller != null) {
            try {
                controller.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        dispose();
        System.exit(0);
    }

    private void initUI() {
        editorPanel = new HexEditorPanel();
        add(editorPanel);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        fileMenu.add(new JMenuItem(new AbstractAction("Open") {
            public void actionPerformed(ActionEvent e) { openFile(); }
        }));

        fileMenu.add(new JMenuItem(new AbstractAction("Save") {
            public void actionPerformed(ActionEvent e) { saveFile(); }
        }));

        menuBar.add(fileMenu);

        JMenu viewMenu = new JMenu("View");

        JMenuItem viewByteItem = new JMenuItem("View as byte");
        viewByteItem.addActionListener(e -> editorPanel.showSelectedValue(1));
        viewMenu.add(viewByteItem);

        JMenuItem viewShortItem = new JMenuItem("View as 2 bytes (short)");
        viewShortItem.addActionListener(e -> editorPanel.showSelectedValue(2));
        viewMenu.add(viewShortItem);

        JMenuItem viewIntItem = new JMenuItem("View as 4 bytes (int/float)");
        viewIntItem.addActionListener(e -> editorPanel.showSelectedValue(4));
        viewMenu.add(viewIntItem);

        JMenuItem viewLongItem = new JMenuItem("View as 8 bytes (long/double)");
        viewLongItem.addActionListener(e -> editorPanel.showSelectedValue(8));
        viewMenu.add(viewLongItem);

        menuBar.add(viewMenu);

        setJMenuBar(menuBar);
    }

    private void openFile() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path path = fc.getSelectedFile().toPath();
                controller = new HexEditorController(new ByteBufferFileModel(path));
                editorPanel.setModel(controller.getFileModel(), controller.getSelectionModel());
                setTitle("HEX Editor - " + path.getFileName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + ex.getMessage());
            }
        }
    }

    private void saveFile() {
        if (controller != null) {
            try {
                controller.handleSave();
                JOptionPane.showMessageDialog(this, "File saved successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}
