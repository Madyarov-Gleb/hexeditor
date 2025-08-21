package com.github.MadyarovGleb.view;

import com.github.MadyarovGleb.model.FileModel;
import com.github.MadyarovGleb.model.SelectionModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HexEditorPanel extends JPanel {
    private JTable hexTable;
    private HexTableModel tableModel;
    private JLabel statusBar;
    private FileModel fileModel;
    private SelectionModel selectionModel;

    private List<Long> searchResults;
    private int patternLength;
    private final Set<Long> highlightedPositions = new HashSet<>();

    private JSpinner bytesPerRowSpinner;
    private JSpinner rowsPerPageSpinner;
    private JTextField offsetField;

    private byte[] clipboardData = null;

    public HexEditorPanel() {
        initEmptyUI();
    }

    public void setModel(FileModel fileModel, SelectionModel selectionModel) {
        this.fileModel = fileModel;
        this.selectionModel = selectionModel;
        removeAll();
        try {
            initHexView(fileModel);
        } catch (IOException e) {
            showError("Error loading file: " + e.getMessage());
            initEmptyUI();
        }
        revalidate();
        repaint();
    }

    private void initEmptyUI() {
        setLayout(new BorderLayout());
        add(new JLabel("Open a file to begin editing", SwingConstants.CENTER), BorderLayout.CENTER);
        statusBar = new JLabel(" No file loaded ");
        add(statusBar, BorderLayout.SOUTH);
    }

    private void initHexView(FileModel fileModel) throws IOException {
        setLayout(new BorderLayout());

        JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bytesPerRowSpinner = new JSpinner(new SpinnerNumberModel(16, 1, 4096, 1));
        rowsPerPageSpinner = new JSpinner(new SpinnerNumberModel(64, 1, 2000, 1));
        offsetField = new JTextField("0", 12);

        JButton apply = new JButton("Go");
        JButton prev = new JButton("Prev");
        JButton next = new JButton("Next");

        control.add(new JLabel("Bytes/Row:"));
        control.add(bytesPerRowSpinner);
        control.add(new JLabel("Rows:"));
        control.add(rowsPerPageSpinner);
        control.add(new JLabel("Offset (hex):"));
        control.add(offsetField);
        control.add(apply);
        control.add(prev);
        control.add(next);

        add(control, BorderLayout.NORTH);

        tableModel = new HexTableModel(fileModel);
        hexTable = new JTable(tableModel);
        configureTable();
        setupContextMenu();

        add(new JScrollPane(hexTable), BorderLayout.CENTER);

        statusBar = new JLabel(" " + fileModel.getFilePath() + " | Size: " +
                formatSize(fileModelSafeSize()) + " ");
        add(statusBar, BorderLayout.SOUTH);

        hexTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateByteValue();
                updateSelectionModel();
            }
        });
        hexTable.getColumnModel().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateByteValue();
                updateSelectionModel();
            }
        });

        bytesPerRowSpinner.addChangeListener(e -> {
            tableModel.setBytesPerRow((Integer) bytesPerRowSpinner.getValue());
            reconfigureColumns();
            updateStatusBar();
        });

        rowsPerPageSpinner.addChangeListener(e -> {
            tableModel.setRowsPerPage((Integer) rowsPerPageSpinner.getValue());
            reconfigureColumns();
            updateStatusBar();
        });

        apply.addActionListener(e -> {
            try {
                long off = parseHexOffset(offsetField.getText());
                tableModel.setFileOffset(off);
                hexTable.clearSelection();
                updateStatusBar();
            } catch (Exception ex) {
                showError("Invalid offset");
            }
        });

        prev.addActionListener(e -> {
            long step = (long) tableModel.getBytesPerRow() * tableModel.getRowsPerPage();
            long newOff = Math.max(0, tableModel.getFileOffset() - step);
            tableModel.setFileOffset(newOff);
            offsetField.setText(Long.toHexString(newOff).toUpperCase());
            hexTable.clearSelection();
            updateStatusBar();
        });

        next.addActionListener(e -> {
            try {
                long step = (long) tableModel.getBytesPerRow() * tableModel.getRowsPerPage();
                long max = tableModel.getMaxOffset();
                long newOff = Math.min(max, tableModel.getFileOffset() + step);
                tableModel.setFileOffset(newOff);
                offsetField.setText(Long.toHexString(newOff).toUpperCase());
                hexTable.clearSelection();
                updateStatusBar();
            } catch (IOException ex) {
                showError("Cannot move to next page: " + ex.getMessage());
            }
        });

        offsetField.setText("0");
        reconfigureColumns();
        updateStatusBar();
    }

    private long fileModelSafeSize() {
        try {
            return fileModel.getFileSize();
        } catch (IOException e) {
            return 0L;
        }
    }

    private void updateSelectionModel() {
        int[] selectedRows = hexTable.getSelectedRows();
        int[] selectedCols = hexTable.getSelectedColumns();

        if (selectedRows.length == 0 || selectedCols.length == 0) {
            selectionModel.clearSelection();
            return;
        }

        Long minPos = null;
        Long maxPos = null;

        for (int row : selectedRows) {
            for (int col : selectedCols) {
                if (col < 1) continue;
                long pos = tableModel.positionForCell(row, col);
                if (minPos == null || pos < minPos) minPos = pos;
                if (maxPos == null || pos > maxPos) maxPos = pos;
            }
        }

        if (minPos != null && maxPos != null) {
            selectionModel.setSelection(minPos, maxPos);
        } else {
            selectionModel.clearSelection();
        }
    }

    private void updateByteValue() {
        int row = hexTable.getSelectedRow();
        int col = hexTable.getSelectedColumn();

        if (row >= 0 && col >= 1) {
            try {
                long pos = tableModel.positionForCell(row, col);
                ByteBuffer buffer = fileModel.getBytes(pos, 1);
                if (buffer.remaining() == 0) {
                    statusBar.setText(" Out of range ");
                    return;
                }
                byte b = buffer.get();

                String status = String.format("Offset: 0x%08X | Byte: %d (signed) | %d (unsigned) | Hex: %02X",
                        pos, b, Byte.toUnsignedInt(b), b);
                statusBar.setText(status);

            } catch (IOException ex) {
                statusBar.setText("Error reading byte");
            }
        } else {
            updateStatusBar();
        }
    }

    private void updateStatusBar() {
        try {
            long start = tableModel.getFileOffset();
            long end = Math.min(fileModel.getFileSize(),
                    start + (long) tableModel.getBytesPerRow() * tableModel.getRowsPerPage()) - 1;
            if (end < start) end = start;
            statusBar.setText(String.format(" %s | Size: %s | View: 0x%08X - 0x%08X | BPR=%d Rows=%d",
                    fileModel.getFilePath(),
                    formatSize(fileModel.getFileSize()),
                    start, end,
                    tableModel.getBytesPerRow(), tableModel.getRowsPerPage()
            ));
        } catch (IOException e) {
            statusBar.setText(" Status unavailable ");
        }
    }

    private void configureTable() {
        hexTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        hexTable.setCellSelectionEnabled(true);
        hexTable.setFont(new Font("Monospaced", Font.PLAIN, 14));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                long pos = tableModel.positionForCell(row, column);
                if (highlightedPositions.contains(pos)) {
                    c.setBackground(Color.YELLOW);
                } else if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                } else {
                    c.setBackground(Color.WHITE);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };

        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            hexTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
            hexTable.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 100 : 30);
        }

        hexTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = hexTable.rowAtPoint(evt.getPoint());
                    int col = hexTable.columnAtPoint(evt.getPoint());
                    if (col >= 1) {
                        editByteAt(row, col);
                    }
                }
            }
        });
    }

    private void reconfigureColumns() {
        if (hexTable.getColumnModel().getColumnCount() != tableModel.getColumnCount()) {
            hexTable.createDefaultColumnsFromModel();
        }
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            int w = (i == 0) ? 100 : 30;
            hexTable.getColumnModel().getColumn(i).setPreferredWidth(w);
        }
        hexTable.revalidate();
        hexTable.repaint();
    }

    private void setupContextMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem byteItem = new JMenuItem("View as byte");
        byteItem.addActionListener(e -> showSelectedValue(1));
        menu.add(byteItem);

        JMenuItem shortItem = new JMenuItem("View as 2 bytes (short)");
        shortItem.addActionListener(e -> showSelectedValue(2));
        menu.add(shortItem);

        JMenuItem intItem = new JMenuItem("View as 4 bytes (int/float)");
        intItem.addActionListener(e -> showSelectedValue(4));
        menu.add(intItem);

        JMenuItem longItem = new JMenuItem("View as 8 bytes (long/double)");
        longItem.addActionListener(e -> showSelectedValue(8));
        menu.add(longItem);

        menu.addSeparator();

        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener(e -> copySelection());
        menu.add(copyItem);

        JMenuItem cutZeroItem = new JMenuItem("Cut (Zero Fill)");
        cutZeroItem.addActionListener(e -> cutSelection(true));
        menu.add(cutZeroItem);

        JMenuItem cutShiftItem = new JMenuItem("Cut (Shift Left)");
        cutShiftItem.addActionListener(e -> cutSelection(false));
        menu.add(cutShiftItem);

        JMenuItem pasteOverwriteItem = new JMenuItem("Paste (Overwrite)");
        pasteOverwriteItem.addActionListener(e -> pasteClipboard(true));
        menu.add(pasteOverwriteItem);

        JMenuItem pasteInsertItem = new JMenuItem("Paste (Insert)");
        pasteInsertItem.addActionListener(e -> pasteClipboard(false));
        menu.add(pasteInsertItem);

        menu.addSeparator();

        JMenuItem insertItem = new JMenuItem("Insert Bytes...");
        insertItem.addActionListener(e -> insertBytesDialog());
        menu.add(insertItem);

        JMenuItem deleteItem = new JMenuItem("Delete selected bytes...");
        deleteItem.addActionListener(e -> showDeleteDialog());
        menu.add(deleteItem);

        hexTable.setComponentPopupMenu(menu);
    }

    private void showValueDialog(long position, int byteCount) {
        try {
            byteCount = (int) Math.min(byteCount, fileModel.getFileSize() - position);
            if (byteCount <= 0) {
                showError("Nothing to display at this position");
                return;
            }
            ByteBuffer buffer = fileModel.getBytes(position, byteCount);
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            ValueInterpretationDialog dialog = new ValueInterpretationDialog(parentFrame, data, position);
            dialog.setVisible(true);
        } catch (IOException ex) {
            showError("Error reading data: " + ex.getMessage());
        }
    }

    public void showSelectedValue(int byteCount) {
        int row = hexTable.getSelectedRow();
        int col = hexTable.getSelectedColumn();

        if (row >= 0 && col >= 1) {
            long pos = tableModel.positionForCell(row, col);
            showValueDialog(pos, byteCount);
        } else {
            showError("Please select a cell first");
        }
    }

    public void showSearchDialog() {
        if (fileModel == null) return;

        SearchDialog dialog = new SearchDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);

        if (!dialog.isConfirmed()) return;

        try {
            byte[] pattern = dialog.getBytePattern();
            boolean[] mask = dialog.getMask();
            searchResults = BytePatternMatcher.search(fileModel, pattern, mask);
            highlightedPositions.clear();

            for (long pos : searchResults) {
                for (int i = 0; i < pattern.length; i++) {
                    highlightedPositions.add(pos + i);
                }
            }

            if (searchResults.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No matches found.");
            } else {
                patternLength = pattern.length;
                hexTable.repaint();
                JOptionPane.showMessageDialog(this, "Found " + searchResults.size() + " match(es).");
            }
        } catch (Exception ex) {
            showError("Search error: " + ex.getMessage());
        }
    }

    public void clearHighlight() {
        highlightedPositions.clear();
        hexTable.repaint();
    }

    private void editByteAt(int row, int col) {
        long pos = tableModel.positionForCell(row, col);

        try {
            ByteBuffer buffer = fileModel.getBytes(pos, 1);
            if (buffer.remaining() == 0) {
                showError("Position out of bounds");
                return;
            }
            byte current = buffer.get();

            String hexValue = JOptionPane.showInputDialog(this,
                    String.format("Enter new value at 0x%08X (current: %02X)", pos, current),
                    String.format("%02X", current));

            if (hexValue == null) return;

            hexValue = hexValue.trim();
            if (hexValue.length() == 0 || hexValue.length() > 2) {
                showError("Invalid hex value.");
                return;
            }

            byte newByte = (byte) Integer.parseInt(hexValue, 16);
            fileModel.setByte(pos, newByte);
            hexTable.repaint();
        } catch (IOException | NumberFormatException e) {
            showError("Failed to edit byte: " + e.getMessage());
        }
    }

    public void showDeleteDialog() {
        if (!selectionModel.hasSelection()) {
            showError("No bytes selected.");
            return;
        }

        long start = selectionModel.getSelectionStart();
        long end = selectionModel.getSelectionEnd();
        long length = end - start + 1;

        Object[] options = { "Zero Fill", "Shift Left (Remove)" };
        int choice = JOptionPane.showOptionDialog(this,
                String.format("Delete %d byte(s):", length),
                "Delete Bytes",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == -1) return;

        boolean fillWithZeros = (choice == 0);
        try {
            fileModel.deleteBytes(start, length, fillWithZeros);
            selectionModel.clearSelection();
            hexTable.clearSelection();
            hexTable.repaint();
        } catch (IOException e) {
            showError("Delete failed: " + e.getMessage());
        }
    }

    public void copySelection() {
        if (!selectionModel.hasSelection()) {
            showError("No bytes selected.");
            return;
        }
        try {
            long start = selectionModel.getSelectionStart();
            long length = selectionModel.getSelectionLength();
            ByteBuffer buf = fileModel.getBytes(start, (int) length);
            clipboardData = new byte[buf.remaining()];
            buf.get(clipboardData);
            JOptionPane.showMessageDialog(this, "Copied " + length + " bytes.");
        } catch (IOException e) {
            showError("Copy failed: " + e.getMessage());
        }
    }

    public void cutSelection(boolean fillWithZeros) {
        if (!selectionModel.hasSelection()) {
            showError("No bytes selected.");
            return;
        }
        try {
            long start = selectionModel.getSelectionStart();
            long length = selectionModel.getSelectionLength();
            ByteBuffer buf = fileModel.getBytes(start, (int) length);
            clipboardData = new byte[buf.remaining()];
            buf.get(clipboardData);
            fileModel.deleteBytes(start, length, fillWithZeros);
            selectionModel.clearSelection();
            hexTable.clearSelection();
            hexTable.repaint();
            JOptionPane.showMessageDialog(this, "Cut " + length + " bytes.");
        } catch (IOException e) {
            showError("Cut failed: " + e.getMessage());
        }
    }

    public void pasteClipboard(boolean overwrite) {
        if (clipboardData == null || clipboardData.length == 0) {
            showError("Clipboard is empty.");
            return;
        }
        try {
            long pos = selectionModel.hasSelection()
                    ? selectionModel.getSelectionStart()
                    : tableModel.getFileOffset();
            if (overwrite) {
                fileModel.setBytes(pos, clipboardData);
            } else {
                fileModel.insertBytes(pos, clipboardData);
            }
            hexTable.repaint();
            JOptionPane.showMessageDialog(this, "Pasted " + clipboardData.length + " bytes.");
        } catch (IOException e) {
            showError("Paste failed: " + e.getMessage());
        }
    }

    public void insertBytesDialog() {
        String input = JOptionPane.showInputDialog(this,
                "Enter hex bytes to insert (e.g. DE AD BE EF):");
        if (input == null || input.trim().isEmpty()) return;

        try {
            String[] tokens = input.trim().split("\\s+");
            byte[] data = new byte[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                data[i] = (byte) Integer.parseInt(tokens[i], 16);
            }
            long pos = selectionModel.hasSelection()
                    ? selectionModel.getSelectionStart()
                    : tableModel.getFileOffset();
            fileModel.insertBytes(pos, data);
            hexTable.repaint();
            JOptionPane.showMessageDialog(this, "Inserted " + data.length + " bytes.");
        } catch (Exception e) {
            showError("Insert failed: " + e.getMessage());
        }
    }

    private String formatSize(long size) {
        if (size < 1024) return size + " bytes";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024L * 1024L * 1024L) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private long parseHexOffset(String text) {
        String t = text.trim();
        if (t.startsWith("0x") || t.startsWith("0X")) t = t.substring(2);
        if (t.isEmpty()) return 0;
        return Long.parseUnsignedLong(t, 16);
    }
}
