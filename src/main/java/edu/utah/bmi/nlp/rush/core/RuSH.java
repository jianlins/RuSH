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


package edu.utah.bmi.nlp.rush.core;

import edu.utah.bmi.nlp.core.IOUtil;
import edu.utah.bmi.nlp.core.Span;
import edu.utah.bmi.nlp.rush.core.DeterminantValueSet.Determinants;
import edu.utah.bmi.nlp.rush.core.DeterminantValueSet.DirectionPrefer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jianlin Shi
 */
public class RuSH {

    public static Logger logger = IOUtil.getLogger(RuSH.class);
    protected static FastCRuleProcessor fcrp;
    protected static Determinants begin, end;
    @Deprecated
    protected boolean debug = false;


    public RuSH(String rule) {
        initiate(rule);
    }

    public void initiate(String rule) {
        fcrp = new FastCRuleProcessor(rule);
        fcrp.setReplicationSupport(true);

        fcrp.setCompareMethod("scorewidth");
        begin = Determinants.stbegin;
        end = Determinants.stend;

    }

    public ArrayList<String> segToSentenceStrings(String text) {
        ArrayList<String> output = new ArrayList<String>();
        return output;

    }

    public ArrayList<Span> segToSentenceSpans(String text) {
        ArrayList<Span> output = new ArrayList<Span>();
        HashMap<Determinants, ArrayList<Span>> result = fcrp.processString(text, DirectionPrefer.none);

        if (logger.isLoggable(Level.FINE)) {
            text = text.replaceAll("\n", " ");
            for (Map.Entry<Determinants, ArrayList<Span>> ent : result.entrySet()) {
                logger.finer(ent.getKey().toString());
                for (Span span : ent.getValue()) {
                    logger.finer("\t" + span.begin + "-" + span.end + ":" + span.score + "\t" +
                            text.substring(0, span.begin) + "<" + text.substring(span.begin, span.begin + 1)
                            + ">\t" + span.ruleId + "\t" + fcrp.getRuleString(span.ruleId));
                }

            }
        }

        ArrayList<Span> begins = result.get(begin);
        ArrayList<Span> ends = result.get(end);

//        if(begins==null)
//        System.out.println(text);
        if (begins == null || begins.size() == 0) {
            begins = new ArrayList<Span>();
            begins.add(new Span(0, 1, 1, -1));
        }
        if (ends == null || ends.size() == 0) {
            ends = new ArrayList<Span>();
            ends.add(new Span(text.length() - 1, text.length(), 1, -1));
        }


        int stBegin = 0;
        boolean sentenceStarted = false;
        int stEnd = 0, i = 0, j = 0;
        for (i = 0; i < begins.size(); i++) {
            if (!sentenceStarted) {
                stBegin = begins.get(i).begin;
                if (begins.get(i).score == 1 || stBegin < stEnd)
                    continue;
                sentenceStarted = true;
            } else if(begins.get(i).begin < stEnd) {
                continue;
            }
            for (int k = j; k < ends.size(); k++) {
                if (ends.get(k).score == 3)
                    continue;
                if (i < begins.size() - 1 && k < ends.size() - 1
                        && begins.get(i + 1).getBegin() < ends.get(k).begin + 1) {
                    break;
                }
                stEnd = ends.get(k).begin + 1;
                j = k;
//                right trim
                while (stEnd >= 1 && (Character.isWhitespace(text.charAt(stEnd - 1)) || (int) text.charAt(stEnd - 1) == 160)) {
                    stEnd--;
                }

                if (stEnd < stBegin)
//                   if this end is for previous sentence, move the pointer to the next
                    continue;
                else if (sentenceStarted) {
//                   if current status is after a sentence begin marker
                    output.add(new Span(stBegin, stEnd));
                    sentenceStarted = false;
                    if (i == begins.size() - 1 ||
                            (k < ends.size() - 1
                                    && begins.get(i + 1).getBegin() > ends.get(k + 1).getEnd()))
                        continue;
                    break;
                } else {
//                   if current status is after a sentence end marker, then replace the last output
                    output.set(output.size() - 1, new Span(stBegin, stEnd));
                    sentenceStarted = false;
                }
            }
        }
        return output;
    }

    @Deprecated
    public void setDebug(boolean debug) {
        this.debug = debug;
        fcrp.setDebug(debug);
    }

    public void setSpecialCharacterSupport(Boolean scSupport) {
        fcrp.setSpecialCharacterSupport(scSupport);
    }
}
