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

import edu.utah.bmi.nlp.core.IOUtil;
import edu.utah.bmi.nlp.core.Span;
import edu.utah.bmi.nlp.core.WildCardChecker;
import edu.utah.bmi.nlp.rush.core.DeterminantValueSet.Determinants;
import edu.utah.bmi.nlp.rush.core.DeterminantValueSet.DirectionPrefer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.*;

/**
 * This class is an extension of FastRules. Instead of handling string-element rules, it handles char-element rules
 * Wildcard definition:
 * <p>
 * (   Beginning of capturing a group
 * )   End of capturing a group
 * </p>   A punctuation
 * <p>
 * \ plus following characters
 * +   An addition symbol (to distinguish the "+" after a wildcard)
 * (   A left parentheses symbol
 * )   A right parentheses symbol
 * d   A digit
 * C   A capital letter
 * c   A lowercase letter
 * s   A whitespace
 * a   A Non-whitespace character
 * u   A unusual character: not a letter, not a number, not a punctuation, not a whitespace
 * n   A return
 * </p>
 * The wildcard plus "+": 1 or more wildcard
 *
 * @author Jianlin Shi
 */
@SuppressWarnings("rawtypes")
public class FastCRules {
    //  other  fields are defined in abstract class
    protected HashMap<Integer, Double> scores = new HashMap<Integer, Double>();
    protected HashMap<Integer, String> ruleStore = new HashMap<Integer, String>();
    protected int ruleId = 0;
    protected final Determinants END = Determinants.END;
    //  max length of repeat char---to prevent overflow 25 works perfect, 10 is optimized for speed
    protected int maxRepeatLength = 100;
    protected boolean supportReplications = false, scSupport = false;
    protected boolean debug = true;

    protected HashMap rules = new HashMap();
    protected Pattern pdigit;
    protected Matcher mt;


    //    different comparision method for spans if there are overlap, there four options
//    score, scorewidth, width, widthscore
    protected String method = "width";


    protected FastCRules() {
    }


    public FastCRules(String ruleFileName) {
        initiate(ruleFileName);
    }


    /**
     * <p>Read from ruleFile to construct the rules. The Determinants (enum type) will be generated dynamically
     * by read the last element of rule;
     * The format of rule file (Using \t to separate):
     * </p>
     * <p>
     * chars    score   determinant
     * </p>
     *
     * @param ruleFile The path string of the rule file
     */

    protected void initiate(String ruleFile) {
        IOUtil ioUtil = new IOUtil(ruleFile, false);
        if(ioUtil.getSettings().containsKey("maxRepeatLength"))
            maxRepeatLength=Integer.parseInt(ioUtil.getSettings().get("maxRepeatLength"));
        for (ArrayList<String> cells : ioUtil.getRuleCells()) {
            parseRow(cells);
        }
    }

    private void parseRow(ArrayList<String> cells) {
        double score = 0d;
        if (cells.size() > 1)
            score = Double.parseDouble(cells.get(1));
        if (cells.size()<3)
            System.out.println(cells);
        String determinant = cells.get(2).trim();
        char[] rule = cells.get(0).toCharArray();
        addRule(rule, Determinants.valueOf(determinant), score);
    }


