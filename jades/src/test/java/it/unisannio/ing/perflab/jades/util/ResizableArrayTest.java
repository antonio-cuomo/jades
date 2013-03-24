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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ResizableArrayTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testAdd() {
        ResizableArray<Integer> ra = new ResizableArray<Integer>(2);
        ra.add(1);
        ra.add(2);
        ra.add(3);
        assertEquals(new Integer(1), ra.get(0));
        assertEquals(new Integer(2), ra.get(1));
        assertEquals(new Integer(3), ra.get(2));

    }

    @Test
    public void testGet() {
        ResizableArray<Integer> ra = new ResizableArray<Integer>(2);
        exception.expect(ArrayIndexOutOfBoundsException.class);
        ra.get(0);
    }

}
