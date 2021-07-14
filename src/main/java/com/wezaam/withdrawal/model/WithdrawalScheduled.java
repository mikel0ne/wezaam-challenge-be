package com.wezaam.withdrawal.model;

import java.time.Instant;
import javax.persistence.Entity;

@Entity(name = "scheduled_withdrawals")
public class WithdrawalScheduled extends WithdrawalTemplate {

	private static final long serialVersionUID = 1L;
	
	private Instant executeAt;

    public Instant getExecuteAt() {
        return executeAt;
    }

    public void setExecuteAt(Instant executeAt) {
        this.executeAt = executeAt;
    }

}
