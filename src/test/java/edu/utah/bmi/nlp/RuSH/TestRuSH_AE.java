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
import edu.utah.bmi.nlp.rush.uima.RuSHTest_AE;
import edu.utah.bmi.nlp.type.system.SectionBody;
import edu.utah.bmi.nlp.type.system.Sentence;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
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
		String typeDescriptor = "desc/type/All_Types";
		jCas = JCasFactory.createJCas(typeDescriptor);
		analysisEngine = AnalysisEngineFactory.createEngine(
				RuSH_AE.class,
				RuSH_AE.PARAM_SENTENCE_TYPE_NAME, "Sentence",
				RuSH_AE.PARAM_TOKEN_TYPE_NAME, "Token",
				RuSH_AE.PARAM_RULE_STR, "conf/rush_rules.tsv",
				RuSH_AE.PARAM_FIX_GAPS, true);
		testAnalysisEngine = AnalysisEngineFactory.createEngine(
				RuSHTest_AE.class,
				RuSHTest_AE.PARAM_SENTENCE_TYPE, "Sentence",
				RuSHTest_AE.PARAM_PRINT_SPAN, true,
				RuSHTest_AE.PARAM_PRINT_TEXT, true);
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
		String text="The patient was treated with IV antibiotics and ventilatory support and at the time of " +
				"this dictation, she has recently been taken to the operating room where it was felt that the airway " +
				"sufficient and she was extubated. She was doing well with good p.o.s, good airway, good voice, and desiring" +
				" to be discharged home. So, the patient is being prepared for discharge at this point. ";
		jCas.setDocumentText(text);
		SourceDocumentInformation sourceDocumentInformation = new SourceDocumentInformation(jCas, 0, text.length());
		sourceDocumentInformation.addToIndexes();
		analysisEngine.process(jCas);
		testAnalysisEngine.process(jCas);
	}

	@Test
	public void test3() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "History: The patient was treated with IV antibiotics and ventilatory support and at the time of " +
				"this dictation, she has recently been taken to the operating room where it was felt that the airway " +
				"sufficient and she was extubated. \nRecommendations: She was doing well with good p.o.s, good airway, good voice, and desiring" +
				" to be discharged home. So, the patient is being prepared for discharge at this point. ";
		jCas.reset();
		jCas.setDocumentText(text);
		SourceDocumentInformation sourceDocumentInformation = new SourceDocumentInformation(jCas, 0, text.length());
		sourceDocumentInformation.addToIndexes();
		SectionBody sectionBody = new SectionBody(jCas, text.indexOf("The patient was treated"), text.indexOf("\nRecommendations") - 1);
		sectionBody.addToIndexes();
		analysisEngine = AnalysisEngineFactory.createEngine(
				RuSH_AE.class,
				RuSH_AE.PARAM_INSIDE_SECTIONS, "SectionBody",
				RuSH_AE.PARAM_SENTENCE_TYPE_NAME, "Sentence",
				RuSH_AE.PARAM_TOKEN_TYPE_NAME, "Token",
				RuSH_AE.PARAM_RULE_STR, "conf/rush_rules.tsv",
				RuSH_AE.PARAM_INCLUDE_PUNCTUATION,true,
				RuSH_AE.PARAM_FIX_GAPS, true);
		testAnalysisEngine = AnalysisEngineFactory.createEngine(
				RuSHTest_AE.class,
				RuSHTest_AE.PARAM_SENTENCE_TYPE, Annotation.class.getCanonicalName(),
				RuSHTest_AE.PARAM_PRINT_SPAN, true,
				RuSHTest_AE.PARAM_PRINT_TEXT, true);
		analysisEngine.process(jCas);
		testAnalysisEngine.process(jCas);
	}

	@Test
	public void test4() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text =
				"TEMP 98.6,   FIO2 WEANED TO .70 DURING ";
		jCas.reset();
		jCas.setDocumentText(text);
		SourceDocumentInformation sourceDocumentInformation = new SourceDocumentInformation(jCas, 0, text.length());
		sourceDocumentInformation.addToIndexes();
		SectionBody sectionBody = new SectionBody(jCas, 0, text.length());
		sectionBody.addToIndexes();
		analysisEngine = AnalysisEngineFactory.createEngine(
				RuSH_AE.class,
				RuSH_AE.PARAM_INSIDE_SECTIONS, "SectionBody",
				RuSH_AE.PARAM_SENTENCE_TYPE_NAME, "Sentence",
				RuSH_AE.PARAM_TOKEN_TYPE_NAME, "Token",
				RuSH_AE.PARAM_RULE_STR, "src/test/resources/edw.tsv",
				RuSH_AE.PARAM_INCLUDE_PUNCTUATION,true,
				RuSH_AE.PARAM_FIX_GAPS, true);
		testAnalysisEngine = AnalysisEngineFactory.createEngine(
				RuSHTest_AE.class,
				RuSHTest_AE.PARAM_SENTENCE_TYPE, Annotation.class.getCanonicalName(),
				RuSHTest_AE.PARAM_PRINT_SPAN, true,
				RuSHTest_AE.PARAM_PRINT_TEXT, true);
		analysisEngine.process(jCas);
		testAnalysisEngine.process(jCas);
	}
}
