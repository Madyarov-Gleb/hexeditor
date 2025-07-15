package com.github.MadyarovGleb.view;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ValueViewerDialog extends JDialog {
    public ValueViewerDialog(Frame owner, byte[] data, long position) {
        super(owner, "Value at 0x" + String.format("%08X", position), true);

        setLayout(new GridLayout(0, 1, 5, 5));
        setSize(350, 400);

        add(new JLabel("Byte values: " + bytesToHex(data)));
        add(createValueTable(data));

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        add(closeButton);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    private JPanel createValueTable(byte[] data) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        if (data.length >= 1) {
            byte b = buffer.get(0);
            panel.add(new JLabel("1 Byte (unsigned):"));
            panel.add(new JLabel(String.valueOf(Byte.toUnsignedInt(b))));

            panel.add(new JLabel("1 Byte (signed):"));
            panel.add(new JLabel(String.valueOf(b)));
        }

        if (data.length >= 2) {
            short s = buffer.getShort(0);
            panel.add(new JLabel("2 Bytes (unsigned):"));
            panel.add(new JLabel(String.valueOf(Short.toUnsignedInt(s))));

            panel.add(new JLabel("2 Bytes (signed):"));
            panel.add(new JLabel(String.valueOf(s)));
        }

        if (data.length >= 4) {
            int i = buffer.getInt(0);
            panel.add(new JLabel("4 Bytes (unsigned):"));
            panel.add(new JLabel(Long.toString(Integer.toUnsignedLong(i))));

            panel.add(new JLabel("4 Bytes (signed):"));
            panel.add(new JLabel(String.valueOf(i)));

            panel.add(new JLabel("Float (32-bit):"));
            panel.add(new JLabel(String.valueOf(buffer.getFloat(0))));
        }

        if (data.length >= 8) {
            long l = buffer.getLong(0);
            panel.add(new JLabel("8 Bytes (signed):"));
            panel.add(new JLabel(String.valueOf(l)));

            panel.add(new JLabel("Double (64-bit):"));
            panel.add(new JLabel(String.valueOf(buffer.getDouble(0))));
        }

        return panel;
    }
}
