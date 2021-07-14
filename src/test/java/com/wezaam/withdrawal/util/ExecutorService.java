package com.wezaam.withdrawal.util;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ExecutorService extends AbstractExecutorService {
	
	public Future<?> submit(Runnable task) {
		task.run();
		return null;
	}

	@Override
	public void shutdown() {
	}

	@Override
	public List<Runnable> shutdownNow() {
		return null;
	}

	@Override
	public boolean isShutdown() {
		return false;
	}

	@Override
	public boolean isTerminated() {
		return false;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return false;
	}

	@Override
	public void execute(Runnable command) {
	}
}
