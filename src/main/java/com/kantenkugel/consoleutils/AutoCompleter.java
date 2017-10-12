package com.kantenkugel.consoleutils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Kantenkugel (Michael Ritter)
 */
public class AutoCompleter implements Consumer<ConsoleInputEvent> {
    private final Function<String, String[]> optionProvider;
    private final DependentOptions depOptions;

    private String result = null;

    public AutoCompleter(Function<String, String[]> optionProvider) {
        this.optionProvider = optionProvider;
        this.depOptions = null;
    }

    public AutoCompleter(DependentOptions depOptions) {
        this.optionProvider = null;
        this.depOptions = depOptions;
    }

    public String get() throws IOException {
        //prep (cleanup prev invocations)
        currentAuto = null;
        lastOptionCallArg = null;
        //call sync method (will eventually populate result before returning)
        ConsoleReader.startLoop(this);
        return result;
    }

    private String currentAuto = null;
    private int matchLength = 0;

    private String lastOptionCallArg = null;
    private PrefixTree options;

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
                if(depOptions != null) {
                    options = depOptions.getTreeForPrevInput(previousInput);
                } else if(optionProvider != null) {
                    options = PrefixTree.from(optionProvider.apply(previousInput));
                }
            }
            String longestPrefix = options.getLongestPrefix(lastWord);
            if(longestPrefix != null && !longestPrefix.equals(currentAuto)) {
                nextAuto = longestPrefix;
                matchLength = lastWord.length();
                if(!nextAuto.equals(currentAuto)) {
                    System.out.print(nextAuto.substring(matchLength));
                    if(currentAuto != null && currentAuto.length() > nextAuto.length()) {
                        clear(currentAuto.length() - nextAuto.length());
                    }
                    for(int i = 0; i < nextAuto.length() - matchLength; i++) {
                        System.out.print(CharConstants.CHAR_BACKSPACE);
                    }
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

    public static class DependentOptions {
        private Map<String, DependentOptions> children = new HashMap<>();
        private String[] options = null;
        private boolean multi = false;

        public DependentOptions markMulti() {
            multi = true;
            return this;
        }

        public DependentOptions createOption(String key) {
            DependentOptions child = new DependentOptions();
            children.put(key, child);
            return child;
        }

        public DependentOptions createOptions(String... finalOptions) {
            options = finalOptions;
            markMulti();
            return this;
        }

        private PrefixTree getTree() {
            if(options != null)
                return PrefixTree.from(options);
            return PrefixTree.from(children.keySet().toArray(new String[0]));
        }

        private PrefixTree getTreeForPrevInput(String input) {
            if(input.length() == 0)
                return getTree();
            String[] split = input.split("\\s+", 2);

            DependentOptions child = children.get(split[0]);
            if(child == null) {
                if(this.multi)
                    return getTree();
                return new PrefixTree();
            }
            return child.getTreeForPrevInput(split.length == 1 ? "" : split[1]);
        }
    }

    private static class PrefixTree {
        private static class Node {
            final char data;
            final List<Node> children = new ArrayList<>(3);
            boolean finalNode = false;

            Node(char c) {
                this.data = c;
            }
        }

        private PrefixTree() {}

        private Node root = new Node('\0');

        public String getLongestPrefix(String start) {
            Node current = getNode(start);
            if(current == null)
                return null;

            StringBuilder b = new StringBuilder(start);
            while(current.children.size() == 1) {
                current = current.children.get(0);
                b.append(current.data);
                if(current.finalNode)
                    break;
            }
            return b.toString();
        }

        private Node getNode(String input) {
            Node current = root;
            for(char c : input.toCharArray()) {
                current = getNode(current, c);
                if(current == null)
                    return null;
            }
            return current;
        }

        private static Node getNode(Node current, char next) {
            return current.children.stream().filter(n -> n.data == next).findAny().orElse(null);
        }

        public static PrefixTree from(String... strings) {
            PrefixTree tree = new PrefixTree();
            for(String s : strings) {
                Node current = tree.root;
                for(char c : s.toCharArray()) {
                    Node next = getNode(current, c);
                    if(next == null) {
                        next = new Node(c);
                        current.children.add(next);
                    }
                    current = next;
                }
                current.finalNode = true;
            }
            return tree;
        }
    }
}
