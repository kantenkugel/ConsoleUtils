package com.kantenkugel.consoleutils;

import biz.source_code.utils.RawConsoleInput;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RawConsoleInput.class, ConsoleUtils.class})
public class ConsoleUtilsTests {

    @Test
    public void testEmptyInput() throws IOException {
        Supplier<String> mock = mock("testing\nshould not matter");
        String s = ConsoleUtils.readHidden("");
        assertEquals("ConsoleUtils.readHidden should return first input line", "testing", s);
        assertEquals("Console should only contain newline", "\n", mock.get());
    }

    @Test
    public void testPlaceholder() throws IOException {
        Supplier<String> mock = mock("testing\nshould not matter");
        String s = ConsoleUtils.readHidden("*");
        assertEquals("ConsoleUtils.readHidden should return first input line", "testing", s);
        assertEquals("Console should contain placeholders followed by newline", "*******\n", mock.get());
    }

    @Test
    public void testBackSpaceWithPlaceholders() throws Exception {
        Supplier<String> mock = mock("testing\u0008\u0008\u0008ing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readHidden("*");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(3));
        ConsoleUtils.backspace();
        assertEquals("ConsoleUtils.readHidden should return first input line", "testing", s);
        assertEquals("Console should contain placeholders, backspaces and spaces followed by newline",
                "*******\u0008 \u0008\u0008 \u0008\u0008 \u0008***\n", mock.get());
    }

    @Test
    public void testBackSpaceWithPlaceholdersTwo() throws Exception {
        Supplier<String> mock = mock("test\u0008\u0008\u0008\u0008\u0008ing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readHidden("*");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(4));
        ConsoleUtils.backspace();
        assertEquals("ConsoleUtils.readHidden should return first input line", "ing", s);
        assertEquals("Console should contain placeholders, backspaces and spaces followed by newline",
                "****\u0008 \u0008\u0008 \u0008\u0008 \u0008\u0008 \u0008***\n", mock.get());
    }

    @Test
    public void testWithBuffer() throws IOException {
        Supplier<String> mock = mock("testing\nshould not matter");
        String s = ConsoleUtils.readWithInitialBuffer("buffer");
        assertEquals("ConsoleUtils.readWithInitialBuffer should return buffer + input line", "buffertesting", s);
        assertEquals("Console should contain buffer + input + newline", "buffertesting\n", mock.get());
    }

    @Test
    public void testBufferDeleteOne() throws Exception {
        Supplier<String> mock = mock("testing\u0008\u0008\u0008ing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(3));
        ConsoleUtils.backspace();
        assertEquals("ConsoleUtils.readWithInitialBuffer should return buffer + input line", "bufftesting", s);
        assertEquals("Console should contain buffer + input + backspaces + newline",
                "bufftesting\u0008 \u0008\u0008 \u0008\u0008 \u0008ing\n", mock.get());
    }

    @Test
    public void testBufferDeleteTwo() throws Exception {
        Supplier<String> mock = mock("test\u0008\u0008\u0008\u0008\u0008\u0008ing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(6));
        ConsoleUtils.backspace();
        assertEquals("ConsoleUtils.readWithInitialBuffer should return part of buffer + input line", "buing", s);
        assertEquals("Console should contain buffer + input + backspaces + newline",
                "bufftest\u0008 \u0008\u0008 \u0008\u0008 \u0008\u0008 \u0008\u0008 \u0008\u0008 \u0008ing\n", mock.get());
    }

    @Test
    public void testBufferDeleteThree() throws Exception {
        Supplier<String> mock = mock("test\u0008\u0008\u0008\u0008\u0008\u0008\u0008\u0008\u0008ing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(8));
        ConsoleUtils.backspace();
        assertEquals("ConsoleUtils.readWithInitialBuffer should return part of input line", "ing", s);
        assertEquals("Console should contain buffer + input + backspaces + newline",
                "bufftest\u0008 \u0008\u0008 \u0008\u0008 \u0008\u0008 \u0008\u0008 \u0008\u0008 \u0008\u0008 \u0008\u0008 \u0008ing\n", mock.get());
    }

    private Supplier<String> mock(String input) throws IOException {
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
}
