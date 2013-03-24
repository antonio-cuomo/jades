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

import it.unisannio.ing.perflab.jades.scheduler.ProcessState;
import it.unisannio.ing.perflab.jades.scheduler.Runner;
import it.unisannio.ing.perflab.jades.scheduler.Scheduler;
import it.unisannio.ing.perflab.jades.trace.CreateTrace;
import it.unisannio.ing.perflab.jades.trace.HoldTrace;
import it.unisannio.ing.perflab.jades.trace.TerminateTrace;
import it.unisannio.ing.perflab.jades.trace.Tracer;

import java.io.Serializable;

import org.apache.commons.javaflow.Continuation;

/**
 * @author Antonio Cuomo
 * 
 *         Processes are the main abstraction of the simulator. They are active
 *         entities that can make use of resources, signal and wait for events.
 */
public abstract class Process implements Serializable, Runnable {

    private String name;

    private int identity;

    private int priority;

    private Serializable processStructure;

    private Scheduler scheduler;

    private Tracer tracer;
    
    private Runner runner;
    
    private ProcessState processState;
    
    private double wakeUpTime;
    
    

    public Runner getRunner() {
        return runner;
    }

    public void setRunner(Runner runner) {
        this.runner = runner;
    }

    public ProcessState getProcessState() {
        return processState;
    }

    public void setProcessState(ProcessState processState) {
        this.processState = processState;
    }

    public double getWakeUpTime() {
        return wakeUpTime;
    }

    public void setWakeUpTime(double wakeUpTime) {
        this.wakeUpTime = wakeUpTime;
    }

    protected Process(String name) {
        this.name = name;
        this.priority = -1; // invalid initial priority
        this.identity = -1; // invalid initial identity
    }

    void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

  
    void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    public abstract void run();

    /**
     * Adds a process to the system, to be scheduled at the current time
     * 
     * @param p
     */
    public void add(Process p) {
        if (p.priority() == -1)
            p.set_priority(this.priority);
        p.setScheduler(scheduler);
        p.setTracer(tracer);
        scheduler.addProcess(p, scheduler.getCurrentTime());
        if (tracer.isTraceEnabled())
            tracer.trace(scheduler.getCurrentTime(), this,
                    new CreateTrace(p.name(), p.identity()));
        // if(scheduler.fastScheduleCurrentProcess(scheduler.getCurrentTime())
        // ==false)
        // runner.block(); //This is redundant
    }

    /**
     * Adds a process to the system, to be scheduled after time time from the
     * current clock.
     * 
     */
    public void addAfter(Process p, double time) {
        if (p.priority() == -1)
            p.set_priority(this.priority);
        p.setScheduler(scheduler);
        scheduler.addProcess(p, scheduler.getCurrentTime() + time);
        if (tracer.isTraceEnabled())
            tracer.trace(scheduler.getCurrentTime(), this,
                    new CreateTrace(p.name(), p.identity()));
        // if(scheduler.fastScheduleCurrentProcess(scheduler.getCurrentTime())
        // ==false)
        // runner.block();

    }

    /**
     * Delays the process by amount_of_time units.
     * 
     */
    public void hold(double amount_of_time) {
        if (tracer.isTraceEnabled())
            tracer.trace(scheduler.getCurrentTime(), this, new HoldTrace(
                    amount_of_time));
        double wakeupTime = scheduler.getCurrentTime() + amount_of_time;
        if (scheduler.fastScheduleCurrentProcess(wakeupTime) == false){
            this.processState = (ProcessState.BLOCKED);
            runner.block();
        }
    }
    

    /**
     * Terminates this process
     * 
     */
    public void terminate() {
        if (tracer.isTraceEnabled())
            tracer.trace(scheduler.getCurrentTime(), this, new TerminateTrace());
    }

    /**
     * Returns the process name
     * 
     * @return the process name as a string
     */
    public String name() {
        return this.name;
    }

    /**
     * Returns the process identity. Process identities are unique and range
     * from 0 to n-1
     * 
     * @returns the integer representing the process identity
     */
    public int identity() {
        return this.identity;
    }

    /**
     * Sets the process identity
     * 
     * @param identity the new process identity
     */
    public void setIdentity(int identity) {
        this.identity = identity;
    }

    /**
     * Returns the process priority
     * 
     */
    public int priority() {
        return this.priority;
    }

    /**
     * Sets the process priority
     * 
     */
    public void set_priority(int priority) {
        this.priority = priority;
    }

    public boolean equals(Object o) {
        Process p = (Process) o;
        if (this.identity == p.identity())
            return true;
        return false;
    }

    public Object getProcessStructure() {
        return this.processStructure;
    }

    public void setProcessStructure(Object s) {
        this.processStructure = (Serializable) s;
    }

    public void traceMessage(String message){
        tracer.trace(scheduler.getCurrentTime(), this, message);
    }
}
