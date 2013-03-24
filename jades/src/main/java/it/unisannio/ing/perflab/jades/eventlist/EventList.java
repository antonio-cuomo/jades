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

/**
 *         The @EventList is probably one of the most important abstractions in
 *         sequential discrete-event simulation. All simulation events are
 *         enqueued here and dequeued according to their timestamp.
 *         Current implementations are @EventHeap, based on the binary heap and
 *         @EventListWithJavaPQ, a simple wrapper around the
 *         Java standard @PriorityQueue class. Implementation should be provided
 *         with calendar queues and splay trees.
 *         @author Antonio Cuomo
 */
public interface EventList {

    /**
     * Inserts a Process with the specified key into the eventList.
     * @param key the key that must be associated with the process runner
     * @param p the process runner that must be enqueued
     * */
    void enqueue(Process p);

    /**
     * Removes the element with the smallest key from the event list
     * and returns the process runner.
     * @return The process runner with the smallest key
     */
    Process dequeue();

    /**
     * Returns the smallest key in the list,
     * without removing the element from the event list.
     * @return the smallest key in the list
     */
    Double peekNextKey();

    /**Returns the number of elements in the list.
     * @return the number of elements in the list
     */
    int getLength();


}
