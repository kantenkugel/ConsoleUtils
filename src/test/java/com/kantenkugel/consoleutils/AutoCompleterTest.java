package com.kantenkugel.consoleutils;

import biz.source_code.utils.RawConsoleInput;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.*;

import static org.junit.Assert.assertEquals;

import static com.kantenkugel.consoleutils.MockUtils.mockIO;

/**
 * @author Kantenkugel (Michael Ritter)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({RawConsoleInput.class, ConsoleReader.class})
public class AutoCompleterTest {
    private static final String[] OPTIONS = {"test", "testing", "auto", "autocomplete", "foo", "bar", "aurora", "auras"};

    @Mock
    private Function<String, String[]> optionProvider;
    @Mock
    private Predicate<String> resultPredicate;
    @Captor
    private ArgumentCaptor<String> optionCalls;
    @Captor
    private ArgumentCaptor<String> results;

    @Before
    public void setupMocks() throws IOException {
        MockUtils.mockConsoleReader();
        MockitoAnnotations.initMocks(this);
        Mockito.when(optionProvider.apply(Mockito.anyString())).thenReturn(OPTIONS);
        Mockito.when(resultPredicate.test(Mockito.anyString())).thenReturn(false);
    }

    private AutoCompleter getDefaultCompleter() {
        return new AutoCompleter(resultPredicate, optionProvider);
    }

    private void verifyResults(List<String> expected) {
        Mockito.verify(resultPredicate, Mockito.atLeast(0)).test(results.capture());
        assertEquals("Returned results mismatch", expected, results.getAllValues());
    }

    private void verifyOptionCalls(List<String> expected) {
        Mockito.verify(optionProvider, Mockito.atLeast(0)).apply(optionCalls.capture());
        assertEquals("Option provider calls mismatch", expected, optionCalls.getAllValues());
    }

    @Test
    public void testEmptyInput() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("");
        getDefaultCompleter().run();
        verifyResults(Collections.singletonList(null));
        verifyOptionCalls(Collections.emptyList());
        Pair<String, String> result = mock.get();
        assertEquals("There should be no more console input", "", result.getKey());
        assertEquals("There should be no console output", "", result.getValue());
    }

    @Test
    public void showsCompletion() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("f");
        getDefaultCompleter().run();
        verifyResults(Collections.singletonList(null));
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("There should be no more console input", "", result.getKey());
        assertEquals("Autocompletion for 'foo' with caret after 'f' should be shown", "foo\b\b", result.getValue());
    }

    @Test
    public void newlineSubmits() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("x\nnope");
        getDefaultCompleter().run();
        verifyResults(Collections.singletonList("x"));
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("Second line should remain in input buffer", "nope", result.getKey());
        assertEquals("Only 'x' should be in output", "x", result.getValue());
    }

    @Test
    public void newlineClearsSuggestion() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("f\nnope");
        getDefaultCompleter().run();
        verifyResults(Collections.singletonList("f"));
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("Second line should remain in input buffer", "nope", result.getKey());
        assertEquals("Autocompletion of 'foo' should be deleted after caret ('f')", "foo\b\b  \b\b", result.getValue());
    }

    @Test
    public void tabCompletes() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("f\t\nnope");
        getDefaultCompleter().run();
        verifyResults(Collections.singletonList("foo"));
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("Second line should remain in input buffer", "nope", result.getKey());
        assertEquals("Autocompletion of 'foo' should be printed", "foo\b\boo", result.getValue());
    }

    @Test
    public void spaceClearsCompletion() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("f \nnope");
        getDefaultCompleter().run();
        verifyResults(Collections.singletonList("f "));
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("Second line should remain in input buffer", "nope", result.getKey());
        assertEquals("Autocompletion of 'foo' should be cleared actually typed char", "foo\b\b  \b", result.getValue());
    }

    @Test
    public void backspaceClearsCompletion() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("f\b\nnope");
        getDefaultCompleter().run();
        verifyResults(Collections.singletonList(""));
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("Second line should remain in input buffer", "nope", result.getKey());
        //write autocompletion, reset pointer to after f, on backspace delete 1 char, no more completion -> clear completion (3 chars)
        assertEquals("Autocompletion of 'foo' should be cleared", "foo\b\b\b \b   \b\b\b", result.getValue());
    }

    @Test
    public void incompatibleCharClearsCompletion() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("fr\nnope");
        getDefaultCompleter().run();
        verifyResults(Collections.singletonList("fr"));
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("Second line should remain in input buffer", "nope", result.getKey());
        //write autocompletion, reset pointer to after f, write next char, no more completion -> clear completion (1 chars)
        assertEquals("Autocompletion of 'foo' should be cleared", "foo\b\br \b", result.getValue());
    }

    @Test
    public void switchesToNextCompletionAfterComplete() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("t\t");
        getDefaultCompleter().run();
        verifyResults(Collections.singletonList(null));
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("No input remaining", "", result.getKey());
        //write autocompletion for 'test', autocomplete, then show autocompletion for 'testing'
        assertEquals("Autocompletion of 'test' with followup completion of 'testing'",
                "test\b\b\besting\b\b\b", result.getValue());
    }

    @Test
    public void switchesToNextCompletionAfterMismatch() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("aur");
        getDefaultCompleter().run();
        verifyResults(Collections.singletonList(null));
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("No input remaining", "", result.getKey());
        //write autocompletion for auto, then upon aur, clear last char and complete to aurora
        assertEquals("Autocompletion of 'auto' with correction to 'aurora'",
                "auto\b\b\burora\b\b\b", result.getValue());
    }

    @Test
    public void clearsRemainingCharsAfterSwitch() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("aura");
        getDefaultCompleter().run();
        verifyResults(Collections.singletonList(null));
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("No input remaining", "", result.getKey());
        //write autocompletion for auto, then upon aur, switch to aurora and after a switch to auras (and clear remaining
        assertEquals("Autocompletion of 'auto' with correction to 'aurora' and correction to 'auras'",
                "auto\b\b\burora\b\b\bas \b\b", result.getValue());
    }

    //TODO: A load more tests
}
