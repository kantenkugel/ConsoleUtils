package com.kantenkugel.consoleutils;

import biz.source_code.utils.RawConsoleInput;
import org.junit.Test;
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

@RunWith(PowerMockRunner.class)
@PrepareForTest({RawConsoleInput.class, ConsoleUtils.class})
public class ConsoleUtilsTests {

    @Test
    public void testEmptyInput() throws IOException {
        Supplier<String> mock = mockIO("testing\nshould not matter");
        String s = ConsoleUtils.readHidden("");
        assertEquals("ConsoleUtils.readHidden should return first input line", "testing", s);
        assertEquals("Console should only contain newline", "\n", mock.get());
    }

    @Test
    public void testPlaceholder() throws IOException {
        Supplier<String> mock = mockIO("testing\nshould not matter");
        String s = ConsoleUtils.readHidden("*");
        assertEquals("ConsoleUtils.readHidden should return first input line", "testing", s);
        assertEquals("Console should contain placeholders followed by newline", "*******\n", mock.get());
    }

    @Test
    public void testBackSpaceWithPlaceholders() throws Exception {
        Supplier<String> mock = mockIO("testing\u0008\u0008\u0008ing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readHidden("*");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(3));
        ConsoleUtils.backspace();
        assertEquals("ConsoleUtils.readHidden should return first input line", "testing", s);
        assertEquals("Console should contain placeholders, backspaces and spaces followed by newline",
                String.format("*******%1$s%1$s%1$s***\n", "\u0008 \u0008"), mock.get());
    }

    @Test
    public void testBackSpaceWithPlaceholdersTwo() throws Exception {
        Supplier<String> mock = mockIO("test\u0008\u0008\u0008\u0008\u0008ing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readHidden("*");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(4));
        ConsoleUtils.backspace();
        assertEquals("ConsoleUtils.readHidden should return first input line", "ing", s);
        assertEquals("Console should contain placeholders, backspaces and spaces followed by newline",
                String.format("****%1$s%1$s%1$s%1$s***\n", "\u0008 \u0008"), mock.get());
    }

    @Test
    public void testBackSpaceWithPlaceholdersThree() throws Exception {
        Supplier<String> mock = mockIO("test\u0008\u0008\u0008\u0008ing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readHidden("##");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(8));
        ConsoleUtils.backspace();
        assertEquals("ConsoleUtils.readHidden should return first input line", "ing", s);
        assertEquals("Console should contain placeholders, backspaces and spaces followed by newline",
                String.format("########%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s######\n", "\u0008 \u0008"), mock.get());
    }

    @Test
    public void testControlSequenceWithPlaceholders() throws Exception {
        Supplier<String> mock = mockIO("%snope\nshould not matter", CharConstants.CHAR_CTRL_C);
        String s = ConsoleUtils.readHidden("*");
        assertNull("ConsoleUtils.readHidden should return null if control sequence empty input", s);
        assertEquals("Console should be empty", "", mock.get());
    }

    @Test
    public void testControlSequenceWithPlaceholdersTwo() throws Exception {
        //"test", 4*BS, ^C, "nope..."
        Supplier<String> mock = mockIO("test\u0008\u0008\u0008\u0008%snope\nshould not matter", CharConstants.CHAR_CTRL_C);
        String s = ConsoleUtils.readHidden("*");
        assertNull("ConsoleUtils.readHidden should return null if control sequence empty input", s);
        assertEquals("Console should be empty (through backspaces)",
                String.format("****%1$s%1$s%1$s%1$s", "\u0008 \u0008"), mock.get());
    }

    @Test
    public void testControlSequenceWithPlaceholdersThree() throws Exception {
        //"test", 5*BS, ^C, "nope..."
        Supplier<String> mock = mockIO("test\u0008\u0008\u0008\u0008\u0008%snope\nshould not matter", CharConstants.CHAR_CTRL_C);
        String s = ConsoleUtils.readHidden("*");
        assertNull("ConsoleUtils.readHidden should return null if control sequence empty input", s);
        assertEquals("Console should be empty (through backspaces)",
                String.format("****%1$s%1$s%1$s%1$s", "\u0008 \u0008"), mock.get());
    }

    @Test
    public void testWithBuffer() throws IOException {
        Supplier<String> mock = mockIO("testing\nshould not matter");
        String s = ConsoleUtils.readWithInitialBuffer("buffer");
        assertEquals("ConsoleUtils.readWithInitialBuffer should return buffer + input line", "buffertesting", s);
        assertEquals("Console should contain buffer + input + newline", "buffertesting\n", mock.get());
    }

    @Test
    public void testBufferDeleteOne() throws Exception {
        Supplier<String> mock = mockIO("testing\u0008\u0008\u0008ing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(3));
        ConsoleUtils.backspace();
        assertEquals("ConsoleUtils.readWithInitialBuffer should return buffer + input line", "bufftesting", s);
        assertEquals("Console should contain buffer + input + backspaces + newline",
                String.format("bufftesting%1$s%1$s%1$sing\n", "\u0008 \u0008"), mock.get());
    }

    @Test
    public void testBufferDeleteTwo() throws Exception {
        Supplier<String> mock = mockIO("test\u0008\u0008\u0008\u0008\u0008\u0008ing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(6));
        ConsoleUtils.backspace();
        assertEquals("ConsoleUtils.readWithInitialBuffer should return part of buffer + input line", "buing", s);
        assertEquals("Console should contain buffer + input + backspaces + newline",
                String.format("bufftest%1$s%1$s%1$s%1$s%1$s%1$sing\n", "\u0008 \u0008"), mock.get());
    }

    @Test
    public void testBufferDeleteThree() throws Exception {
        Supplier<String> mock = mockIO("test\u0008\u0008\u0008\u0008\u0008\u0008\u0008\u0008\u0008ing\nshould not matter");
        PowerMockito.mockStatic(ConsoleUtils.class, Answers.CALLS_REAL_METHODS);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        PowerMockito.verifyStatic(ConsoleUtils.class, Mockito.times(8));
        ConsoleUtils.backspace();
        assertEquals("ConsoleUtils.readWithInitialBuffer should return part of input line", "ing", s);
        assertEquals("Console should contain buffer + input + backspaces + newline",
                String.format("bufftest%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$sing\n", "\u0008 \u0008"), mock.get());
    }

    @Test
    public void testControlSequenceWithBuffer() throws Exception {
        Supplier<String> mock = mockIO("%snope\nshould not matter", CharConstants.CHAR_CTRL_C);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        assertNull("ConsoleUtils.readWithInitialBuffer should return null if control sequence empty input", s);
        assertEquals("Console should be only buffer", "buff", mock.get());
    }

    @Test
    public void testControlSequenceWithBufferTwo() throws Exception {
        //"test", 4*BS, ^C, "nope..."
        Supplier<String> mock = mockIO("test\u0008\u0008\u0008\u0008%snope\nshould not matter", CharConstants.CHAR_CTRL_C);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        assertNull("ConsoleUtils.readWithInitialBuffer should return null if control sequence empty input", s);
        assertEquals("Console should contain buffer+input+backspaces+newline",
                String.format("bufftest%1$s%1$s%1$s%1$s", "\u0008 \u0008"), mock.get());
    }

    @Test
    public void testControlSequenceWithBufferThree() throws Exception {
        //"test", 8*BS, ^C, "nope..."
        Supplier<String> mock = mockIO("test\u0008\u0008\u0008\u0008\u0008\u0008\u0008\u0008%snope\nshould not matter", CharConstants.CHAR_CTRL_C);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        assertNull("ConsoleUtils.readWithInitialBuffer should return null if control sequence empty input", s);
        assertEquals("Console should be empty (through backspaces)",
                String.format("bufftest%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s", "\u0008 \u0008"), mock.get());
    }

    @Test
    public void testControlSequenceWithBufferFour() throws Exception {
        //"test", 5*BS, "a", ^C, "nope..."
        Supplier<String> mock = mockIO("test\u0008\u0008\u0008\u0008\u0008a%snope\nshould not matter", CharConstants.CHAR_CTRL_C);
        String s = ConsoleUtils.readWithInitialBuffer("buff");
        assertEquals("ConsoleUtils.readWithInitialBuffer should return input if control sequence on buffer.length but unequal", "bufa", s);
        assertEquals("Console should be buffer+input+backspaces+rest of input+newline",
                String.format("bufftest%1$s%1$s%1$s%1$s%1$sa\n", "\u0008 \u0008"), mock.get());
    }


}