    /**
     * Override addRule method
     *
     * @param rule        A char array of rule
     * @param determinant Specify which type of rule is this
     * @param score       Specify the priority score of this rule
     * @return true: if the rule is added
     * false: if the rule is a duplicate
     */
    @SuppressWarnings("unchecked")
    protected boolean addRule(char[] rule, Determinants determinant, double score) {
//      use to store the HashMap sub-chain that have the key chain that overlap with the current rule
//      rule1 to temporally store the hinges of existing HashMap chain that overlap with current rule
        HashMap rule1 = rules;
//      rule2 to construct the new HashMap sub-chain that doesn't overlap with existing chain
        HashMap rule2 = new HashMap();
        HashMap rulet = new HashMap();
        int length = rule.length;
        int i = 0;

        while (i < length && rule1 != null && rule1.containsKey(rule[i])) {
            rule1 = (HashMap) rule1.get(rule[i]);
            i++;
        }
        // if the rule has been included
        if (i == length && rule1.containsKey(END) && rule1.get(END) == determinant) {
            if (debug)
                System.out.println("This rule has been included");
            return false;
        }
        // start with the determinant, construct the last descendant HashMap
        // <Determinant.end, <Determinant, ruleId>>
        if (i == length) {
            if (rule1.containsKey(END)) {
                ((HashMap) rule1.get(END)).put(determinant, ruleId);
            } else {
                rule2.put(determinant, ruleId);
                rule1.put(END, rule2.clone());
            }
            setScore(ruleId, score);
            if (debug) {
                ruleStore.put(ruleId, new String(rule));
            }
            ruleId++;
            return true;
        } else {
            rule2.put(determinant, ruleId);
            rule2.put(END, rule2.clone());
            rule2.remove(determinant);

            // filling the HashMap chain which rules doesn't have the key chain
            for (int j = length - 1; j > i; j--) {
                rulet = (HashMap) rule2.clone();
                rule2.clear();
                rule2.put(rule[j], rulet);
            }
        }
//      map rule to score;
        setScore(ruleId, score);
        if (debug) {
            ruleStore.put(ruleId, new String(rule));
        }
        ruleId++;
//        System.out.println("rule length="+rule.length+" \ti="+i);
        rule1.put(rule[i], rule2.clone());
        return true;
    }

    /**
     * Because the input parameters are different, this method is overridden.
     *
     * @param text            The input text string
     * @param matches         Save the matched string in an ArrayList of Spans
     * @param directionPrefer Specify the preference of directions
     */
    public void processRules(String text, HashMap<Determinants, ArrayList<Span>> matches,
                             DirectionPrefer directionPrefer) {
        // use the first "startposition" to remember the original start matching
        // position.
        // use the 2nd one to remember the start position in which recursion.
//        System.out.println(">" + text + "<");
        char[] textChars = text.toCharArray();
        for (int i = 0; i < textChars.length; i++) {
            char previousChar = i > 0 ? textChars[i - 1] : ' ';
//            System.out.println(">"+textChars[i]+"<");
            processRules(textChars, rules, i, 0, i, matches, directionPrefer, previousChar, false, ' ');
        }

    }


    /**
     * @param textChars       Input string in the format of character array
     * @param rule            The constructed rules Map for processing
     * @param matchBegin      Store the beginning position of matching
     * @param matchEnd        Store the ending position of matching
     * @param currentPosition Store the current position of matching
     * @param matches         Save the matched string in an ArrayList of Spans
     * @param directionPrefer Specify the preference of directions
     * @param previousChar    Store the previous character for wildcard matching use
     * @param wildcard        Whether wildcard is enabled
     * @param previousKey     Store the previous previous matched character for replication detection use
     */
    protected void processRules(char[] textChars, HashMap rule, int matchBegin, int matchEnd, int currentPosition,
                                HashMap<Determinants, ArrayList<Span>> matches, DirectionPrefer directionPrefer,
                                char previousChar, boolean wildcard, char previousKey) {
        // when reach the end of the tunedcontext, end the iteration
        if (currentPosition < textChars.length) {
            char thisChar = textChars[currentPosition];
//			System.out.println("thisToken-"+thisToken);
//            System.out.println("key:"+rule.keySet());
//            System.out.println(rule.values());
            if (rule.containsKey('\\')) {
                processWildCards(textChars, (HashMap) rule.get('\\'), matchBegin, matchEnd, currentPosition, matches, directionPrefer, previousChar);
            }
            if (rule.containsKey('(')) {
                processRules(textChars, (HashMap) rule.get('('), currentPosition, matchEnd, currentPosition, matches,
                        directionPrefer, previousChar, false, '(');
            }
            if (rule.containsKey(')')) {
                processRules(textChars, (HashMap) rule.get(')'), matchBegin, currentPosition - 1, currentPosition, matches,
                        directionPrefer, previousChar, false, ')');
            }
            // if the end of a rule is met

            if (rule.containsKey(END)) {
                addDeterminants(rule, matches, matchBegin, matchEnd, currentPosition, directionPrefer);
            }

//            if(currentRepeats>0)
//                currentRepeats=currentRepeats;
//          Replications of current char
            if (supportReplications && rule.containsKey('+')) {
                processRules(textChars, (HashMap) rule.get('+'), matchBegin, matchEnd, currentPosition, matches,
                        directionPrefer, previousChar, false, ' ');
                processRepetition(textChars, (HashMap) rule.get('+'), matchBegin, matchEnd, currentPosition, matches,
                        directionPrefer, thisChar, wildcard, previousKey);

            }

            // if the current token match the element of a rule
            if (rule.containsKey(thisChar) && (thisChar != ')' && thisChar != '(')) {
                processRules(textChars, (HashMap) rule.get(thisChar), matchBegin, matchEnd, currentPosition + 1, matches,
                        directionPrefer, thisChar, false, thisChar);
            }
        } else if (currentPosition == textChars.length && rule.containsKey(END)) {
            addDeterminants(rule, matches, matchBegin, matchEnd, currentPosition, directionPrefer);
        } else if (currentPosition == textChars.length && rule.containsKey('\\') && ((HashMap) rule.get('\\')).containsKey('e')) {
            HashMap deterRule = ((HashMap) ((HashMap) rule.get('\\')).get('e'));
            addDeterminants(deterRule, matches, matchBegin, matchEnd, currentPosition - 1, directionPrefer);
        } else if (currentPosition == textChars.length && rule.containsKey(')')) {
            HashMap deterRule = (HashMap) rule.get(')');
            if (deterRule.containsKey('\\') && ((HashMap) deterRule.get('\\')).containsKey('e'))
                processRules(textChars, (HashMap) ((HashMap) deterRule.get('\\')).get('e'), matchBegin, matchEnd, currentPosition, matches, directionPrefer, previousChar, false, ' ');
        } else if (currentPosition == textChars.length && rule.containsKey('+')) {
            HashMap deterRule = (HashMap) rule.get('+');
            processRules(textChars, deterRule, matchBegin, matchEnd, currentPosition, matches, directionPrefer, previousChar, wildcard, previousKey);
        }
    }


