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


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.LongStream;


public class LockTest
{
	private static final int READ_ITERATIONS = 500_000;
	private static final int WRITE_ITERATIONS = 500_000;
	private static final int READ_THREADS = 1000;
	private static final int WRITE_THREADS = 25 * 2;
	
	public static void main(String[] args) throws InterruptedException
	{
//		concurrentHashMap_computeIfAbsent_Test();
//		Thread.sleep(1000);
		
		sharedResourceTest( new SharedResourceWithoutLock() );
		Runtime.getRuntime().gc();
		Thread.sleep(1000);
		
		sharedResourceTest( new SharedResourceWithReentrantLock() );
		Runtime.getRuntime().gc();
		Thread.sleep(1000);
		
		sharedResourceTest( new SharedResourceWithReadLockOnly() );
		Runtime.getRuntime().gc();
		Thread.sleep(1000);
		
		sharedResourceTest( new SharedResourceWithReadWriteLock() );
		Runtime.getRuntime().gc();
		Thread.sleep(1000);
		
		sharedResourceTest( new SharedResourceWithSynchronized() );
		Runtime.getRuntime().gc();
		Thread.sleep(1000);
		
		sharedResourceTest( new SharedResourceWithConcurrentHashMap() );
		Runtime.getRuntime().gc();
		Thread.sleep(1000);
		
		sharedResourceTest( new SharedResourceWithConcurrentHashMap_getAsCompute() );
	}
	
