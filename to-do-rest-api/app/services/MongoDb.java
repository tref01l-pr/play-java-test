package services;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.Indexes;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import play.inject.ApplicationLifecycle;
import utils.Config;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class MongoDb {
    private static final int TIMEOUT_CONNECT = 15 * 1000;

    private final MongoClient mongoClient;
    private final MongoDatabase db;
    private final Datastore ds;
    private final GridFSBucket gridFSBucket;

    @Inject
    public MongoDb(ApplicationLifecycle appLifecycle) {
        String username = Config.get(Config.Option.MONGODB_USERNAME);
        String password = Config.get(Config.Option.MONGODB_PASSWORD);
        String hostname = Config.get(Config.Option.MONGODB_HOSTNAME);
        String database = Config.get(Config.Option.MONGODB_DATABASE);
        // Don't use TLS by default for local development environments and for MongoDBs in OpenShift containers
        Boolean tls = !(Config.getBoolean(Config.Option.MONGODB_DISABLE_TLS) || "localhost".equals(hostname));
        String mongoUrl;
        if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
            mongoUrl = "mongodb://" + username + ":" + password + "@" + hostname + ":27017/" + database + "?tls=" + tls.toString().toLowerCase() + "&connecttimeoutms=" + TIMEOUT_CONNECT;
        } else {
            mongoUrl = "mongodb://" + hostname + ":27017/?tls=" + tls.toString().toLowerCase() + "&connecttimeoutms=" + TIMEOUT_CONNECT;
        }

        mongoClient = MongoClients.create(mongoUrl);
        db = this.mongoClient.getDatabase(database);
        ds = Morphia.createDatastore(mongoClient, database);

        ds.getDatabase().getCollection("users")
                .createIndex(Indexes.ascending("username"), new IndexOptions().unique(true));

        ds.getDatabase().getCollection("todos")
                .createIndex(
                        Indexes.compoundIndex(Indexes.ascending("userId"), Indexes.ascending("tags")),
                        new IndexOptions().unique(false));

        gridFSBucket = GridFSBuckets.create(db);

        appLifecycle.addStopHook(() -> {
            mongoClient.close();
            return CompletableFuture.completedFuture(null);
        });
    }

    public MongoDatabase get() {
        return db;
    }

    public Datastore getDS() {
        return ds;
    }
    public GridFSBucket getGridFSBucket() { return gridFSBucket; }
    public MongoClient getMongoClient() {
        return mongoClient;
    }
}

