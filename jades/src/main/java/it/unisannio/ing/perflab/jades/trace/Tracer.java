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

import java.io.PrintStream;

import it.unisannio.ing.perflab.jades.core.Process;
import it.unisannio.ing.perflab.jades.scheduler.Scheduler;

public class Tracer {

    private int traceId;

    private boolean traceEnabled = false;

    private PrintStream traceStream;

    public Tracer(PrintStream traceStream) {
        traceId = 0;
        this.traceStream = traceStream;
    }

    public void reset() {
        traceId = 0;
    }

    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    public void setTraceEnabled(boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
    }

    public void trace(double time, Process current, Trace t) {
        t.setTraceId(traceId++);
        t.setTime(time);
        if (current == null) {
            t.setProcessPriority(-1);
            t.setProcessId(-1);
            t.setProcessName("NULL");
        } else {
            t.setProcessPriority(current.priority());
            t.setProcessId(current.identity());
            t.setProcessName(current.name());
        }
        traceStream.println(t.toString());
    }

    public void trace(double time, Process current, String msg) {
        GenericMsgTrace t = new GenericMsgTrace(msg);
        t.setTraceId(traceId++);
        t.setTime(time);
        if (current == null) {
            t.setProcessPriority(-1);
            t.setProcessId(-1);
            t.setProcessName("NULL");
        } else {
            t.setProcessPriority(current.priority());
            t.setProcessId(current.identity());
            t.setProcessName(current.name());
        }
        traceStream.println(t.toString());
    }
    
    public void trace(String msg) {
       traceStream.println(msg);
    }

    public void setTraceStream(PrintStream traceStream) {
        this.traceStream = traceStream;
    }
}
