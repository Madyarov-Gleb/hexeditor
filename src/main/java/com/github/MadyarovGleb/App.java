package com.github.MadyarovGleb;

import com.github.MadyarovGleb.controller.HexEditorController;
import com.github.MadyarovGleb.model.ByteBufferFileModel;
import com.github.MadyarovGleb.view.HexEditorPanel;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;

public class App extends JFrame {
    private HexEditorController controller;
    private HexEditorPanel editorPanel;
    private JMenuItem saveMenuItem;

    public App() {
        initUI();
        setTitle("HEX Editor");
        setSize(1000, 700);
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
        if (!confirmUnsavedChanges("exit")) {
            return;
        }

        if (controller != null) {
            try {
                controller.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        dispose();
    }

    private boolean confirmUnsavedChanges(String action) {
        if (controller != null && controller.hasUnsavedChanges()) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Save changes before " + action + "?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION
            );

            if (option == JOptionPane.YES_OPTION) {
                try {
                    controller.handleSave();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error saving file: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return false;
            }
        }
        return true;
    }

    private void initUI() {
        editorPanel = new HexEditorPanel();
        add(editorPanel);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem(new AbstractAction("Open") {
            public void actionPerformed(ActionEvent e) { openFile(); }
        }));

        saveMenuItem = new JMenuItem(new AbstractAction("Save") {
            public void actionPerformed(ActionEvent e) { saveFile(); }
        });
        saveMenuItem.setEnabled(false);
        fileMenu.add(saveMenuItem);

        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        editMenu.add(new JMenuItem(new AbstractAction("Copy") {
            public void actionPerformed(ActionEvent e) { editorPanel.copySelection(); }
        }));
        editMenu.add(new JMenuItem(new AbstractAction("Cut (Zero Fill)") {
            public void actionPerformed(ActionEvent e) { editorPanel.cutSelection(true); }
        }));
        editMenu.add(new JMenuItem(new AbstractAction("Cut (Shift Left)") {
            public void actionPerformed(ActionEvent e) { editorPanel.cutSelection(false); }
        }));
        editMenu.add(new JMenuItem(new AbstractAction("Paste (Overwrite)") {
            public void actionPerformed(ActionEvent e) { editorPanel.pasteClipboard(true); }
        }));
        editMenu.add(new JMenuItem(new AbstractAction("Paste (Insert)") {
            public void actionPerformed(ActionEvent e) { editorPanel.pasteClipboard(false); }
        }));
        editMenu.addSeparator();
        editMenu.add(new JMenuItem(new AbstractAction("Insert Bytes") {
            public void actionPerformed(ActionEvent e) { editorPanel.insertBytesDialog(); }
        }));
        editMenu.add(new JMenuItem(new AbstractAction("Delete Selected Bytes") {
            public void actionPerformed(ActionEvent e) { editorPanel.showDeleteDialog(); }
        }));
        menuBar.add(editMenu);

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

        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.add(new JMenuItem(new AbstractAction("Search") {
            public void actionPerformed(ActionEvent e) {
                editorPanel.showSearchDialog();
            }
        }));
        toolsMenu.add(new JMenuItem(new AbstractAction("Clear Highlight") {
            public void actionPerformed(ActionEvent e) {
                editorPanel.clearHighlight();
            }
        }));
        menuBar.add(toolsMenu);

        setJMenuBar(menuBar);
    }

    private void openFile() {
        if (!confirmUnsavedChanges("opening a new file")) {
            return;
        }

        if (controller != null) {
            try {
                controller.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            controller = null;
            updateSaveButtonState();
        }

        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path path = fc.getSelectedFile().toPath();
                controller = new HexEditorController(new ByteBufferFileModel(path));
                editorPanel.setModel(controller.getFileModel(), controller.getSelectionModel());
                setTitle("HEX Editor - " + path.getFileName());
                updateSaveButtonState();
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

    private void updateSaveButtonState() {
        saveMenuItem.setEnabled(controller != null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}
