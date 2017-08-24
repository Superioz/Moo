package de.superioz.moo.cloud.events;

import de.superioz.moo.api.database.DatabaseConnection;
import de.superioz.moo.api.event.Event;
import lombok.Getter;

/**
 * Event if the connection is active/not active
 */
@Getter
public class DatabaseConnectionEvent implements Event {

    private DatabaseConnection connection;
    private boolean connectionActive;

    public DatabaseConnectionEvent(DatabaseConnection connection, boolean active) {
        this.connection = connection;
        this.connectionActive = active;
    }
}
