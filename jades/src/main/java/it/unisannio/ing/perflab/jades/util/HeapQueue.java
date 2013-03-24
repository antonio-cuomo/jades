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
package it.unisannio.ing.perflab.jades.util;

import java.util.Arrays;

public class HeapQueue implements PriorityQueue {

    private Comparable[] heap;
    private int elems;
    private static final int DEFAULT_INITIAL_SIZE = 16;

    public HeapQueue() {
        this(DEFAULT_INITIAL_SIZE);
    }

    public HeapQueue(int initialSize) {
        heap = new Comparable[initialSize];
        elems = 0;
    }

    public Comparable dequeue() {
        if (elems == 0) {
            return null;
        }
        Comparable minElem = heap[0];
        heap[0] = heap[elems - 1];
        elems--;
        minHeapify(0);
        return minElem;
    }

    public Comparable peek() {
        return heap[0];
    }

    public void enqueue(Comparable e) {
        if (elems == heap.length) {
            resize();
        }
        elems++;
        heap[elems - 1] = Integer.MAX_VALUE;
        try {
            decreaseKey(elems - 1, e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void resize() {
        int newSize = heap.length * 2;
        heap = Arrays.copyOf(heap, newSize);
    }

    private void decreaseKey(int i, Comparable e) throws Exception {
        /*
         * if (e.compareTo(heap[i])>0) throw new Exception
         * ("New key bigger than current key");
         */
        heap[i] = e;
        while (i > 0 && heap[parent(i)].compareTo(heap[i]) > 0) {
            Comparable temp = heap[i];
            int parent = parent(i);
            heap[i] = heap[parent];
            heap[parent] = temp;
            i = parent;
        }

    }

    private void minHeapify(int i) {
        int l = left(i);
        int r = right(i);
        int smallest;
        if (l < elems && heap[l].compareTo(heap[i]) < 0) {
            smallest = l; 
        }
        else {
            smallest = i;
        }
        if (r < elems && heap[r].compareTo(heap[smallest]) < 0) {
            smallest = r;
        }
        if (smallest != i) {
            Comparable temp = heap[i];
            heap[i] = heap[smallest];
            heap[smallest] = temp;
            minHeapify(smallest);
        }

    }

    private int parent(int i) {
        return (i - 1) / 2;
    }

    private int left(int i) {
        return 2 * i + 1;
    }

    private int right(int i) {
        return 2 * i + 2;
    }

    public int getLength() {
        return elems;
    }

    public Object remove(Object o) {
        for (int i = 0; i < elems; i++) {
            if (heap[i].equals(o)) {
                Comparable match = heap[i];
                heap[i] = heap[elems - 1];
                elems--;
                minHeapify(i);
                return match;
            }

        }
        return null;
    }

    public Comparable peekTail() {
        // the costly way...
        Comparable tail = null;
        for (int i = 0; i < this.elems; i++) {
            if (tail == null || tail.compareTo(heap[i]) < 0) {
                tail = heap[i];
            }
        }
        return tail;
    }

}
