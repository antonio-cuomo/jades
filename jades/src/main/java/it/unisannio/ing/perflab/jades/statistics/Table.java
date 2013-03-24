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

import it.unisannio.ing.perflab.jades.core.Model;
import it.unisannio.ing.perflab.jades.scheduler.Scheduler;
import it.unisannio.ing.perflab.jades.util.NotYetImplementedException;

/*used to collect data during the 
 execution of a model*/
public class Table {

    private String name;

    private int countt;

    private double min;

    private double max;

    private double sum;

    private double sumSquares;

    private boolean permanent;

    private Histogram histogram;

    private double runLengthAccuracy;

    private double runLengthConfLevel;

    private double runLengthMaxTime;

    private boolean runLengthEnabled;

    private boolean calculateConfidenceIntervals;

    private boolean converged;

    private BatchStatistics batchStatistics;

    private Scheduler scheduler;

    public Table(String name) {
        this.name = name;
        init();
        scheduler = Scheduler.getScheduler();
        scheduler.registerTable(this);
    }

    private void init() {
        this.countt = 0;
        this.max = 0.0;
        this.min = 0.0;
        this.sum = 0.0;
        this.sumSquares = 0.0;
        this.permanent = false;
        this.converged = false;
        if (calculateConfidenceIntervals)
            batchStatistics.reset();
    }

    public void add_histogram(long nbucket, double min, double max) {
        this.histogram = new Histogram(nbucket, min, max);

    }

    public void confidence() {
        this.calculateConfidenceIntervals = true;
        batchStatistics = new BatchStatistics();
        batchStatistics.addConfidenceLevel(0.90);
        batchStatistics.addConfidenceLevel(0.95);
        batchStatistics.addConfidenceLevel(0.98);
    }

    public ConfidenceInterval confidenceInterval() {
        throw new NotYetImplementedException();
    }

    public int countt() {
        return this.countt;
    }

    public double cv() {
        return stddev() / mean();
    }

    public double elapsedTime() {
        throw new NotYetImplementedException();
    }

    public Histogram histogram() {
        return this.histogram;
    }

    public double max() {
        return this.max;
    }

    public double mean() {
        return this.sum / this.countt;
    }

    public double min() {
        return this.min;
    }

    public String name() {
        return this.name;
    }

    public double range() {
        return this.max - this.min;
    }

    public void record(double value) {
        this.countt++;
        this.sum += value;
        this.sumSquares += (value * value);
        if (value > max || countt == 1)
            max = value;
        if (value < min || countt == 1)
            min = value;
        if (histogram != null)
            histogram.record(value);
        if (calculateConfidenceIntervals)
            batchStatistics.record(value);
        if (runLengthEnabled) {
            if (scheduler.getCurrentTime() >= runLengthMaxTime) {
                converged = false;
                scheduler.getConverged().set();
            } else {
                double confInterval = batchStatistics
                        .getConfInterval(runLengthConfLevel);// check the
                                                             // accuracy of
                                                             // confidence
                                                             // intervals;
                if (confInterval < /* check this */runLengthAccuracy * mean()) {
                    converged = true;
                    scheduler.getConverged().set();
                }
            }
        }

    }

    public void reset() {
        if (!permanent)
            init();

    }

    public void setPermanent(boolean t) {
        this.permanent = t;

    }

    public double stddev() {
        return Math.sqrt(var());
    }

    public double sum() {
        return this.sum;
    }

    public double sumSquares() {
        return this.sumSquares;
    }

    public void tabulate(double value) {
        record(value);
    }

    public double var() {
        double mean = this.mean();
        double var = (this.sumSquares - ((this.sum * this.sum) / countt))
                / (this.countt - 1);
        // double var = (this.sumSquares + (countt * mean * mean) - (2 * mean *
        // sum)) /(this.countt -1);
        return var;
    }

    public int histogram_bucket(int n) {
        if (histogram != null)
            return histogram.bucket(n);
        return 0;
    }

    public double histogram_high() {
        if (histogram != null)
            return histogram.high();
        return 0.0;
    }

    public double histogram_low() {
        if (histogram != null)
            return histogram.low();
        return 0.0;
    }

    public double histogram_num() {
        if (histogram != null)
            return histogram.num();
        return 0.0;
    }

    public double histogram_total() {
        if (histogram != null)
            return histogram.total();
        return 0.0;
    }

    public double histogram_width() {
        if (histogram != null)
            return histogram.width();
        return 0.0;
    }

    public double conf_halfwidth(double confLevel) {
        if (calculateConfidenceIntervals) {
            double halfWidth = batchStatistics.getConfInterval(confLevel);
            if (halfWidth < Double.POSITIVE_INFINITY)
                return halfWidth;
            else
                return 0;
        }
        return 0.0;
    }

    public double conf_lower(double confLevel) {
        if (calculateConfidenceIntervals) {
            double halfWidth = batchStatistics.getConfInterval(confLevel);
            if (halfWidth < Double.POSITIVE_INFINITY)
                return batchStatistics.sampleMean() - halfWidth;
            else
                return 0.0;
        }
        return 0.0;
    }

    public double conf_upper(double confLevel) {
        if (calculateConfidenceIntervals) {
            double halfWidth = batchStatistics.getConfInterval(confLevel);
            if (halfWidth < Double.POSITIVE_INFINITY)
                return batchStatistics.sampleMean() + halfWidth;
            else
                return 0.0;
        }
        return 0.0;
    }

    public void run_length(double accuracy, double conf_level, double max_time) {
        this.runLengthEnabled = true;
        this.runLengthAccuracy = accuracy;
        this.runLengthConfLevel = conf_level;
        this.runLengthMaxTime = max_time;
        confidence();
        batchStatistics.addConfidenceLevel(conf_level);
    }

    public boolean converged() {
        return converged;
    }
}
