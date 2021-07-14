package com.wezaam.withdrawal.service;

import com.wezaam.withdrawal.exception.TransactionException;
import com.wezaam.withdrawal.model.PaymentMethod;
import com.wezaam.withdrawal.model.Withdrawal;
import com.wezaam.withdrawal.model.WithdrawalScheduled;
import com.wezaam.withdrawal.model.WithdrawalStatus;
import com.wezaam.withdrawal.model.WithdrawalTemplate;
import com.wezaam.withdrawal.repository.PaymentMethodRepository;
import com.wezaam.withdrawal.repository.WithdrawalRepository;
import com.wezaam.withdrawal.repository.WithdrawalScheduledRepository;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class WithdrawalService {

	private final Log log = LogFactory.getLog(WithdrawalService.class);
	
    @Autowired
    private WithdrawalRepository withdrawalRepository;
    @Autowired
    private WithdrawalScheduledRepository withdrawalScheduledRepository;
    @Autowired
    private WithdrawalProcessingService withdrawalProcessingService;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private EventsService eventsService;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Scheduled(fixedDelay = 5000)
    public void run() {
        withdrawalScheduledRepository.findAllByExecuteAtBefore(Instant.now())
                .forEach(w -> process(w, withdrawalScheduledRepository));
    }

    public Withdrawal create(String userId, String paymentMethodId, String amount) {
    	Withdrawal withdrawal = fillAndSave(new Withdrawal(), withdrawalRepository,
    			userId, paymentMethodId, amount, WithdrawalStatus.PENDING);

        executorService.submit(() -> {
            process(withdrawal, withdrawalRepository);
        });
        
        return withdrawal;
    }

    public WithdrawalScheduled schedule(String userId, String paymentMethodId, String amount, String executeAt) {
    	WithdrawalScheduled withdrawalScheduled = new WithdrawalScheduled();
    	withdrawalScheduled.setExecuteAt(Instant.parse(executeAt));
    	return fillAndSave(withdrawalScheduled, withdrawalScheduledRepository,
    			userId, paymentMethodId, amount, WithdrawalStatus.PENDING);
    }

    public <T extends WithdrawalTemplate> T fillAndSave(T withdrawal, JpaRepository<T, Long> withdrawalTemplateRepository,
    		String userId, String paymentMethodId, String amount, WithdrawalStatus status) {
        withdrawal.setUserId(Long.parseLong(userId));
        withdrawal.setPaymentMethodId(Long.parseLong(paymentMethodId));
        withdrawal.setAmount(Double.parseDouble(amount));
        withdrawal.setCreatedAt(Instant.now());
        withdrawal.setStatus(status);
        
        return withdrawalTemplateRepository.save(withdrawal);
    }

    private <T extends WithdrawalTemplate> void process(T withdrawal, JpaRepository<T, Long> withdrawalTemplateRepository) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(withdrawal.getPaymentMethodId()).orElse(null);
        if (paymentMethod != null) {
            try {
                var transactionId = withdrawalProcessingService.sendToProcessing(withdrawal.getAmount(), paymentMethod);
                withdrawal.setStatus(WithdrawalStatus.PROCESSING);
                withdrawal.setTransactionId(transactionId);
            } catch (TransactionException e) {
                withdrawal.setStatus(WithdrawalStatus.FAILED);
                log.error("Process withdrawal " + WithdrawalStatus.FAILED.name(), e);
            } catch (Exception e) {
                withdrawal.setStatus(WithdrawalStatus.INTERNAL_ERROR);
                log.error("Process withdrawal " + WithdrawalStatus.INTERNAL_ERROR.name(), e);
            }
            withdrawalTemplateRepository.save(withdrawal);
			eventsService.send(withdrawal);
        }
    }
}
