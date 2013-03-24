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

public class ResizableArray<T> {

    private static final int DEFAULT_INITIAL_SIZE = 16;

    private T[] theArray;

    private int arraySize;

    private int arrayIndex;

    public ResizableArray() {
        this(DEFAULT_INITIAL_SIZE);
    }

    public ResizableArray(int initialSize) {
        arraySize = initialSize;
        theArray = (T[]) new Object[arraySize];
    }

    public void add(T element) {
        if (arrayIndex == arraySize) {
            resize();
        }
        theArray[arrayIndex++] = element;
    }

    private void resize() {
        arraySize = arraySize * 2;
        theArray = Arrays.copyOf(theArray, arraySize);
    }

    public T get(int index) {
        if (index < 0 || index >= arrayIndex) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return theArray[index];
    }

}
