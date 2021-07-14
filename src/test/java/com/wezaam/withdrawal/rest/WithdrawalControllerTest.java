
package com.wezaam.withdrawal.rest;

import com.wezaam.withdrawal.Application;
import com.wezaam.withdrawal.model.WithdrawalStatus;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Application.class)
@AutoConfigureMockMvc
@Sql(scripts = {"/sql_test/delete.sql", "/import.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class WithdrawalControllerTest {

	@Autowired
	private MockMvc mvc;
	
	String userId = "1";
	String paymentMethodId = "1";
	String amount = "50.0";
	String executeAt = "ASAP";
	
	@Test
	void shouldCreateWithdrawal() throws Exception{
		mvc.perform(post("/withdrawals").param("userId", userId)
				.param("paymentMethodId", paymentMethodId)
				.param("amount", amount).param("executeAt", executeAt)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))

				.andExpect(MockMvcResultMatchers.status().isCreated())
				
	            .andExpect(jsonPath("$.id").exists())
	            .andExpect(jsonPath("$.transactionId").exists())
	            .andExpect(jsonPath("$.amount").value(amount))
	            .andExpect(jsonPath("$.createdAt").exists())
	            .andExpect(jsonPath("$.userId").value(userId))
	            .andExpect(jsonPath("$.paymentMethodId").value(paymentMethodId))
	            .andExpect(jsonPath("$.status").value(WithdrawalStatus.PROCESSING.name()));
	}
	
	@Test
	void shouldCreateWithdrawalScheduled() throws Exception{
		String executeAt = Instant.now().toString();
		
		mvc.perform(post("/withdrawals").param("userId", userId)
				.param("paymentMethodId", paymentMethodId)
				.param("amount", amount).param("executeAt", executeAt)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))

				.andExpect(MockMvcResultMatchers.status().isCreated())
				
	            .andExpect(jsonPath("$.id").exists())
	            .andExpect(jsonPath("$.transactionId").isEmpty())
	            .andExpect(jsonPath("$.amount").value(amount))
	            .andExpect(jsonPath("$.createdAt").exists())
	            .andExpect(jsonPath("$.executeAt").value(executeAt))
	            .andExpect(jsonPath("$.userId").value(userId))
	            .andExpect(jsonPath("$.paymentMethodId").value(paymentMethodId))
	            .andExpect(jsonPath("$.status").value(WithdrawalStatus.PENDING.name()));
	}
	
	@Test
	void shouldRespondBadRequest() throws Exception{
		mvc.perform(post("/withdrawals")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))

				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	void shouldRespondUserNotFound() throws Exception{
		mvc.perform(post("/withdrawals").param("userId", "-1")
				.param("paymentMethodId", paymentMethodId)
				.param("amount", amount).param("executeAt", executeAt)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))

				.andExpect(MockMvcResultMatchers.status().isNotFound());
	}
	
	@Test
	void shouldRespondPaymentMethodNotFound() throws Exception{
		mvc.perform(post("/withdrawals").param("userId", userId)
				.param("paymentMethodId", "-1")
				.param("amount", amount).param("executeAt", executeAt)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))

				.andExpect(MockMvcResultMatchers.status().isNotFound());
	}
	
	@Test
	void shouldRespondConflict() throws Exception{
		mvc.perform(post("/withdrawals").param("userId", userId)
				.param("paymentMethodId", "3")
				.param("amount", amount).param("executeAt", executeAt)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))

				.andExpect(MockMvcResultMatchers.status().isConflict());
	}
	
	@Test
	public void shouldRetrieveAllWithdrawals() throws Exception{
			mvc.perform(get("/withdrawals")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))

					.andExpect(MockMvcResultMatchers.status().isOk())
					
		            .andExpect(jsonPath("$").isArray())
		            .andExpect(jsonPath("$[*].id").exists())
		            .andExpect(jsonPath("$[*].transactionId").exists())
		            .andExpect(jsonPath("$[*].amount").exists())
		            .andExpect(jsonPath("$[*].createdAt").exists())
		            .andExpect(jsonPath("$[*].userId").exists())
		            .andExpect(jsonPath("$[*].paymentMethodId").exists())
		            .andExpect(jsonPath("$[*].status").exists());
	}
}
