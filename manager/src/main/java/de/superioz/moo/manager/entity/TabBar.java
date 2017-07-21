package de.superioz.moo.manager.entity;

import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.manager.events.ChangeTabEvent;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class TabBar implements EventListener {

    private List<TabbedButton> tabs;
    private TabbedButton current;

    public TabBar(TabbedButton... tabs) {
        this.tabs = Arrays.asList(tabs);
        this.current = this.tabs.stream().filter(TabbedButton::isVisible).iterator().next();
    }

    /**
     * Changes the tab to given new tab
     * @param newTab The newTab
     */
    public void changeTab(TabbedButton newTab) {
        current.toggle();
        current = newTab;
        current.toggle();
    }

    @EventHandler
    public void onTabChange(ChangeTabEvent event) {
        this.changeTab(event.getUsedButton());
    }

}
