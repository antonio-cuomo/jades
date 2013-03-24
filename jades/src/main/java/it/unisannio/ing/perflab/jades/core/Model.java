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

import it.unisannio.ing.perflab.jades.resources.Event;
import it.unisannio.ing.perflab.jades.resources.Facility;
import it.unisannio.ing.perflab.jades.resources.Mailbox;
import it.unisannio.ing.perflab.jades.resources.ServerStats;
import it.unisannio.ing.perflab.jades.resources.Storage;
import it.unisannio.ing.perflab.jades.scheduler.Scheduler;
import it.unisannio.ing.perflab.jades.statistics.Table;
import it.unisannio.ing.perflab.jades.trace.CreateTrace;
import it.unisannio.ing.perflab.jades.trace.Trace;
import it.unisannio.ing.perflab.jades.trace.Tracer;
import it.unisannio.ing.perflab.jades.util.NotYetImplementedException;
import it.unisannio.ing.perflab.jades.util.TextPrinter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * The Model class is the entry point to define simulation models.
 * @author Antonio Cuomo
 */
public abstract class Model implements Serializable {

    /** String that contains the model name. */
    private String modelName;

    /** Default random stream (CSIM for Java Compatibility). */
    protected Random rand;

    /** Built-in event to signal that the event list is empty (CSIM for Java Compatibility). */
    protected Event eventListEmpty;

    /** Built-in converged event (CSIM for Java Compatibility). */
    protected Event converged;

    /** OutputStream to which model output is directed. */
    private PrintStream outputStream;

    /**Keeps track of the last time point (in simulated time) in which
     * the simulation was reset. */
    private double lastResetTime = 0.0;

    /** The process scheduler associated with the model.*/
    private Scheduler scheduler;

    /** The event tracer associated with the model.*/
    private Tracer tracer;

    /** True if the simulation is already started, false otherwise.*/
    private boolean started;

    /** Starting time of simulation (in real time) - used by the
     * {@link public double executionTime {executionTime}} method. */
    private long startNanos;

    /**
     * Construct a {@link Model} instance. This method should only be invoked
     * by subclasses. For CSIM for Java Compatibility, when no name is specified
     * for the model, the model name will be def.
     */
    protected Model() {
        this("def");
    }

    /**
     * Construct a {@link Model} instance with the specified name. This method
     * should only be invoked by subclasses.
     * @param name  the name that must be assigned to the model
     */
    protected Model(String name) {
        this.modelName = name;
        initModel();
    }

    /**
     * Initializes the model.
     * */
    private void initModel() {
        rand = new Random();
        started = false;
        scheduler = new Scheduler();
        tracer = new Tracer(System.out);
        scheduler.setTracer(tracer);
        scheduler.init();
        eventListEmpty = scheduler.getEventListEmpty();
        converged = scheduler.getConverged();
        outputStream = System.out;
    }

    /**
     * Resets the models. All the model state is restored
     * to the initial values (most important, simulation time is reset to 0.0).
     * All the resources and queues are cleared.
     */
    public void reset() {
        this.lastResetTime = scheduler.getCurrentTime();

        for (Facility facility : scheduler.getFacilities()) {
            facility.reset();
        }

        for (Mailbox mailbox : scheduler.getMailboxes()) {
            mailbox.reset();
        }
        for (Storage storage : scheduler.getStorages()) {
            storage.reset();
        }
        for (Table table : scheduler.getTables()) {
            table.reset();
        }
        tracer.reset();
        scheduler.reset();
    }

    /**
     *  This method must be provided by implementors and defines the starting point of a simulation
     */
    public abstract void run();

    /**
     * Retrieves the current value of the simulation clock.
     * @return a double representing the current value of the simulation clock.
     */
    public double clock() {
        return scheduler.getCurrentTime();
    }

    /**
     *  Starts the first simulation process.
     *  @param p  the Process to be started
     */
    public void start(Process p) {
        this.startNanos = getJVMCpuTime();
        if (tracer.isTraceEnabled()) {
            tracer.trace(Trace.traceHeader());
        }
        if (started) {
            this.reset();
        }
        started = true;
        p.setScheduler(scheduler);
        p.setTracer(tracer);
        if (p.priority() == -1) {
            p.set_priority(1);
        }
        scheduler.addProcess(p, scheduler.getCurrentTime());
        if (tracer.isTraceEnabled()) {
            tracer.trace(scheduler.getCurrentTime(), null,
                    new CreateTrace(p.name(), p.identity()));
        }
        scheduler.startScheduler();
        scheduler.waitModelFinish();
    }

    /**
     * Returns the model name.
     * @return a String representing the model name
     */
    public String name() {
        return this.modelName;
    }

    /**
     * Changes the model name to be equal to the argument name.
     * @param name  the name for the model
     */
    public void setName(String name) {
        this.modelName = name;
    }

    /**
     * Enables or disables the tracing of simulation events
     * 
     * @param enable if true, tracing should be enabled
     */
    public void enableTrace(boolean enable) {
        tracer.setTraceEnabled(enable);
    }

