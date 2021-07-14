package com.wezaam.withdrawal.rest;

import com.wezaam.withdrawal.Application;
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
public class UserControllerTest {

	@Autowired
	private MockMvc mvc;
	
	@Test
	void shouldRetrieveUserById() throws Exception{
		mvc.perform(get("/users/{id}", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))

				.andExpect(MockMvcResultMatchers.status().isOk())

	            .andExpect(jsonPath("$.id").exists())
	            .andExpect(jsonPath("$.firstName").exists())
	            .andExpect(jsonPath("$.paymentMethods").isArray())
	            .andExpect(jsonPath("$.paymentMethods[*].id").exists())
	            .andExpect(jsonPath("$.paymentMethods[*].name").exists())
	            .andExpect(jsonPath("$.maxWithdrawalAmount").exists());
	}
	
	@Test
	public void shouldRetrieveAllWithdrawals() throws Exception{
		mvc.perform(get("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))

				.andExpect(MockMvcResultMatchers.status().isOk())
				
	            .andExpect(jsonPath("$").isArray())
	            .andExpect(jsonPath("$[*].id").exists())
	            .andExpect(jsonPath("$[*].firstName").exists())
	            .andExpect(jsonPath("$[*].paymentMethods").isArray())
	            .andExpect(jsonPath("$[*].paymentMethods[*].id").exists())
	            .andExpect(jsonPath("$[*].paymentMethods[*].name").exists())
	            .andExpect(jsonPath("$[*].maxWithdrawalAmount").exists());
	}
}
