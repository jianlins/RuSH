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

import edu.utah.bmi.nlp.core.Span;
import edu.utah.bmi.nlp.rush.core.DeterminantValueSet.Determinants;
import edu.utah.bmi.nlp.rush.core.DeterminantValueSet.DirectionPrefer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is an extension of FastRulesProcessor, so that it supports capturing group within rule.
 * Process the input tokens against rules.
 *
 * @author Jianlin Shi
 */
public class FastCRuleProcessor {
    protected FastCRules fastCRules;


    public FastCRuleProcessor(String ruleFile) {
        // read rules from ruleFile, initiate Patterns

        initiate(ruleFile);
    }


    protected void initiate(String ruleFile) {
        fastCRules = new FastCRules(ruleFile);
    }


    public HashMap<Determinants, ArrayList<Span>> processString(String text, DirectionPrefer directionPrefer) {
        HashMap<Determinants, ArrayList<Span>> matchedRules = new HashMap<Determinants, ArrayList<Span>>();
        matchedRules.clear();
        fastCRules.processRules(text, matchedRules, directionPrefer);
        return matchedRules;
    }

    public void setReplicationSupport(boolean support) {
        fastCRules.setReplicationSupport(support);
    }

    public void setCompareMethod(String method) {
        fastCRules.setCompareMethod(method);
    }

    public void setSpecialCharacterSupport(Boolean scSupport) {
        fastCRules.setSpecialCharacterSupport(scSupport);
    }

    @Deprecated
    public void setDebug(boolean debug) {
        fastCRules.setDebug(debug);
    }

    public String getRuleString(int ruleId) {
        return fastCRules.getRuleString(ruleId);
    }


}


