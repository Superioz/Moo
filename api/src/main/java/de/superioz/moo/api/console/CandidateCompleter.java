package de.superioz.moo.api.console;

import com.google.common.base.Preconditions;
import jline.console.completer.Completer;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.events.TabCompleteEvent;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Part of the tab completor. It fetches suggestions from the {@link TabCompleteEvent} to complete
 * the input inside the {@link CommandTerminal}
 */
public class CandidateCompleter implements Completer {

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        // buffer could be null
        Preconditions.checkNotNull(candidates);
        SortedSet<String> strings = new TreeSet<>();

        TabCompleteEvent event = new TabCompleteEvent(buffer);
        EventExecutor.getInstance().execute(event);
        if(!event.isCancelled()) {
            List<String> l = event.getSuggestions();
            if(l != null) strings.addAll(l);
        }

        String currentBuffer = event.getCurrentBuffer();
        String add = event.getBeforeBuffer();

        if(currentBuffer == null) {
            for(String string : strings) {
                candidates.add(add + string);
            }
        }
        else {
            for(String match : strings.tailSet(currentBuffer)) {
                if(!match.startsWith(currentBuffer)) {
                    break;
                }
                candidates.add(add + match);
            }
        }

        return candidates.isEmpty() ? -1 : 0;
    }

}
