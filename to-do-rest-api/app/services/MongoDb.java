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

        Boolean tls = !(Config.getBoolean(Config.Option.MONGODB_DISABLE_TLS) || "localhost".equals(hostname));

        StringBuilder mongoUrl = new StringBuilder();
        if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
            mongoUrl.append(String.format("mongodb://%s:%s@", username, password))
                    .append(String.join(",", "mongo1:27017", "mongo2:27017", "mongo3:27017"))
                    .append("/").append(database)
                    .append("?authSource=admin")
                    .append("&tls=").append(tls.toString().toLowerCase())
                    .append("&connectTimeoutMS=").append(TIMEOUT_CONNECT)
                    .append("&replicaSet=dbrs")
                    .append("&readPreference=primaryPreferred")
                    .append("&retryWrites=true")
                    .append("&w=majority");
        } else {
            mongoUrl.append("mongodb://")
                    .append(String.join(",", "mongo1:27017", "mongo2:27017", "mongo3:27017"))
                    .append("/?")
                    .append("tls=").append(tls.toString().toLowerCase())
                    .append("&connectTimeoutMS=").append(TIMEOUT_CONNECT)
                    .append("&replicaSet=dbrs")
                    .append("&readPreference=primaryPreferred")
                    .append("&retryWrites=true")
                    .append("&w=majority");
        }

        mongoClient = MongoClients.create(mongoUrl.toString());
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

