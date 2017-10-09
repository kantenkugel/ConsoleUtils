package com.kantenkugel.consoleutils;

import com.kantenkugel.consoleutils.AutoCompleter;
import com.kantenkugel.consoleutils.ConsoleUtils;

import java.io.IOException;

public class ConsoleUtilTestMain {
    private static String[] AUTOCOMPLETE = {"auto", "autocomplete", "test", "testing"};

    public static void main(String[] args) throws IOException {
        if(System.console() == null)
            return;
//        System.out.println(ConsoleUtils.readHidden("*"));
        new AutoCompleter(s -> System.out.print('\n' + s), pre -> AUTOCOMPLETE).run();
    }
}
