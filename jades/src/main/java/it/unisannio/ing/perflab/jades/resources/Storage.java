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
package it.unisannio.ing.perflab.jades.resources;

import it.unisannio.ing.perflab.jades.core.Model;
import it.unisannio.ing.perflab.jades.core.Process;
import it.unisannio.ing.perflab.jades.scheduler.Scheduler;
import it.unisannio.ing.perflab.jades.trace.AllocateStorageTrace;
import it.unisannio.ing.perflab.jades.trace.DeallocateStorageTrace;
import it.unisannio.ing.perflab.jades.trace.Tracer;
import it.unisannio.ing.perflab.jades.util.HeapQueue;
import it.unisannio.ing.perflab.jades.util.MovingAverage;
import it.unisannio.ing.perflab.jades.util.NotYetImplementedException;
import it.unisannio.ing.perflab.jades.util.PriorityQueue;

import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.javaflow.Continuation;

/*resources that can be partially allocated to 
 processes*/
public class Storage {

    private String name;

    private int size;

    private int available;

    PriorityQueue processQueue;

    private int totalQueued;

    private Map<Integer, Double> arrivalTime;

    private Map<Integer, Double> processServiceTime;

    private double serviceTime;

    private double responseTime;

    private int completions;

    private MovingAverage queueLengthMA;

    private double waitingTime;

    private Scheduler scheduler;

    private Tracer tracer;

    public Storage(String name, int size) {
        this.name = name;
        this.size = size;
        this.processQueue = new HeapQueue();
        arrivalTime = new Hashtable<Integer, Double>();
        processServiceTime = new Hashtable<Integer, Double>();
        queueLengthMA = new MovingAverage();
        scheduler = Scheduler.getScheduler();
        scheduler.registerStorage(this);
    }

    public void allocate(int amount) {
        Process currentProcess = scheduler.getCurrentProcess();
        if (tracer.isTraceEnabled())
            tracer.trace(scheduler.getCurrentTime(), scheduler
                    .getCurrentProcess(),
                    new AllocateStorageTrace(amount, this.name));
        arrivalTime.put(currentProcess.identity(),
                scheduler.getCurrentTime());
        QueueableProcess nextQueued = (QueueableProcess) processQueue.peek();
        if (amount > available
                || (nextQueued != null && nextQueued.getProcess().priority() >= currentProcess.priority())) {
            queueLengthMA.update(scheduler.getCurrentTime(),
                    processQueue.getLength());
            processQueue.enqueue(new QueueableProcess(currentProcess,
                    scheduler.getCurrentTime()));
            totalQueued++;
            currentProcess.getRunner().block();
        }
        processServiceTime.put(currentProcess.identity(),
                scheduler.getCurrentTime());
        return;
    }

    public void deallocate(int amount) {
        Process currentProcess = scheduler
                .getCurrentProcess();
        double pServiceTime = scheduler.getCurrentTime()
                - processServiceTime.get(currentProcess.identity());
        serviceTime += pServiceTime;
        double rTime = scheduler.getCurrentTime()
                - arrivalTime.get(currentProcess.identity());
        responseTime += rTime;
        available += amount;
        completions++;
        this.arrivalTime.remove(currentProcess.identity());
        if (tracer.isTraceEnabled())
            tracer.trace(scheduler.getCurrentTime(), scheduler
                    .getCurrentProcess(),
                    new DeallocateStorageTrace(amount, this.name));
        if (processQueue.getLength() > 0) {
            queueLengthMA.update(scheduler.getCurrentTime(),
                    processQueue.getLength());
            QueueableProcess process = (QueueableProcess) processQueue
                    .dequeue();
            double qTimeStart = arrivalTime.get(process.getProcess().identity());
            double qTime = scheduler.getCurrentTime() - qTimeStart;
            waitingTime = waitingTime + qTime;
            scheduler.reactivate(process.getProcess());
            if (scheduler
                    .fastScheduleCurrentProcess(scheduler.getCurrentTime()) == false)
                currentProcess.getRunner().block();

        }
    }

    public int available() {
        return this.available;
    }

    public int capacity() {
        return this.size;
    }

    public String name() {
        return this.name;
    }

    public int qlength() {
        return processQueue.getLength();
    }

    public int allocCount() {
        throw new NotYetImplementedException();
    }

    public double busySum() {
        throw new NotYetImplementedException();
    }

    public int deallocCount() {
        throw new NotYetImplementedException();
    }

    public double elapsedTime() {
        throw new NotYetImplementedException();
    }

    public double queueLength() {
        return queueLengthMA.getCurrentValue();
    }

    public double responseTime() {
        throw new NotYetImplementedException();
    }

    public int sumAllocs() {
        throw new NotYetImplementedException();
    }

    public int sumDeAllocs() {
        throw new NotYetImplementedException();
    }

    public double utilization() {
        throw new NotYetImplementedException();
    }

    public double waitingTime() {
        throw new NotYetImplementedException();
    }

    public void reset() {
        throw new NotYetImplementedException();
    }

}
