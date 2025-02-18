package store;

import dev.morphia.transactions.MorphiaTransaction;

import java.util.function.Supplier;

public interface TransactionManager {
    <T> T executeInTransaction(MorphiaTransaction<T> transaction);
}
