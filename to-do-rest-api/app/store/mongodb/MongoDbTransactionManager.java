package store.mongodb;

import CustomExceptions.DatabaseException;
import CustomExceptions.ServiceUnavailableException;
import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import dev.morphia.transactions.MorphiaTransaction;
import play.Logger;
import services.MongoDb;
import store.TransactionManager;
import store.UserSessionsStore;

import javax.inject.Inject;
import java.util.function.Supplier;

public class MongoDbTransactionManager implements TransactionManager {
    private final MongoDb mongoDb;

    @Inject
    public MongoDbTransactionManager(MongoDb mongoDb) {
        this.mongoDb = mongoDb;
    }

    @Override
    public <T> T executeInTransaction(MorphiaTransaction<T> transaction) {
        try {
            return mongoDb.getDS().withTransaction(transaction);
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout during transaction", e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error during transaction", e);
            throw new DatabaseException("Error accessing database", e);
        }
    }
}
