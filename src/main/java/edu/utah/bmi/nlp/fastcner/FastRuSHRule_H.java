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

import static java.lang.Character.*;

/**
 * Change the rule processing engine's output format.
 */
public class FastRuSHRule_H extends FastCRuleSB implements FastRuSHRule {
    protected HashMap<String, String> typeMergeMap = new HashMap<>();
    public static Logger logger = IOUtil.getLogger(FastRule.class);
    public boolean tokenRuleEnabled = false;

    public FastRuSHRule_H(String ruleStr) {
        Object[] output = FastRuSHFactory.buildRuleStore(ruleStr);
        HashMap<Integer, Rule> ruleStore = (HashMap<Integer, Rule>) output[0];
        tokenRuleEnabled = (boolean) output[2];
        initiate(ruleStore);
    }

    public FastRuSHRule_H(HashMap<Integer, Rule> ruleStore) {
        super(ruleStore);
    }


    public HashMap<String, ArrayList<Marker>> processText(String text) {
        // use the first "startposition" to remember the original start matching
        // position.
        // use the 2nd one to remember the start position in which recursion.
        HashMap<String, HashMap<Float, Marker>> matches = new HashMap<>();
        char[] textChars = text.toCharArray();
        for (int i = 0; i < textChars.length; i++) {
            char previousChar = i > 0 ? textChars[i - 1] : ' ';
            processRules(text, textChars, rulesMap, i, i, 0, i, matches, previousChar, false, ' ');
        }
        HashMap<String, ArrayList<Marker>> res = new HashMap<>();
        for (String type : matches.keySet()) {
            HashMap<Float, Marker> spans = matches.get(type);
            ArrayList<Marker> list = new ArrayList<>();
            for (Marker marker : spans.values()) {
                if (removePseudo && ruleStore.get(marker.ruleId).type == DeterminantValueSet.Determinants.PSEUDO)
                    continue;
                list.add(marker);
            }
            Collections.sort(list);
            res.put(type, list);
        }
        return res;

    }

