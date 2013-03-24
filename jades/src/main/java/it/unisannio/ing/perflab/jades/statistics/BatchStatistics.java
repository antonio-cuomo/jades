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

import it.unisannio.ing.perflab.jades.util.ResizableArray;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class BatchStatistics {

    ResizableArray<Double> observations;
    private int numObservations = 0;

    int nIMinus1 = 600;
    int nI = 800;

    private List<Double> confLevels;
    private double sampleMean;

    private Hashtable<Double, Double> calculatedIntervals;

    public BatchStatistics() {
        observations = new ResizableArray<Double>();
        this.confLevels = new ArrayList<Double>();
        calculatedIntervals = new Hashtable<Double, Double>();
    }

    public void addConfidenceLevel(double confLevel) {
        if (confLevels.contains(confLevel) == false) {
            confLevels.add(confLevel);
            calculatedIntervals.put(confLevel, Double.POSITIVE_INFINITY);
        }
    }

    public void record(double value) {
        numObservations++;
        observations.add(value);
        if (numObservations > nI) {
            lawCarsonProcedure();
        }
    }

    public double getConfInterval(double confLevel) {
        Double confIntervalObj = calculatedIntervals.get(confLevel);
        if (confIntervalObj == null)
            return Double.POSITIVE_INFINITY;
        else
            return confIntervalObj.doubleValue();
    }

    public double sampleMean() {
        return this.sampleMean;
    }

    public int getUsedObservations() {
        return nIMinus1;
    }

    public void lawCarsonProcedure() {
        double[] batchMeans400 = partition((numObservations / 800) * 800, 400);
        double serCorr400 = estimateSerialCorrelation(batchMeans400);
        boolean executeStep3 = serCorr400 > 0 && serCorr400 < 0.4;
        boolean executeStep4 = serCorr400 < 0;
        if (executeStep3) {
            double[] batchMeans200 = partition((numObservations / 800) * 800,
                    200);
            double serCorr200 = estimateSerialCorrelation(batchMeans200);
            if (serCorr200 <= serCorr400)
                executeStep4 = true;
        }
        if (executeStep4) {
            double[] batchMeans40 = partition((numObservations / 800) * 800, 40);
            sampleMean = 0.0;
            for (int i = 0; i < 40; i++) {
                sampleMean += batchMeans40[i];
            }
            sampleMean /= 40;
            double var = 0.0;
            for (int i = 0; i < 40; i++) {
                double temp = (batchMeans40[i] - sampleMean);
                var += temp * temp;
            }
            var /= 40.0 * 39.0;

            for (double confLevel : confLevels) {
                double tValue = InverseT.tValue((1.0 - confLevel) / 2.0, 39);
                double interval = tValue * Math.sqrt(var);
                if (interval / sampleMean < Double.POSITIVE_INFINITY)
                    calculatedIntervals.put(confLevel, interval);
            }
        }
        int temp = nI;
        nI = 2 * nIMinus1;
        nIMinus1 = temp;
    }

    private double estimateSerialCorrelation(double[] batchMeans) {
        int n = batchMeans.length;
        double sampleMean = 0.0;
        for (int i = 0; i < n; i++) {
            sampleMean += batchMeans[i];
        }
        sampleMean /= n;
        double num = 0.0;
        for (int i = 0; i < n - 1; i++) {
            num += ((batchMeans[i] - sampleMean) * (batchMeans[i + 1] - sampleMean));

        }
        double den = 0.0;
        for (int i = 0; i < n; i++) {
            double temp = (batchMeans[i] - sampleMean);
            den += temp * temp;
        }
        double ro = num / den;

        int k = n / 2;

        double sampleMean1 = 0.0;
        for (int i = 0; i < k; i++) {
            sampleMean1 += batchMeans[i];
        }
        sampleMean1 /= k;
        double num1 = 0.0;
        for (int i = 0; i < k - 1; i++) {
            num1 += ((batchMeans[i] - sampleMean1) * (batchMeans[i + 1] - sampleMean1));

        }
        double den1 = 0.0;
        for (int i = 0; i < k; i++) {
            double temp = (batchMeans[i] - sampleMean1);
            den1 += temp * temp;
        }

        double ro1 = num1 / den1;

        double sampleMean2 = 0.0;
        for (int i = k; i < n; i++) {
            sampleMean2 += batchMeans[i];
        }

        sampleMean2 /= k;
        double num2 = 0.0;
        for (int i = k; i < n - 1; i++) {
            num2 += ((batchMeans[i] - sampleMean2) * (batchMeans[i + 1] - sampleMean2));

        }

        double den2 = 0.0;
        for (int i = k; i < n; i++) {
            double temp = (batchMeans[i] - sampleMean2);
            den2 += temp * temp;
        }

        double ro2 = num2 / den2;
        return 2 * ro - (ro1 + ro2) / 2;

    }

    // returns the batch means
    private double[] partition(int nObs, int nBatches) {
        double[] batchMeans = new double[nBatches];
        int batchSize = nObs / nBatches;
        for (int i = 0; i < nObs; i++) {
            double observation = observations.get(i);
            batchMeans[i / batchSize] += observation;
        }
        for (int i = 0; i < nBatches; i++) {
            batchMeans[i] /= batchSize;
        }
        return batchMeans;
    }

    public void reset() {
        this.numObservations = 0;
        this.sampleMean = 0.0;
        this.calculatedIntervals.clear();
    }

}
