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

import java.util.Arrays;
import it.unisannio.ing.perflab.jades.core.Process;

/**
 *  The @EventHeap class provides an heap-based implementation
 *  of the @EventList interface. The heap is maintained through
 *  an array. The array is expanded when capacity is exhausted.
 * @author Antonio Cuomo
 */

public class EventHeap implements EventList {
    /** The heap array. */
    private Process[] heap;

    /** Number of elements currently in the heap.*/
    private int elems;

    /** Default initial capacity of the heap array. */
    private static final int DEFAULT_INITIAL_CAPACITY = 200;

    /** Constructs an heap of the default capacity. */
    public EventHeap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /** Constructs an heap with the specified initial size.
     *  @param initialCapacity the initial size of the array
     * */
    public EventHeap(int initialCapacity) {
        heap = new Process[initialCapacity];
        elems = 0;
    }

    
    public Process dequeue() {
        if (elems == 0) {
            return null;
        }
        if (elems == 1) {
            Process minElem = heap[0];
            heap[0] = null;
            elems--;
            return minElem;
        }
        Process minElem = heap[0];
        heap[0] = heap[elems - 1];
        elems--;
        minHeapify(0);
        return minElem;
    }

    
    public void enqueue(Process pr) {
        if (elems == heap.length) {
            resize();
        }
        elems++;
        heap[elems - 1] = pr;
        decreaseKey(elems - 1, heap[elems - 1]);
    }

    
    public int getLength() {
        return elems;
    }

    public Double peekNextKey() {
        if (elems == 0) {
            return null;
        }
        return heap[0].getWakeUpTime();
    }

    /**
     * Resizes the heap array.
     * */
    private void resize() {
        int newSize = heap.length * 2;
        heap = Arrays.copyOf(heap, newSize);
    }

    private void decreaseKey(int i, Process p) {
        heap[i] = p;
        while (i > 0 && heap[(i - 1) >> 1].getWakeUpTime() > heap[i].getWakeUpTime()) { // parent(i)
            Process temp = heap[i];
            int parent = (i - 1) >> 1; // parent(i)
            heap[i] = heap[parent];
            heap[parent] = temp;
            i = parent;
        }

    }

    private void minHeapify(int i) {
        int l = (i << 1) + 1;// left(i);
        int r = l + 1; // right(i);
        int smallest;
        if (l < elems && heap[l].getWakeUpTime() < heap[i].getWakeUpTime())
            smallest = l;
        else
            smallest = i;
        if (r < elems && heap[r].getWakeUpTime() < heap[smallest].getWakeUpTime())
            smallest = r;
        if (smallest != i) {
            Process temp = heap[i];
            heap[i] = heap[smallest];
            heap[smallest] = temp;
            minHeapify(smallest);
        }

    }

    private final int parent(int i) {
        return (i - 1) / 2;
    }

    private final int left(int i) {
        return 2 * i + 1;
    }

    private final int right(int i) {
        return 2 * i + 2;
    }
}
