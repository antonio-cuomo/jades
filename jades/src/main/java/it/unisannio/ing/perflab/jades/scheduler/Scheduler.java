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
package it.unisannio.ing.perflab.jades.scheduler;

import it.unisannio.ing.perflab.jades.core.Process;
import it.unisannio.ing.perflab.jades.eventlist.EventHeap;
import it.unisannio.ing.perflab.jades.eventlist.EventList;
import it.unisannio.ing.perflab.jades.resources.Event;
import it.unisannio.ing.perflab.jades.resources.Facility;
import it.unisannio.ing.perflab.jades.resources.Mailbox;
import it.unisannio.ing.perflab.jades.resources.Storage;
import it.unisannio.ing.perflab.jades.statistics.Table;
import it.unisannio.ing.perflab.jades.trace.Tracer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Scheduler implements Runnable, Serializable {

    private static final int READY_QUEUE_SIZE = 400;

    private static Scheduler schedulerInstance;
    
    private EventList readyQueue;

    private Process currentProcess;

    private Event eventListEmpty;

    private Event converged;

    private boolean simulationEnded;

    private int nextProcessId;

    private Process toBeReactivated;

    private double currentTime;

    private Runner schedulerRunner;

    private ProcessContextManager contextManager;

    private List<Facility> facilities;

    private List<Storage> storages;

    private List<Mailbox> mailboxes;

    private List<Table> tables;

    private List<Process> currentProcessList;

    private Tracer tracer;

    // should a SchedulingPolicy class be created?
    public Scheduler() {
        readyQueue = new EventHeap(READY_QUEUE_SIZE);// new CalendarQueue();
        // readyQueue = new SplayEventList();
        // readyQueue = new EventListWithJavaPQ();
        // readyQueue = new SplayTree();
        nextProcessId = 1;
        this.currentTime = 0.0;
        schedulerInstance = this;
        //schedulerInstances.set(this);
        currentProcessList = new LinkedList<Process>();
    }

    public void init() {
        eventListEmpty = new Event("eventListEmpty");
        converged = new Event("eventListEmpty");
        facilities = new ArrayList<Facility>();
        storages = new ArrayList<Storage>();
        tables = new ArrayList<Table>();
        mailboxes = new ArrayList<Mailbox>();
        String provider = System.getProperty("provider");
        if (provider == null)
            contextManager = new ContinuationBasedProcessContextManager();
        else if (provider.equals("threads"))
            contextManager = new ThreadBasedProcessContextManager();
        else if (provider.equals("continuations"))
            contextManager = new ContinuationBasedProcessContextManager();
    }

    public void reset() {
        nextProcessId = 1;
        this.currentTime = 0.0;
        readyQueue = new EventHeap(READY_QUEUE_SIZE);
        this.simulationEnded = false;

    }

    public void addProcess(Process p, double wakeupTime) {
        int procIdentity = nextProcessId++;
        p.setIdentity(procIdentity);
        p.setProcessState(ProcessState.CREATED);
        p.setWakeUpTime(wakeupTime);
        Runner pr = contextManager.newRunner(p);
        p.setRunner(pr);
        if (wakeupTime > currentTime){
            readyQueue.enqueue(p);
        }
        else
            currentProcessList.add(p);
    }

    public void addProcess(Process p) {
        int procIdentity = nextProcessId++;
        p.setIdentity(procIdentity);
        p.setProcessState(ProcessState.CREATED);
        p.setWakeUpTime(currentTime);
        Runner pr = contextManager.newRunner(p);
        p.setRunner(pr);
        currentProcessList.add(p);
    }

    public void addToReadyProcesses(Process p) {
        p.setWakeUpTime(currentTime);
        currentProcessList.add(p);
    }

    public void addToReadyProcesses(Process p, double wakeupTime) {
        p.setWakeUpTime(wakeupTime);
        if (wakeupTime > currentTime)
            readyQueue.enqueue(p);
        else
            currentProcessList.add(p);
    }

    public void terminateCurrentProcess() {
        currentProcess.terminate();
    }

    public Process getCurrentProcess() {
        return this.currentProcess;
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public boolean fastScheduleCurrentProcess(double wakeupTime) {
        if (toBeReactivated != null) {
            currentProcess.setWakeUpTime(wakeupTime);
            readyQueue.enqueue(currentProcess);
            return false;
        }
        if (wakeupTime == currentTime)
            return true;
        Double nextWakeUpTime;
        if (currentProcessList.size() > 0)
            nextWakeUpTime = currentTime;
        else
            nextWakeUpTime = readyQueue.peekNextKey();
        if (nextWakeUpTime == null || nextWakeUpTime > wakeupTime) { // if
                                                                     // current
                                                                     // process
                                                                     // is also
                                                                     // the next
            this.setCurrentTime(wakeupTime);
            return true;
        } else {
            currentProcess.setWakeUpTime(wakeupTime);
            readyQueue.enqueue(currentProcess);
            return false;
        }
    }

    public void run() {
        while (!simulationEnded) {
            // Passing the baton
            if (toBeReactivated != null) {
                Process oldProcess = currentProcess;
                oldProcess.setProcessState(ProcessState.READY);
                currentProcess = toBeReactivated;
                toBeReactivated = null;
                currentProcess.setProcessState(ProcessState.RUNNING);
                currentProcess.getRunner().resume();
            } else {
                Process dequeued;
                do{
                    if (currentProcessList.size() > 0)
                        dequeued = currentProcessList.remove(0);
                    else
                        dequeued = readyQueue.dequeue();
                }
                while(dequeued != null && dequeued.getProcessState() == ProcessState.BLOCKED);
                if (dequeued == null) { // ready queue is empty --simulation is
                    // ended!
                    eventListEmpty.set();
                    
                } else {
                    // Current process from RUNNING TO READY
                    if (currentProcess != null)
                        currentProcess.setProcessState(ProcessState.READY);

                    double wakeupTime = dequeued.getWakeUpTime();
                    ProcessState previousState = dequeued.getProcessState();
                    dequeued.setProcessState(ProcessState.RUNNING);

                    currentProcess = dequeued;
                    currentTime = wakeupTime;
                    // if first scheduling, make the process start - else resume
                    // it
                    if (previousState == ProcessState.CREATED)
                        currentProcess.getRunner().start();
                    else
                        currentProcess.getRunner().resume();
                }
            }
        }
        System.out.printf("Simulation finished --time is %1.3f\n", currentTime);
        contextManager.notifyModelFinish();
    }

    public void setSimulationEnded(boolean b) {
        this.simulationEnded = true;

    }

    public void setCurrentTime(double time) {
        this.currentTime = time;
    }

    public void reactivate(Process p) {
        this.toBeReactivated = p;
    }

    public void setSchedulerRunner(Runner schedulerRunner) {
        this.schedulerRunner = schedulerRunner;
    }

    public Runner getSchedulerRunner() {
        return schedulerRunner;
    }

    public void startScheduler() {
        Runner runner = contextManager.newRunner(this);
        this.schedulerRunner = runner;
        runner.start();
    }

    public void waitModelFinish() {
        contextManager.waitModelFinish();
    }

    public void schedule() {

    }

    public Event getEventListEmpty() {
        return eventListEmpty;
    }

    public Event getConverged() {
        return converged;
    }

    public static Scheduler getScheduler() {
            return schedulerInstance;
    }

    public void registerFacility(Facility facility) {
        facilities.add(facility);
    }

    public void registerStorage(Storage storage) {
        storages.add(storage);
    }

    public void registerTable(Table table) {
        tables.add(table);
    }

    public void registerMailbox(Mailbox mailbox) {
        mailboxes.add(mailbox);
    }

    public List<Facility> getFacilities() {
        return facilities;
    }

    public List<Mailbox> getMailboxes() {
        return mailboxes;
    }

    public List<Storage> getStorages() {
        return storages;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTracer(Tracer tracer) {
        this.tracer = tracer;

    }

    public Tracer getTracer() {
        return tracer;
    }

}
