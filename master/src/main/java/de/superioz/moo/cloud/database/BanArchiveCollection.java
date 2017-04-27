package de.superioz.moo.cloud.database;

import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.database.DatabaseConnection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.object.Ban;

import java.util.UUID;

public class BanArchiveCollection extends DatabaseCollection<UUID, Ban> {

    public BanArchiveCollection(DatabaseConnection backend) {
        super(backend);
        super.architecture(Ban.class);
    }

    @Override
    public String getName() {
        return DatabaseType.BAN_ARCHIVE.getName();
    }
}
