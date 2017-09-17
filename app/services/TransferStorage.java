package services;

import models.Account;
import models.ApplicationStore;
import models.Transfer;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Basic API for storing and maintaining a list of {@link Transfer}s.
 */
public interface TransferStorage {
    /**
     * Lists all {@link Transfer}s.
     * @return A set with all existing transfers.
     */
    Set<Transfer> listTransfers();
    /**
     * Lists all {@link Transfer}s that match possible field values and sorted by a certain field as well.
     * @param originAccountId Optional origin account id to filter transfers by.
     * @param destinationAccountId Optional destination account id to filter transfers by.
     * @param amount Optional amount to filter transfers by.
     * @param aboveAmount Optional amount to filter transfers bigger than that.
     * @param belowAmount Optional amount to filter transfers smaller than that.
     * @param sort Optional field name to sort for. Prepend with '-' for descending order.
     * @return An array of transfers possibly sorted by 'sort' param and that match provided field values.
     */
    Stream<Transfer> listTransfers(String originAccountId, String destinationAccountId, Float amount, Float aboveAmount, Float belowAmount, String sort);
    /**
     * Retrieves a certain {@link Transfer}.
     * @param id The transfer's id.
     * @return The transfer.
     */
    Transfer getTransfer(String id);
    /**
     * Creates a new transfer.
     * Transfer's field values must be valid (non nulls or empty strings and positive amount).
     * The storage has to verify if both accounts exists and are not the same and try to withdraw from one and deposit into the other.
     * A new id and timestamp should be generated and assigned to the transfer.
     * @param transfer The transfer to be created.
     * @return The transfer with its new id and timestamp.
     * @throws ApplicationStore.AccountNotFoundException If any of the account ids do not exist.
     * @throws Account.InsufficientFundsException If the origin account does not have sufficient funds for the transfer.
     */
    Transfer createTransfer(Transfer transfer) throws ApplicationStore.AccountNotFoundException, Account.InsufficientFundsException;
    /**
     * Deletes a certain {@link Transfer}.
     * @param id The transfer's id.
     * @return True if the transfer exists, false otherwise.
     */
    boolean deleteTransfer(String id);
    /**
     * Deletes all transfers.
     */
    void clearTransfers();
}
