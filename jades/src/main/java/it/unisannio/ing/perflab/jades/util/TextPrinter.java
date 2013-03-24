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
package it.unisannio.ing.perflab.jades.util;

public class TextPrinter {

    private static final int DEFAULT_LINE_LENGTH = 120;
    private int lineLength;
    private String spaceString;

    public TextPrinter() {
        this(DEFAULT_LINE_LENGTH);
    }

    public TextPrinter(int llength) {
        this.lineLength = llength;
        char[] spaces = new char[lineLength];
        for (int i = 0; i < lineLength; i++) {
            spaces[i] = ' ';
        }
        spaceString = new String(spaces);
    }

    public String center(String origString) {
        StringBuilder sb = new StringBuilder(spaceString);
        int length = origString.length();
        int offset = (lineLength - length) / 2;
        for (int i = 0; i < length; i++) {
            sb.setCharAt(i + offset, origString.charAt(i));
        }
        return sb.toString();
    }

    public String fillLineWith(char x) {
        StringBuilder sb = new StringBuilder(spaceString);
        for (int i = 0; i < lineLength; i++) {
            sb.setCharAt(i, x);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        TextPrinter tp = new TextPrinter();
        System.out.println(tp.center("TextPrinter Test"));
        System.out.println(tp
                .center("All the text you're reading should be centered"));
        System.out.println(tp
                .center("If not, please reexamine TextPrinter implementation"));
    }
}
