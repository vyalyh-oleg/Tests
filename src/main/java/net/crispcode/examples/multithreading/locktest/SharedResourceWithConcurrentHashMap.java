package net.crispcode.examples.multithreading.locktest;


import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class SharedResourceWithConcurrentHashMap implements SharedResource
{
	private static final AtomicInteger secureInvocation = new AtomicInteger(0);
	
	private String key = "a8be5597-75cb-4d85-9753-bb4e8676f886";
	
	private ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
	
	public SharedResourceWithConcurrentHashMap()
	{
		for (int i = 0; i < 1000; i++) {
			map.put(UUID.randomUUID().toString(), 0);
		}
		
		map.put(key, 0);
	}
	
	@Override
	public int getCounter()
	{
		return map.get(key);
	}
	
	@Override
	public void increment()
	{
		map.computeIfPresent(key, (k, v) -> incrementValue(v));
	}
	
	@Override
	public void decrement()
	{
		map.computeIfPresent(key, (k, v) -> decrementValue(v));
	}
	
/*
	@Override
	public void increment()
	{
		map.computeIfPresent(key, (k, v) -> ++v);
	}
	
	@Override
	public void decrement()
	{
		map.computeIfPresent(key, (k, v) -> --v);
	}
*/
	
	private int incrementValue(int value)
	{
		return ++value;
	}
	
	private int decrementValue(int value)
	{
		return --value;
	}
	
	private int incrementWithCounter(int value)
	{
		try {
			int counter = secureInvocation.getAndIncrement();
			if (counter > 0) {
				throw new IllegalStateException("Two methods in secure area!");
			}
			return ++value;
		}
		finally {
			int finishCounter = secureInvocation.getAndDecrement();
			if (finishCounter != 1) {
				throw new IllegalStateException("Two methods in secure area!");
			}
		}
	}
	
	private int decrementWithCounter(int value)
	{
		try {
			int counter = secureInvocation.getAndIncrement();
			if (counter > 0) {
				throw new IllegalStateException("Two methods in secure area!");
			}
			return --value;
		}
		finally {
			int finishCounter = secureInvocation.getAndDecrement();
			if (finishCounter != 1) {
				throw new IllegalStateException("Two methods in secure area!");
			}
		}
	}
}
