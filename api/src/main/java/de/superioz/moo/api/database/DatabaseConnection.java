package de.superioz.moo.api.database;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.UuidCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inspired by @navopw<br>
 * Last part between the database and the program
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public final class DatabaseConnection {

    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat("database-pool-%d").build());
    private MongoClient mongoClient;
    private MongoDatabase currentDatabase;

    private final String host;
    private final int port;
    private final String user;
    private final String database;
    private final String password;

    private boolean connected;

    /**
     * Runs something asynchronous
     *
     * @param runnable The runnable
     */
    private void runAsynchronous(Runnable runnable) {
        this.executor.execute(runnable);
    }

    /**
     * Returns a list of existing collections in the current database
     *
     * @return Get all collections
     */
    public List<String> getCollections() {
        MongoCursor<String> iterator = this.currentDatabase.listCollectionNames().iterator();
        List<String> collections = new ArrayList<>();
        iterator.forEachRemaining(collections::add);
        return collections;
    }

    public MongoCollection<Document> getCollection(String name) {
        return this.currentDatabase.getCollection(name);
    }

    /**
     * Checks if collection exists
     *
     * @param collection The collection
     * @return Returns true if the collection exists
     */
    public boolean collectionExist(String collection) {
        return this.getCollections().contains(collection);
    }

    /**
     * Counts object in given mongoCollection
     *
     * @param collection The collection
     * @param callback   The callback
     */
    public void count(MongoCollection<Document> collection, Consumer<Long> callback) {
        this.runAsynchronous(() -> callback.accept(collection.count()));
    }

    public void count(MongoCollection<Document> collection, Bson filter, Consumer<Long> callback) {
        this.runAsynchronous(() -> callback.accept(collection.count(filter)));
    }

    public long count(MongoCollection<Document> collection) {
        return collection.count();
    }

    public long count(MongoCollection<Document> collection, Bson filter) {
        return collection.count(filter);
    }

    /**
     * Finds objects from mongoCollection
     *
     * @param collection The collection
     * @param filter     The filter (null for none)
     * @param callback   The callback
     */
    public void find(MongoCollection<Document> collection, Bson filter, int limit, Consumer<FindIterable<Document>> callback) {
        this.runAsynchronous(() -> {
            if(filter != null) {
                callback.accept(collection.find(filter).limit(limit));
            }
            else {
                callback.accept(collection.find().limit(limit));
            }
        });
    }

    public void find(MongoCollection<Document> collection, Bson filter, Consumer<FindIterable<Document>> callback) {
        find(collection, filter, -1, callback);
    }

    public FindIterable<Document> findSync(MongoCollection<Document> collection, Bson filter, int limit) {
        if(filter != null) return collection.find(filter).limit(limit);
        else return collection.find();
    }

    public FindIterable<Document> findSync(MongoCollection<Document> collection, Bson filter) {
        return findSync(collection, filter, -1);
    }

    public void findOne(MongoCollection<Document> collection, Bson filter, Consumer<Document> callback) {
        this.runAsynchronous(() -> callback.accept(this.findOneSync(collection, filter)));
    }

    public Document findOneSync(MongoCollection<Document> collection, Bson filter) {
        return collection.find(filter).first();
    }

    /**
     * Updates something from the mongoCollection
     *
     * @param collection The collection
     * @param filter     The filter to search for updatable values
     * @param document   The document
     * @param callback   The callback
     */
    public void update(MongoCollection<Document> collection, Bson filter, Document document, Consumer<Long> callback) {
        this.runAsynchronous(() -> callback.accept(this.updateSync(collection, filter, document)));
    }

    public long updateSync(MongoCollection<Document> collection, Bson filter, Document document) {
        return collection.updateOne(filter, document).getModifiedCount();
    }

    public void updateMany(MongoCollection<Document> collection, Bson filter, Document document, Consumer<Long> callback) {
        this.runAsynchronous(() -> callback.accept(this.updateManySync(collection, filter, document)));
    }

    public long updateManySync(MongoCollection<Document> collection, Bson filter, Document document) {
        return collection.updateMany(filter, document).getModifiedCount();
    }

    /**
     * Upserts something from the collection
     *
     * @param collection The collection
     * @param filter     The filter
     * @param document   The document
     */
    public void upsert(MongoCollection<Document> collection, Bson filter, Document document, Consumer<Long> callback) {
        this.runAsynchronous(() -> callback.accept(this.upsertSync(collection, filter, document)));
    }

    public long upsertSync(MongoCollection<Document> collection, Bson filter, Document document) {
        UpdateResult result = collection.updateOne(filter, document, new UpdateOptions().upsert(true));
        return result.getUpsertedId() == null ? result.getModifiedCount() : 1;
    }

    public void upsertMany(MongoCollection<Document> collection, Bson filter, Document document, Consumer<Long> callback) {
        this.runAsynchronous(() -> callback.accept(this.upsertManySync(collection, filter, document)));
    }

    public long upsertManySync(MongoCollection<Document> collection, Bson filter, Document document) {
        UpdateResult result = collection.updateMany(filter, document, new UpdateOptions().upsert(true));
        return result.getUpsertedId() == null ? result.getModifiedCount() : 1;
    }

    /**
     * Insert new values into the collection
     *
     * @param collection The collection
     * @param document   The document
     */
    public void insert(MongoCollection<Document> collection, Document document) {
        this.runAsynchronous(() -> {
            collection.insertOne(document);
        });
    }

    public void insertMany(MongoCollection<Document> collection, List<Document> documents) {
        this.runAsynchronous(() -> {
            collection.insertMany(documents);
        });
    }

    /**
     * Deletes objects from collection
     *
     * @param collection The collection
     * @param filter     The filter
     * @param callback   The callback
     */
    public void deleteOne(MongoCollection<Document> collection, Bson filter, Consumer<Long> callback) {
        this.runAsynchronous(() -> callback.accept(collection.deleteOne(filter).getDeletedCount()));
    }

    public long deleteOneSync(MongoCollection<Document> collection, Bson filter) {
        return collection.deleteOne(filter).getDeletedCount();
    }

    public void deleteMany(MongoCollection<Document> collection, Bson filter, Consumer<Long> callback) {
        this.runAsynchronous(() -> callback.accept(collection.deleteMany(filter).getDeletedCount()));
    }

    public long deleteManySync(MongoCollection<Document> collection, Bson filter) {
        return collection.deleteMany(filter).getDeletedCount();
    }

    /**
     * Deletes a collection
     *
     * @param collection The collection
     */
    public void dropCollection(MongoCollection<Document> collection) {
        this.runAsynchronous(collection::drop);
    }

    /**
     * Disables the mongo logger
     */
    public void disableLogger() {
        this.setLoggerLevel(Level.OFF);
    }

    /**
     * Sets the logger level
     *
     * @param level The level
     */
    public void setLoggerLevel(Level level) {
        Logger.getLogger("org.mongodb.driver").setLevel(level);
    }

    /**
     * Connects to the database and if the connection is finished, given callback is called
     *
     * @param callback If an exception was called
     */
    public void connect(Consumer<DatabaseConnection> callback) {
        /*this.runAsynchronous(() -> {

        });*/
        try {
            // options
            MongoClientOptions.Builder builder = MongoClientOptions.builder();
            builder.serverSelectionTimeout(1000 * 3);
            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(CodecRegistries.fromCodecs(new UuidCodec(UuidRepresentation.STANDARD)),
                    MongoClient.getDefaultCodecRegistry());
            builder.codecRegistry(codecRegistry);
            MongoClientOptions options = builder.build();

            if(this.password != null) {
                MongoCredential credential = MongoCredential.createCredential(this.user, this.database, this.password.toCharArray());
                this.mongoClient = new MongoClient(new ServerAddress(this.host, this.port), Collections.singletonList(credential), options);
            }
            else {
                MongoCredential credential = MongoCredential.createCredential(this.user, this.database, new char[0]);
                this.mongoClient = new MongoClient(new ServerAddress(this.host, this.port), Collections.singletonList(credential), options);
            }

            this.currentDatabase = this.mongoClient.getDatabase(this.database);

            callback.accept(this);
            connected = true;
        }
        catch(Exception exception) {
            if(exception instanceof MongoTimeoutException) {
                connected = false;
                System.err.println("Database Connection timed out.");
            }
            else {
                System.err.println("Error while connecting to database:");
                exception.printStackTrace();
            }
        }
    }

    /**
     * Disconnects from the collection
     */
    public void disconnect() {
        mongoClient.close();
        executor.shutdownNow();
    }

    /**
     * This builder is to (as the name says) build the database connection
     */
    @NoArgsConstructor
    public static class Builder {

        private String host = "localhost";
        private Integer port = 27017;
        private String user = "admin";
        private String password = null;
        private String database = "admin";
        private boolean logger = true;

        /**
         * Sets the host (Default: localhost)
         *
         * @param host The hostname
         * @return This
         */
        public Builder host(String host) {
            this.host = host;
            return this;
        }

        /**
         * Sets the port (Default: 27017)
         *
         * @param port The port
         * @return This
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Sets the user (Default: admin)
         *
         * @param user The user (auth)
         * @return This
         */
        public Builder user(String user) {
            this.user = user;
            return this;
        }

        /**
         * Sets the password (optional)
         *
         * @param password The password (auth)
         * @return This
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the database you want connect to (Default: admin)
         *
         * @param database The database
         * @return This
         */
        public Builder database(String database) {
            this.database = database;
            return this;
        }

        /**
         * Enables/Disables the logger from Mongodb
         *
         * @param bool De-/Activates the logger
         * @return This
         */
        public Builder logger(boolean bool) {
            this.logger = bool;
            return this;
        }

        /**
         * Returns the fully configured DatabaseConnection object
         *
         * @return A new databaseModule
         */
        public DatabaseConnection build() {
            DatabaseConnection connection = new DatabaseConnection(this.host, this.port, this.user, this.database, this.password);

            if(!this.logger) {
                connection.disableLogger();
            }

            return connection;
        }

    }

}
