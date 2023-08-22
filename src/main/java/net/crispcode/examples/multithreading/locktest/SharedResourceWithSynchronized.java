package net.crispcode.examples.multithreading.locktest;


class SharedResourceWithSynchronized implements SharedResource
{
	private final Object syncObj = new Object();
	private int counter = 0;
	
	public int getCounter()
	{
		synchronized (syncObj) {
			return counter;
		}
	}
	
	public void increment()
	{
		synchronized (syncObj) {
			counter++;
		}
	}
	
	public void decrement()
	{
		synchronized (syncObj) {
			counter--;
		}
	}
}
