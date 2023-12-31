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