    /**
     * @param textChars       Input string in the format of character array
     * @param rule            The constructed rules Map for processing
     * @param matchBegin      Store the beginning position of matching
     * @param matchEnd        Store the ending position of matching
     * @param currentPosition Store the current position of matching
     * @param matches         Save the matched string in an ArrayList of Spans
     * @param directionPrefer Specify the preference of directions
     * @param previousChar    Store the previous character for wildcard matching use
     * @param wildcard        Whether wildcard is enabled
     * @param previousKey     Store the previous previous matched character for replication detection use
     */
    protected void processRepetition(char[] textChars, HashMap rule, int matchBegin, int matchEnd, int currentPosition, HashMap<Determinants, ArrayList<Span>> matches, DirectionPrefer directionPrefer, char previousChar, boolean wildcard, char previousKey) {
        char thisChar = textChars[currentPosition];
        int currentRepeats = 0;
        if (wildcard) {
            switch (previousKey) {
                case 's':
                    //                        if (thisChar == ' ' || thisChar == '\t' || (int)thisChar==160 || (scSupport && !(isLetterOrDigit(thisChar) || isWhitespace(thisChar) || WildCardChecker.isPunctuation(thisChar)))) {
                    if ((thisChar == ' ' || thisChar == '\t' || (int) thisChar == 160)) {
                        while ((thisChar == ' ' || thisChar == '\t' || (int) thisChar == 160) && currentRepeats < maxRepeatLength && currentPosition < textChars.length - 1) {
                            currentPosition++;
                            currentRepeats++;
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
                case 'n':
                    if ((thisChar == '\n' || thisChar == '\r')) {
                        while ((thisChar == '\n' || thisChar == '\r') && currentRepeats < maxRepeatLength && currentPosition < textChars.length - 1) {
                            currentPosition++;
                            currentRepeats++;
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
                case 'd':
                    if ((isDigit(thisChar))) {
                        while ((isDigit(thisChar)) && currentRepeats < maxRepeatLength && currentPosition < textChars.length - 1) {
                            currentPosition++;
                            currentRepeats++;
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
                case 'C':
                    if (isUpperCase(thisChar)) {
                        while ((isUpperCase(thisChar)) && currentRepeats < maxRepeatLength && currentPosition < textChars.length - 1) {
                            currentPosition++;
                            currentRepeats++;
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
                case 'c':
                    if ((isLowerCase(thisChar))) {
                        while ((isLowerCase(thisChar)) && currentRepeats < maxRepeatLength && currentPosition < textChars.length - 1) {
                            currentPosition++;
                            currentRepeats++;
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
                case 'p':
                    if (WildCardChecker.isPunctuation(thisChar)) {
                        while ((WildCardChecker.isPunctuation(thisChar)) && currentRepeats < maxRepeatLength && currentPosition < textChars.length - 1) {
                            currentPosition++;
                            currentRepeats++;
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
                case 'a':
                    if (!Character.isWhitespace(thisChar)) {
                        while ((!Character.isWhitespace(thisChar)) && currentRepeats < maxRepeatLength && currentPosition < textChars.length - 1) {
                            currentPosition++;
                            currentRepeats++;
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
                case 'u':
                    if (WildCardChecker.isSpecialChar(thisChar)) {
                        while ((WildCardChecker.isSpecialChar(thisChar)) && currentRepeats < maxRepeatLength && currentPosition < textChars.length - 1) {
                            currentPosition++;
                            currentRepeats++;
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
                case 'w':
                    if (isWhitespace(thisChar) || (int) thisChar == 160 || WildCardChecker.isSpecialChar(thisChar)) {
                        while ((isWhitespace(thisChar) || (int) thisChar == 160 || WildCardChecker.isSpecialChar(thisChar)) && currentRepeats < maxRepeatLength && currentPosition < textChars.length - 1) {
                            currentPosition++;
                            currentRepeats++;
                            //                                System.out.println(textChars.length+":"+currentPosition);
                            thisChar = textChars[currentPosition];
                        }
                    }
                    break;
            }
        } else if (thisChar == previousKey) {
            while ((thisChar == previousKey) && currentRepeats < maxRepeatLength && currentPosition < textChars.length - 1) {
                currentPosition++;
                currentRepeats++;
                thisChar = textChars[currentPosition];
            }
        }

//                processRules(textChars, rule, matchBegin, matchEnd, currentPosition, matches,
//                        directionPrefer, thisChar, wildcard, previousKey);
        processRules(textChars, rule, matchBegin, matchEnd, currentPosition, matches,
                directionPrefer, previousChar, false, '+');
        if (currentPosition == textChars.length - 1) {
            processRules(textChars, rule, matchBegin, matchEnd, currentPosition + 1, matches,
                    directionPrefer, previousChar, false, ' ');
        }


    }

    /**
     * \d   A digit
     * \C   A capital letter
     * \c   A lower case letter
     * \s   A whitespace
     * \n   A return
     * \(   Beginning of capturing a group
     * \)   End of capturing a group
     * \p   A punctuation
     * \+   An addition symbol (to distinguish the "+" after a wildcard)
     * The wildcard plus "+": 1 or more wildcard
     *
     * @param textChars       Input string in the format of character array
     * @param rule            The constructed rules Map for processing
     * @param matchBegin      Store the beginning position of matching
     * @param matchEnd        Store the ending position of matching
     * @param currentPosition Store the current position of matching
     * @param matches         Save the matched string in an ArrayList of Spans
     * @param directionPrefer Specify the preference of directions
     */
    protected void processWildCards(char[] textChars, HashMap rule, int matchBegin, int matchEnd, int currentPosition, HashMap<Determinants, ArrayList<Span>> matches, DirectionPrefer directionPrefer, char previousChar) {
        char thisChar = textChars[currentPosition];
        for (Object rulechar : rule.keySet()) {
            char thisRuleChar = (Character) rulechar;
            switch (thisRuleChar) {
                case 's':
//                    if (thisChar == ' ' || thisChar == '\t' || (scSupport && !(isLetterOrDigit(thisChar) || isWhitespace(thisChar) || WildCardChecker.isPunctuation(thisChar)))) {
                    if (thisChar == ' ' || thisChar == '\t' || (int) thisChar == 160) {
                        processRules(textChars, (HashMap) rule.get('s'), matchBegin, matchEnd, currentPosition + 1, matches,
                                directionPrefer, thisChar, true, 's');
                    }
                    break;
                case 'n':
                    if (thisChar == '\n' || thisChar == '\r') {
                        processRules(textChars, (HashMap) rule.get('n'), matchBegin, matchEnd, currentPosition + 1, matches,
                                directionPrefer, thisChar, true, 'n');
                    }
                    break;
                case '(':
                    if (thisChar == '(')
                        processRules(textChars, (HashMap) rule.get('('), matchBegin, matchEnd, currentPosition + 1, matches,
                                directionPrefer, thisChar, true, '(');
                    break;
                case ')':
                    if (thisChar == ')')
                        processRules(textChars, (HashMap) rule.get(')'), matchBegin, matchEnd, currentPosition + 1, matches,
                                directionPrefer, thisChar, true, ')');
                    break;
                case 'd':
                    if (isDigit(thisChar)) {
                        processRules(textChars, (HashMap) rule.get('d'), matchBegin, matchEnd, currentPosition + 1, matches,
                                directionPrefer, thisChar, true, 'd');
                    }
                    break;
                case 'C':
                    if (isUpperCase(thisChar)) {
                        processRules(textChars, (HashMap) rule.get('C'), matchBegin, matchEnd, currentPosition + 1, matches,
                                directionPrefer, thisChar, true, 'C');
                    }
                    break;
                case 'c':
                    if (isLowerCase(thisChar)) {
                        processRules(textChars, (HashMap) rule.get('c'), matchBegin, matchEnd, currentPosition + 1, matches,
                                directionPrefer, thisChar, true, 'c');
                    }
                    break;
                case 'p':
                    if (WildCardChecker.isPunctuation(thisChar)) {
                        processRules(textChars, (HashMap) rule.get('p'), matchBegin, matchEnd, currentPosition + 1, matches,
                                directionPrefer, thisChar, true, 'p');
                    }
                    break;
                case '+':
                    if (thisChar == '+') {
                        processRules(textChars, (HashMap) rule.get('+'), matchBegin, matchEnd, currentPosition + 1, matches,
                                directionPrefer, thisChar, true, '+');
                    }
                    break;
                case '\\':
                    if (thisChar == '\\') {
                        processRules(textChars, (HashMap) rule.get('\\'), matchBegin, matchEnd, currentPosition + 1, matches,
                                directionPrefer, thisChar, false, '\\');
                    }
                    break;
                case 'b':
                    if (currentPosition == 0)
                        processRules(textChars, (HashMap) rule.get('b'), matchBegin, matchEnd, currentPosition, matches,
                                directionPrefer, previousChar, false, 'b');
                    break;
                case 'a':
                    if (!Character.isWhitespace(thisChar) && (int) thisChar != 160)
//                    if(thisChar!=' ' && thisChar!='\t' && thisChar!='\r' && thisChar!='\n')
                        processRules(textChars, (HashMap) rule.get('a'), matchBegin, matchEnd, currentPosition + 1, matches,
                                directionPrefer, thisChar, true, 'a');
                    break;
                case 'u':
                    if (WildCardChecker.isSpecialChar(thisChar))
                        processRules(textChars, (HashMap) rule.get('u'), matchBegin, matchEnd, currentPosition + 1, matches,
                                directionPrefer, thisChar, true, 'u');
                    break;

                case 'w':
                    if (isWhitespace(thisChar) || (int) thisChar == 160 || WildCardChecker.isSpecialChar(thisChar)) {
                        processRules(textChars, (HashMap) rule.get('w'), matchBegin, matchEnd, currentPosition + 1, matches,
                                directionPrefer, thisChar, true, 'w');
                    }
                    break;
            }
        }

    }

    /**
     * <p>
     * if reaches the end of one or more rules, add all corresponding
     * determinants into the results
     * </p>
     * <p>
     * The priority of multiple applicable rules can be modified. This version
     * uses the following three rules:
     * 1. if determinant spans overlap, choose the determinant with the widest
     * span
     * 2. else if prefer right determinant, choose the determinant with the
     * largest end.
     * 3. else if prefer left determinant, choose the determinant with the
     * smallest begin.
     * </p>
     *
     * @param rule            The constructed rules Map for processing
     * @param matches         Save the matched string in an ArrayList of Spans
     * @param matchBegin      Store the beginning position of matching
     * @param matchEnd        Store the ending position of matching
     * @param currentPosition Store the current position of matching
     * @param directionPrefer Specify the preference of directions
     */
    @SuppressWarnings("unchecked")
    protected void addDeterminants(HashMap rule, HashMap<Determinants, ArrayList<Span>> matches,
                                   int matchBegin, int matchEnd, int currentPosition,
                                   DirectionPrefer directionPrefer) {
        HashMap<Determinants, Integer> deterRule = (HashMap<Determinants, Integer>) rule.get(END);
        int end = matchEnd == 0 ? currentPosition - 1 : matchEnd;
        Span currentSpan = new Span(matchBegin, end);
        ArrayList<Span> currentSpanList = new ArrayList<Span>();
//		TODO need to fix to Interval tree
        for (Object key : deterRule.keySet()) {
            int ruleId = deterRule.get(key);
            double score = getScore(ruleId);
            currentSpan.score = score;
            currentSpan.ruleId = ruleId;
//          If needed, implement your own selection rules and score updating logic below
            if (matches.containsKey(key)) {
//              because the rules are all processed at the same time from the input left to the input right,
//                it becomes more efficient to compare the overlaps
                currentSpanList = matches.get(key);
                Span lastSpan = currentSpanList.get(currentSpanList.size() - 1);

//                  Since there is no directional preference, assume the span is not exclusive within each determinant.
                if (currentSpan.end <= lastSpan.end) {
                    if (currentSpan.end < lastSpan.begin) {
                        currentSpanList.remove(currentSpanList.size() - 1);
                        currentSpanList.add(currentSpan);
                        currentSpanList.add(lastSpan);
                    }
//                      if currentSpan is within lastSpan
                    continue;
                } else if (lastSpan.end >= currentSpan.begin) {
//                      if overlap and current span is has lower priority(wilder by default) than last span
                    if (lastSpan.begin >= currentSpan.end) {
                        currentSpanList.add(currentSpan);
                        continue;
                    } else {
                        if (!compareSpan(currentSpan, lastSpan)) {
                            continue;
                        }
                        currentSpanList.remove(currentSpanList.size() - 1);
                    }
                }
                currentSpanList.add(currentSpan);

            }
            if (currentSpanList.size() == 0)
                currentSpanList.add(currentSpan);
            matches.put((Determinants) key, currentSpanList);
        }
    }


    public double getScore(int ruleId) {
        return scores.get(ruleId);
    }

    public void setScore(int ruleId, double score) {
        scores.put(ruleId, score);
    }

    /**
     * Using "+" to support replications might slow down the performance of FastCRules,
     * try to avoid using it as much as possible.
     *
     * @param support Whether support replication grammar
     */
    public void setReplicationSupport(boolean support) {
        this.supportReplications = support;
    }

    public void setCompareMethod(String method) {
        this.method = method;
    }

    protected boolean compareScoreOnly(Span a, Span b) {
        if (a.score < 0)
            return true;
        if (b.score < 0)
            return false;
        return a.score > b.score;
    }

    protected boolean compareWidthOnly(Span a, Span b) {
        return a.width > b.width;
    }

    protected boolean compareScorePrior(Span a, Span b) {
        if (a.score < 0)
            return true;
        if (b.score < 0)
            return false;
        if (a.score > b.score) {
            return true;
        } else if (a.score == b.score && a.width > b.width) {
            return true;
        }
        return false;
    }

    protected boolean compareWidthPrior(Span a, Span b) {
        if (a.width > b.width) {
            return true;
        } else if (a.width == b.width && a.score > b.score) {
            return true;
        }
        return false;
    }

    protected boolean compareSpan(Span a, Span b) {
        switch (method) {
            case "score":
                return compareScoreOnly(a, b);
            case "scorewidth":
                return compareScorePrior(a, b);
            case "widthscore":
                return compareWidthPrior(a, b);
            default:
                return compareWidthOnly(a, b);
        }
    }

    public void setSpecialCharacterSupport(Boolean scSupport) {
        this.scSupport = scSupport;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getRuleString(int ruleId) {
        if (debug && this.ruleStore.containsKey(ruleId)) {
            return this.ruleStore.get(ruleId);
        } else {
            return "";
        }
    }
}
