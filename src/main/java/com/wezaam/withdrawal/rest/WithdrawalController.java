package com.wezaam.withdrawal.rest;

import com.wezaam.withdrawal.model.Withdrawal;
import com.wezaam.withdrawal.model.WithdrawalScheduled;
import com.wezaam.withdrawal.model.WithdrawalTemplate;
import com.wezaam.withdrawal.repository.PaymentMethodRepository;
import com.wezaam.withdrawal.repository.WithdrawalRepository;
import com.wezaam.withdrawal.repository.WithdrawalScheduledRepository;
import com.wezaam.withdrawal.service.WithdrawalService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;

@Api
@RestController
public class WithdrawalController {

    @Autowired
    private UserController userController;
    @Autowired
    private WithdrawalService withdrawalService;
    @Autowired
    private WithdrawalRepository withdrawalRepository;
    @Autowired
    private WithdrawalScheduledRepository withdrawalScheduledRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @PostMapping("/withdrawals")
    @ResponseStatus(HttpStatus.CREATED)
    public WithdrawalTemplate create(@RequestParam String userId, @RequestParam String paymentMethodId,
    		@RequestParam String amount, @RequestParam String executeAt) {
        if (userId == null || paymentMethodId == null || amount == null || executeAt == null) {
        	throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required params are missing");
        }
        userController.findById(Long.parseLong(userId));
        if (!paymentMethodRepository.findById(Long.parseLong(paymentMethodId)).orElseThrow(
        		() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment method not found"))
        		.getUser().getId().toString().equals(userId)) {
        	throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment method does not belong to the user");
        }

        WithdrawalTemplate withdrawal;
        if (executeAt.equals("ASAP")) {
            withdrawal = withdrawalService.create(userId, paymentMethodId, amount);
        } else {
            withdrawal = withdrawalService.schedule(userId, paymentMethodId, amount, executeAt);
        }

        return withdrawal;
    }

    @GetMapping("/withdrawals")
    public List<WithdrawalTemplate> findAll() {
        List<Withdrawal> withdrawals = withdrawalRepository.findAll();
        List<WithdrawalScheduled> withdrawalsScheduled = withdrawalScheduledRepository.findAll();
        List<WithdrawalTemplate> result = new ArrayList<>();
        result.addAll(withdrawals);
        result.addAll(withdrawalsScheduled);

        return result;
    }
}
