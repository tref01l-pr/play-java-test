import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import jwt.JwtControllerHelper;
import jwt.JwtControllerHelperImpl;
import jwt.JwtValidator;
import jwt.JwtValidatorImpl;
import services.MongoDb;
import store.*;
import store.mongodb.*;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(JwtValidator.class).to(JwtValidatorImpl.class).asEagerSingleton();
        bind(JwtControllerHelper.class).to(JwtControllerHelperImpl.class).asEagerSingleton();
        bind(MongoDb.class).asEagerSingleton(); // We only want one for proper connection pooling etc.
        bind(UsersStore.class).to(MongoDbUsersStore.class);
        bind(FilesStore.class).to(MongoDbFilesStore.class);
        bind(UserSessionsStore.class).to(MongoDbUserSessionsStore.class);
        bind(ToDosStore.class).to(MongoDbToDosStore.class);
        bind(TransactionManager.class).to(MongoDbTransactionManager.class);
    }
}
