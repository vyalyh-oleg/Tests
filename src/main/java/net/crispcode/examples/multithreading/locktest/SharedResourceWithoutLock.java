package net.crispcode.examples.multithreading.locktest;


public class SharedResourceWithoutLock implements SharedResource
{
	private int counter = 0;
	
	@Override
	public int getCounter()
	{
		return counter;
	}
	
	@Override
	public void increment()
	{
		counter++;
	}
	
	@Override
	public void decrement()
	{
		counter--;
	}
}
