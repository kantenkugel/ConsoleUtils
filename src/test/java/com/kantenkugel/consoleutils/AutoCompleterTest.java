package com.kantenkugel.consoleutils;

import biz.source_code.utils.RawConsoleInput;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.*;

import static org.junit.Assert.assertEquals;

import static com.kantenkugel.consoleutils.MockUtils.mockIO;

/**
 * @author Kantenkugel (Michael Ritter)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(RawConsoleInput.class)
public class AutoCompleterTest {
    private static final String[] OPTIONS = {"test", "testing", "auto", "autocomplete", "foo", "bar", "aurora", "auras"};

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10);

    @Mock
    private Function<String, String[]> optionProvider;
    @Captor
    private ArgumentCaptor<String> optionCalls;

    @Before
    public void setupMocks() throws IOException {
        MockitoAnnotations.initMocks(this);
        Mockito.when(optionProvider.apply(Mockito.anyString())).thenReturn(OPTIONS);
    }

    private AutoCompleter getDefaultCompleter() {
        return new AutoCompleter(optionProvider);
    }

    private void verifyOptionCalls(List<String> expected) {
        Mockito.verify(optionProvider, Mockito.atLeast(0)).apply(optionCalls.capture());
        assertEquals("Option provider calls mismatch", expected, optionCalls.getAllValues());
    }

    @Test
    public void testEmptyInput() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("");
        assertEquals("Return of AutoCompleter#get mismatches", null, getDefaultCompleter().get());
        verifyOptionCalls(Collections.emptyList());
        Pair<String, String> result = mock.get();
        assertEquals("There should be no more console input", "", result.getKey());
        assertEquals("There should be no console output", "", result.getValue());
    }

    @Test
    public void showsCompletion() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("f");
        assertEquals("Return of AutoCompleter#get mismatches", null, getDefaultCompleter().get());
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("There should be no more console input", "", result.getKey());
        assertEquals("Autocompletion for 'foo' with caret after 'f' should be shown", "foo\b\b", result.getValue());
    }

    @Test
    public void newlineSubmits() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("x\nnope");
        assertEquals("Return of AutoCompleter#get mismatches", "x", getDefaultCompleter().get());
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("Second line should remain in input buffer", "nope", result.getKey());
        assertEquals("Only 'x' should be in output", "x", result.getValue());
    }

    @Test
    public void newlineClearsSuggestion() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("f\nnope");
        assertEquals("Return of AutoCompleter#get mismatches", "f", getDefaultCompleter().get());
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("Second line should remain in input buffer", "nope", result.getKey());
        assertEquals("Autocompletion of 'foo' should be deleted after caret ('f')", "foo\b\b  \b\b", result.getValue());
    }

    @Test
    public void tabCompletes() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("f\t\nnope");
        assertEquals("Return of AutoCompleter#get mismatches", "foo", getDefaultCompleter().get());
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("Second line should remain in input buffer", "nope", result.getKey());
        assertEquals("Autocompletion of 'foo' should be printed", "foo\b\boo", result.getValue());
    }

    @Test
    public void spaceClearsCompletion() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("f \nnope");
        assertEquals("Return of AutoCompleter#get mismatches", "f ", getDefaultCompleter().get());
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("Second line should remain in input buffer", "nope", result.getKey());
        assertEquals("Autocompletion of 'foo' should be cleared actually typed char", "foo\b\b  \b", result.getValue());
    }

    @Test
    public void backspaceClearsCompletion() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("f\b\nnope");
        assertEquals("Return of AutoCompleter#get mismatches", "", getDefaultCompleter().get());
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("Second line should remain in input buffer", "nope", result.getKey());
        //write autocompletion, reset pointer to after f, on backspace delete 1 char, no more completion -> clear completion (3 chars)
        assertEquals("Autocompletion of 'foo' should be cleared", "foo\b\b\b \b   \b\b\b", result.getValue());
    }

    @Test
    public void incompatibleCharClearsCompletion() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("fr\nnope");
        assertEquals("Return of AutoCompleter#get mismatches", "fr", getDefaultCompleter().get());
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("Second line should remain in input buffer", "nope", result.getKey());
        //write autocompletion, reset pointer to after f, write next char, no more completion -> clear completion (1 chars)
        assertEquals("Autocompletion of 'foo' should be cleared", "foo\b\br \b", result.getValue());
    }

    @Test
    public void switchesToNextCompletionAfterComplete() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("t\t");
        assertEquals("Return of AutoCompleter#get mismatches", null, getDefaultCompleter().get());
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("No input remaining", "", result.getKey());
        //write autocompletion for 'test', autocomplete, then show autocompletion for 'testing'
        assertEquals("Autocompletion of 'test' with followup completion of 'testing'",
                "test\b\b\besting\b\b\b", result.getValue());
    }

    @Test
    public void showsOnlyShortestCommonString() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("auro");
        assertEquals("Return of AutoCompleter#get mismatches", null, getDefaultCompleter().get());
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("No input remaining", "", result.getKey());
        //write common prefix 'au' (auto, aurora, aura), on 'r' do nothing (aurora, aura), on 'o' print completion for aurora
        assertEquals("Autocompletion of 'auto' with correction to 'aurora'",
                "au\burora\b\b", result.getValue());
    }

    @Test
    public void tabOnNoCompletionDoesNothing() throws IOException {
        Supplier<Pair<String, String>> mock = mockIO("c\tool");
        assertEquals("Return of AutoCompleter#get mismatches", null, getDefaultCompleter().get());
        verifyOptionCalls(Collections.singletonList(""));
        Pair<String, String> result = mock.get();
        assertEquals("No input remaining", "", result.getKey());
        assertEquals("Tab should not do anything if no autocompletion",
                "cool", result.getValue());
    }

    @Test
    public void dependentOptionsWorks() throws IOException {
        AutoCompleter.DependentOptions root = new AutoCompleter.DependentOptions();

        AutoCompleter.DependentOptions node = root.createOption("node");
        node.createOption("i").createOptions("express", "evernode", "react");
        node.createOption("u").createOptions("forever", "underscore");

        root.createOption("notepad");

        AutoCompleter autoCompleter = new AutoCompleter(root);

        //"node i express react"
        Supplier<Pair<String, String>> mock = mockIO("node i ex\t r\t\nnewline");
        assertEquals("Return of AutoCompleter#get mismatches", "node i express react", autoCompleter.get());
        Pair<String, String> result = mock.get();
        assertEquals("Second input line remaining", "newline", result.getKey());
        assertEquals("'node i express react' with DependentOptions fails to complete",
                "no\bode\be i express\b\b\b\b\bpress react\b\b\b\beact", result.getValue());

        //"notepad"
        mock = mockIO("n\tt\t\nnewline");
        assertEquals("Return of AutoCompleter#get mismatches", "notepad", autoCompleter.get());
        result = mock.get();
        assertEquals("Second input line remaining", "newline", result.getKey());
        assertEquals("'notepad' with DependentOptions fails to complete",
                "no\botepad\b\b\b\bepad", result.getValue());

        //"node u n" (check level - should not complete n to node)
        mock = mockIO("node u n\nnewline");
        assertEquals("Return of AutoCompleter#get mismatches", "node u n", autoCompleter.get());
        result = mock.get();
        assertEquals("Second input line remaining", "newline", result.getKey());
        assertEquals("'node u n' with DependentOptions fails",
                "no\bode\be u n", result.getValue());
    }
}
