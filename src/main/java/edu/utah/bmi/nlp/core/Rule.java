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
package edu.utah.bmi.nlp.core;

/**
 * @author Jianlin Shi
 */
public class Rule implements Cloneable {
    // The rule id in the definition file or owl file
    public int id;
    // The name(class) of name entity
    public String ruleName;
    // The actual definition string of this name entity
    public String rule;
    // Whether the rule is pseudo or actual
    public DeterminantValueSet.Determinants type;

    public double score;


    public Rule(int id, String rule, String ruleName, DeterminantValueSet.Determinants type) {
        this.id = id;
        this.ruleName = ruleName;
        this.rule = rule;
        this.type = type;
    }

    public Rule(int id, String rule, String ruleName, double score, DeterminantValueSet.Determinants type) {
        this.id = id;
        this.ruleName = ruleName;
        this.rule = rule;
        this.type = type;
        this.score = score;
    }

    public String toString() {
        StringBuilder serialized = new StringBuilder();
        serialized.append("Rule ");
        serialized.append(id);
        serialized.append(":\n");
        serialized.append("\trule content:\t");
        serialized.append(rule);
        serialized.append("\trule name:\t");
        serialized.append(ruleName);
        serialized.append("\trule score:\t");
        serialized.append(score);
        serialized.append("\trule type:\t");
        serialized.append(type);
        return serialized.toString();
    }

    public Rule clone() {
        return new Rule(id, rule, ruleName, score, type);
    }
}
