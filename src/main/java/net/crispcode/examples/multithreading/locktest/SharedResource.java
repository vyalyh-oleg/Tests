package net.crispcode.examples.multithreading.locktest;


public interface SharedResource
{
	int getCounter();
	void increment();
	void decrement();
}
