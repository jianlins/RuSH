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
import edu.utah.bmi.nlp.fastcner.FastCNER;
import edu.utah.bmi.nlp.rush.core.RuSH;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This rule set are more inclusive that is trying to include all non-whitespace characters in a sentence.
 *
 * @Author Jianlin Shi
 */
public class TestRuSHCN {
    private RuSH rush;

    public static void printDetails(ArrayList<Span> sentences, String input) {
        for (int i = 0; i < sentences.size(); i++) {
            Span sentence = sentences.get(i);
            System.out.println("assert (sentences.get(" + i + ").begin == " + sentence.begin + " &&" +
                    " sentences.get(" + i + ").end == " + sentence.end + ");");
        }

    }

    @Test
    public void testCharacterizer(){
        String input="患者血压123/88mmHg，呼吸3.0次/分。";
        String rule="@fastcnercn\n" +
//                "\\b(\\a\t0\ttobegin\n" +
//                "\\a\\e\t2\ttoend\n" +
                "\\C\t0\ttobegin\n" +
                "\\C\t2\ttoend\n" +
                "\\d+\t0\ttobegin\n" +
                "\\d+\t2\ttoend\n" +
                "\\p+\t0\ttobegin\n" +
                "\\p+\t2\ttoend\n" +
                "\\d+/\\d+\t0\ttobegin\n" +
                "\\d+/\\d+\t2\ttoend\n" +
                "\\d+.\\d+\t0\ttobegin\n" +
                "\\d+.\\d+\t2\ttoend";
        FastCNER fcrp=new FastCNER(rule);
        HashMap<String, ArrayList<Span>> res = fcrp.processString(input);
        fcrp.printRulesMap();
        for(Map.Entry<String,ArrayList<Span>>entry:res.entrySet()){
            System.out.println(entry.getKey()+"\n"+entry.getValue());
        }
    }

    @Test
    public void testTokenizer(){
        String input="患者血压123/88mmHg，呼吸3.0次/分。";
        String rule="@fastcnercn\n" +
                "\\b(\\a\t0\tstbegin\n" +
                "\\a\\e\t2\tstend\n" +
                "\\C\t0\ttobegin\n" +
                "\\C\t2\ttoend\n" +
                "\\d+\t0\ttobegin\n" +
                "\\d+\t2\ttoend\n" +
                "\\p+\t0\ttobegin\n" +
                "\\p+\t2\ttoend\n" +
                "\\d+/\\d+\t0\ttobegin\n" +
                "\\d+/\\d+\t2\ttoend\n" +
                "\\d+.\\d+\t0\ttobegin\n" +
                "\\d+.\\d+\t2\ttoend";
        rush=new RuSH(rule);
        rush.fillTextInSpan=true;
        ArrayList<Span> sentences = rush.segToSentenceSpans(input);
        ArrayList<ArrayList<Span>> tokens = rush.tokenize(sentences, input);
        System.out.println(tokens);
        for(ArrayList<Span>token:tokens){
            System.out.println(token);
        }
    }

    @Test
    public void testSimpleTokenizer(){
        String input="患者血压123/88mmHg，呼吸3.0次/分。";
        String rule="@fastcnercn\n" +
                "\\b(\\a\t0\tstbegin\n" +
                "\\a\\e\t2\tstend\n" ;
        rush=new RuSH(rule);
        rush.fillTextInSpan=true;
        ArrayList<Span> sentences = rush.segToSentenceSpans(input);
        ArrayList<ArrayList<Span>> tokens = rush.tokenize(sentences, input);
        System.out.println(tokens);
        for(ArrayList<Span>token:tokens){
            System.out.println(token);
        }
    }


}