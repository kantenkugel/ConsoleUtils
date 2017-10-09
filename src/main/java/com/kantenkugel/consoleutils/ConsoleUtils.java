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
        if(initialBuffer != null)
            System.out.print(initialBuffer);
        StringBuilder b = initialBuffer == null ? new StringBuilder() : new StringBuilder(initialBuffer);
        int read;
        while ((read = RawConsoleInput.read(true)) != -1) {
            if(!isPrintableChar((char) read)) {
                if(read == CharConstants.CHAR_BACKSPACE) {
                    if(b.length() == 0) continue;
                    b.setLength(b.length() - 1);
                    if(placeholder != null) {
                        for(int i = 0; i < placeholder.length(); i++)
                            ConsoleUtils.backspace();
                    } else {
                        ConsoleUtils.backspace();
                    }
                    continue;
                }
                if(read == CharConstants.CHAR_CTRL_C && (b.length() == 0 ||
                        (initialBuffer != null && initialBuffer.length() == b.length() && initialBuffer.equals(b.toString())))) {
                    //if user pressed ctrl+c on "empty" input, return null to let calling code know
                    return null;
                }
                break;
            }
            b.append((char) read);
            if(placeholder != null && placeholder.length() > 0)
                System.out.print(placeholder);
            else if(placeholder == null)
                System.out.print((char) read);
        }
        System.out.print("\n");
        return b.toString();
    }

    private ConsoleUtils() {}
}
