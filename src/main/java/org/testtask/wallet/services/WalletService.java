package org.testtask.wallet.services;

import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;
import org.testtask.wallet.dtos.Transfer;

public interface WalletService {
    @Transactional
    @Retryable(retryFor = DataAccessException.class, maxAttempts = 5, backoff = @Backoff(delay = 500))
    void processTransfer(Transfer transfer);

    Long getBalance(String walletUuid);
}
