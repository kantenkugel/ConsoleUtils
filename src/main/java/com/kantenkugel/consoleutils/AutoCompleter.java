package com.kantenkugel.consoleutils;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Kantenkugel (Michael Ritter)
 */
public class AutoCompleter implements Consumer<ConsoleInputEvent> {
    private final Function<String, String[]> optionProvider;

    private String result = null;

    public AutoCompleter(Function<String, String[]> optionProvider) {
        this.optionProvider = optionProvider;
    }

    public String get() throws IOException {
        ConsoleReader.startLoop(this);
        return result;
    }

    private String currentAuto = null;
    private int matchLength = 0;

    private String lastOptionCallArg = null;
    private String[] options;

    @Override
    public void accept(ConsoleInputEvent e) {
        char addedChar = e.getAddedChar();
        if(addedChar == CharConstants.CHAR_CTRL_C || addedChar == CharConstants.CHAR_CTRL_D
                || addedChar == CharConstants.CHAR_CTRL_Z) {
            e.cancelLoop();
            result = null;
            return;
        }
        if(addedChar == '\n') {
            if(currentAuto != null) {
                clear(currentAuto.length() - matchLength);
            }
            result = e.getCurrentBuffer().substring(0, e.getCurrentBuffer().length() - 1);
            e.cancelLoop();
            return;
        }
        if(addedChar != CharConstants.CHAR_TAB) {
            if(addedChar == CharConstants.CHAR_BACKSPACE)
                System.out.print(CharConstants.CHAR_BACKSPACE + " ");
            System.out.print(addedChar);
        }
        if(addedChar == CharConstants.CHAR_BACKSPACE && currentAuto != null) {
            matchLength--;
        } else if(addedChar == CharConstants.CHAR_TAB && currentAuto != null) {
            String substring = currentAuto.substring(matchLength);
            System.out.print(substring);
            e.getCurrentBuffer().replace(e.getCurrentBuffer().length() - 1, e.getCurrentBuffer().length(), substring);
            currentAuto = null;
        } else if(currentAuto != null) {
            matchLength++;
        }
        int index = e.getCurrentBuffer().lastIndexOf(" ");
        String lastWord = e.getCurrentBuffer().substring(index + 1);
        String nextAuto = null;
        if(lastWord.length() > 0) {
            String previousInput = e.getCurrentBuffer().substring(0, Math.max(0, index));
            if(!previousInput.equals(lastOptionCallArg)) {
                lastOptionCallArg = previousInput;
                options = optionProvider.apply(previousInput);
            }
            for(String s : options) {
                if(s.startsWith(lastWord) && s.length() != lastWord.length()) {
                    nextAuto = s;
                    matchLength = lastWord.length();
                    if(nextAuto.equals(currentAuto))
                        break;
                    System.out.print(s.substring(matchLength));
                    if(currentAuto != null && currentAuto.length() > nextAuto.length()) {
                        clear(currentAuto.length() - nextAuto.length());
                    }
                    for(int i = 0; i < s.length() - matchLength; i++) {
                        System.out.print(CharConstants.CHAR_BACKSPACE);
                    }
                    break;
                }
            }
        }
        if(nextAuto == null && currentAuto != null) {
            clear(currentAuto.length() - matchLength);
        }
        currentAuto = nextAuto;
    }

    private void clear(int amount) {
        for(int i = 0; i < amount; i++) {
            System.out.print(" ");
        }
        for(int i = 0; i < amount; i++) {
            System.out.print(CharConstants.CHAR_BACKSPACE);
        }
    }
}
