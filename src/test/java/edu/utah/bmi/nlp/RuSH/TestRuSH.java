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
package edu.utah.bmi.nlp.RuSH;

import edu.utah.bmi.nlp.core.Span;
import edu.utah.bmi.nlp.rush.core.RuSH;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * This rule set are more inclusive that is trying to include all non-whitespace characters in a sentence.
 * @Author Jianlin Shi
 */
public class TestRuSH {
    private RuSH segmenter;
    private boolean debug = true;

    public static void printDetails(ArrayList<Span> sentences, String input, boolean debug) {
        if (debug) {
            for (Span sentence : sentences) {
                System.out.println(sentence.begin + "-" + sentence.end + "\t" + ">" + input.substring(sentence.begin, sentence.end) + "<");
            }
            for (int i = 0; i < sentences.size(); i++) {
                Span sentence = sentences.get(i);
                System.out.println("assert (sentences.get(" + i + ").begin == " + sentence.begin + " &&" +
                        " sentences.get(" + i + ").end == " + sentence.end + ");");
            }
        }
    }

    @Before
    public void initiate() {
        segmenter = new RuSH("conf/rush_rules.csv");
        segmenter.setDebug(debug);
        segmenter.setSpecialCharacterSupport(true);
    }


    @Test
    public void test1() throws Exception {
        String input = "Can Mr. K check it. Look\n good.\n";
        ArrayList<Span> sentences = segmenter.segToSentenceSpans(input);
        input = input.replaceAll("\\n", " ");
        printDetails(sentences, input, debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 19);
        assert (sentences.get(1).begin == 20 && sentences.get(1).end == 31);

    }

    @Test
    public void test2() {
        String input = "S/p C6-7 ACDF. No urgent events overnight. Pain control ON. ";
        ArrayList<Span> sentences = segmenter.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input, debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 14);
        assert (sentences.get(1).begin == 15 && sentences.get(1).end == 42);
        assert (sentences.get(2).begin == 43 && sentences.get(2).end == 59);

    }

}