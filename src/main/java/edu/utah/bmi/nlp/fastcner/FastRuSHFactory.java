package edu.utah.bmi.nlp.fastcner;

import edu.utah.bmi.nlp.core.DeterminantValueSet;
import edu.utah.bmi.nlp.core.DeterminantValueSet.Determinants;
import edu.utah.bmi.nlp.core.IOUtil;
import edu.utah.bmi.nlp.core.Rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static edu.utah.bmi.nlp.rush.core.RuSH.TOKENBEGIN;

public class FastRuSHFactory {
    public static FastRuSHRule_H createFastRuSHRule(String ruleStr) {
        FastRuSHRule_H frr;
        Object[] output = buildRuleStore(ruleStr);
        HashMap<Integer, Rule> ruleStore = (HashMap<Integer, Rule>) output[0];
        String type = (String) output[1];
        boolean tokenRuleEnabled = (boolean) output[2];
        switch (type) {
            case "cn":
                frr = new FastRuSHRule_HCN(ruleStore);
                break;
            default:
                frr = new FastRuSHRule_H(ruleStore);
                break;
        }
        frr.tokenRuleEnabled = tokenRuleEnabled;
        return frr;
    }

    public static Object[] buildRuleStore(String ruleStr) {
        HashMap<Integer, Rule> ruleStore = new HashMap<>();
        IOUtil ioUtil = new IOUtil(ruleStr);
        String type = "en";
        if (ioUtil.settings.containsKey("cn"))
            type = "cn";
        else if (!ioUtil.settings.containsKey("en"))
            FastRuSHRule_H.logger.warning("RuSH rule type is not specified. Assume it is 'en'.");
        boolean tokenRuleEnabled = false;
//        old format doesn't have ACTUAL, PSEUDO type, but inferring the type based on the scores
        boolean oldformat = true;
        for (ArrayList<String> cells : ioUtil.getRuleCells()) {
            int id = Integer.parseInt(cells.get(0));
            String rule = cells.get(1);
            String ruleName;
            double score = 0;
            DeterminantValueSet.Determinants determinant = null;
            score = Double.parseDouble(cells.get(2));
            ruleName = cells.get(3).trim();
            if (!tokenRuleEnabled && ruleName.equals(TOKENBEGIN))
                tokenRuleEnabled = true;
            if (cells.size() > 4) {
                determinant = DeterminantValueSet.Determinants.valueOf(cells.get(4));
                oldformat = false;
            }
            ruleStore.put(id, new Rule(id, rule, ruleName, score, determinant));
        }
        if (oldformat) {
            for (Map.Entry<Integer, Rule> entry : ruleStore.entrySet()) {
                Rule rule = entry.getValue();
                if (rule.type == null) {
                    rule.type = rule.score % 2 == 0 ? Determinants.ACTUAL : Determinants.PSEUDO;
                }
            }
        } else {
            for (Map.Entry<Integer, Rule> entry : ruleStore.entrySet()) {
                Rule rule = entry.getValue();
                if (rule.type == null) {
                    rule.type = Determinants.ACTUAL;
                }
            }
        }
        return new Object[]{ruleStore, type, tokenRuleEnabled};
    }
}
