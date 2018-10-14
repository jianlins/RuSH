package edu.utah.bmi.nlp.rush.core;

import edu.utah.bmi.nlp.core.DeterminantValueSet;
import edu.utah.bmi.nlp.core.Span;

public class Boundary extends Span {
    public String ruleStr;
    public String ruleName;
    public DeterminantValueSet.Determinants type;

    public Boundary(int begin, int end, String ruleStr, String ruleName, DeterminantValueSet.Determinants type) {
        setBegin(begin);
        setEnd(end);
        setRuleStr(ruleStr);
        setRuleName(ruleName);
        setType(type);
    }

    public String getRuleStr() {
        return ruleStr;
    }

    public void setRuleStr(String ruleStr) {
        this.ruleStr = ruleStr;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public DeterminantValueSet.Determinants getType() {
        return type;
    }

    public void setType(DeterminantValueSet.Determinants type) {
        this.type = type;
    }


    public int hashCode(){
        return this.ruleStr.hashCode();
    }
}
