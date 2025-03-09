package org.testtask.wallet.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testtask.wallet.dtos.Transfer;
import org.testtask.wallet.services.WalletService;
import org.testtask.wallet.services.WalletServiceImpl;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(WalletController.class)
@Import(WalletControllerTest.ControllerTestConfiguration.class)
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletService walletService;

    @Test
    public void withdrawSuccess() throws Exception {
        doNothing().when(walletService).processTransfer(any(Transfer.class));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletId\": \"" + UUID.randomUUID() + "\", " +
                                "\"OperationType\": \"WITHDRAW\", " +
                                "\"amount\": 1000}")
                )
                .andExpect(status().isAccepted());
        verify(walletService, times(1)).processTransfer(any(Transfer.class));
    }

    @Test
    public void getBalanceSuccess() throws Exception {
        String uuidStr = UUID.randomUUID().toString();
        when(walletService.getBalance(uuidStr)).thenReturn(1000L);

        mockMvc.perform(get("/api/v1/wallets/" + uuidStr))
                .andExpect(status().isOk());
        verify(walletService, times(1)).getBalance(uuidStr);
    }

    @Test
    public void negativeAmountFail() throws Exception {
        doNothing().when(walletService).processTransfer(any(Transfer.class));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletId\": \"" + UUID.randomUUID() + "\", " +
                                "\"OperationType\": \"WITHDRAW\", " +
                                "\"amount\": -100}")
                )
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    static class ControllerTestConfiguration {
        @Bean
        public WalletService walletService() {
            return Mockito.mock(WalletServiceImpl.class);
        }
    }
}
