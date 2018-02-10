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

import edu.utah.bmi.nlp.core.DeterminantValueSet;
import edu.utah.bmi.nlp.core.IOUtil;
import edu.utah.bmi.nlp.core.Span;
import edu.utah.bmi.nlp.rush.core.RuSH3;
import edu.utah.bmi.nlp.uima.common.AnnotationOper;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.logging.Logger;


/**
 * This is RuSH Wrapper into UIMA analyses engine.
 *
 * @author Jianlin Shi
 */
public class RuSH_AE extends JCasAnnotator_ImplBase {

    private static Logger logger = IOUtil.getLogger(RuSH_AE.class);
    private RuSH3 rush;
    private boolean autoFixGaps = true;


    //	a list of section names that limit the scope of sentence detection
    // (can use short name if name space is "edu.utah.bmi.nlp.type.system."),
    // separated by "|", ",", or ";".
    public static final String PARAM_INSIDE_SECTIONS = "InsideSections";

    public static final String PARAM_FIX_GAPS = "AutoFixGaps";
    public static final String PARAM_RULE_STR = "RuleStr";
    public static final String PARAM_SENTENCE_TYPE_NAME = "SentenceTypeName";

    //  if this parameter is set, then the adjacent sentences will be annotated in different color-- easy to review
    public static final String PARAM_ALTER_SENTENCE_TYPE_NAME = "AlterSentenceTypeName";

    public static final String PARAM_TOKEN_TYPE_NAME = "TokenTypeName";

    public static final String PARAM_INCLUDE_PUNCTUATION = "IncludePunctuation";

    //   mLanguage is defined in rule file/string
    @Deprecated
    public static final String PARAM_LANGUAGE = "Language";

    @Deprecated
    public static final String PARAM_DEBUG = "Debug";

    protected Class<? extends Annotation> SentenceType, AlterSentenceType, TokenType;
    protected boolean includePunctuation = false, differentColoring = false, colorIndicator = false;
    protected static Constructor<? extends Annotation> SentenceTypeConstructor, AlterSentenceTypeConstructor, TokenTypeConstructor;

    //   mLanguage is defined in rule file/string
    @Deprecated
    private String mLanguage;

    private LinkedHashSet<Class> sectionClasses = new LinkedHashSet<>();

