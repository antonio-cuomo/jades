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
import it.unisannio.ing.perflab.jades.scheduler.ProcessState;
import it.unisannio.ing.perflab.jades.scheduler.Scheduler;
import it.unisannio.ing.perflab.jades.trace.SetEventTrace;
import it.unisannio.ing.perflab.jades.trace.Tracer;
import it.unisannio.ing.perflab.jades.util.HeapQueue;
import it.unisannio.ing.perflab.jades.util.NotYetImplementedException;
import it.unisannio.ing.perflab.jades.util.PriorityQueue;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.commons.javaflow.Continuation;

public class Event {

    private String name;

    private boolean occurred;

    private List<QueueableProcess> waitingList;

    private PriorityQueue waitingQueue;

    private Hashtable<Integer, Boolean> processToEventOccurred;

    private Scheduler scheduler;

    private Tracer tracer;

    public Event(String name) {
        this.name = name;
        occurred = false;
        waitingList = new ArrayList<QueueableProcess>();
        waitingQueue = new HeapQueue(10);
        processToEventOccurred = new Hashtable<Integer, Boolean>();
        this.scheduler = Scheduler.getScheduler();
        this.tracer = scheduler.getTracer();
    }

    public void untimed_wait() {
        if (occurred) {
            occurred = false;
            return;
        }
        Process currentProcess = scheduler
                .getCurrentProcess();
        waitingList.add(new QueueableProcess(currentProcess, 0.0));
        currentProcess.setProcessState(ProcessState.BLOCKED);
        currentProcess.getRunner().block();
    }

    public boolean timed_wait(double timeout) {
        throw new NotYetImplementedException();
    }

    public void queue() {
        untimed_queue();
    }

    public void set() {
        if (tracer.isTraceEnabled())
            tracer.trace(scheduler.getCurrentTime(), scheduler
                    .getCurrentProcess(), new SetEventTrace(
                    this.name));
        if (occurred)
            return;
        int waitingProcesses = waitingList.size() + waitingQueue.getLength();
        if (waitingProcesses == 0) {
            occurred = true;
            return;
        } else {
            occurred = false;
            Process currentProcess = scheduler
                    .getCurrentProcess();
            for (QueueableProcess qp : waitingList) {
                processToEventOccurred.put(currentProcess.identity(), true);
                scheduler.addToReadyProcesses(qp.getProcess());
            }
            QueueableProcess process = (QueueableProcess) waitingQueue
                    .dequeue();
            if (process != null) {
                processToEventOccurred.put(process.getProcess().identity(), false);
                // enqueue the current process
                scheduler.addToReadyProcesses(process.getProcess());
            }
        }
    }

    public boolean timed_queue(double timeout) {
        throw new NotYetImplementedException();
    }

    public void untimed_queue() {
        if (occurred) {
            occurred = false;
            return;
        }
        Process currentProcess = scheduler
                .getCurrentProcess();
        waitingQueue.enqueue(new QueueableProcess(currentProcess,
                scheduler.getCurrentTime()));
        currentProcess.setProcessState(ProcessState.BLOCKED);
        currentProcess.getRunner().block();
    }

    public void clear() {
        occurred = false;
    }
}
