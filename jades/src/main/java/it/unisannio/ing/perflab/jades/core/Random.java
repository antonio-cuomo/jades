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
package it.unisannio.ing.perflab.jades.core;

import it.unisannio.ing.perflab.jades.util.NotYetImplementedException;

/**
 * The Random class provides basic support for random number generation.
 * @author Antonio Cuomo
 */
public class Random {

    private int seed;

    private java.util.Random javaRandom;

    private static final int BASE_SEED = 0xAAA;
    private static int nextSeed = 0;
    private static final int SEED_SPACING = 100000;
    private double storedVal = 0.;

    public Random() {
        if (nextSeed == 100)
            nextSeed = 0;
        this.seed = BASE_SEED + SEED_SPACING * nextSeed;
        nextSeed++;
        javaRandom = new java.util.Random(seed);
    }

    public double exponential(double mean) {
        double d = javaRandom.nextDouble();
        double d1 = -Math.log(d) * mean;
        return d1;
    }

    public void setSeed(int n) {
        this.seed = n;
        javaRandom.setSeed(n);
    }

    public double erlang(double mean, double var) {
        throw new NotYetImplementedException();
    }

    public double hyperexponential(double mean, double var) {
        throw new NotYetImplementedException();
    }

    public double hypoexponential(double mean, double var) {
        throw new NotYetImplementedException();
    }

    public double lognormal(double mean, double stddev) {
        throw new NotYetImplementedException();
    }

    public double normal(double mean, double stddev) {
        double v1, v2, rsq, fac;
        if (storedVal == 0.) {
            do {
                v1 = 2.0 * javaRandom.nextDouble() - 1.0;
                v2 = 2.0 * javaRandom.nextDouble() - 1.0;
                rsq = v1 * v1 + v2 * v2;
            } while (rsq >= 1.0 || rsq == 0.0);
            fac = Math.sqrt(-2.0 * Math.log(rsq) / rsq);
            storedVal = v1 * fac;
            return mean + stddev * v2 * fac;
        } else {
            fac = storedVal;
            storedVal = 0.;
            return mean + stddev * fac;
        }
    }

    public double triangular(double min, double max, double mode) {
        throw new NotYetImplementedException();
    }

    public double uniform(double min, double max) {
        throw new NotYetImplementedException();
    }

    public int uniform_int(int min, int max) {
        throw new NotYetImplementedException();
    }
}
