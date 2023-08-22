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


import java.util.concurrent.locks.ReentrantLock;


class SharedResourceWithReentrantLock implements SharedResource
{
	private final ReentrantLock reentrantLock = new ReentrantLock();
	private int counter = 0;
	
	public int getCounter()
	{
		reentrantLock.lock();
		try {
			return counter;
		}
		finally {
			reentrantLock.unlock();
		}
		
	}
	
	public void increment()
	{
		reentrantLock.lock();
		try {
			counter++;
		}
		finally {
			reentrantLock.unlock();
		}
	}
	
	public void decrement()
	{
		reentrantLock.lock();
		try {
			counter--;
		}
		finally {
			reentrantLock.unlock();
		}
	}
	
}