	public static void sharedResourceTest(final SharedResource sharedResource) throws InterruptedException
	{
		System.out.printf("%nTest '%s' is preparing...%n", sharedResource.getClass().getSimpleName());
		System.out.println("--------------------------------------------------------------------------------");
		
		System.out.printf("Read iterations: %s%n", READ_ITERATIONS);
		System.out.printf("Write iterations: %s%n", WRITE_ITERATIONS);
		System.out.printf("Write/Read iterations ratio: %s%n%n", (double)WRITE_ITERATIONS/READ_ITERATIONS);
		
		System.out.printf("Read threads: %s%n", READ_THREADS);
		System.out.printf("Write threads: %s%n", WRITE_THREADS);
		System.out.printf("Write/Read threads ratio: %s%n%n", (double)WRITE_THREADS/READ_THREADS);
		
		final long totalReadOperations = READ_ITERATIONS * READ_THREADS;
		final long totalWriteOperations = WRITE_ITERATIONS * WRITE_THREADS;
		System.out.printf("Total read operations: %s%n", totalReadOperations);
		System.out.printf("Total write operations: %s%n", totalWriteOperations);
		System.out.printf("Total write/read operations ratio: %s%n%n", (double)totalWriteOperations/totalReadOperations);
		
		ArrayList<Thread> readThreads = new ArrayList<>();
		ArrayList<Thread> incrementThreads = new ArrayList<>();
		ArrayList<Thread> decrementThreads = new ArrayList<>();
		final long[] readThreadsFinishTimePoints = new long[READ_THREADS];
		final long[] incrThreadsFinishTimePoints = new long[WRITE_THREADS / 2];
		final long[] decrThreadsFinishTimePoints = new long[WRITE_THREADS / 2];
		final CountDownLatch latch = new CountDownLatch( READ_THREADS + WRITE_THREADS + 1 );
		
		// prepare read threads
		for (int i = 0; i < READ_THREADS; i++) {
			Thread readThread = new Thread(new ReadTask(latch, i, readThreadsFinishTimePoints, READ_ITERATIONS, sharedResource));
			readThread.setDaemon(true);
			readThreads.add(readThread);
		}
		
		// prepare write threads
		for (int i = 0; i < WRITE_THREADS / 2; i++) {
			Thread incrThread = new Thread(new IncrementTask(latch, i, incrThreadsFinishTimePoints, WRITE_ITERATIONS, sharedResource));
			incrThread.setDaemon(true);
			incrementThreads.add(incrThread);
			
			Thread decrThread = new Thread(new DecrementTask(latch, i, decrThreadsFinishTimePoints, WRITE_ITERATIONS, sharedResource));
			decrThread.setDaemon(true);
			decrementThreads.add(decrThread);
		}
		
		readThreads.forEach(Thread::start);
		incrementThreads.forEach(Thread::start);
		decrementThreads.forEach(Thread::start);
		
		while ( latch.getCount() != 1 )
			Thread.sleep(500);
		
		final long startTimePoint = System.nanoTime();
		latch.countDown();
		System.out.printf("Test has been started [%s].%n", LocalDateTime.now());
		
		for (int i = 0; i < READ_THREADS; i++) {
			readThreads.get(i).join();
		}
		
		for (int i = 0; i < WRITE_THREADS / 2; i++) {
			incrementThreads.get(i).join();
			decrementThreads.get(i).join();
		}
		
		final double endTimePoint = (System.nanoTime() - startTimePoint) / 1.e9; // in seconds
		System.out.printf("Test has been finished [%s].%n", LocalDateTime.now());
		System.out.printf("Total time: %.6f seconds%n", endTimePoint);
		
		long latestReadTimePoint = LongStream.of(readThreadsFinishTimePoints).max().getAsLong() - startTimePoint;
		double readStandardDeviation = standardDeviation(readThreadsFinishTimePoints);
		int readOutliers = numberOfOutliersInNormalDistribution(readThreadsFinishTimePoints, readStandardDeviation);
		
		long latestIncrTimePoint = LongStream.of(incrThreadsFinishTimePoints).max().getAsLong() - startTimePoint;
		double incrStandardDeviation = standardDeviation(incrThreadsFinishTimePoints);
		int incrOutliers = numberOfOutliersInNormalDistribution(incrThreadsFinishTimePoints, incrStandardDeviation);
		
		long latestDecrTimePoint = LongStream.of(decrThreadsFinishTimePoints).max().getAsLong() - startTimePoint;
		double decrStandardDeviation = standardDeviation(decrThreadsFinishTimePoints);
		int decrOutliers = numberOfOutliersInNormalDistribution(decrThreadsFinishTimePoints, decrStandardDeviation);
		
		System.out.println("Time points from the beginning (0 seconds): ");
		
		System.out.printf("\tlatestReadTimePoint: %.3f seconds, deviation: %.3f ms, outliers in deviations (normality): %s/%s (%.3f)%n", latestReadTimePoint / 1.e9, readStandardDeviation / 1.e6, readOutliers, readThreadsFinishTimePoints.length, (double)readOutliers/readThreadsFinishTimePoints.length );
		System.out.printf("\tlatestIncrTimePoint: %.3f seconds, deviation: %.3f ms, outliers in deviations (normality): %s/%s (%.3f)%n", latestIncrTimePoint / 1.e9, incrStandardDeviation / 1.e6, incrOutliers, incrThreadsFinishTimePoints.length, (double)incrOutliers/incrThreadsFinishTimePoints.length);
		System.out.printf("\tlatestDecrTimePoint: %.3f seconds, deviation: %.3f ms, outliers in deviations (normality): %s/%s (%.3f)%n", latestDecrTimePoint / 1.e9, decrStandardDeviation / 1.e6, decrOutliers, decrThreadsFinishTimePoints.length, (double)decrOutliers/decrThreadsFinishTimePoints.length );
		
		double singleOperationTime = endTimePoint / (totalReadOperations + totalWriteOperations);
		System.out.printf("%nTime for one operation: %.3f ns%n", singleOperationTime * 1e9);
		
		System.out.printf("%nSharedResource state: %s", sharedResource.getCounter());
		if (sharedResource.getCounter() != 0) {
			System.out.print(" (inconsistent)");
		}
		System.out.print("\n\n");
	}
	
