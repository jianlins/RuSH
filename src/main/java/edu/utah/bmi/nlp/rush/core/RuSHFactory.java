package edu.utah.bmi.nlp.rush.core;

import edu.utah.bmi.nlp.core.DeterminantValueSet.Determinants;
import edu.utah.bmi.nlp.core.IOUtil;
import edu.utah.bmi.nlp.core.Rule;
import edu.utah.bmi.nlp.fastcner.FastCRuleCN;
import edu.utah.bmi.nlp.fastcner.FastCRuleSB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static edu.utah.bmi.nlp.rush.core.RuSH.TOKENBEGIN;

public class RuSHFactory {
    public static Object[] createFastRuSHRule(String ruleStr) {
        FastCRuleSB frr;
        Object[] output = buildRuleStore(ruleStr);
        HashMap<Integer, Rule> ruleStore = (HashMap<Integer, Rule>) output[0];
        String type = (String) output[1];
        boolean tokenRuleEnabled = (boolean) output[2];
        switch (type) {
            case "cn":
                frr = new FastCRuleCN(ruleStore);
                break;
            default:
                frr = new FastCRuleSB(ruleStore);
                break;
        }
        frr.setReplicationSupport(true);
        frr.setCompareMethod("scorewidth");
        return new Object[]{frr, tokenRuleEnabled, type};
    }

    public static Object[] buildRuleStore(String ruleStr) {
        HashMap<Integer, Rule> ruleStore = new HashMap<>();
        IOUtil ioUtil = new IOUtil(ruleStr);
        String type = "en";
        if (ioUtil.settings.containsKey("cn"))
            type = "cn";
        else if (!ioUtil.settings.containsKey("en"))
            FastCRuleSB.logger.warning("RuSH rule type is not specified. Assume it is 'en'.");
        boolean tokenRuleEnabled = false;
//        old format doesn't have ACTUAL, PSEUDO type, but inferring the type based on the scores
        boolean oldformat = true;
        for (ArrayList<String> cells : ioUtil.getRuleCells()) {
            int id = Integer.parseInt(cells.get(0));
            String rule = cells.get(1);
            String ruleName;
            double score = 0;
            Determinants determinant = null;
            score = Double.parseDouble(cells.get(2));
            ruleName = cells.get(3).trim();
            if (!tokenRuleEnabled && ruleName.equals(TOKENBEGIN))
                tokenRuleEnabled = true;
            if (cells.size() > 4) {
                determinant = Determinants.valueOf(cells.get(4));
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
