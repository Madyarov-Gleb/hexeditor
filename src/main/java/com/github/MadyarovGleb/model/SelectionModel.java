package com.github.MadyarovGleb.model;

public class SelectionModel {
    private long cursorPosition;
    private long selectionStart;
    private long selectionEnd;
    private boolean hasSelection;

    public SelectionModel() {
        cursorPosition = 0;
        selectionStart = 0;
        selectionEnd = 0;
        hasSelection = false;
    }

    public void setSelection(long start, long end) {
        this.selectionStart = start;
        this.selectionEnd = end;
        this.hasSelection = true;
    }

    public boolean hasSelection() {
        return hasSelection;
    }

    public long getSelectionStart() {
        return hasSelection ? Math.min(selectionStart, selectionEnd) : cursorPosition;
    }

    public long getSelectionEnd() {
        return hasSelection ? Math.max(selectionStart, selectionEnd) : cursorPosition;
    }

    public long getSelectionLength() {
        return hasSelection ? Math.abs(selectionEnd - selectionStart) + 1 : 0;
    }

    public void clearSelection() {
        hasSelection = false;
    }
}
