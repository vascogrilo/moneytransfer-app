import models.Account;
import org.junit.Test;
import static org.junit.Assert.*;

public class AccountTest {

    @Test
    public void constructorDoesntFillId() {
        Account account = new Account();
        assertEquals(0f, account.getBalance(), 0.01);
        assertNull(account.getId());
    }

    @Test
    public void idTest() {
        Account account = new Account(100f);
        assertEquals(100f, account.getBalance(), 0.01);

        account.setId("42");
        assertEquals("42", account.getId());
    }

    @Test
    public void balanceTest() {
        Account account = new Account(20f);
        assertEquals(20f, account.getBalance(), 0.01);

        account.setBalance(1f);
        assertEquals(1f, account.getBalance(), 0.01);
    }

    @Test
    public void depositTest() {
        Account account = new Account(20f);
        assertEquals(20f, account.getBalance(), 0.01);

        account.deposit(1f);
        assertEquals(21f, account.getBalance(), 0.01);
        account.deposit(10000f);
        assertEquals(10021f, account.getBalance(), 0.01);
    }

    @Test(expected = Account.InsufficientFundsException.class)
    public void withdrawTest() throws Account.InsufficientFundsException {
        Account account = new Account(20f);
        assertEquals(20f, account.getBalance(), 0.01);

        account.withdraw(1f);
        assertEquals(19f, account.getBalance(), 0.01);
        account.withdraw(10000f);
    }
}
