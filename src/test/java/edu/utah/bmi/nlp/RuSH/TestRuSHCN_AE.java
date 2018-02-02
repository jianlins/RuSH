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

import edu.utah.bmi.nlp.rush.uima.RuSHTest_AE;
import edu.utah.bmi.nlp.rush.uima.RuSH_AE;
import edu.utah.bmi.nlp.type.system.ConceptBASE;
import edu.utah.bmi.nlp.type.system.SectionBody;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
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
public class TestRuSHCN_AE {
	JCas jCas;
	AnalysisEngine analysisEngine, testAnalysisEngine;

	@Before
	public void init() throws UIMAException {
		String typeDescriptor = "desc/type/All_Types";
		jCas = JCasFactory.createJCas(typeDescriptor);
		String rule="@fastcnercn\n" +
				"\\b(\\a\t0\tstbegin\n" +
				"\\a\\e\t2\tstend\n" ;
		analysisEngine = AnalysisEngineFactory.createEngine(
				RuSH_AE.class,
				RuSH_AE.PARAM_SENTENCE_TYPE_NAME, "Sentence",
				RuSH_AE.PARAM_TOKEN_TYPE_NAME, "Token",
				RuSH_AE.PARAM_RULE_STR, rule,
				RuSH_AE.PARAM_INCLUDE_PUNCTUATION,true,
				RuSH_AE.PARAM_LANGUAGE,"cn",
				RuSH_AE.PARAM_FIX_GAPS, true);
		testAnalysisEngine = AnalysisEngineFactory.createEngine(
				RuSHTest_AE.class,
				RuSHTest_AE.PARAM_SENTENCE_TYPE, "Sentence",
				RuSHTest_AE.PARAM_PRINT_SPAN, true,
				RuSHTest_AE.PARAM_PRINT_TEXT, true);
	}

	@Test
	public void test() throws AnalysisEngineProcessException {
		String input="患者血压123/88mmHg，呼吸3.0次/分。";
		jCas.reset();
		jCas.setDocumentText(input);
		new SourceDocumentInformation(jCas,0,input.length()).addToIndexes();
		analysisEngine.process(jCas);
		printAnnotations(jCas);
//		testAnalysisEngine.process(jCas);
	}

	@Test
	public void test2() throws AnalysisEngineProcessException {
		String input="患者血压123/88mmHg，呼吸3.0次/分。";
		jCas.reset();
		jCas.setDocumentText(input);
		new SourceDocumentInformation(jCas,0,input.length()).addToIndexes();
		analysisEngine.process(jCas);
		printAnnotations(jCas);
//		testAnalysisEngine.process(jCas);
	}

	private void printAnnotations(JCas jCas) {
		FSIterator it = jCas.getAnnotationIndex(Annotation.type).iterator();
		while (it.hasNext()) {
			Annotation thisAnnotation = (Annotation) it.next();
			System.out.println(thisAnnotation.getClass().getSimpleName());
			System.out.println("\t" + thisAnnotation.getCoveredText());
			System.out.println("\t" + thisAnnotation.getBegin()+"-" + thisAnnotation.getEnd());
		}
	}
}
