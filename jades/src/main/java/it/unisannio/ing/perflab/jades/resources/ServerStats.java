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

public class ServerStats {

    private int completions;

    private double serviceTime;

    private double totalTime;

    public ServerStats(int completions, double serviceTime, double totalTime) {
        this.completions = completions;
        this.serviceTime = serviceTime;
        this.totalTime = totalTime;
    }

    public int completions() {
        return this.completions;
    }

    public double serviceTime() {
        return serviceTime / (double) completions;
    }

    public double throughput() {
        return completions / totalTime;
    }

    public double utilization() {
        return serviceTime / totalTime;
    }

}
