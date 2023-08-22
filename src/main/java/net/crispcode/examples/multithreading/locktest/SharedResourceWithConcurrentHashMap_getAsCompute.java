/*
      Copyright 2023, Vyalyh Oleg Olegovich,
      <crispcode.net@gmail.com>

      Licensed under the Apache License, Version 2.0 (the "License"); you may not
      use this file except in compliance with the License. You may obtain a copy
      of the License at

          http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
      WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
      License for the specific language governing permissions and limitations
      under the License.
 */

package net.crispcode.examples.multithreading.locktest;


import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class SharedResourceWithConcurrentHashMap_getAsCompute implements SharedResource
{
	private static final AtomicInteger secureInvocation = new AtomicInteger(0);
	
	private String key = "a8be5597-75cb-4d85-9753-bb4e8676f886";
	
	private ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
	
	public SharedResourceWithConcurrentHashMap_getAsCompute()
	{
		for (int i = 0; i < 1000; i++) {
			map.put(UUID.randomUUID().toString(), 0);
		}
		
		map.put(key, 0);
	}
	
	@Override
	public int getCounter()
	{
		return map.computeIfPresent(key, (k, v) -> v);
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
