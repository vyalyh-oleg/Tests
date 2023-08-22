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


import java.util.concurrent.locks.ReentrantReadWriteLock;


class SharedResourceWithReadLockOnly implements SharedResource
{
	private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
	private final ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
	private final ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
	private int counter = 0;
	
	public int getCounter()
	{
		readLock.lock();
		try {
			return counter;
		}
		finally {
			readLock.unlock();
		}
		
	}
	
	public void increment()
	{
		readLock.lock();
		try {
			counter++;
		}
		finally {
			readLock.unlock();
		}
	}
	
	public void decrement()
	{
		readLock.lock();
		try {
			counter--;
		}
		finally {
			readLock.unlock();
		}
	}
	
}
