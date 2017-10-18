package com.kantenkugel.consoleutils;

import biz.source_code.utils.RawConsoleInput;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.kantenkugel.consoleutils.MockUtils.mockIO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Kantenkugel (Michael Ritter)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(RawConsoleInput.class)
public class ConsoleReaderTests {
    @Rule
    public Timeout globalTimeout = Timeout.seconds(10);

    private List<String> bufferCollection;
    private List<Character> charsAdded;

    private Consumer<ConsoleInputEvent> getConsumer(int amountEventsAccepted) {
        final AtomicInteger runsLeft = new AtomicInteger(amountEventsAccepted);
        return event -> {
            bufferCollection.add(event.getCurrentBuffer().toString());
            charsAdded.add(event.getAddedChar());
            int left = runsLeft.decrementAndGet();
            if(left == 0 || left < 0 && event.getAddedChar() == CharConstants.CHAR_CTRL_D)
                event.cancelLoop();
        };
    }

    private List<String> getExpectedBuffer(String sequence) {
        StringBuilder b = new StringBuilder(sequence.length());
        ArrayList<String> out = new ArrayList<>(sequence.length());
        for(char c : sequence.toCharArray()) {
            b.append(c);
            out.add(b.toString());
        }
        return out;
    }

    @Before
    public void setupLists() {
        bufferCollection = new ArrayList<>();
        charsAdded = new ArrayList<>();
    }

    @Test
    public void testEmptyInput() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("");
        ConsoleReader.startLoop(getConsumer(-1));
        Pair<String, String> result = mock.get();

        assertEquals("No remainnig input", "", result.getKey());
        assertEquals("Nothing printed to console", "", result.getValue());

        assertEquals("Should have called event twice with EOF chars", getExpectedBuffer("\u0004"), bufferCollection);
        assertEquals("Should have called event twice with EOF char",
                Collections.singletonList(CharConstants.CHAR_CTRL_D), charsAdded);
    }

    @Test
    public void eventCanCancelLoop() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("testing");
        ConsoleReader.startLoop(getConsumer(4));
        Pair<String, String> result = mock.get();

        assertEquals("There should be remaining input (only 3 chars consumed)", "ing", result.getKey());
        assertEquals("Nothing printed to console", "", result.getValue());

        assertEquals("Should have called event 4 times with collecting buffer",
                Arrays.asList("t", "te", "tes", "test"), bufferCollection);
        assertEquals("Should have called event 4 times with first 4 chars of 'testing'",
                Arrays.asList('t', 'e', 's', 't'), charsAdded);
    }

    @Test
    public void backspaceClearsFromBuffer() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("testt\bing");
        ConsoleReader.startLoop(getConsumer(-1));
        Pair<String, String> result = mock.get();

        assertEquals("There should be no remaining input", "", result.getKey());
        assertEquals("Nothing printed to console", "", result.getValue());

        assertEquals("Should have called event 10 times with collecting buffer",
                Arrays.asList("t", "te", "tes", "test", "testt", "test", "testi", "testin", "testing", "testing\u0004"),
                bufferCollection
        );
        assertEquals("Should have called event 4 times with first 4 chars of 'testing'",
                Arrays.asList('t', 'e', 's', 't', 't', '\b', 'i', 'n', 'g', '\u0004'), charsAdded);
    }

}
