package de.superioz.moo.api.console;

import jline.console.ConsoleReader;
import jline.console.CursorBuffer;
import jline.console.completer.CompletionHandler;

import java.io.IOException;
import java.util.*;

/**
 * A custom implementation of the {@link jline.console.completer.CandidateListCompletionHandler}<br>
 * It's just for cleaning the candidates on print that means 'arg1 arg2' becomes 'arg2'
 */
public class CandidateCompletionHandler implements CompletionHandler {

    public CandidateCompletionHandler() {
    }

    public boolean complete(ConsoleReader reader, List<CharSequence> candidates, int pos) throws IOException {
        CursorBuffer buf = reader.getCursorBuffer();
        if(candidates.size() == 1) {
            CharSequence value1 = (CharSequence) candidates.get(0);
            if(value1.equals(buf.toString())) {
                return false;
            }
            else {
                setBuffer(reader, value1, pos);
                return true;
            }
        }
        else {
            if(candidates.size() > 1) {
                String value = this.getUnambiguousCompletions(candidates);
                setBuffer(reader, value, pos);
            }

            printCandidates(reader, candidates);
            reader.drawLine();
            return true;
        }
    }

    public static void setBuffer(ConsoleReader reader, CharSequence value, int offset) throws IOException {
        while(reader.getCursorBuffer().cursor > offset && reader.backspace()){
            ;
        }

        reader.putString(value);
        reader.setCursorPosition(offset + value.length());
    }

    public static void printCandidates(ConsoleReader reader, Collection<CharSequence> candidates) throws IOException {
        HashSet distinct = new HashSet(candidates);
        if(distinct.size() > reader.getAutoprintThreshold()) {
            reader.print(Messages.DISPLAY_CANDIDATES.format(candidates.size()));
            reader.flush();
            String i$ = Messages.DISPLAY_CANDIDATES_NO.format();
            String next = Messages.DISPLAY_CANDIDATES_YES.format();
            char[] allowed = new char[]{next.charAt(0), i$.charAt(0)};

            int copy;
            while((copy = reader.readCharacter(allowed)) != -1){
                String tmp = new String(new char[]{(char) copy});
                if(i$.startsWith(tmp)) {
                    reader.println();
                    return;
                }

                if(next.startsWith(tmp)) {
                    break;
                }

                reader.beep();
            }
        }

        if(distinct.size() != candidates.size()) {
            ArrayList copy1 = new ArrayList();
            Iterator i$1 = ((Collection) candidates).iterator();

            while(i$1.hasNext()){
                CharSequence next1 = (CharSequence) i$1.next();
                if(!copy1.contains(next1)) {
                    copy1.add(next1);
                }
            }

            candidates = copy1;
        }

        reader.println();

        // clear candidates
        Collection<CharSequence> clearedCandidates = new ArrayList<>();
        for(CharSequence candidate : candidates) {
            String str = candidate + "";
            String[] spl = str.split(" ");
            if(spl.length != 1) {
                str = spl[spl.length - 1];
            }
            clearedCandidates.add(str);
        }

        reader.printColumns(clearedCandidates);
    }

    private String getUnambiguousCompletions(List<CharSequence> candidates) {
        if(candidates != null && !candidates.isEmpty()) {
            String[] strings = (String[]) candidates.toArray(new String[candidates.size()]);
            String first = strings[0];
            StringBuilder candidate = new StringBuilder();

            for(int i = 0; i < first.length() && this.startsWith(first.substring(0, i + 1), strings); ++i) {
                candidate.append(first.charAt(i));
            }

            return candidate.toString();
        }
        else {
            return null;
        }
    }

    private boolean startsWith(String starts, String[] candidates) {
        String[] arr$ = candidates;
        int len$ = candidates.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String candidate = arr$[i$];
            if(!candidate.startsWith(starts)) {
                return false;
            }
        }

        return true;
    }

    private enum Messages {
        DISPLAY_CANDIDATES,
        DISPLAY_CANDIDATES_YES,
        DISPLAY_CANDIDATES_NO;

        private static final ResourceBundle bundle = ResourceBundle.getBundle(jline.console.completer.CandidateListCompletionHandler.class.getName(), Locale.getDefault());

        private Messages() {
        }

        public String format(Object... args) {
            return bundle == null ? "" : String.format(bundle.getString(this.name()), args);
        }
    }

}
