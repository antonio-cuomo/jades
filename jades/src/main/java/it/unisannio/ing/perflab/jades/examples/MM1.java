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
package it.unisannio.ing.perflab.jades.examples;

import java.io.IOException;

import it.unisannio.ing.perflab.jades.core.Model;
import it.unisannio.ing.perflab.jades.core.Process;
import it.unisannio.ing.perflab.jades.core.Random;
import it.unisannio.ing.perflab.jades.resources.FCFSFacility;

/**
 * A sample model of an M/M/1 Queue
 */
public class MM1 extends Model {

    private double simTime;
    private double iarTime;
    private double srvTime;
    private int numServers;
    private static FCFSFacility m_fac;
    private static Random srvRand;
    private static Random iarRand;


    public MM1(double simTime, double iarTime, double srvTime, int ns) {
        super("App");
        this.simTime = simTime;
        this.iarTime = iarTime;
        this.srvTime = srvTime;
        this.numServers = ns;
        srvRand = new Random();
        iarRand = new Random();
        m_fac = new FCFSFacility("server", numServers);
    }
    
    public void run() {
        start(new Sim());
    }

    private class Sim extends Process {
        public Sim() {
            super("Sim");
        }

        public void run() {
            add(new Gen());
            hold(simTime);
        }
    }

    private class Gen extends Process {
        public Gen() {
            super("Gen");
        }

        public void run() {
            while (true) {
                add(new Job());
                hold(iarRand.exponential(iarTime));
            }
        }
    }

    private class Job extends Process {
        public Job() {
            super("Client");
        }

        public void run() {
            m_fac.use(srvRand.exponential(srvTime));
        }
    }

    public static void main(String[] args) throws IOException {
        double simTime, iarTime, srvTime;
        int numServers;
        MM1 model = null;
        if (args.length < 4) {
            System.out.println("MM1 - a simple model that simulates an M/M/1 queue");
            System.out.println("Usage: MM1 <simTime> <iarTime> <srvTime> <numServers>");
            System.out.println("e.g.: MM1 1000.0 1.0 0.5 1");
            System.exit(0);
           
        }
        else {
            simTime = Double.parseDouble(args[0]);
            iarTime = Double.parseDouble(args[1]);
            srvTime = Double.parseDouble(args[2]);
            numServers = Integer.parseInt(args[3]);
            model = new MM1(simTime, iarTime, srvTime, numServers);
        }
        //System.in.read();
        long startTime = System.currentTimeMillis();
        model.enableTrace(false);
        model.run();
        model.report();
        System.out.printf("Execution time %d", System.currentTimeMillis() - startTime);
        model.theory();
    }

    private void theory() {
        double rho, nbar, rtime, tput;

        System.out.println("\n\n\n\t\t\tM/M/1 Theoretical Results\n");

        tput = 1.0 / iarTime;
        rho = tput * srvTime;
        nbar = rho / (1.0 - rho);
        rtime = srvTime / (1.0 - rho);

        System.out.printf("\n\n");
        System.out.printf("\t\tInter-arrival time = %10.3f\n", iarTime);
        System.out.printf("\t\tService time       = %10.3f\n", srvTime);
        System.out.printf("\t\tUtilization        = %10.3f\n", rho);
        System.out.printf("\t\tThroughput rate    = %10.3f\n", tput);
        System.out.printf("\t\tMn nbr at queue    = %10.3f\n", nbar);
        System.out.printf("\t\tMn queue length    = %10.3f\n", nbar - rho);
        System.out.printf("\t\tResponse time      = %10.3f\n", rtime);
        System.out.printf("\t\tTime in queue      = %10.3f\n", rtime - srvTime);
    }
}
