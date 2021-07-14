package com.wezaam.withdrawal.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public class TestUtils {
	
	public static <T> void bypassRepositorySave(JpaRepository<T, ?> repository, Class<T> type) {
		when(repository.save(any(type))).thenAnswer(new Answer<T>() {
		    @SuppressWarnings("unchecked")
			@Override
		    public T answer(InvocationOnMock invocation) throws Throwable {
		    	return (T) invocation.getArguments()[0];
		    }
		});
	}
}
