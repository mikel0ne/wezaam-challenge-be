package com.wezaam.withdrawal.service;

import com.wezaam.withdrawal.model.Withdrawal;
import com.wezaam.withdrawal.model.WithdrawalNotification;
import com.wezaam.withdrawal.model.WithdrawalStatus;
import com.wezaam.withdrawal.repository.WithdrawalNotificationRepository;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class EventsServiceTest {

	@InjectMocks
	EventsService eventsService;

	@Mock JmsTemplate jmsTemplate;
	@Mock private WithdrawalService withdrawalService;
    @Mock private WithdrawalNotificationRepository withdrawalNotificationRepository;

	String userId_s = "1";
	String paymentMethodId_s = "1";
	String amount_s = "50";
	Long userId = Long.parseLong(userId_s);
	Long paymentMethodId = Long.parseLong(paymentMethodId_s);
	Double amount = Double.parseDouble(amount_s);
    
	@Test
	public void shouldSendNotification() throws Exception {
		Withdrawal withdrawal = withdrawalFactory();
		
		eventsService.send(withdrawal);

		verify(jmsTemplate, times(1)).convertAndSend(anyString(), any(Withdrawal.class));
	}
    
	@Test
	public void shouldNotSend() throws Exception {
		Withdrawal withdrawal = withdrawalFactory();
		doThrow(new RuntimeException()).when(jmsTemplate)
				.convertAndSend(anyString(), any(Withdrawal.class));
		
		eventsService.send(withdrawal);

		verify(jmsTemplate, times(1)).convertAndSend(anyString(), any(Withdrawal.class));
		verify(withdrawalService, times(1)).fillAndSave(any(WithdrawalNotification.class),
				any(WithdrawalNotificationRepository.class),
				anyString(), anyString(), anyString(), any(WithdrawalStatus.class));
	}
	
	@BeforeEach
	public void setUp() {
		ReflectionTestUtils.setField(eventsService, "notificationsQueue", "notifications");
	}

    public Withdrawal withdrawalFactory() {
    	Withdrawal withdrawal = new Withdrawal();
        withdrawal.setUserId(userId);
        withdrawal.setPaymentMethodId(paymentMethodId);
        withdrawal.setAmount(amount);
        withdrawal.setCreatedAt(Instant.now());
        withdrawal.setStatus(WithdrawalStatus.PROCESSING);
        
        return withdrawal;
    }
}