    /**
     * Sets the output stream of the model.
     * @param  s  the stream to which model output will be directed
     */
    public void setOutputStream(PrintStream s) {
        this.outputStream = s;
    }

    /**
     * Gets the output stream of the model.
     * @return the model output stream
     */
    public PrintStream getOutputStream() {
        return outputStream;
    }

    /**
     * Sets the stream to which trace events are written.
     * @param s the output stream to which model traces will be directed.
     */
    public void setTraceStream(PrintStream s) {
        tracer.setTraceStream(s);
    }

    /**
     * Sets the file to which the model can write the tracing events. If the
     * file does not exist, it is created
     * @param fileName filename (optionally with path) of the desired file
     */
    public void setTraceFile(String fileName) {
        PrintStream s;
        try {
            s = new PrintStream(new FileOutputStream(new File(fileName)));
            tracer.setTraceStream(s);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns timeOfDay (for compatibility with CSIM for JAVA).
     * For compatibility, the format of the returned string is:
     * Month dd,yyyy hh:mm:ss AM/PM Timezone.
     * Example: August 14, 2005 9:32:46 PM CDT.
     * @return a string representing the time of day
     */
    public String timeOfDay() {
        String pattern = "MMMMM dd, yyyy hh:mm:ss a z";
        SimpleDateFormat dFormat = new SimpleDateFormat(
                pattern, Locale.ENGLISH);
        return dFormat.format(new Date());
    }

    /**
     * Returns an estimate of the execution time, in seconds, that has
     * been consumed by the model till the invocation of this method.
     * @return the execution time, in seconds, represented as a double
     */
    public double executionTime() {
        long cpuNanos = getJVMCpuTime();
        double cpuNanosDouble = cpuNanos - startNanos;
        return cpuNanosDouble / 1e9;
    }

    
    /**
     * Returns the amount of CPU time, in seconds, that has been consumed by the
     * model till the invocation of this method.
     * @return the amount of CPU time, in seconds, represented as a long
     */
    private long getJVMCpuTime() {
        ThreadMXBean tmxb = (ThreadMXBean) ManagementFactory.getThreadMXBean();
        return tmxb.getThreadCpuTime(Thread.currentThread().getId());
    }

    /* START OF THE REPORTING FRAMEWORK */

    /* COMPLETE REPORT */
    /**
     *   Produces (on the output stream) a report on the execution of the model.
     */
    public void report() {
        report_hdr();
        report_facilities();
        report_storages();
        // report_buffers();
        // report_classes();
        // report_events();
        // report_mailboxes();
        report_tables();
        // report_qtables();
        // report_meters();
        // report_boxes();
    }

    /* PARTIAL REPORTS */
    /**
     * Prints the report header.
     */
    public void report_hdr() {
        TextPrinter tp = new TextPrinter();
        outputStream.println(tp.center("JADES Simulation Report"));
        outputStream.println(tp.center(new Date().toString()));
        outputStream.println(tp.center("Ending simulation time: "
                + this.clock()));
        outputStream.println(tp.center("Elapsed simulation time: "
                + (this.clock() - this.lastResetTime)));
        outputStream.println(tp.center("Execution (CPU) time: "
                + this.executionTime()));

    }

    /**
     * Prints the report on the facility usage.
     */
    public void report_facilities() {
        TextPrinter tp = new TextPrinter();
        if (scheduler.getFacilities().size() == 0){
            return;
        }
        String[] tableHeaders = {"facility name", "service disc",
                "service time", "util.", "throughput", "queue length",
                "responseTime", "complCount"};
        String format = "%1$-15s%2$-15s%3$-15s%4$-15s%5$-15s%6$-15s%7$-15s%8$-15s";
        String formatted = String.format(format, (Object[]) tableHeaders);
        outputStream.println(tp.center(formatted));
        outputStream.println(tp.fillLineWith('-'));
        format = "%1$-15s%2$-15s%3$-15.5s%4$-15.5s%5$-15.5s%6$-15.5s%7$-15.5s%8$-15s";
        for (Facility facility : scheduler.getFacilities()) {
            Object args[] = new Object[8];
            args[0] = facility.name();
            args[1] = facility.type();
            args[2] = facility.serviceTime();
            args[3] = facility.utilization();
            args[4] = facility.throughput();
            args[5] = facility.queueLength();
            args[6] = facility.responseTime();
            args[7] = facility.completions();
            formatted = String.format(format, (Object[]) args);
            outputStream.println(tp.center(formatted));
            // outputStream.printf(Locale.ENGLISH,"%s\t%s\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t%d\n",
            // facility.name(), facility.type(), facility.serviceTime(),
            // facility.utilization(), facility.throughput(),
            // facility.queueLength(), facility.responseTime(),
            // facility.completions());
            int numServers = (int) facility.numServers();
            String serverFormat = " >server %1$-21s%2$-15.5s%3$-15.5s%4$-45.5s%5$-15s";
            if (numServers > 1) {
                for (int i = 0; i < numServers; i++) {
                    ServerStats sstats = facility.serverStats(i);
                    Object[] serverArgs = { i, sstats.serviceTime(),
                            sstats.utilization(), sstats.throughput(),
                            sstats.completions() };
                    formatted = String.format(serverFormat,
                            (Object[]) serverArgs);
                    outputStream.println(formatted);
                    // outputStream.append(String.format(" >server %d\t\t%.3f\t%.3f\t%.3f\t%d\n",
                    // i, sstats.serviceTime(), sstats.utilization(),
                    // sstats.throughput(), sstats.completions()));
                }
            }
        }
    }

    public void report_storages() {
        TextPrinter tp = new TextPrinter();
        if (scheduler.getStorages().size() == 0)
            return;
        String[] tableHeaders = { "storage name", "size", "alloc amount",
                "alloc count", "dealloc amount", "dealloc count", "util",
                "in-que length", "in-que time" };
        String format = "%1$-15s%2$-15s%3$-15s%4$-15s%5$-15s%6$-15s%7$-15s%8$-15s";
        String formatted = String.format(format, (Object[]) tableHeaders);
        outputStream.println(tp.center(formatted));

        outputStream.println(tp.fillLineWith('-'));
        for (Storage storage : scheduler.getStorages()) {
            outputStream.printf(Locale.ENGLISH,
                    "%s\t%d\t%.3f\t%d\t%.3f\t%d\t%.3f\t%.3f\t%.3f\n",
                    storage.name(), storage.capacity(), storage.busySum()
                            / storage.allocCount(), storage.allocCount(),
                    storage.sumDeAllocs(), storage.sumAllocs(),
                    storage.utilization(), storage.queueLength(),
                    storage.waitingTime());
        }
    }

    public void report_buffers() {
        throw new NotYetImplementedException();
    }

    public void report_classes() {
        throw new NotYetImplementedException();
    }

    public void report_events() {
        throw new NotYetImplementedException();
    }

    public void report_mailboxes() {
        throw new NotYetImplementedException();
    }

    public void report_tables() {
        if (scheduler.getTables().size() == 0)
            return;
        int tableIndex = 1;
        for (Table table : scheduler.getTables()) {
            outputStream.println("TABLE " + tableIndex++ + ": " + table.name());
            outputStream.append("minimum\t\t");
            outputStream.append(String.format("%.6f", table.min()));
            outputStream.append('\t');
            outputStream.append("mean\t\t\t");
            outputStream.append(String.format("%.6f", table.mean()));
            outputStream.append('\n');
            outputStream.append("maximum\t\t");
            outputStream.append(String.format("%.6f", table.max()));
            outputStream.append('\t');
            outputStream.append("variance\t\t");
            outputStream.append(String.format("%.6f", table.var()));
            outputStream.append('\n');
            outputStream.append("range\t\t");
            outputStream.append(String.format("%.6f", table.range()));
            outputStream.append('\t');
            outputStream.append("standard deviation\t");
            outputStream.append(String.format("%.6f", table.stddev()));
            outputStream.append('\n');
            outputStream.append("observations\t");
            outputStream.append(String.format("%8d", table.countt()));
            outputStream.append("\t");
            outputStream.append("coefficient of var\t");
            outputStream.append(String.format("%.6f", table.cv()));
            outputStream.append("\n\n");
            int num = (int) table.histogram_num();
            if (num != 0) {
                outputStream.println("\t\t\t\t\t\tcumulative");
                outputStream
                        .println("lower limit\tfrequency\tproportion\tproportion");
                double min = table.histogram_low();
                double width = table.histogram_width();
                double total = table.histogram_total();
                double cumProportion = 0.0;
                for (int i = 1; i < num && cumProportion < 1.0; i++) {
                    int bucketContent = table.histogram_bucket(i);
                    double lowLimit = min + (i - 1) * width;
                    double proportion = bucketContent / total;
                    cumProportion += proportion;
                    outputStream.print(String.format(Locale.ENGLISH,
                            "%11.5f\t%d\t\t%10.6f\t%10.6f", lowLimit,
                            bucketContent, proportion, cumProportion));
                    outputStream.println();
                }
                outputStream.println();
                outputStream.println();
            }
        }

    }

    public void report_qtables() {
        throw new NotYetImplementedException();
    }

    public void report_meters() {
        throw new NotYetImplementedException();
    }

    public void report_boxes() {
        throw new NotYetImplementedException();
    }

    public void status_buffers() {
        throw new NotYetImplementedException();
    }

    public void status_events() {
        throw new NotYetImplementedException();
    }

    public void status_facilities() {
        throw new NotYetImplementedException();
    }

    public void status_mailboxes() {
        throw new NotYetImplementedException();
    }

    public void status_next_event_list() {
        throw new NotYetImplementedException();
    }

    public void status_storages() {
        throw new NotYetImplementedException();
    }

}
