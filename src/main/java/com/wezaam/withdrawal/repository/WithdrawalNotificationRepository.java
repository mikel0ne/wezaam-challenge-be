package com.wezaam.withdrawal.repository;

import com.wezaam.withdrawal.model.WithdrawalNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalNotificationRepository extends JpaRepository<WithdrawalNotification, Long> {
}
