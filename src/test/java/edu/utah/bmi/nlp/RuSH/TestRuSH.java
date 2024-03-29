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
 *
 * @Author Jianlin Shi
 */
public class TestRuSH {
    private RuSH rush;

    public static void printDetails(ArrayList<Span> sentences, String input) {
        for (int i = 0; i < sentences.size(); i++) {
            Span sentence = sentences.get(i);
            System.out.println(">"+input.substring(sentence.begin,sentence.end)+"<");
            System.out.println("assert (sentences.get(" + i + ").fbegin == " + sentence.begin + " &&" +
                    " sentences.get(" + i + ").fend == " + sentence.end + ");");
        }
    }

    @Before
    public void initiate() {
        rush = new RuSH("conf/rush_rules.tsv");

//        rush = new RuSH(this.getClass().getClassLoader().getResource("mimic.tsv").getPath());
//        rush = new RuSH("conf/rush_rules.xlsx");
    }


    @Test
    public void test1() throws Exception {
        String input = "Can Mr. K check it. Look\n good.\n";
        ArrayList<Span> sentences = rush.segToSentenceSpans(input);
        input = input.replaceAll("\\n", " ");
        printDetails(sentences, input);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 19);
        assert (sentences.get(1).begin == 20 && sentences.get(1).end == 31);

    }

    @Test
    public void test2() {
        String input = "S/p C6-7 ACDF. No urgent events overnight. Pain control ON. ";
        ArrayList<Span> sentences = rush.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 14);
        assert (sentences.get(1).begin == 15 && sentences.get(1).end == 42);
        assert (sentences.get(2).begin == 43 && sentences.get(2).end == 59);

    }

    @Test
    public void test3() {
        String input = " •  Coagulopathy (HCC)    \n" +
                "\n" +
                "\n" +
                "\n" +
                " •  Hepatic encephalopathy (HCC)    \n" +
                "\n" +
                "\n" +
                "\n" +
                " •  Hepatorenal syndrome (HCC)    \n" +
                "\n";
        ArrayList<Span> sentences = rush.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input);
        assert (sentences.get(0).begin == 1 && sentences.get(0).end == 22);
        assert (sentences.get(1).begin == 31 && sentences.get(1).end == 62);
        assert (sentences.get(2).begin == 71 && sentences.get(2).end == 100);
    }


    @Test
    public void test4() {
        String input = "Delirium - ";
        ArrayList<Span> sentences = rush.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input);
    }

    @Test
    public void test5() {
        String input = "The patient complained about the TIA \n\n No memory issues. \"I \n\nOrdered the MRI scan.- ";
        ArrayList<Span> sentences = rush.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input);
    }

    @Test
    public void test6() {
        String input = "S9%\\. Te";
        ArrayList<Span> sentences = rush.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input);
    }

    @Test
    public void test7() {
        String input = "\n" +
                "\n" +
                "Admission Date:  1995-5-24       Discharge Date:  1995-6-2\n" +
                "\n" +
                "Date of Birth:   1921-8-13       Sex:  F\n" +
                "\n" +
                "Service:  Cardiothoracic Service\n" +
                "\n" +
                "CHIEF COMPLAINT:  The patient is a 72-year-old woman with\n" +
                "chest pain and acute myocardial infarction status post failed\n" +
                "thrombolytic therapy at an outside hospital transferred to\n" +
                "University Of Alabama Birmingham Hospital of cardiac\n" +
                "catheterization.";
        ArrayList<Span> sentences = rush.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input);
    }
}