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

import it.unisannio.ing.perflab.jades.core.Process;
import it.unisannio.ing.perflab.jades.core.ProcessClass;
import it.unisannio.ing.perflab.jades.scheduler.ProcessState;
import it.unisannio.ing.perflab.jades.scheduler.Scheduler;
import it.unisannio.ing.perflab.jades.trace.HoldTrace;
import it.unisannio.ing.perflab.jades.trace.ReleaseFacilityTrace;
import it.unisannio.ing.perflab.jades.trace.ReserveFacilityTrace;
import it.unisannio.ing.perflab.jades.trace.Tracer;
import it.unisannio.ing.perflab.jades.trace.UseFacilityTrace;
import it.unisannio.ing.perflab.jades.util.HeapQueue;
import it.unisannio.ing.perflab.jades.util.MovingAverage;
import it.unisannio.ing.perflab.jades.util.NotYetImplementedException;

import java.io.Serializable;
import java.util.List;

public class FCFSFacility implements Facility, Serializable {

    it.unisannio.ing.perflab.jades.util.PriorityQueue processQueue;

    private String name;

    private double serviceTime;

    private int totalQueued;

    private int completions;

    private int entered;

    // private Map<Integer, Double> arrivalTime;

    private int numServers;

    private int[] servers;

    private int[] serverCompletions;

    private double[] serverSingleServiceTime;
    private double[] serverTotalServiceTime;

    private int freeServers;

    private double waitingTime;

    private double responseTime;

    private MovingAverage queueLengthMA;

    private Scheduler scheduler;

    private Tracer tracer;

    public FCFSFacility() {
        this("fac", 1);
    }

    public FCFSFacility(String name, int numServers) {
        this.name = name;
        this.servers = new int[numServers];
        for (int i = 0; i < numServers; i++) {
            servers[i] = -1;
        }
        this.numServers = numServers;
        this.freeServers = numServers;
        this.serverCompletions = new int[numServers];
        this.serverSingleServiceTime = new double[numServers];
        this.serverTotalServiceTime = new double[numServers];
        this.processQueue = new HeapQueue();
        // arrivalTime = new HashMap<Integer, Double>();
        queueLengthMA = new MovingAverage();
        reset();
        this.scheduler = Scheduler.getScheduler();
        scheduler.registerFacility(this);
        this.tracer = scheduler.getTracer();
    }

    public String name() {
        return this.name;
    }

    public void use(double amount_of_time) {
        if (tracer.isTraceEnabled())
            tracer.trace(scheduler.getCurrentTime(), scheduler
                    .getCurrentProcess(),
                    new UseFacilityTrace(name, amount_of_time));
        reserve();
        if (tracer.isTraceEnabled())
            tracer.trace(scheduler.getCurrentTime(), scheduler
                    .getCurrentProcess(), new HoldTrace(
                    amount_of_time));
        double wakeupTime = scheduler.getCurrentTime() + amount_of_time;
        if (scheduler.fastScheduleCurrentProcess(wakeupTime) == false){
            scheduler.getCurrentProcess().setProcessState(ProcessState.BLOCKED);
            scheduler.getCurrentProcess().getRunner().block();
        }
        release();
    }

    public int reserve() {
        Process currentProcess = scheduler
                .getCurrentProcess();
        if (tracer.isTraceEnabled())
            tracer.trace(scheduler.getCurrentTime(), scheduler
                    .getCurrentProcess(),
                    new ReserveFacilityTrace(this.name));
        // arrivalTime.put(currentProcessRunner.getProcess().identity(),
        // scheduler.getCurrentTime());
        entered++;
        if (freeServers == 0) {
            totalQueued++;
            queueLengthMA.update(scheduler.getCurrentTime(),
                    processQueue.getLength());
            // Enqueue in the facility queue and suspend
            processQueue.enqueue(new QueueableProcess(currentProcess,
                    scheduler.getCurrentTime()));
            currentProcess.setProcessState(ProcessState.BLOCKED);
            currentProcess.getRunner().block();
        }
        int serverIndex = acquireServer(currentProcess);
        serverSingleServiceTime[serverIndex] = scheduler.getCurrentTime();
        return serverIndex;
    }

    private int acquireServer(Process process) {
        // find free one -- this would be faster with a list
        freeServers--;
        int firstFreeServer = 0;
        while (servers[firstFreeServer] != -1)
            firstFreeServer++;
        servers[firstFreeServer] = process.identity();
        return firstFreeServer;
    }

    public void release() {
        Process currentProcess = scheduler
                .getCurrentProcess();
        // Find the server used by this process --data structures may be
        // improved
        int foundServer = -1;
        for (int i = 0; i < servers.length; i++) {
            if (servers[i] == currentProcess.identity())
                foundServer = i;
        }
        if (foundServer == -1)
            throw new RuntimeException(
                    "Process tried to release server it didn't own");
        servers[foundServer] = -1;
        serverCompletions[foundServer]++;
        serverSingleServiceTime[foundServer] = scheduler.getCurrentTime()
                - serverSingleServiceTime[foundServer];
        serverTotalServiceTime[foundServer] += serverSingleServiceTime[foundServer];
        serviceTime += serverSingleServiceTime[foundServer];
        // double rTime = scheduler.getCurrentTime() -
        // arrivalTime.get(currentProcessRunner.getProcess().identity());
        // responseTime += rTime;
        freeServers++;
        completions++;
        if (tracer.isTraceEnabled())
            tracer.trace(scheduler.getCurrentTime(), scheduler
                    .getCurrentProcess(),
                    new ReleaseFacilityTrace(this.name));
        if (processQueue.getLength() > 0) {
            queueLengthMA.update(scheduler.getCurrentTime(),
                    processQueue.getLength());
            QueueableProcess process = (QueueableProcess) processQueue
                    .dequeue();
            // double qTimeStart
            // =arrivalTime.get(process.getProcessRunner().getProcess().identity());
            // double qTime =scheduler.getCurrentTime() - qTimeStart;
            // waitingTime = waitingTime + qTime;
            scheduler.reactivate(process.getProcess());
            if (scheduler
                    .fastScheduleCurrentProcess(scheduler.getCurrentTime()) == false){
                currentProcess.setProcessState(ProcessState.BLOCKED);
                currentProcess.getRunner().block();
             }
        }
    }

