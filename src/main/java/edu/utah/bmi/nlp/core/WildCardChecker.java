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
public class WildCardChecker {
    public static boolean isPunctuation(String s) {
//        !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
        char c = s.charAt(0);
        return s.length() == 1 && (c == '!'
                || c == '"' || c == '#' || c == '$' || c == '%'
                || c == '&' || c == '\'' || c == '(' || c == ')'
                || c == '*' || c == '+' || c == ',' || c == '-'
                || c == '.' || c == '/' || c == ':' || c == ';'
                || c == '<' || c == '=' || c == '>' || c == '?'
                || c == '@' || c == '[' || c == '\\' || c == ']'
                || c == '^' || c == '_' || c == '`' || c == '{'
                || c == '|' || c == '}' || c == '~');
    }

    public static boolean isPunctuation(char c) {
//        !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~

        return c == '!'
                || c == '"' || c == '#' || c == '$' || c == '%'
                || c == '&' || c == '\'' || c == '(' || c == ')'
                || c == '*' || c == '+' || c == ',' || c == '-'
                || c == '.' || c == '/' || c == ':' || c == ';'
                || c == '<' || c == '=' || c == '>' || c == '?'
                || c == '@' || c == '[' || c == '\\' || c == ']'
                || c == '^' || c == '_' || c == '`' || c == '{'
                || c == '|' || c == '}' || c == '~';
    }

    public static boolean isSpecialChar(char c) {
        int d = (int) c;
        return d > 126 && d != 160;
    }

}
