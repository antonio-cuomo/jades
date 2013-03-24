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

import static org.junit.Assert.*;

import org.junit.Test;

public class HeapQueueTest {

    @Test
    public void testDequeue() {
        HeapQueue h = new HeapQueue();
        h.enqueue(5.0);
        h.enqueue(2.0);
        assertEquals(2.0, h.dequeue());
        assertEquals(5.0, h.dequeue());
        assertNull(h.dequeue());
    }

    @Test
    public void testPeek() {
        HeapQueue h = new HeapQueue();
        assertNull(h.peek());
        h.enqueue(5.0);
        assertEquals(5.0, h.peek());
        assertEquals(5.0, h.peek());
    }

    @Test
    public void testEnqueue() {
        HeapQueue h = new HeapQueue();
        h.enqueue(5.0);
        assertEquals(1, h.getLength());
        h.enqueue(2.0);
        assertEquals(2, h.getLength());
        assertEquals(2.0, h.peek());
    }

    @Test
    public void testGetLength() {
        HeapQueue h = new HeapQueue();
        h.enqueue(5.0);
        assertEquals(1, h.getLength());
        h.enqueue(2.0);
        assertEquals(2, h.getLength());
        h.dequeue();
        assertEquals(1, h.getLength());
        h.dequeue();
        assertEquals(0, h.getLength());
    }

    @Test
    public void testRemove() {
        HeapQueue h = new HeapQueue();
        h.enqueue(5.0);
        assertEquals(1, h.getLength());
        h.enqueue(2.0);
        assertEquals(2, h.getLength());
        h.remove(2.0);
        assertEquals(1, h.getLength());
    }

    @Test
    public void testPeekTail() {
        HeapQueue h = new HeapQueue();
        h.enqueue(5.0);
        h.enqueue(2.0);
        assertEquals(5.0, h.peekTail());
    }

}
