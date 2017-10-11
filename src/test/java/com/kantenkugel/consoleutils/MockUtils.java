package com.kantenkugel.consoleutils;

import biz.source_code.utils.RawConsoleInput;
import javafx.util.Pair;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Used to help with Mocking of console input and System.out
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
     * @return A Supplier that once called restores System.out and returns a Pair consisting of
     *         remaining (unconsumed) input and produced output
     *
     * @see #mockIO(String, Object...)
     */
    static Supplier<Pair<String, String>> mockIO(String input) throws IOException {
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
     * @return A Supplier that once called restores System.out and returns a Pair consisting of
     *         remaining (unconsumed) input and produced output
     */
    static Supplier<Pair<String, String>> mockIO(String input, Object... format) throws IOException {
        if(format != null && format.length > 0)
            input = String.format(input, format);
        PrintStream out = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));
        PowerMockito.mockStatic(RawConsoleInput.class);
        Queue<Integer> codePoints = input.codePoints().collect(LinkedList<Integer>::new, List::add, List::addAll);
        Mockito.when(RawConsoleInput.read(true)).thenAnswer((Answer<Integer>) invocation -> {
            if(codePoints.isEmpty())
                return (int) CharConstants.CHAR_CTRL_D;
            return codePoints.poll();
        });
        return () -> {
            System.setOut(out);
            return new Pair<>(
                    new String(codePoints.stream().mapToInt(Integer::intValue).toArray(),0, codePoints.size()),
                    new String(bos.toByteArray(), StandardCharsets.UTF_8)
            );
        };
    }

    /**
     * Mocks ConsoleReader to not use a Thread for console reading.
     * This enables verification of calls & arguments in a deterministic manner.
     *
     * @throws IOException
     *         In case of an IO error (unexpected)
     */
    static void mockConsoleReader() throws IOException {
        try {
            Method loop = ConsoleReader.class.getDeclaredMethod("loop", Consumer.class, AtomicBoolean.class);
            PowerMockito.mockStatic(ConsoleReader.class, Answers.CALLS_REAL_METHODS);
            PowerMockito.doAnswer((Answer<Runnable>) invocation -> {
                loop.invoke(null, invocation.getArgument(0), new AtomicBoolean(true));
                return () -> {};
            }).when(ConsoleReader.class);
            ConsoleReader.startLoop(Mockito.any());
        } catch(NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private MockUtils() {}
}
