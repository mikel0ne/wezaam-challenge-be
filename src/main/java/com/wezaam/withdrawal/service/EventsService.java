package com.wezaam.withdrawal.service;

import com.wezaam.withdrawal.model.WithdrawalNotification;
import com.wezaam.withdrawal.model.WithdrawalTemplate;
import com.wezaam.withdrawal.repository.WithdrawalNotificationRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class EventsService {

	private final Log log = LogFactory.getLog(EventsService.class);
	
	@Value("${withdrawal.activemq.queue.notifications:notifications}")
	private String notificationsQueue;
	
	@Autowired
	JmsTemplate jmsTemplate;
    @Autowired
    private WithdrawalService withdrawalService;
    @Autowired
    private WithdrawalNotificationRepository withdrawalNotificationRepository;

    @Scheduled(fixedDelay = 20000)
    public void run() {
    	withdrawalNotificationRepository.findAll().forEach(w -> resend(w));
    }

    @Async
    public void send(WithdrawalTemplate withdrawal) {
    	try {
    		sendQueue(withdrawal);
    	} catch (Exception e) {
    		withdrawalService.fillAndSave(new WithdrawalNotification(),
    				withdrawalNotificationRepository, withdrawal.getUserId().toString(),
    				withdrawal.getPaymentMethodId().toString(),
    				withdrawal.getAmount().toString(), withdrawal.getStatus());
            log.error("Send event error", e);
		}
    }

    private void resend(WithdrawalNotification withdrawal) {
    	try {
    		sendQueue(withdrawal);
    		withdrawalNotificationRepository.delete(withdrawal);
    	} catch (Exception e) {
		}
    }

    private void sendQueue(WithdrawalTemplate withdrawal) {
    	// change connection to real message queue
    	jmsTemplate.convertAndSend(notificationsQueue, withdrawal);
    }
}
