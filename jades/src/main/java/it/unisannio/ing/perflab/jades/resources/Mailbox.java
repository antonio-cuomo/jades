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
import it.unisannio.ing.perflab.jades.scheduler.Scheduler;
import it.unisannio.ing.perflab.jades.trace.ReceiveMessageTrace;
import it.unisannio.ing.perflab.jades.trace.SendMessageTrace;
import it.unisannio.ing.perflab.jades.trace.Tracer;
import it.unisannio.ing.perflab.jades.util.HeapQueue;
import it.unisannio.ing.perflab.jades.util.MovingAverage;
import it.unisannio.ing.perflab.jades.util.NotYetImplementedException;
import it.unisannio.ing.perflab.jades.util.PriorityQueue;

import java.util.Iterator;
import java.util.LinkedList;

/*used for inter-process communications */
public class Mailbox {

    public class Message {

        private Object message;
        private int sender;

        public Message(int identity, Object msg) {
            this.sender = identity;
            this.message = msg;
        }

        public Object getMessage() {
            return message;
        }

        public void setMessage(Object message) {
            this.message = message;
        }

        public int getSender() {
            return sender;
        }

        public void setSender(int sender) {
            this.sender = sender;
        }

    }

    private String name;

    private PriorityQueue processQueue;

    private LinkedList<Message> messageQueue;

    private MovingAverage msgQLengthMA;

    private MovingAverage procQLengthMA;

    private Scheduler scheduler;

    private Tracer tracer;

    public Mailbox(String name) {
        this.name = name;
        this.processQueue = new HeapQueue();
        this.messageQueue = new LinkedList<Message>();
        this.msgQLengthMA = new MovingAverage();
        this.procQLengthMA = new MovingAverage();
        this.scheduler = Scheduler.getScheduler();
        this.tracer = scheduler.getTracer();
        scheduler.registerMailbox(this);
    }

    public void monitor() {
        throw new NotYetImplementedException();
    }

    public int msg_count() {
        return messageQueue.size() - processQueue.getLength();
    }

    public int proc_delay_count() {
        throw new NotYetImplementedException();
    }

    public double proc_sum() {
        throw new NotYetImplementedException();
    }

    public int queue_count() {
       return processQueue.getLength();
    }

    public Object receive() {
        Process currentProcess = scheduler.getCurrentProcess();
        if (messageQueue.size() == 0) {
            procQLengthMA.update(scheduler.getCurrentTime(),
                    processQueue.getLength());
            processQueue.enqueue(new QueueableProcess(currentProcess,
                    scheduler.getCurrentTime()));
            currentProcess.getRunner().block();
        }
        if (tracer.isTraceEnabled())
            tracer.trace(scheduler.getCurrentTime(), scheduler
                    .getCurrentProcess(),
                    new ReceiveMessageTrace(this.name));
        msgQLengthMA.update(scheduler.getCurrentTime(), messageQueue.size());
        Message msg = messageQueue.removeFirst();
        return msg.getMessage();

    }

    public Object selectiveReceive(int sender) {
        Process currentProcess = scheduler
                .getCurrentProcess();
        Message foundMessage = null;
        Iterator<Message> msgIterator = messageQueue.descendingIterator();
        while (msgIterator.hasNext() && foundMessage == null) {
            Message msg = msgIterator.next();
            if (msg.getSender() == sender) {
                foundMessage = msg;
                messageQueue.remove(foundMessage);
                if (tracer.isTraceEnabled())
                    tracer.trace(scheduler.getCurrentTime(), scheduler
                            .getCurrentProcess(),
                            new ReceiveMessageTrace(this.name));
                msgQLengthMA.update(scheduler.getCurrentTime(),
                        messageQueue.size());
                return msg.getMessage();
            }
        }
        while (foundMessage == null) {
            procQLengthMA.update(scheduler.getCurrentTime(),
                    processQueue.getLength());
            processQueue.enqueue(new QueueableProcess(currentProcess,
                    scheduler.getCurrentTime()));
            currentProcess.getRunner().block();
            msgIterator = messageQueue.listIterator();
            while (msgIterator.hasNext() && foundMessage == null) {
                Message msg = (Message) msgIterator.next();
                if (msg.getSender() == sender) {
                    foundMessage = msg;
                    messageQueue.remove(foundMessage);
                    if (tracer.isTraceEnabled())
                        tracer.trace(scheduler.getCurrentTime(), scheduler
                                .getCurrentProcess(),
                                new ReceiveMessageTrace(this.name));
                    msgQLengthMA.update(scheduler.getCurrentTime(),
                            messageQueue.size());
                    return msg.getMessage();
                }
            }
        }
        return null;

    }

    public void reset() {
        this.processQueue = new HeapQueue();
        this.messageQueue = new LinkedList<Message>();
        this.msgQLengthMA = new MovingAverage();
        this.procQLengthMA = new MovingAverage();
    }

    public void send(Object msg) {
        msgQLengthMA.update(scheduler.getCurrentTime(), messageQueue.size());
        messageQueue.addLast(new Message(scheduler.getCurrentProcess().identity(), msg));
        if (tracer.isTraceEnabled())
            tracer.trace(scheduler.getCurrentTime(), scheduler
                    .getCurrentProcess(),
                    new SendMessageTrace(this.name));
        if (processQueue.getLength() > 0) {
            procQLengthMA.update(scheduler.getCurrentTime(),
                    processQueue.getLength());
            QueueableProcess process = (QueueableProcess) processQueue
                    .dequeue();
            scheduler.reactivate(process.getProcess());
            Process currentProcess = scheduler
                    .getCurrentProcess();
            if (scheduler
                    .fastScheduleCurrentProcess(scheduler.getCurrentTime()) == false)
                currentProcess.getRunner().block();
        }
    }

    public Object timed_receive(double timeout) {
        throw new NotYetImplementedException();
    }

    public int msg_in_queue() {
        return messageQueue.size();
    }
}
