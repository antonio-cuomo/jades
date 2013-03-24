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
package it.unisannio.ing.perflab.jades.statistics;

public class Histogram {

    private long nbuckets;

    private int[] buckets;

    private double min;

    private double max;

    private double width;

    private int total;

    public Histogram(long nbuckets, double min, double max) {
        buckets = new int[(int) nbuckets + 2];
        this.nbuckets = nbuckets;
        this.min = min;
        this.max = max;
        this.width = (max - min) / nbuckets;

    }

    public void record(double value) {
        total++;
        if (value < min)
            buckets[0]++;
        else if (value > max)
            buckets[(int) (nbuckets + 1)]++;
        int index = (int) Math.floor((value - min) / width);
        buckets[index + 1]++;
    }

    public int bucket(int n) {
        return buckets[n];
    }

    public double high() {
        return max;
    }

    public double low() {
        return min;
    }

    public double num() {
        return nbuckets;
    }

    public double total() {
        return this.total;
    }

    public double width() {
        return this.width;
    }
}
