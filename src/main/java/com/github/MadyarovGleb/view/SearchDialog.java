package com.github.MadyarovGleb.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class SearchDialog extends JDialog {
    private JTextField searchField;
    private JTextField maskField;
    private JCheckBox caseSensitiveCheck;
    private JButton searchButton;
    private JButton cancelButton;

    private byte[] searchPattern;
    private byte[] maskPattern;
    private boolean cancelled = true;

    public SearchDialog(Frame owner) {
        super(owner, "Search", true);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));
        setSize(400, 200);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        inputPanel.add(new JLabel("Search for:"));
        searchField = new JTextField();
        inputPanel.add(searchField);

        inputPanel.add(new JLabel("Mask (optional):"));
        maskField = new JTextField();
        inputPanel.add(maskField);

        caseSensitiveCheck = new JCheckBox("Case sensitive");
        inputPanel.add(caseSensitiveCheck);

        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchButton = new JButton("Search");
        cancelButton = new JButton("Cancel");

        searchButton.addActionListener(this::onSearch);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(searchButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(searchButton);
    }

    private void onSearch(ActionEvent e) {
        String searchText = searchField.getText();
        String maskText = maskField.getText();

        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter search pattern",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            searchPattern = searchText.getBytes();
            if (!maskText.isEmpty()) {
                maskPattern = maskText.getBytes();
                if (maskPattern.length != searchPattern.length) {
                    JOptionPane.showMessageDialog(this,
                            "Mask length must match search pattern length",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                maskPattern = null;
            }

            if (!caseSensitiveCheck.isSelected()) {
                searchPattern = new String(searchPattern).toLowerCase().getBytes();
            }

            cancelled = false;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid pattern: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public byte[] getSearchPattern() {
        return Arrays.copyOf(searchPattern, searchPattern.length);
    }

    public byte[] getMaskPattern() {
        return maskPattern != null ? Arrays.copyOf(maskPattern, maskPattern.length) : null;
    }

    public boolean isCaseSensitive() {
        return caseSensitiveCheck.isSelected();
    }
}
