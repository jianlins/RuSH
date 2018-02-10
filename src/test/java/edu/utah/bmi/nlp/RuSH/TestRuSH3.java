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
import edu.utah.bmi.nlp.rush.core.RuSH2;
import edu.utah.bmi.nlp.rush.core.RuSH3;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This rule set are more inclusive that is trying to include all non-whitespace characters in a sentence.
 *
 * @Author Jianlin Shi
 */
public class TestRuSH3 {
    private RuSH3 rush3;

    public static void printDetails(ArrayList<Span> sentences, String input) {
        for (int i = 0; i < sentences.size(); i++) {
            Span sentence = sentences.get(i);
            System.out.println("assert (sentences.get(" + i + ").begin == " + sentence.begin + " &&" +
                    " sentences.get(" + i + ").end == " + sentence.end + ");");
        }

    }

    @Before
    public void initiate() {
        rush3 = new RuSH3("conf/rush_rules_v3.xlsx");

//        rush3 = new RuSH(this.getClass().getClassLoader().getResource("mimic.tsv").getPath());
//        rush3 = new RuSH("conf/rush_rules.xlsx");
        rush3.fillTextInSpan = true;
    }


    @Test
    public void test1() throws Exception {
        String input = "Can Mr. K check it. Look\n good.\n";
        ArrayList<Span> sentences = rush3.segToSentenceSpans(input);
        input = input.replaceAll("\\n", " ");
        printDetails(sentences, input);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 19);
        assert (sentences.get(1).begin == 20 && sentences.get(1).end == 31);

    }

    @Test
    public void test2() {
        String input = "S/p C6-7 ACDF. No urgent events overnight. Pain control ON. ";
        ArrayList<Span> sentences = rush3.segToSentenceSpans(input);
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
        ArrayList<Span> sentences = rush3.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input);
        assert (sentences.get(0).begin == 1 && sentences.get(0).end == 22);
        assert (sentences.get(1).begin == 31 && sentences.get(1).end == 62);
        assert (sentences.get(2).begin == 71 && sentences.get(2).end == 100);

    }


    @Test
    public void test4() {
        String input = "Delirium - ";
        ArrayList<Span> sentences = rush3.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 10);
    }

    @Test
    public void test5() {
        String input = "The patient complained about the TIA \n\n No memory issues. \"I \n\nOrdered the MRI scan.- ";
        ArrayList<Span> sentences = rush3.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input);
        System.out.println(sentences);
    }

    @Test
    public void test6() {
        String input = "S9%\\. Te";
        ArrayList<Span> sentences = rush3.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input);
        System.out.println(sentences);
    }


    @Test
    public void test7() throws IOException {
        String text = FileUtils.readFileToString(new File("../RuSHBenchmark/data/10482.txt"));
        ArrayList<Span> sentences = rush3.segToSentenceSpans(text);

    }

    @Test
    public void test8() throws IOException {
        String tokenRule =
                "\\b(\\a\t0\tstbegin\n" +
                        "\\a\\e\t2\tstend\n" +
                        "\\C\t0\ttobegin\n" +
                        "\\C)\\w\t2\ttoend\n" +
                        "\\C)\\p\t2\ttoend\n" +
                        "\\C)\\d\t2\ttoend\n" +

                        "\\c\t0\ttobegin\n" +
                        "\\c)\\w\t2\ttoend\n" +
                        "\\c)\\p\t2\ttoend\n" +
                        "\\c)\\d\t2\ttoend\n" +

                        "\\d\t0\ttobegin\n" +
                        "\\d(\\d\t1\ttobegin\n" +

                        "\\d)\\c\t2\ttoend\n" +
                        "\\d)\\C\t2\ttoend\n" +
                        "\\d)\\w\t2\ttoend\n" +
                        "\\d)\\p\t2\ttoend\n" +
                        "\\d).\\d\t3\ttoend\n" +
                        "\\d)[| +].[| +]\\d\t3\ttoend\n" +
                        "\\d)[| +]+/[| +]\\d\t3\ttoend\n" +
                        "\\a\\e\t2\ttoend\n" +

                        "\\c\t0\ttobegin\n" +
                        "\\c(\\c\t1\ttobegin\n" +


                        "\\p\t0\ttobegin\n" +
                        "\\p)\\d\t2\ttoend\n" +
                        "\\d[| +](.)[| +]\\d\t3\ttoend\n" +
                        "\\d[| +](/)[| +]\\d\t3\ttoend\n" +
                        "\\p)\\c\t2\ttoend\n" +
                        "\\p)\\C\t2\ttoend\n" +
                        "\\p)\\w\t2\ttoend\n";
        String input = "The pt. DID is fichskl 32/3892 3.0.";
        input = FileUtils.readFileToString(new File("../RuSHBenchmark/data/10482.txt"));
        tokenRule = FileUtils.readFileToString(new File("conf/rush_rules.tsv")) + "\n" + tokenRule;
        rush3 = new RuSH3(tokenRule);
        rush3.fillTextInSpan = true;
        ArrayList<Span> sentences = rush3.segToSentenceSpans(input);
        ArrayList<ArrayList<Span>> tokenss = rush3.tokenize(sentences, input);
        System.out.println(tokenss);

    }

    @Test
    public void test9() throws IOException {
        String tokenRule =
                "\\b(\\a\t0\tstbegin\n" +
                        "\\a\\e\t2\tstend\n" +
                        "\\C\t0\ttobegin\n" +
                        "\\C)\\w\t2\ttoend\n" +
                        "\\C)\\p\t2\ttoend\n" +
                        "\\C)\\d\t2\ttoend\n" +

                        "\\c\t0\ttobegin\n" +
                        "\\c)\\w\t2\ttoend\n" +
                        "\\c)\\p\t2\ttoend\n" +
                        "\\c)\\d\t2\ttoend\n" +

                        "\\d\t0\ttobegin\n" +
                        "\\d(\\d\t1\ttobegin\n" +

                        "\\d)\\c\t2\ttoend\n" +
                        "\\d)\\C\t2\ttoend\n" +
                        "\\d)\\w\t2\ttoend\n" +
                        "\\d)\\p\t2\ttoend\n" +
                        "\\d).\\d\t3\ttoend\n" +
                        "\\d)[| +].[| +]\\d\t3\ttoend\n" +
                        "\\d)[| +]+/[| +]\\d\t3\ttoend\n" +
                        "\\a\\e\t2\ttoend\n" +

                        "\\c\t0\ttobegin\n" +
                        "\\c(\\c\t1\ttobegin\n" +


                        "\\p\t0\ttobegin\n" +
                        "\\p)\\d\t2\ttoend\n" +
                        "\\d[| +](.)[| +]\\d\t3\ttoend\n" +
                        "\\d[| +](/)[| +]\\d\t3\ttoend\n" +
                        "\\p)\\c\t2\ttoend\n" +
                        "\\p)\\C\t2\ttoend\n" +
                        "\\p)\\w\t2\ttoend\n";
        String input =
                "stones\n" +
                "Gout\n" +
                "PUD";
        tokenRule = FileUtils.readFileToString(new File("conf/rush_rules.tsv")) + "\n" + tokenRule;
        rush3 = new RuSH3(tokenRule);
        rush3.fillTextInSpan = true;
        ArrayList<Span> sentences = rush3.segToSentenceSpans(input);
        System.out.println(sentences);
        ArrayList<ArrayList<Span>> tokenss = rush3.tokenize(sentences, input);
        System.out.println(tokenss);

    }

}