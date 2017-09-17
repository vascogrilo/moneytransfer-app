import models.Account;
import models.Transfer;
import org.junit.Test;
import models.ApplicationStore;

import static org.junit.Assert.*;

public class ApplicationStoreTest {

    @Test
    public void createAccountTest() {
        ApplicationStore store = ApplicationStore.newInstance();
        Account account = new Account("name", "me",100.5f);
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
    public void updateAccountTest() {
        ApplicationStore store = ApplicationStore.newInstance();
        Account account = new Account("name", "me", 50f);
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
    public void deleteAccountTest() {
        ApplicationStore store = ApplicationStore.newInstance();
        Account account = new Account("name","me",50f);
        account = store.createAccount(account);
        Account account1 = new Account("name","me",5000f);
        account1 = store.createAccount(account1);

        assertTrue(store.deleteAccount(account.getId()));
        assertNull(store.getAccount(account.getId()));
        assertNotNull(store.getAccount(account1.getId()));

        assertTrue(store.deleteAccount(account1.getId()));
        assertNull(store.getAccount(account1.getId()));
    }

    @Test
    public void getAllAndClearAllAccountsTest() {
        ApplicationStore store = ApplicationStore.newInstance();
        Account account = new Account("name","me",50f);
        store.createAccount(account);
        Account account1 = new Account("name","me",5000f);
        store.createAccount(account1);

        assertEquals(2, store.listAccounts().size());
        store.clearAccounts();
        assertEquals(0, store.listAccounts().size());
    }

    @Test
    public void depositAccountTest() {
        ApplicationStore store = ApplicationStore.newInstance();
        Account account = new Account("name","me",50f);
        store.createAccount(account);
        String id = account.getId();

        account = store.deposit(id, 10f);
        assertNotNull(account);
        assertEquals(60f, account.getBalance(), 0.01);
    }

    @Test
    public void withdrawAccountTest() throws Account.InsufficientFundsException {
        ApplicationStore store = ApplicationStore.newInstance();
        Account account = new Account("name","me",50f);
        store.createAccount(account);
        String id = account.getId();

        account = store.withdraw(id, 10f);
        assertNotNull(account);
        assertEquals(40f, account.getBalance(), 0.01);
    }

    @Test(expected = Account.InsufficientFundsException.class)
    public void withdrawAccountInsufficientFundsTest() throws Account.InsufficientFundsException {
        ApplicationStore store = ApplicationStore.newInstance();
        Account account = new Account("name","me",0f);
        store.createAccount(account);
        String id = account.getId();

        account = store.deposit(id, 10f);
        assertNotNull(account);
        assertEquals(10f, account.getBalance(), 0.01);

        store.withdraw(id, 11f);
    }

    @Test
    public void createTransferTest() throws ApplicationStore.AccountNotFoundException, Account.InsufficientFundsException {
        ApplicationStore store = ApplicationStore.newInstance();
        Account account1 = new Account("name","me",10f);
        account1 = store.createAccount(account1);
        Account account2 = new Account("name","me",20f);
        account2 = store.createAccount(account2);

        Transfer transfer = new Transfer(account1.getId(), account2.getId(), 9f);
        transfer = store.createTransfer(transfer);
        assertNotNull(transfer.getId());
        assertEquals(account1.getId(), transfer.getOriginAccountId());
        assertEquals(account2.getId(), transfer.getDestinationAccountId());
        assertEquals(9f, transfer.getAmount(), 0.01);

        account1 = store.getAccount(account1.getId());
        account2 = store.getAccount(account2.getId());
        assertEquals(1f, account1.getBalance(), 0.01);
        assertEquals(29f, account2.getBalance(), 0.01);

        transfer = store.getTransfer(transfer.getId());
        assertNotNull(transfer);
    }

    @Test(expected = ApplicationStore.AccountNotFoundException.class)
    public void createTransferNoOriginAccountTest() throws ApplicationStore.AccountNotFoundException, Account.InsufficientFundsException {
        ApplicationStore store = ApplicationStore.newInstance();
        Account account1 = new Account("name","me",10f);
        account1 = store.createAccount(account1);

        Transfer transfer = new Transfer("2", account1.getId(), 9f);
        transfer = store.createTransfer(transfer);
        assertNull(transfer);
    }

    @Test(expected = ApplicationStore.AccountNotFoundException.class)
    public void createTransferNoDestinationAccountTest() throws ApplicationStore.AccountNotFoundException, Account.InsufficientFundsException {
        ApplicationStore store = ApplicationStore.newInstance();
        Account account1 = new Account("name","me",10f);
        account1 = store.createAccount(account1);

        Transfer transfer = new Transfer(account1.getId(), "2", 9f);
        store.createTransfer(transfer);
    }

    @Test(expected = Account.InsufficientFundsException.class)
    public void createInsufficientFundsTransferTest() throws ApplicationStore.AccountNotFoundException, Account.InsufficientFundsException {
        ApplicationStore store = ApplicationStore.newInstance();
        Account account1 = new Account("name","me",10f);
        account1 = store.createAccount(account1);
        Account account2 = new Account("name","me",20f);
        account2 = store.createAccount(account2);

        Transfer transfer = new Transfer(account1.getId(), account2.getId(), 20f);
        store.createTransfer(transfer);
    }

    @Test
    public void listAndClearAllTransfers() throws ApplicationStore.AccountNotFoundException, Account.InsufficientFundsException {
        ApplicationStore store = ApplicationStore.newInstance();
        Account account1 = new Account("name","me",10f);
        account1 = store.createAccount(account1);
        Account account2 = new Account("name","me",20f);
        account2 = store.createAccount(account2);

        Transfer transfer1 = new Transfer(account1.getId(), account2.getId(), 9f);
        transfer1 = store.createTransfer(transfer1);
        assertNotNull(transfer1);
        Transfer transfer2 = new Transfer(account2.getId(), account1.getId(), 9f);
        transfer2 = store.createTransfer(transfer2);
        assertNotNull(transfer2);
        assertEquals(2, store.listTransfers().size());
        store.clearTransfers();
        assertEquals(0, store.listTransfers().size());
    }

    @Test
    public void deleteExistingTransfer() throws ApplicationStore.AccountNotFoundException, Account.InsufficientFundsException {
        ApplicationStore store = ApplicationStore.newInstance();
        Account account1 = new Account("name","me",10f);
        account1 = store.createAccount(account1);
        Account account2 = new Account("name","me",20f);
        account2 = store.createAccount(account2);

        Transfer transfer1 = new Transfer(account1.getId(), account2.getId(), 9f);
        transfer1 = store.createTransfer(transfer1);
        assertNotNull(transfer1);
        assertTrue(store.deleteTransfer(transfer1.getId()));
        assertEquals(0, store.listTransfers().size());
    }

    @Test
    public void deleteNonExistingTransfer() throws ApplicationStore.AccountNotFoundException, Account.InsufficientFundsException {
        ApplicationStore store = ApplicationStore.newInstance();
        Account account1 = new Account("name","me",10f);
        account1 = store.createAccount(account1);
        Account account2 = new Account("name","me",20f);
        account2 = store.createAccount(account2);

        Transfer transfer1 = new Transfer(account1.getId(), account2.getId(), 9f);
        transfer1 = store.createTransfer(transfer1);
        assertNotNull(transfer1);
        assertFalse(store.deleteTransfer("1"));
        assertEquals(1, store.listTransfers().size());
    }
}
