package org.testtask.wallet.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testtask.wallet.dtos.Transfer;
import org.testtask.wallet.entities.Wallet;
import org.testtask.wallet.enums.OperationType;
import org.testtask.wallet.repositories.WalletRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class WalletServiceWithDBIntegrationalTest {

    @Autowired
    WalletService walletService;
    @Autowired
    WalletRepository walletRepository;

    UUID uuid;
    Wallet wallet;

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:latest")
    )
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        wallet = new Wallet();
        uuid = UUID.randomUUID();
        wallet.setId(uuid);
        wallet.setBalance(1500L);
        walletRepository.save(wallet);
    }

    @Test
    public void processTransferSuccess() {


        Transfer validTransfer = new Transfer();
        validTransfer.setWalletId(uuid);
        validTransfer.setOperationType(OperationType.WITHDRAW);
        validTransfer.setAmount(1000L);

        walletService.processTransfer(validTransfer);

        Optional<Wallet> walletAfterTransfer = walletRepository.findById(uuid);
        assertThat(walletAfterTransfer.isPresent()).isTrue();
        assertThat(walletAfterTransfer.get().getBalance()).isEqualTo(500L);
    }

    @Test
    public void processTransferInsufficientBalance() {

        Transfer invalidTransfer = new Transfer();
        invalidTransfer.setWalletId(uuid);
        invalidTransfer.setOperationType(OperationType.WITHDRAW);
        invalidTransfer.setAmount(2000L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> walletService.processTransfer(invalidTransfer)
        );

        assertThat(exception.getMessage()).isEqualTo("400 BAD_REQUEST \"Insufficient balance\"");
    }

    @Test
    public void processTransferNotFound() {
        UUID randomUUID = UUID.randomUUID();

        Transfer randomUUIDTransfer = new Transfer();
        randomUUIDTransfer.setWalletId(randomUUID);
        randomUUIDTransfer.setOperationType(OperationType.WITHDRAW);
        randomUUIDTransfer.setAmount(1000L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> walletService.processTransfer(randomUUIDTransfer)
        );

        assertThat(exception.getMessage()).isEqualTo("404 NOT_FOUND \"Wallet not found\"");
    }

    @Test
    public void getBalanceSuccess() {
        assertThat(walletService.getBalance(uuid.toString())).isEqualTo(1500);
    }

    @Test
    public void getBalanceNotFound() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> walletService.getBalance(UUID.randomUUID().toString())
        );

        assertThat(exception.getMessage()).isEqualTo("404 NOT_FOUND \"Wallet not found\"");
    }
}
