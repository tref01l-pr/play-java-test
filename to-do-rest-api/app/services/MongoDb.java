package services;

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

    //private final byte[] encryptionKey;

    @Inject
    public MongoDb(ApplicationLifecycle appLifecycle) {
        String username = Config.get(Config.Option.MONGODB_USERNAME);
        String password = Config.get(Config.Option.MONGODB_PASSWORD);
        String hostname = Config.get(Config.Option.MONGODB_HOSTNAME);
        String database = Config.get(Config.Option.MONGODB_DATABASE);
        // Don't use TLS by default for local development environments and for MongoDBs in OpenShift containers
        Boolean tls = !(Config.getBoolean(Config.Option.MONGODB_DISABLE_TLS) || "localhost".equals(hostname));
        String mongoUrl;
        if (username != null && password != null) {
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
                        new IndexOptions().unique(false)
                );


        appLifecycle.addStopHook(() -> {
            mongoClient.close();
            return CompletableFuture.completedFuture(null);
        });

       /* byte[] key = Hex.decode("0000000000000000000000000000000000000000000000000000000000000000");
        try {
            if (InputUtils.trimToNull(Config.Option.MONGODB_ENCRYPTION_KEY.get()) == null) {
                throw new IllegalArgumentException("Variable is empty");
            }
            key = Hex.decode(Config.Option.MONGODB_ENCRYPTION_KEY.get());
            if (key.length != 32) {
                throw new IllegalArgumentException("Length is not 64 HEX characters (256 bit)");
            }
        } catch (Exception e) {
        }*/
        //encryptionKey = key;
    }

    public MongoDatabase get() {
        return db;
    }

    public Datastore getDS() {
        return ds;
    }

/*    public byte[] getEncryptionKey() {
        return encryptionKey;
    }*/
}

