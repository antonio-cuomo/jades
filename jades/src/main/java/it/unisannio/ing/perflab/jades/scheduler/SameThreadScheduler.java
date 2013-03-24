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
//package it.unisannio.ing.perflab.jades.scheduler;
//import it.unisannio.ing.perflab.jades.core.Process;
//import it.unisannio.ing.perflab.jades.eventlist.EventHeap;
//import it.unisannio.ing.perflab.jades.eventlist.EventList;
//import it.unisannio.ing.perflab.jades.resources.Event;
//
//import java.io.Serializable;
//
//public class SameThreadScheduler extends Scheduler implements Runnable, Serializable{
//
//	private static final int READY_QUEUE_SIZE = 400;
//
//	private EventList readyQueue;
//	
//	private Process currentProcess;
//
//	private Event eventListEmpty;	//WHAT ABOUT THIS
//	
//	private boolean simulationEnded;
//
//	private int nextProcessId;
//
//	private Process toBeReactivated;
//
//	private double currentTime;
//
//	private Runner schedulerRunner;
//	private ProcessContextManager contextManager;
//	// should a SchedulingPolicy class be created?
//	public SameThreadScheduler() {
//		readyQueue = new EventHeap(READY_QUEUE_SIZE);// new CalendarQueue();
//		//readyQueue = new EventListWithJavaPriorityQueue();
//		nextProcessId = 1;
//		this.currentTime = 0.0;
//		String provider = System.getProperty("provider");
//        if (provider.equals("continuations"))
//        	contextManager = new ContinuationBasedProcessContextManager();
//        else if (provider.equals("threads"))
//        	contextManager = new ThreadBasedProcessContextManager();
//	}
//
//	public void reset() {
//		nextProcessId = 1;
//		this.currentTime = 0.0;
//		readyQueue = new EventHeap(READY_QUEUE_SIZE);// new CalendarQueue();
//		//readyQueue = new EventListWithJavaPriorityQueue();
//		this.simulationEnded = false;
//		
//	}
//	
//	public void addProcess(Process p, double wakeupTime){
//		int procIdentity = nextProcessId++;
//		p.setIdentity(procIdentity);
//		p.setRunner(contextManager.newRunner(p));
//		readyQueue.enqueue(wakeupTime, p);
//		
//	}
//	
//	public void addToReadyProcesses(Process pr, double wakeupTime) {
//		pr.setWakeUpTime(wakeupTime);
//		readyQueue.enqueue(wakeupTime, pr);
//	}
//	
//	public void callScheduler(double wakeupTime) {
//		double nextWakeupTime = readyQueue.peekNextKey();
//		if (nextWakeupTime > wakeupTime && toBeReactivated == null){	//if current process is also the next
//			this.setCurrentTime(wakeupTime);
//		}
//		else{
//			readyQueue.enqueue(wakeupTime, currentProcess);
//			currentProcess.setProcessState(ProcessState.READY);
//			currentProcess.setWakeUpTime(wakeupTime);
//			schedule();
//		}
//	}
//	
//	public void terminateCurrentProcess() {
//		currentProcess.terminate();
//	}
//
//	public void blockCurrentProcess() {
//		currentProcess.getRunner().block();
//	}
//	
//	public Process getCurrentProcessRunner() {
//		return this.currentProcess;
//	}
//
//	
//	public double getCurrentTime() {
//		return currentTime;
//	}
//	
//	@Override
//	public void run() {
//		while(!simulationEnded){
//			//Passing the baton 
//			if (toBeReactivated != null){
//				Process oldProcess = currentProcess;
//				oldProcess.setProcessState(ProcessState.READY);
//				currentProcess = toBeReactivated;
//				toBeReactivated = null;
//				currentProcess.setProcessState(ProcessState.RUNNING);
//				currentProcess.getRunner().resume();
//			}
//			else{
//			Process dequeued = (Process) readyQueue.dequeue();
//			if (dequeued == null){	//ready queue is empty --simulation is ended!
//				simulationEnded = true;
//				return;
//			}
//			
//			Process chosenProcessRunner = dequeued;
//			double wakeupTime = dequeued.getWakeUpTime();
//			ProcessState chosenProcessState = chosenProcessRunner.getProcessState();
//			//if first scheduling, make the process start
//			if (chosenProcessState == ProcessState.CREATED) {
//				if(currentProcess !=null){
//					Process oldProcess = currentProcess;
//					oldProcess.setProcessState(ProcessState.READY);
//				}
//				currentProcess = dequeued;
//				currentTime = wakeupTime;
//				currentProcess.setProcessState(ProcessState.RUNNING);
//				currentProcess.getRunner().start();
//			} 
//			//if this is already the running process update time and go on
//			//THIS SHOULD NEVER HAPPEN, AS THIS CASE IS TREATED APART FOR OPTIMIZATION
//			else if (chosenProcessRunner.identity() == currentProcess.identity()){
//				currentTime = wakeupTime;
//				currentProcess.getRunner().resume();
//			}
//			else if (chosenProcessState == ProcessState.READY){
//				if(currentProcess !=null){
//					Process oldProcess = currentProcess;
//					oldProcess.setProcessState(ProcessState.READY);
//				}
//				currentProcess = chosenProcessRunner;
//				currentTime = wakeupTime;
//				currentProcess.setProcessState(ProcessState.RUNNING);
//				currentProcess.getRunner().resume();
//				}
//			}
//		}
//		System.out.printf("Simulation finished --time is %1.3f\n", currentTime);
//		contextManager.notifyModelFinish();
//	}
//	
//	public void markAsReady(Process p){
//		
//	}
//
//	public void setSimulationEnded(boolean b) {
//		this.simulationEnded = true;
//		
//	}
//
//	public void setCurrentTime(double time) {
//		this.currentTime = time;
//	}
//	
//	public void reactivate(Process p){
//		this.toBeReactivated = p;
//	}
//
//	public void setSchedulerRunner(Runner schedulerRunner) {
//		this.schedulerRunner = schedulerRunner;
//	}
//	
//	public Runner getSchedulerRunner() {
//		return schedulerRunner;
//	}
//
//	public void startScheduler(){
//		this.schedule();
//	}
//
//	public void schedule(){
//		//IN LOOP BASED THIS SHOULD ONLY BE currentProcess.block();	--scheduler will intervene of consequence
//		// Passing the baton
//		if(simulationEnded){
//			System.out.printf("Simulation finished --time is %1.3f\n",
//					currentTime);
//			contextManager.notifyModelFinish();	
//		}
//		
//		else if (toBeReactivated != null) {
//			Process oldProcess = currentProcess;
//			oldProcess.setProcessState(ProcessState.READY);
//			currentProcess = toBeReactivated;
//			toBeReactivated = null;
//			currentProcess.setProcessState(ProcessState.RUNNING);
//			currentProcess.getRunner().resume();
//			oldProcess.getRunner().block();
//		} 
//		else {
//			Process dequeued = (Process) readyQueue.dequeue();
//			if (dequeued == null) { // ready queue is empty --simulation is
//									// ended!
//				simulationEnded = true;
//				System.out.printf("Simulation finished --time is %1.3f\n",
//						currentTime);
//				contextManager.notifyModelFinish();
//			}
//			double wakeupTime = dequeued.getWakeUpTime();
//			// if first scheduling, make the process start
//			if (dequeued.getProcessState() == ProcessState.CREATED) {
//				Process oldProcess = currentProcess;
//				currentProcess = dequeued;
//				currentTime = wakeupTime;
//				currentProcess.setProcessState(ProcessState.RUNNING);
//				currentProcess.getRunner().start();
//				if (oldProcess != null && oldProcess.getProcessState()!=ProcessState.TERMINATED) {
//					oldProcess.setProcessState(ProcessState.READY);
//					oldProcess.getRunner().block();
//				}
//			}
//			// if this is already the running process update time and go on
//			//THIS SHOULD NEVER HAPPEN AS THIS CASE IT TREATED APART
//			else if (dequeued.identity() == currentProcess.identity()) {
//				currentTime = wakeupTime;
//				currentProcess.getRunner().resume();
//			} else if (dequeued.getProcessState() == ProcessState.READY) {
//				Process oldProcess = currentProcess;
//				currentProcess = dequeued;
//				currentTime = wakeupTime;
//				currentProcess.setProcessState(ProcessState.RUNNING);
//				currentProcess.getRunner().resume();
//				if (oldProcess != null && oldProcess.getProcessState()!=ProcessState.TERMINATED) {
//					oldProcess.setProcessState(ProcessState.READY);
//					oldProcess.getRunner().block();
//				}
//			}
//		}
//	}
//		
//	public void waitModelFinish() {
//		contextManager.waitModelFinish();
//	}
// }
