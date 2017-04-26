/*******************************************************************************
 * Copyright  2016  Department of Biomedical Informatics, University of Utah
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package edu.utah.bmi.nlp.core;

import java.util.ArrayList;

/**
 * @author Jianlin Shi
 */
public class SimpleParser {

    protected static final int punctuation = 0, letter = 1, digit = 2, dot = 3, returnc = 4;


    /**
     * Tokenize to an ArrayList of Spans, regardless of sentences or paragraphs
     *
     * @param text The input text string for tokenization
     * @param includePunctuation Whether include punctuations when tokenizing
     * @return An ArrayList of tokens in Span object format
     */
    public static ArrayList<Span> tokenize2Spans(String text, boolean includePunctuation) {
        ArrayList<Span> tokens = new ArrayList<Span>();
//        0: punctuation or return, 1: letter, 2: digit,
        int type = 0;
        int tokenBegin = 0, tokenEnd = 0, sentenceBegin = 0, sentenceEnd = 0;
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char thisChar = text.charAt(i);
            if (WildCardChecker.isPunctuation(thisChar)) {
                if (type > 0) {
                    tokens.add(new Span(tokenBegin, i, tmp.toString()));
                    tmp.setLength(0);
                }
                tokenBegin = i;
                if (includePunctuation)
                    tokens.add(new Span(tokenBegin, i + 1, String.valueOf(thisChar)));
                type = 0;
            } else if (thisChar == '\n' || thisChar == '\r') {
                if (type > 0) {
                    tokens.add(new Span(tokenBegin, i, tmp.toString()));
                    tmp.setLength(0);
                }
                tokenBegin = i;
                type = 0;
            } else if (Character.isDigit(thisChar)) {
                if (type == 0) {
                    tokenBegin = i;
                    type = 2;
                } else if (type == 1) {
                    tokens.add(new Span(tokenBegin, i, tmp.toString()));
                    tmp.setLength(0);
                    tokenBegin = i;
                    type = 2;
                }
                tmp.append(thisChar);
            } else if (Character.isLetter(thisChar)) {
                if (type == 0) {
                    tokenBegin = i;
                    type = 1;
                } else if (type == 2) {
                    tokens.add(new Span(tokenBegin, i, tmp.toString()));
                    tmp.setLength(0);
                    tokenBegin = i;
                    type = 1;
                }
                tmp.append(thisChar);
            } else {
                if (type != 0) {
                    tokens.add(new Span(tokenBegin, i, tmp.toString()));
                    tmp.setLength(0);
                }
                type = 0;
            }
        }
        if (type == 1 || type == 2) {
            tokens.add(new Span(tokenBegin, text.length(), text.substring(tokenBegin)));
        }
        return tokens;
    }


}