    public void initialize(UimaContext cont) {
        String ruleFileName = (String) (cont
                .getConfigParameterValue(PARAM_RULE_STR));
        rush = new RuSH3(ruleFileName);
//        rush.setDebug(true);
        Object autoFixGapsObj = cont.getConfigParameterValue(PARAM_FIX_GAPS);
        if (autoFixGapsObj != null) {
            autoFixGaps = (Boolean) autoFixGapsObj;
        }
        String sentenceTypeName, alterSentenceTypeName = null, tokenTypeName;
        Object obj = cont.getConfigParameterValue(PARAM_SENTENCE_TYPE_NAME);
        if (obj != null && obj instanceof String) {
            sentenceTypeName = ((String) obj).trim();
            sentenceTypeName = checkTypeDomain(sentenceTypeName);
        } else {
            sentenceTypeName = DeterminantValueSet.defaultNameSpace + "Sentence";
        }
        obj = cont.getConfigParameterValue(PARAM_TOKEN_TYPE_NAME);
        if (obj != null && obj instanceof String) {
            tokenTypeName = ((String) obj).trim();
            tokenTypeName = checkTypeDomain(tokenTypeName);
        } else {
            tokenTypeName = DeterminantValueSet.defaultNameSpace + "Token";
        }


        obj = cont.getConfigParameterValue(PARAM_ALTER_SENTENCE_TYPE_NAME);
        if (obj != null && obj instanceof String) {
            alterSentenceTypeName = ((String) obj).trim();
            alterSentenceTypeName = checkTypeDomain(alterSentenceTypeName);
            if (alterSentenceTypeName.length() > 0)
                differentColoring = true;
        }
        obj = cont.getConfigParameterValue(PARAM_INCLUDE_PUNCTUATION);
        if (obj != null && obj instanceof Boolean && (Boolean) obj != false)
            includePunctuation = true;

        rush.setIncludePunctuation(includePunctuation);

        obj = cont.getConfigParameterValue(PARAM_LANGUAGE);
        if (obj != null && obj instanceof String) {
            mLanguage = ((String) obj).trim().toLowerCase();
        } else {
            mLanguage = "en";
        }

        obj = cont.getConfigParameterValue(PARAM_INSIDE_SECTIONS);
        if (obj == null)
            sectionClasses.add(SourceDocumentInformation.class);
        else {
            String value = (String) obj;
            if (value.length() == 0)
                sectionClasses.add(SourceDocumentInformation.class);
            else
                for (String sectionName : ((String) obj).split("[\\|,;]")) {
                    sectionName = sectionName.trim();
                    if (sectionName.length() > 0) {
                        sectionClasses.add(AnnotationOper.getTypeClass(DeterminantValueSet.checkNameSpace(sectionName)));
                    }
                }
        }

        try {
            SentenceType = Class.forName(sentenceTypeName).asSubclass(Annotation.class);
            TokenType = Class.forName(tokenTypeName).asSubclass(Annotation.class);
            SentenceTypeConstructor = SentenceType.getConstructor(new Class[]{JCas.class, int.class, int.class});
            TokenTypeConstructor = TokenType.getConstructor(new Class[]{JCas.class, int.class, int.class});
            if (differentColoring) {
                AlterSentenceType = Class.forName(alterSentenceTypeName).asSubclass(Annotation.class);
                AlterSentenceTypeConstructor = AlterSentenceType.getConstructor(new Class[]{JCas.class, int.class, int.class});
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }

    public void process(JCas jCas) throws AnalysisEngineProcessException {
        for (Class sectionClass : sectionClasses) {
            FSIndex annoIndex = jCas.getAnnotationIndex(sectionClass);
            Iterator annoIter = annoIndex.iterator();
            while (annoIter.hasNext()) {
                Annotation section = (Annotation) annoIter.next();
                processOneSection(jCas, section);
            }
        }
    }

    private void processOneSection(JCas jCas, Annotation section) {
        String text = section.getCoveredText();
        int sectionBegin = section.getBegin();
        ArrayList<Span> sentences = rush.segToSentenceSpans(text);
        for (Span sentence : sentences) {
            saveSentence(jCas, sentence, sectionBegin);
        }

        ArrayList<ArrayList<Span>> tokenss = rush.tokenize(sentences, text);
        for (ArrayList<Span> tokens : tokenss) {
            for (Span token : tokens) {
                saveToken(jCas, token.begin, token.end, sectionBegin);
            }
        }


    }


    protected void saveTokens(JCas jcas, Span sentence, ArrayList<Span> tokens, int sectionBegin) {
        int sentBegin = sentence.begin;
        for (int i = 0; i < tokens.size(); i++) {
            Span thisSpan = tokens.get(i);
            saveToken(jcas, thisSpan.begin + sentBegin, thisSpan.end + sentBegin, sectionBegin);
        }
    }

    protected void saveSentence(JCas jcas, Span sentence, int offset) {
        saveSentence(jcas, sentence.begin + offset, sentence.end + offset);
    }


    protected void saveToken(JCas jcas, int begin, int end, int offset) {
        saveAnnotation(jcas, TokenTypeConstructor, begin + offset, end + offset);
    }

    protected void saveSentence(JCas jcas, int begin, int end) {
        if (differentColoring) {
            colorIndicator = !colorIndicator;
            if (!colorIndicator) {
                saveAnnotation(jcas, AlterSentenceTypeConstructor, begin, end);
                return;
            }
        }
        saveAnnotation(jcas, SentenceTypeConstructor, begin, end);

    }

    protected void saveAnnotation(JCas jcas, Constructor<? extends Annotation> annoConstructor, int begin, int end) {
        Annotation anno = null;
        try {
            anno = annoConstructor.newInstance(jcas, begin, end);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        anno.addToIndexes();
    }

    public static String checkTypeDomain(String typeName) {
        if (typeName.indexOf(".") == -1) {
            typeName = "edu.utah.bmi.nlp.type.system." + typeName;
        }
        return typeName;
    }
}