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
