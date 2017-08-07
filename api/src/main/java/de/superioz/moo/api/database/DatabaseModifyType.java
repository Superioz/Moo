package de.superioz.moo.api.database;

public enum DatabaseModifyType {

    /**
     * Inserts something into the database
     */
    CREATE,

    /**
     * Deletes something from the database
     */
    DELETE,

    /**
     * Updates something from the database
     */
    MODIFY,

    /**
     * Updates something from the database (including the primary key)
     */
    MODIFY_PRIMARY

}