    protected void processRules(String text, char[] textChars, HashMap rule, int ruleStartPosition, int matchBegin, int matchEnd, int currentPosition,
                                Map<String, HashMap<Float, Marker>> matches,
                                char previousChar, boolean wildcard, char previousKey) {
        // when reach the fend of the tunedcontext, fend the iteration
        if (currentPosition < textChars.length) {
            char thisChar = textChars[currentPosition];

            if (rule.containsKey('\\')) {
                processWildCards(text, textChars, (HashMap) rule.get('\\'), ruleStartPosition, matchBegin, matchEnd, currentPosition, matches, previousChar, true, '\\');
            }
            if (rule.containsKey('(') && previousKey != '\\') {
                processRules(text, textChars, (HashMap) rule.get('('), ruleStartPosition, currentPosition, matchEnd, currentPosition, matches,
                        previousChar, false, '(');
            }
            if (rule.containsKey(')') && previousKey != '\\') {
                processRules(text, textChars, (HashMap) rule.get(')'), ruleStartPosition, matchBegin, currentPosition, currentPosition, matches,
                        previousChar, false, ')');

            }
            // if the fend of a rule is met

            if (rule.containsKey(END)) {
                addDeterminants(text, rule, matches, ruleStartPosition, matchBegin, matchEnd, currentPosition);
            }
            // if the current token match the element of a rule
            if (rule.containsKey(thisChar) && (thisChar != ')' && thisChar != '(')) {
                processRules(text, textChars, (HashMap) rule.get(thisChar), ruleStartPosition, matchBegin, matchEnd, currentPosition + 1, matches,
                        thisChar, false, thisChar);
            }

//            if(currentRepeats>0)
//                currentRepeats=currentRepeats;
//          Replications of current char
            if (rule.containsKey('+')) {
//                processRules(textChars, (HashMap) rule.get('+'), matchBegin, matchEnd, currentPosition, matches,
//                        previousChar, false, ' ');
                processRules(text, textChars, (HashMap) rule.get('+'), ruleStartPosition, matchBegin, matchEnd, currentPosition, matches,
                        thisChar, false, '+');
                processReplicants(text, textChars, (HashMap) rule.get('+'), ruleStartPosition, matchBegin, matchEnd, currentPosition, matches,
                        thisChar, wildcard, previousKey);
            }


        } else if (currentPosition == textChars.length && rule.containsKey(END)) {
            if (matchEnd == 0)
                addDeterminants(text, rule, matches, ruleStartPosition, matchBegin, currentPosition, currentPosition);
            else
                addDeterminants(text, rule, matches, ruleStartPosition, matchBegin, matchEnd, currentPosition);
        } else if (currentPosition == textChars.length && rule.containsKey('\\') && ((HashMap) rule.get('\\')).containsKey('e')) {
            HashMap deterRule = ((HashMap) ((HashMap) rule.get('\\')).get('e'));
            if (matchEnd == 0)
                addDeterminants(text, deterRule, matches, ruleStartPosition, matchBegin, currentPosition, currentPosition);
            else
                addDeterminants(text, deterRule, matches, ruleStartPosition, matchBegin, matchEnd, currentPosition);
        } else if (currentPosition == textChars.length && rule.containsKey(')')) {
            HashMap deterRule = (HashMap) rule.get(')');
            if (deterRule.containsKey(END)) {
                addDeterminants(text, deterRule, matches, ruleStartPosition, matchBegin, currentPosition, currentPosition);
            } else if (deterRule.containsKey('\\') && ((HashMap) deterRule.get('\\')).containsKey('e'))
                processRules(text, textChars, (HashMap) ((HashMap) deterRule.get('\\')).get('e'), ruleStartPosition, matchBegin, matchEnd, currentPosition, matches, previousChar, false, ' ');
        } else if (currentPosition == textChars.length && rule.containsKey('+')) {
            HashMap deterRule = (HashMap) rule.get('+');
            processRules(text, textChars, deterRule, ruleStartPosition, matchBegin, matchEnd, currentPosition, matches, previousChar, wildcard, previousKey);
        }
    }


    protected boolean iss(char thisChar) {
        return (thisChar == ' ' || thisChar == '\t' || (int) thisChar == 160);
    }

    protected boolean isd(char thisChar) {
        return isDigit(thisChar);
    }

    protected boolean isC(char thisChar) {
        return isUpperCase(thisChar);
    }

    protected boolean isc(char thisChar) {
        return isLowerCase(thisChar);
    }

    protected boolean isp(char thisChar) {
        return WildCardChecker.isPunctuation(thisChar);
    }

    protected boolean isu(char thisChar) {
        return WildCardChecker.isSpecialChar(thisChar);
    }

    protected boolean isw(char thisChar) {
        return isWhitespace(thisChar) || (int) thisChar == 160 || WildCardChecker.isSpecialChar(thisChar);
    }

    protected boolean isa(char thisChar) {
        return !isWhitespace(thisChar) && !((int) thisChar == 160);
    }

