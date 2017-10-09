package com.kantenkugel.consoleutils;

import biz.source_code.utils.RawConsoleInput;

import java.awt.event.KeyEvent;
import java.io.IOException;

public class ConsoleUtils {
    public static String readHidden(String placeholder) throws IOException {
        return readInternal(placeholder, null);
    }

    public static String readWithInitialBuffer(String init) throws IOException {
        return readInternal(null, init);
    }

    public static void backspace() {
        System.out.print(CharConstants.CHAR_BACKSPACE);
        System.out.print(' ');
        System.out.print(CharConstants.CHAR_BACKSPACE);
    }

    public static boolean isPrintableChar( char c ) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
        return (!Character.isISOControl(c)) &&
                c != KeyEvent.CHAR_UNDEFINED &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }

    private static String readInternal(String placeholder, String initialBuffer) throws IOException {
        StringBuilder b = initialBuffer == null ? new StringBuilder() : new StringBuilder(initialBuffer);
        int read;
        while ((read = RawConsoleInput.read(true)) != -1) {
            if (read == '\r' || read == '\n') return b.toString();
            if(!isPrintableChar((char) read)) {
                if(read == CharConstants.CHAR_BACKSPACE) {
                    if(b.length() == 0) continue;
                    b.setLength(b.length() - 1);
                    if(placeholder != null) {
                        for(int i = 0; i < placeholder.length(); i++)
                            ConsoleUtils.backspace();
                    }
                    continue;
                } else if(read == CharConstants.CHAR_CTRL_C && (b.length() == 0 ||
                        (initialBuffer != null && initialBuffer.length() == b.length() && initialBuffer.equals(b.toString())))) {
                    //if user pressed ctrl+c on "empty" input, return null to let calling code know
                    return null;
                } else {
                    return b.toString();
                }
            }
            b.append((char) read);
            if(placeholder != null)
                System.out.print(placeholder);
            else
                System.out.print((char) read);
        }
        return b.toString();
    }

    private ConsoleUtils() {}
}
