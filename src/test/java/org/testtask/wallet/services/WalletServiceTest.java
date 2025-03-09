package org.testtask.wallet.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.testtask.wallet.dtos.Transfer;
import org.testtask.wallet.entities.Wallet;
import org.testtask.wallet.enums.OperationType;
import org.testtask.wallet.repositories.WalletRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    static Wallet wallet;
    static UUID uuid;

    @BeforeAll
    public static void setUp() {
        wallet = new Wallet();
        uuid = UUID.randomUUID();
        wallet.setId(uuid);
        wallet.setBalance(1500L);
    }

    @Test
    public void processTransferInsufficientBalance() {
        when(walletRepository.findById(uuid)).thenReturn(Optional.of(wallet));

        Transfer invalidTransfer = new Transfer();
        invalidTransfer.setWalletId(uuid);
        invalidTransfer.setOperationType(OperationType.WITHDRAW);
        invalidTransfer.setAmount(2000L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> walletService.processTransfer(invalidTransfer)
        );

        assertThat(exception.getMessage()).isEqualTo("400 BAD_REQUEST \"Insufficient balance\"");

        verify(walletRepository, times(1)).findById(uuid);
    }

    @Test
    public void processTransferNotFound() {
        UUID randomUUID = UUID.randomUUID();

        when(walletRepository.findById(randomUUID)).thenReturn(Optional.empty());

        Transfer randomUUIDTransfer = new Transfer();
        randomUUIDTransfer.setWalletId(randomUUID);
        randomUUIDTransfer.setOperationType(OperationType.WITHDRAW);
        randomUUIDTransfer.setAmount(1000L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> walletService.processTransfer(randomUUIDTransfer)
        );

        assertThat(exception.getMessage()).isEqualTo("404 NOT_FOUND \"Wallet not found\"");
        verify(walletRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    public void getBalanceNotFound() {
        UUID randomUUID = UUID.randomUUID();
        when(walletRepository.findById(randomUUID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> walletService.getBalance(randomUUID.toString())
        );

        assertThat(exception.getMessage()).isEqualTo("404 NOT_FOUND \"Wallet not found\"");
        verify(walletRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    public void getBalanceSucess(){
        when(walletRepository.findById(uuid)).thenReturn(Optional.of(wallet));
        assertThat(walletService.getBalance(uuid.toString())).isEqualTo(1500);
        verify(walletRepository, times(1)).findById(uuid);
    }
}

