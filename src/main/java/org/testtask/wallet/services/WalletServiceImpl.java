package org.testtask.wallet.services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.testtask.wallet.dtos.Transfer;
import org.testtask.wallet.entities.Wallet;
import org.testtask.wallet.enums.OperationType;
import org.testtask.wallet.repositories.WalletRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    @Retryable(retryFor = DataAccessException.class, maxAttempts = 5, backoff = @Backoff(delay = 500))
    public void processTransfer(Transfer transfer) {
        Wallet wallet = walletRepository.findById(transfer.getWalletId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
        Long currBalance = wallet.getBalance();
        if (transfer.getOperationType() == OperationType.DEPOSIT) {
            wallet.setBalance(currBalance + transfer.getAmount());
        } else if (transfer.getOperationType() == OperationType.WITHDRAW) {
            Long amountToWithdraw = transfer.getAmount();
            if (currBalance >= amountToWithdraw)
                wallet.setBalance(currBalance - amountToWithdraw);
            else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }
        walletRepository.save(wallet);
    }

    @Override
    public Long getBalance(String walletUuid) {
        UUID walletId = UUID.fromString(walletUuid);
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
        return wallet.getBalance();
    }
}
