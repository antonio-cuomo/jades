/******************************************************************
 * @(#) InverseT.java     1.3     98/10/27
 * 
 * Copyright (c) 2005, John Miller
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   
 * 1. Redistributions of source code must retain the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer. 
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials
 *    provided with the distribution. 
 * 3. Neither the name of the University of Georgia nor the names
 *    of its contributors may be used to endorse or promote
 *    products derived from this software without specific prior
 *    written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @version     1.3, 28 Jan 2005
 * @author      John Miller
 */

package it.unisannio.ing.perflab.jades.statistics;

/******************************************************************
 * This class computes the inverse Student t Distribution.
 */

public class InverseT {
    /**
     * Constants
     */
    private static final int H_SIZE = 4;

    /**
     * Variables
     */
    private static double[] h = new double[H_SIZE];

    /*************************************************************
     * Calculate t-value given p level and df degrees of freedom for Student t
     * Distribution. From K. Pawlikowski (www.cosc.canterbury.ac.nz).
     * ------------------------------------------------------------ COMPUTE pth
     * QUANTILE OF THE t DISTRIBUTION This function computes the upper pth
     * quantile of the t dis- tribution (the value of t for which the area under
     * the curve from t to +infinity is equal to p). It is a transliteration of
     * the 'STUDTP' function given in Appendix C of "Principles of Discrete
     * Event Simulation", G. S. Fishman, Wiley, 1978.
     * ------------------------------------------------------------
     * 
     * @param p
     *            significance value
     * @param df
     *            degrees of freedom
     * @return double t-value
     */
    public static double tValue(double p, double df) {
        double x = 0.0;
        double z1 = Math.abs(InverseZ.zValue(p));
        double z2 = z1 * z1;

        h[0] = 0.25 * z1 * (z2 + 1.0);
        h[1] = 0.010416667 * z1 * ((5.0 * z2 + 16.0) * z2 + 3.0);
        h[2] = 0.002604167 * z1 * (((3.0 * z2 + 19.0) * z2 + 17.0) * z2 - 15.0);
        h[3] = z1
                * ((((79.0 * z2 + 776.0) * z2 + 1482.0) * z2 - 1920.0) * z2 - 945.0);
        h[3] *= 0.000010851;

        for (int i = H_SIZE - 1; i >= 0; i--) {
            x = (x + h[i]) / df;
        }
        ; // for

        z1 += x;
        if (p > 0.5) {
            z1 = -z1;
        }
        ; // if

        return z1;

    }; // tValue

    /*
     * Converted from Dirk Grunwald's C++ code. FIX public double tValue (double
     * p, double df) { double t; boolean positive = p >= 0.5;
     * 
     * p = (positive) ? 1.0 - p : p;
     * 
     * if (p <= 0.0 || df == 0) { t = MAX_DOUBLE; } else if (p == 0.5) { t =
     * 0.0; } else if (df == 1) { t = 1.0 / Math.tan ((p + p) * 1.57079633); }
     * else if (df == 2) { t = Math.sqrt (1.0 / ((p + p) * (1.0 - p)) - 2.0); }
     * else { double ddf = df; double a = Math.sqrt (Math.log (1.0 / (p * p)));
     * double aa = a * a; a = a - ((2.515517 + (0.802853 * a) + (0.010328 * aa))
     * / (1.0 + (1.432788 * a) + (0.189269 * aa) + (0.001308 * aa * a))); t =
     * ddf - 0.666666667 + 1.0 / (10.0 * ddf); t = Math.sqrt (ddf * (Math.exp (a
     * * a * (ddf - 0.833333333) / (t * t)) - 1.0)); }; // if
     * 
     * return (positive) ? t : -t;
     * 
     * }; // tValue
     */

}; // class