    public void release(int serverIndex) {
        Process currentProcess = scheduler
                .getCurrentProcess();
        int owner = servers[serverIndex];
        if (owner == -1)
            throw new RuntimeException("Trying to release a free server");
        servers[serverIndex] = -1;
        freeServers++;
        completions++;
        // this.arrivalTime.put(owner, null);
        if (tracer.isTraceEnabled())
            tracer.trace(scheduler.getCurrentTime(), scheduler
                    .getCurrentProcess(),
                    new ReleaseFacilityTrace(this.name));
        if (processQueue.getLength() > 0) {
            freeServers--;
            QueueableProcess process = (QueueableProcess) processQueue
                    .dequeue();
            // double qTimeStart
            // =arrivalTime.get(process.getProcessRunner().getProcess().identity());
            // double qTime =scheduler.getCurrentTime() - qTimeStart;
            // waitingTime = waitingTime + qTime;
            scheduler.reactivate(process.getProcess());
            if (scheduler
                    .fastScheduleCurrentProcess(scheduler.getCurrentTime()) == false){
                currentProcess.setProcessState(ProcessState.BLOCKED);
                currentProcess.getRunner().block();
            }
        }
    }

    public void reset() {
        serviceTime = 0.0;
        totalQueued = 0;
        waitingTime = 0;
        responseTime = 0;
        completions = 0;
    }

    public long timed_reserve(double timeout) {
        throw new NotYetImplementedException();
        // return 0;
        /*
         * Process currentProcess = scheduler.getCurrentProcess();
         * 
         * Tracer t =Tracer.getTracer(); if(t.isTraceEnabled()) t.trace(new
         * ReserveFacilityTrace(this.name));
         * arrivalTime.set(currentProcess.identity(),
         * scheduler.getCurrentTime()); while (freeServers == 0){
         * queueLengthMA.update(scheduler.getCurrentTime(),
         * processQueue.getLength()); totalQueued++; processQueue.enqueue(new
         * QueueableProcess(currentProcess, scheduler.getCurrentTime()));
         * scheduler.addToReadyProcesses(currentProcess,
         * scheduler.getCurrentTime() + timeout); Continuation.suspend(); }
         */

        /*
         * This must be reengineered if (freeServers == 0) {
         * queueLengthMA.update(re.clock(), processQueue.getLength()); QueueLock
         * l = new QueueLock(); processQueue.enqueue(new
         * QueueableProcess(currentProcess, re.clock(),lock)); totalQueued++;
         * Semaphore semaphore = re.getSemaphore(currentProcess);
         * re.enqueue(currentProcess, re.clock() + timeout); Semaphore s =
         * re.schedule(currentProcess); if(s == null){//Special case of
         * scheduler awakening //delete from facility queue
         * processQueue.remove(currentProcess); //signal unavailability of the
         * facility return -1; } try { semaphore.acquire(); } catch
         * (InterruptedException e) { e.printStackTrace(); } //if we are out of
         * the semaphore because the scheduler awakened us if(freeServers == 0){
         * //delete from facility queue processQueue.remove(currentProcess);
         * //signal unavailability of the facility return -1; } else{ //else we
         * are out of the semaphore because the facility has accepted our
         * reservation re.unschedule(currentProcess); int serverIndex =
         * acquireServer(currentProcess); serverSingleServiceTime[serverIndex] =
         * re.clock(); return serverIndex; } } else { int serverIndex =
         * acquireServer(currentProcess); serverSingleServiceTime[serverIndex] =
         * re.clock(); return serverIndex; }
         */
    }

    public Process first_process() {
        throw new NotYetImplementedException();
        
    }

    public Process last_process() {
        throw new NotYetImplementedException();
        
    }

    public List processList() {
        return null;
    }

    public int completions() {
        return completions;
    }

    public long numServers() {
        return numServers;
    }

    public int num_busy() {
        return numServers - freeServers;
    }

    public int qlength() {
        return processQueue.getLength();
    }

    public double queueLength() {
        return queueLengthMA.getCurrentValue();
    }

    public double responseTime() {
        return responseTime / entered;
    }

    public ServerStats serverStats(long n) {
        int i = (int) n;
        return new ServerStats(serverCompletions[i], serverTotalServiceTime[i],
                scheduler.getCurrentTime());
    }

    public double serviceTime() {
        return serviceTime / (double) completions;
    }

    public FacilityStats stats(long n) {
        throw new NotYetImplementedException();
    }

    public ServerStats stats(ProcessClass cl) {
        throw new NotYetImplementedException();
    }

    public double throughput() {
        return (double) completions / scheduler.getCurrentTime();
    }

    public double timeslice() {
        return 0;
    }

    public void set_timeslice(double slice) {

    }

    public String type() {
        return "fcfs";
    }

    public double utilization() {
        return serviceTime / scheduler.getCurrentTime();
    }

}
