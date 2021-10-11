package edu.utah.bmi.nlp.fastcner;

import edu.utah.bmi.nlp.core.*;
import edu.utah.bmi.nlp.fastner.FastRule;
import edu.utah.bmi.nlp.rush.core.Marker;
import edu.utah.bmi.nlp.rush.core.Marker.MARKERTYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.utah.bmi.nlp.rush.core.RuSH.TOKENBEGIN;
import static java.lang.Character.*;

/**
 * Redefine wildcard syntax to support Chinese Characters, full width characters
 * use @fastcnercn in the rule to specify
 * \C Chinese Character
 * \c alphabetic letter
 * \p punctuations including full width punctuations
 * \w especial characters (does not include non-English language characters)
 * \s whitespace (include full width whitespace)
 * \d include both full width and half width digits
 * <p>
 * 01/26/2018 Jianlin Shi
 */
public class FastRuSHRule_HCN extends FastRuSHRule_H {

    public FastRuSHRule_HCN(String ruleStr) {
        super(ruleStr);
    }

    public FastRuSHRule_HCN(HashMap<Integer, Rule> ruleStore) {
        super(ruleStore);
    }


    protected boolean iss(char thisChar) {
        return (thisChar == ' ' || thisChar == '\t' || (int) thisChar == 160);
    }

    protected boolean isd(char thisChar) {
        return UnicodeChecker.isDigit(thisChar);
    }

    protected boolean isC(char thisChar) {
        return UnicodeChecker.isChinese(thisChar);
    }

    protected boolean isc(char thisChar) {
        return UnicodeChecker.isAlphabetic(thisChar);
    }

    protected boolean isp(char thisChar) {
        return UnicodeChecker.isPunctuation(thisChar);
    }

    protected boolean isu(char thisChar) {
        return UnicodeChecker.isSpecialChar(thisChar);
    }

    protected boolean isw(char thisChar) {
        return isWhitespace(thisChar) || (int) thisChar == 160 || UnicodeChecker.isSpecialChar(thisChar) || thisChar == '　';
    }

    protected boolean isa(char thisChar) {
        return !isWhitespace(thisChar) && (int) thisChar != 160 && thisChar != '　';
    }


//    /**
//     * Redesigned addDeterminants for RuSH purpose
//     *
//     * @param rule            rule Map for processsing
//     * @param matches         matched results
//     * @param matchBegin      fbegin position of matched string
//     * @param matchEnd        fend  position of matched string
//     * @param currentPosition current position
//     * @param text            full text
//     */
//    protected void addDeterminants(String text, HashMap rule, Map<String, HashMap<Float, Marker>> matches, int ruleStartPosition,
//                                   int matchBegin, int matchEnd, int currentPosition) {
//
//        HashMap<DeterminantValueSet.Determinants, Integer> deterRule = (HashMap<DeterminantValueSet.Determinants, Integer>) rule.get(END);
//        int end = matchEnd == 0 ? currentPosition : matchEnd;
////      rule  error detection
//        if (matchBegin > end) {
//            StringBuilder sb = new StringBuilder();
//            for (Object key : deterRule.keySet()) {
//                int rulePos = deterRule.get(key);
//                sb.append(getRule(rulePos).toString());
//                sb.append("\n");
//            }
//            logger.warning("Rule definition error ----matched fbegin > matched fend\n" +
//                    "check the following rules: \n" + sb.toString());
//            int snippetBegin = matchBegin - 100;
//            snippetBegin = snippetBegin < 0 ? 0 : snippetBegin;
//            int snippetEnd = end + 100;
//            snippetEnd = snippetEnd > text.length() ? text.length() : snippetEnd;
//            logger.warning("try to match span: " + text.substring(snippetBegin, end) + "<*>"
//                    + text.substring(end, matchBegin) + "<*>" + text.substring(matchBegin, snippetEnd));
//
//        }
//        int width = end - matchBegin;
//        float begin = matchBegin + offset;
//        end += offset;
//        Marker currentMarker;
//
//        if (logger.isLoggable(Level.FINEST))
//            logger.finest("Try to addDeterminants: " + begin + ", " + end
//                    + "\t" + text.substring(Math.round(begin), Math.round(end)));
//
//
//        for (Object key : deterRule.keySet()) {
//            currentMarker = new Marker(begin, end, currentPosition - ruleStartPosition);
//            HashMap<Float, Marker> matchedSpans = new HashMap<>();
//            int rulePos = deterRule.get(key);
//            Rule matchedRule = this.ruleStore.get(rulePos);
//            double score = matchedRule.score;
//            String type = (String) key;
//            currentMarker.ruleId = rulePos;
//            currentMarker.score = score;
//            if (type.endsWith("end")) {
////                currentMarker.position += 0.6f;
//                currentMarker.fbegin = currentMarker.fend - 0.4f;
//                currentMarker.type = MARKERTYPE.END;
//                if (!typeMergeMap.containsKey(type))
//                    typeMergeMap.put(type, type.substring(0, type.length() - 3));
//                type = typeMergeMap.get(type);
//            } else {
////               assume the type is "xxxbegin";
//                currentMarker.type = MARKERTYPE.BEGIN;
//                if (!typeMergeMap.containsKey(type))
//                    typeMergeMap.put(type, type.substring(0, type.length() - 5));
//                type = typeMergeMap.get(type);
//            }
//            currentMarker.ruleId = rulePos;
//            currentMarker.score = score;
//            if (logger.isLoggable(Level.FINEST))
//                logger.finest("\t\tRule Id: " + rulePos + "\t" + key + "\t" + getRule(rulePos).type + "\t" + getRuleString(rulePos));
////          If needed, implement your own selection ruleStore and score updating logic below
//            if (matches.containsKey(type)) {
////              because the ruleStore are all processed at the same time from the input left to the input right,
////                it becomes more efficient to compare the overlaps
//                matchedSpans = matches.get(type);
//                if (matchedSpans.containsKey(currentMarker.fbegin)) {
//                    Marker existMarker = matchedSpans.get(currentMarker.fbegin);
//                    if (currentMarker.score > existMarker.score || (currentMarker.score == existMarker.score && currentMarker.width > existMarker.width)) {
//                        matchedSpans.put(currentMarker.fbegin, currentMarker);
//                    }
//                } else {
//                    matchedSpans.put(currentMarker.fbegin, currentMarker);
//                }
//            } else {
//                matchedSpans.put(currentMarker.fbegin, currentMarker);
//                matches.put(type, matchedSpans);
//            }
//        }
//    }

}
