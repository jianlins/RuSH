package edu.utah.bmi.nlp.fastcner;

import edu.utah.bmi.nlp.core.DeterminantValueSet;
import edu.utah.bmi.nlp.core.IOUtil;
import edu.utah.bmi.nlp.core.Rule;
import edu.utah.bmi.nlp.core.WildCardChecker;
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


}
