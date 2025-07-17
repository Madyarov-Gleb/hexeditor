package com.github.MadyarovGleb.view;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ValueInterpretationDialog extends JDialog {
    public ValueInterpretationDialog(Frame owner, byte[] data, long position) {
        super(owner, "Value Interpretation @ 0x" + Long.toHexString(position), true);

        setLayout(new BorderLayout());
        add(createDataPanel(data), BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        add(closeButton, BorderLayout.SOUTH);

        setSize(400, 300);
        setLocationRelativeTo(owner);
    }

    private JPanel createDataPanel(byte[] data) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        panel.add(new JLabel("Raw Hex:"));
        panel.add(new JLabel(bytesToHex(data)));

        if (data.length >= 1) {
            byte b = buffer.get(0);
            panel.add(new JLabel("1 Byte (signed):"));
            panel.add(new JLabel(String.valueOf(b)));

            panel.add(new JLabel("1 Byte (unsigned):"));
            panel.add(new JLabel(String.valueOf(Byte.toUnsignedInt(b))));
        }

        if (data.length >= 2) {
            short s = buffer.getShort(0);
            panel.add(new JLabel("2 Bytes (signed short):"));
            panel.add(new JLabel(String.valueOf(s)));

            panel.add(new JLabel("2 Bytes (unsigned short):"));
            panel.add(new JLabel(String.valueOf(Short.toUnsignedInt(s))));
        }

        if (data.length >= 4) {
            int i = buffer.getInt(0);
            panel.add(new JLabel("4 Bytes (signed int):"));
            panel.add(new JLabel(String.valueOf(i)));

            panel.add(new JLabel("4 Bytes (unsigned int):"));
            panel.add(new JLabel(Long.toString(Integer.toUnsignedLong(i))));

            panel.add(new JLabel("Float (32-bit):"));
            panel.add(new JLabel(String.valueOf(buffer.getFloat(0))));
        }

        if (data.length >= 8) {
            long l = buffer.getLong(0);
            panel.add(new JLabel("8 Bytes (signed long):"));
            panel.add(new JLabel(String.valueOf(l)));

            panel.add(new JLabel("Double (64-bit):"));
            panel.add(new JLabel(String.valueOf(buffer.getDouble(0))));
        }

        return panel;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
