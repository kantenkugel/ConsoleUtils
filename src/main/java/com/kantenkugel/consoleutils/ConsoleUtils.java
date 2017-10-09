package com.kantenkugel.consoleutils;

import biz.source_code.utils.RawConsoleInput;

import java.awt.event.KeyEvent;
import java.io.IOException;

public class ConsoleUtils {
    public static String readHidden(String placeholder) throws IOException {
        StringBuilder b = new StringBuilder();
        int read;
        while(true) {
            read = RawConsoleInput.read(true);
            if(read == -1)
                return b.toString();
            if(read == '\r' || read == '\n')
                return b.toString();
            if(!isPrintableChar((char) read)) {
                if(read == CharConstants.CHAR_BACKSPACE) {
                    if(b.length() == 0)
                        continue;
                    b.setLength(b.length() - 1);
                    for(int i = 0; i < placeholder.length(); i++) {
                        System.out.print(CharConstants.CHAR_BACKSPACE);
                    }
                    for(int i = 0; i < placeholder.length(); i++) {
                        System.out.print(" ");
                    }
                    for(int i = 0; i < placeholder.length(); i++) {
                        System.out.print(CharConstants.CHAR_BACKSPACE);
                    }
                    continue;
                } else {
                    return b.toString();
                }
            }
            b.append((char) read);
            System.out.print(placeholder);
        }
    }

    public static boolean isPrintableChar( char c ) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
        return (!Character.isISOControl(c)) &&
                c != KeyEvent.CHAR_UNDEFINED &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }

    private ConsoleUtils() {

    }
}
