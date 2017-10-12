package com.kantenkugel.consoleutils;

import biz.source_code.utils.RawConsoleInput;
import javafx.util.Pair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static com.kantenkugel.consoleutils.MockUtils.mockIO;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * @author Kantenkugel (Michael Ritter)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({RawConsoleInput.class, ConsoleUtils.class})
public class ConsoleUtilsTests {
    @Rule
    public Timeout globalTimeout = Timeout.seconds(10);

    @Test
    public void testEmptyInput() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("testing\nshould not matter");
        String s = ConsoleUtils.readHidden("");
        Pair<String, String> result = mock.get();
        assertEquals("ConsoleUtils.readHidden should return first input line", "testing", s);
        assertEquals("Remaining input mismatches. Program probably consumed to much/to little", 
                "should not matter", result.getKey());
        assertEquals("Console should only contain newline", "\n", result.getValue());
    }

    @Test
    public void testPlaceholder() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("testing\nshould not matter");
        String s = ConsoleUtils.readHidden("*");
        Pair<String, String> result = mock.get();
        assertEquals("ConsoleUtils.readHidden should return first input line", "testing", s);
        assertEquals("Remaining input mismatches. Program probably consumed to much/to little",
                "should not matter", result.getKey());
        assertEquals("Console should contain placeholders followed by newline", "*******\n", result.getValue());
    }

    @Test
    public void testBackSpaceWithPlaceholders() throws Exception {
        Supplier<Pair<String, String>> mock = mockIO("testing\b\b\bing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readHidden("*");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(3));
        ConsoleUtils.backspace();
        Pair<String, String> result = mock.get();
        assertEquals("ConsoleUtils.readHidden should return first input line", "testing", s);
        assertEquals("Remaining input mismatches. Program probably consumed to much/to little",
                "should not matter", result.getKey());
        assertEquals("Console should contain placeholders, backspaces and spaces followed by newline",
                String.format("*******%1$s%1$s%1$s***\n", "\b \b"), result.getValue());
    }

    @Test
    public void testBackSpaceWithPlaceholdersTwo() throws Exception {
        Supplier<Pair<String, String>> mock = mockIO("test\b\b\b\b\bing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readHidden("*");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(4));
        ConsoleUtils.backspace();
        Pair<String, String> result = mock.get();
        assertEquals("ConsoleUtils.readHidden should return first input line", "ing", s);
        assertEquals("Remaining input mismatches. Program probably consumed to much/to little",
                "should not matter", result.getKey());
        assertEquals("Console should contain placeholders, backspaces and spaces followed by newline",
                String.format("****%1$s%1$s%1$s%1$s***\n", "\b \b"), result.getValue());
    }

    @Test
    public void testBackSpaceWithPlaceholdersThree() throws Exception {
        Supplier<Pair<String, String>> mock = mockIO("test\b\b\b\bing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readHidden("##");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(8));
        ConsoleUtils.backspace();
        Pair<String, String> result = mock.get();
        assertEquals("ConsoleUtils.readHidden should return first input line", "ing", s);
        assertEquals("Remaining input mismatches. Program probably consumed to much/to little",
                "should not matter", result.getKey());
        assertEquals("Console should contain placeholders, backspaces and spaces followed by newline",
                String.format("########%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s######\n", "\b \b"), result.getValue());
    }

    @Test
    public void testControlSequenceWithPlaceholders() throws Exception {
        Supplier<Pair<String, String>> mock = mockIO("%snope\nshould not matter", CharConstants.CHAR_CTRL_C);
        String s = ConsoleUtils.readHidden("*");
        Pair<String, String> result = mock.get();
        assertNull("ConsoleUtils.readHidden should return null if control sequence empty input", s);
        assertEquals("Remaining input mismatches. Program probably consumed to much/to little",
                "nope\nshould not matter", result.getKey());
        assertEquals("Console should be empty", "", result.getValue());
    }

    @Test
    public void testControlSequenceWithPlaceholdersTwo() throws Exception {
        //"test", 4*BS, ^C, "nope..."
        Supplier<Pair<String, String>> mock = mockIO("test\b\b\b\b%snope\nshould not matter", CharConstants.CHAR_CTRL_C);
        String s = ConsoleUtils.readHidden("*");
        Pair<String, String> result = mock.get();
        assertNull("ConsoleUtils.readHidden should return null if control sequence empty input", s);
        assertEquals("Remaining input mismatches. Program probably consumed to much/to little",
                "nope\nshould not matter", result.getKey());
        assertEquals("Console should be empty (through backspaces)",
                String.format("****%1$s%1$s%1$s%1$s", "\b \b"), result.getValue());
    }

    @Test
    public void testControlSequenceWithPlaceholdersThree() throws Exception {
        //"test", 5*BS, ^C, "nope..."
        Supplier<Pair<String, String>> mock = mockIO("test\b\b\b\b\b%snope\nshould not matter", CharConstants.CHAR_CTRL_C);
        String s = ConsoleUtils.readHidden("*");
        Pair<String, String> result = mock.get();
        assertNull("ConsoleUtils.readHidden should return null if control sequence empty input", s);
        assertEquals("Remaining input mismatches. Program probably consumed to much/to little",
                "nope\nshould not matter", result.getKey());
        assertEquals("Console should be empty (through backspaces)",
                String.format("****%1$s%1$s%1$s%1$s", "\b \b"), result.getValue());
    }

    @Test
    public void testWithBuffer() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("testing\nshould not matter");
        String s = ConsoleUtils.readWithInitialBuffer("buffer");
        Pair<String, String> result = mock.get();
        assertEquals("ConsoleUtils.readWithInitialBuffer should return buffer + input line", "buffertesting", s);
        assertEquals("Remaining input mismatches. Program probably consumed to much/to little",
                "should not matter", result.getKey());
        assertEquals("Console should contain buffer + input + newline", "buffertesting\n", result.getValue());
    }

    @Test
    public void testBufferDeleteOne() throws Exception {
        Supplier<Pair<String, String>> mock = mockIO("testing\b\b\bing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(3));
        ConsoleUtils.backspace();
        Pair<String, String> result = mock.get();
        assertEquals("ConsoleUtils.readWithInitialBuffer should return buffer + input line", "bufftesting", s);
        assertEquals("Remaining input mismatches. Program probably consumed to much/to little",
                "should not matter", result.getKey());
        assertEquals("Console should contain buffer + input + backspaces + newline",
                String.format("bufftesting%1$s%1$s%1$sing\n", "\b \b"), result.getValue());
    }

    @Test
    public void testBufferDeleteTwo() throws Exception {
        Supplier<Pair<String, String>> mock = mockIO("test\b\b\b\b\b\bing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(6));
        ConsoleUtils.backspace();
        Pair<String, String> result = mock.get();
        assertEquals("ConsoleUtils.readWithInitialBuffer should return part of buffer + input line", "buing", s);
        assertEquals("Remaining input mismatches. Program probably consumed to much/to little",
                "should not matter", result.getKey());
        assertEquals("Console should contain buffer + input + backspaces + newline",
                String.format("bufftest%1$s%1$s%1$s%1$s%1$s%1$sing\n", "\b \b"), result.getValue());
    }

    @Test
    public void testBufferDeleteThree() throws Exception {
        Supplier<Pair<String, String>> mock = mockIO("test\b\b\b\b\b\b\b\b\bing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(8));
        ConsoleUtils.backspace();
        Pair<String, String> result = mock.get();
        assertEquals("ConsoleUtils.readWithInitialBuffer should return part of input line", "ing", s);
        assertEquals("Remaining input mismatches. Program probably consumed to much/to little",
                "should not matter", result.getKey());
        assertEquals("Console should contain buffer + input + backspaces + newline",
                String.format("bufftest%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$sing\n", "\b \b"), result.getValue());
    }

    @Test
    public void testControlSequenceWithBuffer() throws Exception {
        Supplier<Pair<String, String>> mock = mockIO("%snope\nshould not matter", CharConstants.CHAR_CTRL_C);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        Pair<String, String> result = mock.get();
        assertNull("ConsoleUtils.readWithInitialBuffer should return null if control sequence empty input", s);
        assertEquals("Remaining input mismatches. Program probably consumed to much/to little",
                "nope\nshould not matter", result.getKey());
        assertEquals("Console should be only buffer", "buff", result.getValue());
    }

    @Test
    public void testControlSequenceWithBufferTwo() throws Exception {
        //"test", 4*BS, ^C, "nope..."
        Supplier<Pair<String, String>> mock = mockIO("test\b\b\b\b%snope\nshould not matter", CharConstants.CHAR_CTRL_C);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        Pair<String, String> result = mock.get();
        assertNull("ConsoleUtils.readWithInitialBuffer should return null if control sequence empty input", s);
        assertEquals("Remaining input mismatches. Program probably consumed to much/to little",
                "nope\nshould not matter", result.getKey());
        assertEquals("Console should contain buffer+input+backspaces+newline",
                String.format("bufftest%1$s%1$s%1$s%1$s", "\b \b"), result.getValue());
    }

    @Test
    public void testControlSequenceWithBufferThree() throws Exception {
        //"test", 8*BS, ^C, "nope..."
        Supplier<Pair<String, String>> mock = mockIO("test\b\b\b\b\b\b\b\b%snope\nshould not matter", CharConstants.CHAR_CTRL_C);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        Pair<String, String> result = mock.get();
        assertNull("ConsoleUtils.readWithInitialBuffer should return null if control sequence empty input", s);
        assertEquals("Remaining input mismatches. Program probably consumed to much/to little",
                "nope\nshould not matter", result.getKey());
        assertEquals("Console should be empty (through backspaces)",
                String.format("bufftest%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s", "\b \b"), result.getValue());
    }

    @Test
    public void testControlSequenceWithBufferFour() throws Exception {
        //"test", 5*BS, "a", ^C, "nope..."
        Supplier<Pair<String, String>> mock = mockIO("test\b\b\b\b\ba%snope\nshould not matter", CharConstants.CHAR_CTRL_C);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        Pair<String, String> result = mock.get();
        assertEquals("ConsoleUtils.readWithInitialBuffer should return input if control sequence on buffer.length but unequal", "bufa", s);
        assertEquals("Remaining input mismatches. Program probably consumed to much/to little",
                "nope\nshould not matter", result.getKey());
        assertEquals("Console should be buffer+input+backspaces+rest of input+newline",
                String.format("bufftest%1$s%1$s%1$s%1$s%1$sa\n", "\b \b"), result.getValue());
    }


}
