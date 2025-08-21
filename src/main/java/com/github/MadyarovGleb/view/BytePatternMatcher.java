package com.github.MadyarovGleb.view;

import com.github.MadyarovGleb.model.FileModel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BytePatternMatcher {
    public static List<Long> search(FileModel fileModel, byte[] pattern, boolean[] mask) throws IOException {
        List<Long> results = new ArrayList<>();
        long fileSize = fileModel.getFileSize();
        int patternLength = pattern.length;
        if (patternLength == 0) return results;

        for (long i = 0; i <= fileSize - patternLength; i++) {
            ByteBuffer buffer = fileModel.getBytes(i, patternLength);
            if (buffer.remaining() < patternLength) break;
            boolean match = true;
            for (int j = 0; j < patternLength; j++) {
                byte actual = buffer.get(j);
                if (mask[j] && actual != pattern[j]) {
                    match = false;
                    break;
                }
            }
            if (match) results.add(i);
        }

        return results;
    }
}
