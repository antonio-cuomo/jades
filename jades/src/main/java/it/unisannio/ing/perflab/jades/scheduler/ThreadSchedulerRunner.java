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

import org.apache.commons.javaflow.Continuation;

import it.unisannio.ing.perflab.jades.core.Process;

class ThreadSchedulerRunner implements Runnable, Serializable,
        Runner {

    private Runnable runnable;
    private Thread processThread;
    private boolean isNotified;

    public ThreadSchedulerRunner(Runnable runnable) {
        this.runnable = runnable;
        processThread = new Thread(this, "Scheduler");
    }

    public void start() {
        processThread.start();
    }

    public void run() {
        runnable.run();
    }

    public void block() {
        // System.out.println("Blocking the scheduler");
        synchronized (this) {
            if (!isNotified) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            this.isNotified = false;
        }
    }

    public void terminate() {
        processThread.interrupt();
    }

    public void resume() {
        // System.out.println("Resuming the scheduler");
        synchronized (this) {
            this.isNotified = true;
            this.notify();
        }
    }
}
