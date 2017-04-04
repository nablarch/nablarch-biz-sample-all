package please.change.me.common.authentication;

import nablarch.core.db.transaction.SimpleDbTransactionManager;

public class MockSimpleDbTransactionManager extends SimpleDbTransactionManager {

    private boolean isException;
    
    @Override
    public void commitTransaction() {
        if (isException) {
            throw new RuntimeException("rollback test");
        }
        super.commitTransaction();
    }

    public void setException(boolean isException) {
        this.isException = isException;
    }
}
