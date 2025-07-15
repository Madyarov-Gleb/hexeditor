package com.github.MadyarovGleb.view;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ValueDisplayDialog extends JDialog {
    private JLabel byteValueLabel;
    private JLabel shortValueLabel;
    private JLabel intValueLabel;
    private JLabel longValueLabel;
    private JLabel floatValueLabel;
    private JLabel doubleValueLabel;

    public ValueDisplayDialog(Frame owner, byte[] data, long offset) {
        super(owner, "Value at " + String.format("0x%08X", offset), true);

        setLayout(new GridLayout(7, 1, 5, 5));
        setSize(300, 250);

        add(new JLabel("Values at position " + String.format("0x%08X", offset)));

        byteValueLabel = new JLabel();
        add(byteValueLabel);

        shortValueLabel = new JLabel();
        add(shortValueLabel);

        intValueLabel = new JLabel();
        add(intValueLabel);

        longValueLabel = new JLabel();
        add(longValueLabel);

        floatValueLabel = new JLabel();
        add(floatValueLabel);

        doubleValueLabel = new JLabel();
        add(doubleValueLabel);

        updateValues(data);
    }

    private void updateValues(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        if (data.length >= 1) {
            byte b = buffer.get(0);
            byteValueLabel.setText(String.format("Byte: %d (0x%02X), unsigned: %d",
                    b, b & 0xFF, b & 0xFF));
        }

        if (data.length >= 2) {
            short s = buffer.getShort(0);
            shortValueLabel.setText(String.format("Short: %d (0x%04X), unsigned: %d",
                    s, s & 0xFFFF, s & 0xFFFF));
        }

        if (data.length >= 4) {
            int i = buffer.getInt(0);
            float f = buffer.getFloat(0);
            intValueLabel.setText(String.format("Int: %d (0x%08X), unsigned: %d",
                    i, i & 0xFFFFFFFFL, i & 0xFFFFFFFFL));
            floatValueLabel.setText(String.format("Float: %f", f));
        }

        if (data.length >= 8) {
            long l = buffer.getLong(0);
            double d = buffer.getDouble(0);
            longValueLabel.setText(String.format("Long: %d (0x%016X)", l));
            doubleValueLabel.setText(String.format("Double: %f", d));
        }
    }
}
