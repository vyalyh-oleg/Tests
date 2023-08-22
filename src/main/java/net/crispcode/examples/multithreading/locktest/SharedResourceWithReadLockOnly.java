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
