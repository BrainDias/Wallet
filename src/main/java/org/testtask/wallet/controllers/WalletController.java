package org.testtask.wallet.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.testtask.wallet.dtos.Transfer;
import org.testtask.wallet.services.WalletService;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/wallet")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void changeBalance(@RequestBody @Valid final Transfer transfer){
        walletService.processTransfer(transfer);
    }

    @GetMapping("/wallets/{wallet_uuid}")
    @ResponseStatus(HttpStatus.OK)
    public Long getBalance(@PathVariable("wallet_uuid") String walletUuid){
        return walletService.getBalance(walletUuid);
    }
}
