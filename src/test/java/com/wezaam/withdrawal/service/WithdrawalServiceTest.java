package com.wezaam.withdrawal.service;

import com.wezaam.withdrawal.exception.TransactionException;
import com.wezaam.withdrawal.model.PaymentMethod;
import com.wezaam.withdrawal.model.Withdrawal;
import com.wezaam.withdrawal.model.WithdrawalStatus;
import com.wezaam.withdrawal.model.WithdrawalTemplate;
import com.wezaam.withdrawal.repository.PaymentMethodRepository;
import com.wezaam.withdrawal.repository.WithdrawalRepository;
import com.wezaam.withdrawal.repository.WithdrawalScheduledRepository;
import com.wezaam.withdrawal.util.ExecutorService;
import com.wezaam.withdrawal.util.TestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

//@ExtendWith(MockitoExtension.class)
public class WithdrawalServiceTest {

	@InjectMocks
	WithdrawalService withdrawalService;

	@Mock private WithdrawalRepository withdrawalRepository;
	@Mock private WithdrawalScheduledRepository withdrawalScheduledRepository;
	@Mock private WithdrawalProcessingService withdrawalProcessingService;
	@Mock private PaymentMethodRepository paymentMethodRepository;
	@Mock private EventsService eventsService;

	String userId_s = "1";
	String paymentMethodId_s = "1";
	String amount_s = "50";
	Long userId = Long.parseLong(userId_s);
	Long paymentMethodId = Long.parseLong(paymentMethodId_s);
	Double amount = Double.parseDouble(amount_s);
	Instant initTime;

	@Test
	public void shouldFillAndSaveWithdrawal() throws Exception {
		Withdrawal result = withdrawalService.fillAndSave(new Withdrawal(), withdrawalRepository,
				userId_s, paymentMethodId_s, amount_s, WithdrawalStatus.PENDING);

		verify(withdrawalRepository, times(1)).save(any(Withdrawal.class));
		assertPropertiesEquals(result, WithdrawalStatus.PENDING);
	}

	@Test
	public void shouldCreateWithdrawalAndProcess() throws Exception {
		when(paymentMethodRepository.findById(anyLong()))
				.thenReturn(Optional.of(new PaymentMethod()));
		
		Withdrawal result = withdrawalService.create(userId_s, paymentMethodId_s, amount_s);

		verify(withdrawalRepository, times(2)).save(any(Withdrawal.class));
		verify(eventsService, times(1)).send(any(Withdrawal.class));
		assertPropertiesEquals(result, WithdrawalStatus.PROCESSING);
		assertNotNull(result.getTransactionId());
	}

	@Test
	public void shouldCreateWithdrawalAndFail() throws Exception {
		when(paymentMethodRepository.findById(anyLong()))
				.thenReturn(Optional.of(new PaymentMethod()));
		when(withdrawalProcessingService.sendToProcessing(anyDouble(), any(PaymentMethod.class)))
				.thenThrow(new TransactionException());
		
		Withdrawal result = withdrawalService.create(userId_s, paymentMethodId_s, amount_s);

		verify(withdrawalRepository, times(2)).save(any(Withdrawal.class));
		verify(eventsService, times(1)).send(any(Withdrawal.class));
		assertPropertiesEquals(result, WithdrawalStatus.FAILED);
	}

	@Test
	public void shouldCreateWithdrawalAndGetError() throws Exception {
		when(paymentMethodRepository.findById(anyLong()))
				.thenReturn(Optional.of(new PaymentMethod()));
		when(withdrawalProcessingService.sendToProcessing(anyDouble(), any(PaymentMethod.class)))
		.thenThrow(new RuntimeException());
		
		Withdrawal result = withdrawalService.create(userId_s, paymentMethodId_s, amount_s);

		verify(withdrawalRepository, times(2)).save(any(Withdrawal.class));
		verify(eventsService, times(1)).send(any(Withdrawal.class));
		assertPropertiesEquals(result, WithdrawalStatus.INTERNAL_ERROR);
	}

	@Test
	public void shouldCreateWithdrawalAndNotProcess() throws Exception {
		Withdrawal result = withdrawalService.create(userId_s, paymentMethodId_s, amount_s);

		verify(withdrawalRepository, times(1)).save(any(Withdrawal.class));
		assertPropertiesEquals(result, WithdrawalStatus.PENDING);
	}
	
	@BeforeEach
	public void setUp() {
        try (MockedStatic<Executors> mocked = mockStatic(Executors.class)) {
            mocked.when(Executors::newCachedThreadPool).thenReturn(new ExecutorService());
            
            withdrawalService = new WithdrawalService();
            MockitoAnnotations.openMocks(this);
        }
        
		TestUtils.bypassRepositorySave(withdrawalRepository, Withdrawal.class);
		//TestUtils.bypassRepositorySave(withdrawalScheduledRepository, WithdrawalScheduled.class);
		
		initTime = Instant.now();
		try {
			Thread.sleep(1L);
		} catch (InterruptedException e) {
		}
	}
	
	private void assertPropertiesEquals(WithdrawalTemplate withdrawal, WithdrawalStatus status) {
		assertEquals(withdrawal.getUserId(), userId);
		assertEquals(withdrawal.getPaymentMethodId(), paymentMethodId);
		assertEquals(withdrawal.getAmount(), amount);
		assertTrue(withdrawal.getCreatedAt().compareTo(initTime) > 0);
		try {
			Thread.sleep(1L);
		} catch (InterruptedException e) {
		}
		assertTrue(withdrawal.getCreatedAt().compareTo(Instant.now()) < 0);
		assertEquals(withdrawal.getStatus(), status);
	}

}
