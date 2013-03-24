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
package it.unisannio.ing.perflab.jades.trace;

import java.util.Locale;

public abstract class Trace {

    protected int traceId;
    protected double time;
    protected String processName;
    protected int processId;
    protected int processPriority;

    public abstract String toString();

    public void setTraceId(int traceId) {
        this.traceId = traceId;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public void setProcessPriority(int processPriority) {
        this.processPriority = processPriority;
    }

    /*
     * private double time; private String processName; private int processId;
     * private int processPriority; private String status; public Trace(double
     * clock, String name, int identity, int priority, String status) {
     * this.time=clock; this.processName=name; this.processId=identity;
     * this.processPriority=priority; this.status=status; }
     * 
     * public String toString(){ String format = "%9.3f\t%s\t%d\t%d\t%s"; return
     * String.format(Locale.ENGLISH,format, time, processName, processId,
     * processPriority, status); //return time+ "\t"+processName+
     * "\t"+processId+ "\t"+processPriority+ "\t"+status; }
     */

    public static String traceHeader() {
        String format = "\t    time\tprocess\t\tid\tpri\tstatus";
        return String.format(Locale.ENGLISH, format);

    }
}
