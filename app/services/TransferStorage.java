package services;

import models.Account;
import models.ApplicationStore;
import models.Transfer;

import java.util.Set;

public interface TransferStorage {
    Set<Transfer> listTransfers();
    Transfer[] listTransfers(String id, String originAccountId, String destinationAccountId, Float amount, Float aboveAmount, Float belowAmount, String sort);
    Transfer getTransfer(String id);
    Transfer createTransfer(Transfer transfer) throws ApplicationStore.AccountNotFoundException, Account.InsufficientFundsException;
    boolean deleteTransfer(String id);
    void clearTransfers();
}
