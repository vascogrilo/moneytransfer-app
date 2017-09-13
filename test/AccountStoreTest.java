import models.Account;
import org.junit.Test;
import models.AccountStore;
import static org.junit.Assert.*;

public class AccountStoreTest {

    @Test
    public void createTest() {
        AccountStore store = AccountStore.newInstance();
        Account account = new Account(100.5f);
        account = store.createAccount(account);
        String id = account.getId();

        assertNotNull(account);
        assertEquals(100.5f, account.getBalance(), 0.01);
        assertNotNull(account.getId());

        account = store.getAccount(id);
        assertNotNull(account);
        assertEquals(100.5f, account.getBalance(), 0.01);
        assertNotNull(account.getId());
    }

    @Test
    public void updateTest() {
        AccountStore store = AccountStore.newInstance();
        Account account = new Account(50f);
        account = store.createAccount(account);
        String id = account.getId();

        account.setBalance(22f);
        account = store.updateAccount(account);
        assertNotNull(account);
        assertEquals(22f, account.getBalance(), 0.01);

        account = store.getAccount(id);
        assertNotNull(account);
        assertEquals(22f, account.getBalance(), 0.01);
        assertEquals(id, account.getId());
    }

    @Test
    public void deleteTest() {
        AccountStore store = AccountStore.newInstance();
        Account account = new Account(50f);
        account = store.createAccount(account);
        Account account1 = new Account(5000f);
        account1 = store.createAccount(account1);

        assertTrue(store.deleteAccount(account.getId()));
        assertNull(store.getAccount(account.getId()));
        assertNotNull(store.getAccount(account1.getId()));

        assertTrue(store.deleteAccount(account1.getId()));
        assertNull(store.getAccount(account1.getId()));
    }

    @Test
    public void getAllAndClearAllTest() {
        AccountStore store = AccountStore.newInstance();
        Account account = new Account(50f);
        store.createAccount(account);
        Account account1 = new Account(5000f);
        store.createAccount(account1);

        assertEquals(2, store.getAllAccounts().size());
        store.clearAccounts();
        assertEquals(0, store.getAllAccounts().size());
    }

    @Test
    public void depositTest() {
        AccountStore store = AccountStore.newInstance();
        Account account = new Account(50f);
        store.createAccount(account);
        String id = account.getId();

        assertTrue(store.deposit(id, 10f));
        account = store.getAccount(id);
        assertEquals(60f, account.getBalance(), 0.01);
    }

    @Test(expected = IllegalStateException.class)
    public void withdrawTest() {
        AccountStore store = AccountStore.newInstance();
        Account account = new Account(50f);
        store.createAccount(account);
        String id = account.getId();

        assertTrue(store.withdraw(id, 10f));
        account = store.getAccount(id);
        assertEquals(40f, account.getBalance(), 0.01);

        store.withdraw(id, 41f);
    }
}