	private static void concurrentHashMap_computeIfAbsent_Test() throws InterruptedException
	{
		final String key = "key1";
		ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
		final int[] sharedResource = {0};
		
		Thread t1 = new Thread(() ->
		{
			map.computeIfAbsent(key, (k) -> {
				try {
					sharedResource[0] = 1;
					Thread.sleep(5000);
					return 11;
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			});
		});
		t1.setDaemon(true);
		
		Thread t2 = new Thread(() ->
		{
			map.computeIfAbsent(key, (k) -> {
				try {
					Thread.sleep(1500);
					sharedResource[0] = 2;
					Thread.sleep(5500);
					return 12;
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			});
		});
		t2.setDaemon(true);
		
		System.out.println();
		System.out.println("Before:");
		System.out.println("Map value: " + map.get(key));
		System.out.println("Shared value: " + sharedResource[0]);
		System.out.println();
		t2.start();
		Thread.sleep(100);
		t1.start(); // the function of thread-1 will never be invoked
		
		Thread.sleep(400);
		System.out.println("After 500 ms:");
		System.out.println("Map value: " + map.get(key));
		System.out.println("Shared value: " + sharedResource[0]);
		System.out.println();
		
		Thread.sleep(1500);
		System.out.println("After 2000 ms:");
		System.out.println("Map value: " + map.get(key));
		System.out.println("Shared value: " + sharedResource[0]);
		System.out.println();
		
		Thread.sleep(4000);
		System.out.println("After 6000 ms:");
		System.out.println("Map value: " + map.get(key));
		System.out.println("Shared value: " + sharedResource[0]);
		System.out.println();
		
		
		Thread.sleep(7000);
		System.out.println("After 13000 ms:");
		System.out.println("Map value: " + map.get(key));
		System.out.println("Shared value: " + sharedResource[0]);
		System.out.println();
	}
	
	// --------------------------------------------------------------------------------
	
	static abstract class Task implements Runnable
	{
		protected final CountDownLatch latch;
		protected final int threadNumber;
		protected final long[] timePoints;
		protected final int iterations;
		protected final SharedResource resource;
		
		public Task(CountDownLatch latch, int threadNumber, long[] timePoints, int iterations, SharedResource resource)
		{
			this.latch = latch;
			this.threadNumber = threadNumber;
			this.timePoints = timePoints;
			this.iterations = iterations;
			this.resource = resource;
		}
		
		@Override
		public void run()
		{
			latch.countDown();
			try {
				latch.await();
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			for (int i = 0; i < iterations; i++) {
				doSingleIteration();
			}
			timePoints[threadNumber] = System.nanoTime();
		}
		
		abstract void doSingleIteration();
	}
	
	static class IncrementTask extends Task
	{
		public IncrementTask(CountDownLatch latch, int threadNumber, long[] timePoints, int iterations, SharedResource resource)
		{
			super(latch, threadNumber, timePoints, iterations, resource);
		}
		
		@Override
		void doSingleIteration()
		{
			this.resource.increment();
		}
	}
	
	static class DecrementTask extends Task
	{
		public DecrementTask(CountDownLatch latch, int threadNumber, long[] timePoints, int iterations, SharedResource resource)
		{
			super(latch, threadNumber, timePoints, iterations, resource);
		}
		
		@Override
		void doSingleIteration()
		{
			this.resource.decrement();
		}
	}
	
	static class ReadTask extends Task
	{
		public ReadTask(CountDownLatch latch, int threadNumber, long[] timePoints, int iterations, SharedResource resource)
		{
			super(latch, threadNumber, timePoints, iterations, resource);
		}
		
		@Override
		void doSingleIteration()
		{
			this.resource.getCounter();
		}
	}
	
	// --------------------------------------------------------------------------------
	
	static double standardDeviation(long[] dataSet)
	{
		final double average = LongStream.of(dataSet).average().getAsDouble();
		final double sumOfPow = LongStream.of(dataSet).asDoubleStream().map(value -> Math.pow((value - average), 2)).sum();
		return Math.sqrt(sumOfPow / dataSet.length);
	}
	
	static int numberOfOutliersInNormalDistribution(long[] dataSet, double standardDeviation)
	{
		final double average = LongStream.of(dataSet).average().getAsDouble();
		final double doubleDeviation = standardDeviation*2;
		long countOfValues = LongStream.of(dataSet).filter( value -> Math.abs( value - average ) > doubleDeviation ).count();
		return (int) countOfValues;
	}
}
