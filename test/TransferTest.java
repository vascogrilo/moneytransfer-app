import models.Transfer;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

public class TransferTest {

    @Test
    public void constructorDoesntFillIdNorTimestamp() {
        Transfer transfer = new Transfer("1", "2", 3);
        assertNull(transfer.getId());
        assertNull(transfer.getTimestamp());
    }

    @Test
    public void idTest() {
        Transfer transfer = new Transfer();
        transfer.setId("42");
        assertEquals("42", transfer.getId());
    }

    @Test
    public void timestampTest() {
        Instant instant = Instant.now();
        Transfer transfer = new Transfer();
        transfer.setTimestamp(instant.toString());
        assertEquals(instant.toString(), transfer.getTimestamp());
    }

    @Test
    public void constructorTest() {
        Transfer transfer = new Transfer("1","2",3f);
        assertEquals("1", transfer.getOriginAccountId());
        assertEquals("2", transfer.getDestinationAccountId());
        assertEquals(3f, transfer.getAmount(), 0.01);
    }
}
