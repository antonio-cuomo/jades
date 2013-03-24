/*******************************************************************************
 * Copyright 2012 Antonio Cuomo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.unisannio.ing.perflab.jades.eventlist;

import it.unisannio.ing.perflab.jades.core.Process;

import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * The @EventListWithHavaPQ is a simple wrapper around the Java standard @PriorityQueue
 * to conform with the @EventList interface.
 * @author Antonio Cuomo
 */
public class EventListWithJavaPQ implements EventList {

    public class ProcessComparator implements Comparator<Process> {

        public int compare(Process p1, Process p2) {
            return Double.compare(p1.getWakeUpTime(), p2.getWakeUpTime());
        }

    }

    /** The default queue initial size.*/
    private static final int DEFAULT_INITIAL_QUEUE_CAPACITY = 400;

    /** The underlying java priority queue.*/
    private PriorityQueue<Process> pq;

    /** Creates an event list, with the default initial capacity */
    public EventListWithJavaPQ() {
        pq = new PriorityQueue<Process>(DEFAULT_INITIAL_QUEUE_CAPACITY, new ProcessComparator());
    }

    /** Creates an event list, with the specified initial capacity.
     * @param initialCapacity the desired initial capacity of the queue */
    public EventListWithJavaPQ(int initialCapacity) {
        pq = new PriorityQueue<Process>(initialCapacity);
    }

    public Process dequeue() {
        return pq.remove();
    }

    public void enqueue(Process p) {
        pq.add(p);

    }

    public int getLength() {
        return pq.size();
    }

    public Double peekNextKey() {
        return pq.peek().getWakeUpTime();
    }
}
