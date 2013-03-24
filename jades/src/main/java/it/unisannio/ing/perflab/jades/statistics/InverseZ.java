/******************************************************************
 * @(#) InverseZ.java     1.3     98/10/27
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
 * This class computes the inverse Standard Normal Distribution.
 */

public class InverseZ {

    /*************************************************************
     * Calculate z-value given p level for standard Normal Distribution. From K.
     * Pawlikowski (www.cosc.canterbury.ac.nz)
     * ------------------------------------------------------------ This
     * function computes the pth upper quantile of the stand- ard normal
     * distribution (i.e., the value of z for which the are under the curve from
     * z to +infinity is equal to p). 'Z' is a transliteration of the 'STDZ'
     * function in Appendix C of "Principles of Discrete Event Simulation", G.
     * S. Fishman, Wiley, 1978. The approximation used initially appeared in in
     * "Approximations for Digital Computers", C. Hastings, Jr., Princeton U.
     * Press, 1955. ------------------------------------------------------------
     * 
     * @param p
     *            significance value
     * @return double z-value
     */
    public static double zValue(double p) {
        double q = (p > 0.5) ? (1 - p) : p;
        double z1 = Math.sqrt(-2.0 * Math.log(q));
        double n = (0.010328 * z1 + 0.802853) * z1 + 2.515517;
        double d = ((0.001308 * z1 + 0.189269) * z1 + 1.43278) * z1 + 1;

        z1 -= n / d;

        if (p > 0.5) {
            z1 = -z1;
        }
        ; // if

        return z1;

    }; // zValue

    public static void main(String[] args) {
        double[] cLevs = { 0.8, 0.9, 0.95, 0.99 };
        for (int i = 0; i < cLevs.length; i++) {
            System.out
                    .printf("%.3f\t", InverseZ.zValue((1.0 - cLevs[i]) / 2.0));
        }
    }

}; // class

