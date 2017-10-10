package com.kantenkugel.consoleutils;

import biz.source_code.utils.RawConsoleInput;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.powermock.api.mockito.PowerMockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class MockUtils {

    /**
     * Optimized way of calling {@link #mockIO(String, Object...)} without any formatting.
     *
     * @param  input
     *         The String to use for console input
     * @throws IOException
     *         In case of an IO error (unexpected)
     * @return A Supplier that once called restores System.out and returns produced output
     *
     * @see #mockIO(String, Object...)
     */
    public static Supplier<String> mockIO(String input) throws IOException {
        return mockIO(input, (Object[]) null);
    }

    /**
     * Mocks Console input and {@code System.out} for testing by Mocking {@link biz.source_code.utils.RawConsoleInput RawConsoleInput}
     * and Replacing {@code System.out}.
     * <p>
     * This method supports optional Object varargs that are used to format input.
     * <br>If formatting varargs are empty or {@code null}, no formatting is done.
     *
     * @param  input
     *         The String to use for console input
     * @param  format
     *         If non-null and non-empty, this is used to format the input string
     * @throws IOException
     *         In case of an IO error (unexpected)
     * @return A Supplier that once called restores System.out and returns produced output
     */
    public static Supplier<String> mockIO(String input, Object... format) throws IOException {
        if(format != null && format.length > 0)
            input = String.format(input, format);
        PrintStream out = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));
        PowerMockito.mockStatic(RawConsoleInput.class);
        int[] codepoints = input.codePoints().toArray();
        OngoingStubbing<Integer> when = Mockito.when(RawConsoleInput.read(true));
        for(int codepoint : codepoints) {
            when = when.thenReturn(codepoint);
        }
        when.thenThrow(new IOException("To many read calls"));
        return () -> {
            System.setOut(out);
            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        };
    }

    private MockUtils() {}
}
