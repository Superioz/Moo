package de.superioz.moo.manager.object;

import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.client.events.CloudDisconnectedEvent;
import de.superioz.moo.manager.events.ChangeTabEvent;
import de.superioz.moo.manager.content.HomeTab;
import javafx.application.Platform;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class TabBar implements EventListener {

    private List<Tab> tabs;
    private Tab current;
    private Tab home;

    public TabBar(Tab... tabs) {
        this.tabs = Arrays.asList(tabs);
        this.home = Arrays.stream(tabs).filter(tab -> tab instanceof HomeTab).collect(Collectors.toList()).get(0);
        this.changeTab(home);
    }

    /**
     * Changes the tab to given new tab
     *
     * @param newTab The newTab
     */
    public void changeTab(Tab newTab) {
        if(current != null) current.toggle();
        current = newTab;
        current.toggle();
    }

    @EventHandler
    public void onTabChange(ChangeTabEvent event) {
        this.changeTab(event.getUsedButton());
    }

    @EventHandler
    public void onMooDisconnect(CloudDisconnectedEvent event) {
        if(!(current instanceof HomeTab)) {
            Platform.runLater(() -> changeTab(home));
        }
    }

}
