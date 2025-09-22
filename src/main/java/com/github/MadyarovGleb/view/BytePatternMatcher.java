package com.github.MadyarovGleb.view;

import com.github.MadyarovGleb.model.FileModel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BytePatternMatcher {
    private static final int BUFFER_SIZE = 64 * 1024;

    public static List<Long> search(FileModel fileModel, byte[] pattern, boolean[] mask) throws IOException {
        List<Long> results = new ArrayList<>();
        long fileSize = fileModel.getFileSize();
        int patternLength = pattern.length;
        if (patternLength == 0 || fileSize < patternLength) return results;

        byte[] buffer = new byte[BUFFER_SIZE + patternLength - 1];
        long position = 0;

        while (position < fileSize) {
            int toRead = (int) Math.min(BUFFER_SIZE, fileSize - position);
            ByteBuffer chunk = fileModel.getBytes(position, toRead);

            chunk.get(buffer, 0, toRead);

            if (position > 0 && patternLength > 1) {
                int overlap = patternLength - 1;
                fileModel.getBytes(position - overlap, overlap).get(buffer, 0, overlap);
                toRead += overlap;
            }

            for (int i = 0; i <= toRead - patternLength; i++) {
                boolean match = true;
                for (int j = 0; j < patternLength; j++) {
                    if (mask[j] && buffer[i + j] != pattern[j]) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    long foundPos = position - (position > 0 ? (patternLength - 1) : 0) + i;
                    results.add(foundPos);
                }
            }

            position += BUFFER_SIZE;
        }

        return results;
    }
}
