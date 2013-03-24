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

import java.io.Serializable;
import java.util.concurrent.ExecutorService;

import org.apache.commons.javaflow.Continuation;

import it.unisannio.ing.perflab.jades.core.Process;

public class ThreadRunner implements Runnable,
        Serializable, Runner {

    private Process process;
    private Scheduler scheduler;
    private boolean isNotified;
    private ExecutorService executor;

    public ThreadRunner(Process process, Scheduler scheduler, ExecutorService executor) {
        this.process = process;
        this.scheduler = scheduler;
        this.executor = executor;
    }

   
    public void run() {
        process.run();
        if (process.identity() == 1) { // quit simulation if first process
                                          // ends
            scheduler.setSimulationEnded(true);
        }
        scheduler.getSchedulerRunner().resume();
    }

      public void suspend() {
        scheduler.getSchedulerRunner().resume();
        synchronized (this) {
            if (!isNotified) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    System.out
                            .println("Thread "
                                    + Thread.currentThread().getName()
                                    + " interrupted");
                }
            }
            this.isNotified = false;
        }
    }
    
    public void block() {
        scheduler.getSchedulerRunner().resume();
        synchronized (this) {
            if (!isNotified) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    System.out
                            .println("Thread "
                                    + Thread.currentThread().getName()
                                    + " interrupted");
                }
            }
            this.isNotified = false;
        }
    }

    public void terminate() {

    }

    public void resume() {
        synchronized (this) {
            this.isNotified = true;
            this.notify();
        }
        scheduler.getSchedulerRunner().block();
    }

    public void start() {
        // System.out.println("Starting process " + this.theProcess.name());
        executor.execute(this);
        scheduler.getSchedulerRunner().block();
    }
}
