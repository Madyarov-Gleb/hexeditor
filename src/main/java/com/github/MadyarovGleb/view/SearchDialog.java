package com.github.MadyarovGleb.view;

import javax.swing.*;
import java.awt.*;

public class SearchDialog extends JDialog {
    private JTextField patternField;
    private boolean confirmed = false;

    public SearchDialog(Frame owner) {
        super(owner, "Search Pattern", true);
        setLayout(new BorderLayout());

        patternField = new JTextField();
        add(new JLabel("Enter hex pattern (e.g. DE AD ?? BE EF):"), BorderLayout.NORTH);
        add(patternField, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton ok = new JButton("Search");
        JButton cancel = new JButton("Cancel");
        buttons.add(ok);
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);

        ok.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        cancel.addActionListener(e -> dispose());

        setSize(400, 120);
        setLocationRelativeTo(owner);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public byte[] getBytePattern() {
        String[] tokens = patternField.getText().trim().split("\\s+");
        byte[] pattern = new byte[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            pattern[i] = tokens[i].equals("??") ? 0 : (byte) Integer.parseInt(tokens[i], 16);
        }
        return pattern;
    }

    public boolean[] getMask() {
        String[] tokens = patternField.getText().trim().split("\\s+");
        boolean[] mask = new boolean[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            mask[i] = !tokens[i].equals("??");
        }
        return mask;
    }
}
