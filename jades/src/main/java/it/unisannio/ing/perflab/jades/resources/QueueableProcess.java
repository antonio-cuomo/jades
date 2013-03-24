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

public class QueueableProcess implements Comparable {
    private Process process;
    private double arrivalTime;

    public QueueableProcess(Process process, double arrivalTime) {
        this.process = process;
        this.arrivalTime = arrivalTime;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int compareTo(Object o) {
        if (o instanceof Integer)
            return -1;
        QueueableProcess qp = (QueueableProcess) o;
        if (this.process.priority() < qp.process.priority())
            return 1;
        else if (this.process.priority() > qp.process.priority())
            return -1;
        else {
            // when priority is equal compare arrival times
            if (this.arrivalTime > qp.arrivalTime)
                return 1;
            if (this.arrivalTime < qp.arrivalTime)
                return -1;
            return 0;
        }
    }

    public boolean equals(Object o) {
        if (o instanceof Integer)
            return false;
        if (o instanceof Process) {
            Process p = (Process) o;
            return this.process.identity() == p.identity();
        }
        QueueableProcess qp = (QueueableProcess) o;
        if (this.process.identity() == qp.process.identity())
            return true;
        return false;
    }

}
