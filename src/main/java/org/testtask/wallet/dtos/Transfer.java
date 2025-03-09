package org.testtask.wallet.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.testtask.wallet.enums.OperationType;

import java.util.UUID;

@Data
public class Transfer {

    @NotNull(message = "ID must be provided")
    UUID walletId;
    @NotNull(message = "Operation type must be provided")
    OperationType operationType;
    @Positive(message = "Amount must be positive")
    Long amount;
}