    protected void processReplicants(String text, char[] textChars, HashMap rule, int ruleStartPosition, int matchBegin, int matchEnd, int currentPosition,
                                     Map<String, HashMap<Float, Marker>> matches, char previousChar, boolean wildcard, char previousKey) {
        char thisChar = textChars[currentPosition];
        int currentRepeats = 0;
        if (wildcard) {
            switch (previousKey) {
                case 's':
                    //                        if (thisChar == ' ' || thisChar == '\t' || (int)thisChar==160 || (scSupport && !(isLetterOrDigit(thisChar) || isWhitespace(thisChar) || WildCardChecker.isPunctuation(thisChar)))) {
                    if (iss(thisChar)) {
                        while (iss(thisChar) && currentRepeats < maxRepeatLength && currentPosition < textChars.length) {
                            currentPosition++;
                            currentRepeats++;
                            if (currentPosition == textChars.length)
                                break;
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
                case 'n':
                    if ((thisChar == '\n' || thisChar == '\r')) {
                        while ((thisChar == '\n' || thisChar == '\r') && currentRepeats < maxRepeatLength && currentPosition < textChars.length) {
                            currentPosition++;
                            currentRepeats++;
                            if (currentPosition == textChars.length)
                                break;
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
                case 'd':
                    if (isd(thisChar)) {
                        while ((isd(thisChar)) && currentRepeats < maxRepeatLength && currentPosition < textChars.length) {
                            currentPosition++;
                            currentRepeats++;
                            if (currentPosition == textChars.length)
                                break;
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
                case 'C':
                    if (isC(thisChar)) {
                        while ((isC(thisChar)) && currentRepeats < maxRepeatLength && currentPosition < textChars.length) {
                            currentPosition++;
                            currentRepeats++;
                            if (currentPosition == textChars.length)
                                break;
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
                case 'c':
                    if (isc(thisChar)) {
                        while ((isc(thisChar)) && currentRepeats < maxRepeatLength && currentPosition < textChars.length) {
                            currentPosition++;
                            currentRepeats++;
                            if (currentPosition == textChars.length)
                                break;
                            thisChar = textChars[currentPosition];
                        }//
                    }
                    break;
                case 'p':
                    if (isp(thisChar)) {
                        while ((isp(thisChar)) && currentRepeats < maxRepeatLength && currentPosition < textChars.length) {
                            currentPosition++;
                            currentRepeats++;
                            if (currentPosition == textChars.length)
                                break;
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
                case 'a':
                    if (isa(thisChar)) {
                        while ((isa(thisChar)) && currentRepeats < maxRepeatLength && currentPosition < textChars.length) {
                            currentPosition++;
                            currentRepeats++;
                            if (currentPosition == textChars.length)
                                break;
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
                case 'u':
                    if (isu(thisChar)) {
                        while (isu(thisChar) && currentRepeats < maxRepeatLength && currentPosition < textChars.length) {
                            currentPosition++;
                            currentRepeats++;
                            if (currentPosition == textChars.length)
                                break;
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
                case 'w':
                    if (isw(thisChar)) {
                        while (isw(thisChar) && currentRepeats < maxRepeatLength && currentPosition < textChars.length) {
                            currentPosition++;
                            currentRepeats++;
                            if (currentPosition == textChars.length)
                                break;
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
            }
            processRules(text, textChars, rule, ruleStartPosition, matchBegin, matchEnd, currentPosition, matches,
                    previousChar, false, '+');
        } else if (thisChar == previousKey) {
            while ((thisChar == previousKey) && currentRepeats < maxRepeatLength && currentPosition < textChars.length) {
                currentPosition++;
                currentRepeats++;
                if (currentPosition == textChars.length)
                    break;
                thisChar = textChars[currentPosition];

            }
            processRules(text, textChars, rule, ruleStartPosition, matchBegin, matchEnd, currentPosition, matches,
                    previousChar, false, '+');
        }
    }


    protected void processWildCards(String text, char[] textChars, HashMap rule, int ruleStartPosition, int matchBegin, int matchEnd, int currentPosition,
                                    Map<String, HashMap<Float, Marker>> matches, char previousChar, boolean wildcard, char previousKey) {
        char thisChar = textChars[currentPosition];
        for (Object rulechar : rule.keySet()) {
            char thisRuleChar = (Character) rulechar;
            switch (thisRuleChar) {
                case 's':
//                    if (thisChar == ' ' || thisChar == '\t' || (scSupport && !(isLetterOrDigit(thisChar) || isWhitespace(thisChar) || WildCardChecker.isPunctuation(thisChar)))) {
                    if (iss(thisChar)) {
                        processRules(text, textChars, (HashMap) rule.get('s'), ruleStartPosition, matchBegin, matchEnd, currentPosition + 1, matches,
                                thisChar, true, 's');
                    }
                    break;
                case 'n':
                    if (thisChar == '\n' || thisChar == '\r') {
                        processRules(text, textChars, (HashMap) rule.get('n'), ruleStartPosition, matchBegin, matchEnd, currentPosition + 1, matches,
                                thisChar, true, 'n');
                    }
                    break;
                case '(':
                    if (thisChar == '(')
                        processRules(text, textChars, (HashMap) rule.get('('), ruleStartPosition, matchBegin, matchEnd, currentPosition + 1, matches,
                                thisChar, true, '(');
                    break;
                case ')':
                    if (thisChar == ')')
                        processRules(text, textChars, (HashMap) rule.get(')'), ruleStartPosition, matchBegin, matchEnd, currentPosition + 1, matches,
                                thisChar, true, ')');
                    break;
                case 'd':
                    if (isd(thisChar)) {
                        processRules(text, textChars, (HashMap) rule.get('d'), ruleStartPosition, matchBegin, matchEnd, currentPosition + 1, matches,
                                thisChar, true, 'd');
                    }
                    break;
                case 'C':
                    if (isC(thisChar)) {
                        processRules(text, textChars, (HashMap) rule.get('C'), ruleStartPosition, matchBegin, matchEnd, currentPosition + 1, matches,
                                thisChar, true, 'C');
                    }
                    break;
                case 'c':
                    if (isc(thisChar)) {
                        processRules(text, textChars, (HashMap) rule.get('c'), ruleStartPosition, matchBegin, matchEnd, currentPosition + 1, matches,
                                thisChar, true, 'c');
                    }
                    break;
                case 'p':
                    if (isp(thisChar)) {
                        processRules(text, textChars, (HashMap) rule.get('p'), ruleStartPosition, matchBegin, matchEnd, currentPosition + 1, matches,
                                thisChar, true, 'p');
                    }
                    break;
                case '+':
                    if (thisChar == '+') {
                        processRules(text, textChars, (HashMap) rule.get('+'), ruleStartPosition, matchBegin, matchEnd, currentPosition + 1, matches,
                                thisChar, true, '+');
                    }
                    break;
                case '\\':
                    if (thisChar == '\\') {
                        processRules(text, textChars, (HashMap) rule.get('\\'), ruleStartPosition, matchBegin, matchEnd, currentPosition + 1, matches,
                                thisChar, false, '\\');
                    }
                    break;
                case 'b':
                    if (currentPosition == 0)
                        processRules(text, textChars, (HashMap) rule.get('b'), ruleStartPosition, matchBegin, matchEnd, currentPosition, matches,
                                previousChar, false, 'b');
                    break;
                case 'a':
                    if (isa(thisChar))
//                    if(thisChar!=' ' && thisChar!='\t' && thisChar!='\r' && thisChar!='\n')
                        processRules(text, textChars, (HashMap) rule.get('a'), ruleStartPosition, matchBegin, matchEnd, currentPosition + 1, matches,
                                thisChar, true, 'a');
                    break;
                case 'u':
                    if (isu(thisChar))
                        processRules(text, textChars, (HashMap) rule.get('u'), ruleStartPosition, matchBegin, matchEnd, currentPosition + 1, matches,
                                thisChar, true, 'u');
                    break;

                case 'w':
                    if (isw(thisChar)) {
                        processRules(text, textChars, (HashMap) rule.get('w'), ruleStartPosition, matchBegin, matchEnd, currentPosition + 1, matches,
                                thisChar, true, 'w');
                    }
                    break;
//                TODO negation rule
//                case '^':
//                    break;

            }
        }

    }

    /**
     * Redesigned addDeterminants for RuSH purpose
     *
     * @param rule            rule Map for processsing
     * @param matches         matched results
     * @param matchBegin      fbegin position of matched string
     * @param matchEnd        fend  position of matched string
     * @param currentPosition current position
     * @param text            full text
     */
    protected void addDeterminants(String text, HashMap rule, Map<String, HashMap<Float, Marker>> matches, int ruleStartPosition,
                                   int matchBegin, int matchEnd, int currentPosition) {
        HashMap<DeterminantValueSet.Determinants, Integer> deterRule = (HashMap<DeterminantValueSet.Determinants, Integer>) rule.get(END);
        int end = matchEnd == 0 ? currentPosition : matchEnd;
//      rule  error detection
        if (matchBegin > end) {
            StringBuilder sb = new StringBuilder();
            for (Object key : deterRule.keySet()) {
                int rulePos = deterRule.get(key);
                sb.append(getRule(rulePos).toString());
                sb.append("\n");
            }
            logger.warning("Rule definition error ----matched fbegin > matched fend\n" +
                    "check the following rules: \n" + sb.toString());
            int snippetBegin = matchBegin - 100;
            snippetBegin = snippetBegin < 0 ? 0 : snippetBegin;
            int snippetEnd = end + 100;
            snippetEnd = snippetEnd > text.length() ? text.length() : snippetEnd;
            logger.warning("try to match span: " + text.substring(snippetBegin, end) + "<*>"
                    + text.substring(end, matchBegin) + "<*>" + text.substring(matchBegin, snippetEnd));

        }
        int width = end - matchBegin;
        float begin = matchBegin + offset;
        end += offset;
        Marker currentMarker;

        if (logger.isLoggable(Level.FINEST))
            logger.finest("Try to addDeterminants: " + begin + ", " + end
                    + "\t" + text.substring(Math.round(begin), Math.round(end)));


        for (Object key : deterRule.keySet()) {
            currentMarker = new Marker(begin, end, currentPosition - ruleStartPosition);
            HashMap<Float, Marker> matchedSpans = new HashMap<>();
            int rulePos = deterRule.get(key);
            Rule matchedRule = this.ruleStore.get(rulePos);
            double score = matchedRule.score;
            String type = (String) key;
            currentMarker.ruleId = rulePos;
            currentMarker.score = score;
            if (type.endsWith("end")) {
//                currentMarker.position += 0.6f;
                currentMarker.position = currentMarker.end - 0.4f;
                currentMarker.type = MARKERTYPE.END;
                if (!typeMergeMap.containsKey(type))
                    typeMergeMap.put(type, type.substring(0, type.length() - 3));
                type = typeMergeMap.get(type);
            } else {
//               assume the type is "xxxbegin";
                currentMarker.type = MARKERTYPE.BEGIN;
                if (!typeMergeMap.containsKey(type))
                    typeMergeMap.put(type, type.substring(0, type.length() - 5));
                type = typeMergeMap.get(type);
            }
            currentMarker.ruleId = rulePos;
            currentMarker.score = score;
            if (logger.isLoggable(Level.FINEST))
                logger.finest("\t\tRule Id: " + rulePos + "\t" + key + "\t" + getRule(rulePos).type + "\t" + getRuleString(rulePos));
//          If needed, implement your own selection ruleStore and score updating logic below
            if (matches.containsKey(type)) {
//              because the ruleStore are all processed at the same time from the input left to the input right,
//                it becomes more efficient to compare the overlaps
                matchedSpans = matches.get(type);
                if (matchedSpans.containsKey(currentMarker.position)) {
                    Marker existMarker = matchedSpans.get(currentMarker.position);
                    if (currentMarker.score > existMarker.score || (currentMarker.score == existMarker.score && currentMarker.width > existMarker.width)) {
                        matchedSpans.put(currentMarker.position, currentMarker);
                    }
                } else {
                    matchedSpans.put(currentMarker.position, currentMarker);
                }
            } else {
                matchedSpans.put(currentMarker.position, currentMarker);
                matches.put(type, matchedSpans);
            }
        }
    }


}
