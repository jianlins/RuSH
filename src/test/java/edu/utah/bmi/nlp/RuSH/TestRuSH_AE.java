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

import edu.utah.bmi.nlp.rush.uima.RuSH_AE;
import edu.utah.bmi.nlp.rush.uima.RuSHTestAE_General;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

/**
 * @Author Jianlin Shi
 */
public class TestRuSH_AE {
    JCas jCas;
    AnalysisEngine analysisEngine, testAnalysisEngine;

    @Before
    public void init() throws UIMAException {
        String typeDescriptor = "desc/All_Types";
        jCas = JCasFactory.createJCas(typeDescriptor);
        analysisEngine = AnalysisEngineFactory.createEngine(
                RuSH_AE.class,
                RuSH_AE.PARAM_SENTENCE_TYPE_NAME, "Sentence",
                RuSH_AE.PARAM_TOKEN_TYPE_NAME, "Token",
                RuSH_AE.PARAM_RULE_FILE, "conf/rush_rules.csv",
                RuSH_AE.PARAM_FIX_GAPS, true);
        testAnalysisEngine = AnalysisEngineFactory.createEngine(
                RuSHTestAE_General.class,
                RuSHTestAE_General.PARAM_SENTENCE_TYPE, "Sentence",
                RuSHTestAE_General.PARAM_PRINT_SPAN, true,
                RuSHTestAE_General.PARAM_PRINT_TEXT, true);
    }

    @Test
    public void test() throws AnalysisEngineProcessException {
        jCas.reset();
        jCas.setDocumentText("The patient was admitted on 03/26/08\n and was started on IV antibiotics elevation" +
                            ", was also counseled to minimizing the cigarette smoking. The patient had edema\n\n" +
                            "\n of his bilateral lower extremities. The hospital consult was also obtained to " +
                            "address edema issue question was related to his liver hepatitis C. Hospital consult" +
                            " was obtained. This included an ultrasound of his abdomen, which showed just mild " +
                            "cirrhosis. ");
        analysisEngine.process(jCas);
        testAnalysisEngine.process(jCas);
    }

    @Test
    public void test2() throws AnalysisEngineProcessException {
        jCas.reset();
        jCas.setDocumentText("The patient was treated with IV antibiotics and ventilatory support and at the time of " +
                "this dictation, she has recently been taken to the operating room where it was felt that the airway " +
                "sufficient and she was extubated. She was doing well with good p.o.s, good airway, good voice, and desiring" +
                " to be discharged home. So, the patient is being prepared for discharge at this point. ");
        analysisEngine.process(jCas);
        testAnalysisEngine.process(jCas);
    }
}
