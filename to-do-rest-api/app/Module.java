import com.google.inject.AbstractModule;
import jwt.JwtControllerHelper;
import jwt.JwtControllerHelperImpl;
import jwt.JwtValidator;
import jwt.JwtValidatorImpl;
import services.MongoDb;
import store.ToDosStore;
import store.UserSessionsStore;
import store.UsersStore;
import store.mongodb.MongoDbToDosStore;
import store.mongodb.MongoDbUserSessionsStore;
import store.mongodb.MongoDbUsersStore;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(JwtValidator.class).to(JwtValidatorImpl.class).asEagerSingleton();
        bind(JwtControllerHelper.class).to(JwtControllerHelperImpl.class).asEagerSingleton();
        bind(MongoDb.class).asEagerSingleton(); // We only want one for proper connection pooling etc.
        bind(UsersStore.class).to(MongoDbUsersStore.class);
        bind(UserSessionsStore.class).to(MongoDbUserSessionsStore.class);
        bind(ToDosStore.class).to(MongoDbToDosStore.class);
    }
}
