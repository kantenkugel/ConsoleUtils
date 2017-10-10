package com.kantenkugel.consoleutils;

import biz.source_code.utils.RawConsoleInput;
import javafx.util.Pair;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Supplier;

/**
 * @author Kantenkugel (Michael Ritter)
 */
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
    public static Supplier<Pair<String, String>> mockIO(String input) throws IOException {
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
    public static Supplier<Pair<String, String>> mockIO(String input, Object... format) throws IOException {
        if(format != null && format.length > 0)
            input = String.format(input, format);
        PrintStream out = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));
        PowerMockito.mockStatic(RawConsoleInput.class);
        Queue<Integer> codePoints = input.codePoints().collect(LinkedList<Integer>::new, List::add, List::addAll);
        Mockito.when(RawConsoleInput.read(true)).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                if(codePoints.isEmpty())
                    throw new IOException("Too many read calls");
                return codePoints.poll();
            }
        });
        return () -> {
            System.setOut(out);
            return new Pair<>(
                    new String(codePoints.stream().mapToInt(Integer::intValue).toArray(),0, codePoints.size()),
                    new String(bos.toByteArray(), StandardCharsets.UTF_8)
            );
        };
    }

    private MockUtils() {}
}
