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
package edu.utah.bmi.nlp.rush.uima;


import edu.utah.bmi.nlp.type.system.Sentence;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * This is a test AE to check the output of RuSH (or other sentence segmenter) in UIMA pipeline.
 *
 * @author Jianlin Shi
 */
public class RuSHTestAE_General extends JCasAnnotator_ImplBase {


    protected static int sentenceTypeId = 0;
    public static final String PARAM_SENTENCE_TYPE = "SentenceTypeName";
    public static final String PARAM_PRINT_SPAN = "PrintSpan";
    public static final String PARAM_PRINT_TEXT = "PrintText";
    protected boolean printSpan = true, printText = false;

    @Override
    public void initialize(UimaContext cont) {
        String sentenceTypeName = "edu.utah.bmi.nlp.type.system.Sentence";
        Object obj = cont.getConfigParameterValue(PARAM_SENTENCE_TYPE);
        if (obj != null)
            sentenceTypeName = (String) obj;
        sentenceTypeName = RuSH_AE.checkTypeDomain(sentenceTypeName);

        obj = cont.getConfigParameterValue(PARAM_PRINT_SPAN);
        if (obj != null && obj instanceof Boolean && (Boolean) obj == false)
            printSpan = false;

        obj = cont.getConfigParameterValue(PARAM_PRINT_TEXT);
        if (obj != null && obj instanceof Boolean && (Boolean) obj != false)
            printText = true;

        try {
            super.initialize(cont);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            sentenceTypeId = getTypeId(Class.forName(sentenceTypeName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException {
        FSIndex annoIndex = jcas.getAnnotationIndex(sentenceTypeId);
        Iterator annoIter = annoIndex.iterator();
        ArrayList<Sentence> sentences = new ArrayList<>();
        while (annoIter.hasNext()) {
            Sentence thisSentence = (Sentence) annoIter.next();
            sentences.add(thisSentence);
            if (printSpan)
                if (printText)
                    System.out.println(thisSentence.getType().getShortName() + "(" + thisSentence.getBegin() + "~" + thisSentence.getEnd() + "):\t" + thisSentence.getCoveredText().replaceAll("\\n", "<\\\\n>"));
                else
                    System.out.println(thisSentence.getType().getShortName() + ":\t" + thisSentence.getBegin() + "~" + thisSentence.getEnd());
        }
        System.out.println("Total sentences: " + sentences.size());
    }


    /**
     * Get Type System registered Id
     *
     * @param typeClass The class object of an UIMA Type
     * @return The registered Id of the input Type
     */
    public int getTypeId(Class typeClass) {
        int id = 0;
        try {
            id = typeClass.getField("type").getInt(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return id;
    }

}