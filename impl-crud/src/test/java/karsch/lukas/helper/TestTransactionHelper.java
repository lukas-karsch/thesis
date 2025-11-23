package karsch.lukas.helper;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.function.Supplier;

public class TestTransactionHelper {

    /**
     * Run the given piece of code inside a database transaction and return the result
     */
    public static <T> T inTransaction(Supplier<T> supplier, PlatformTransactionManager transactionManager) {
        var tx = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            var result = supplier.get();
            transactionManager.commit(tx);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(tx);
            throw e;
        }
    }
}
