/*
 * Copyright  2017  Department of Biomedical Informatics, University of Utah
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
 */
package edu.utah.bmi.nlp.rush.core;

import edu.utah.bmi.nlp.core.Span;

import java.io.Serializable;

import static java.lang.Math.round;

/**
 * Overwrite original RushSpan, support float fbegin and fend for sorting purpose
 *
 * @author Jianlin Shi
 */
public class RushSpan extends Span implements Comparable<Span>, Serializable {
    public enum RUSHTYPE {BEGIN, END}

    public float fbegin, fend;
    public RUSHTYPE type;

    public RushSpan(float fbegin, float fend) {
        this.fbegin = fbegin;
        this.fend = fend;
        this.begin = round(fbegin);
        this.end=round(fend);
        this.width = end-begin;

    }

    public RushSpan(float fbegin, float fend, String text) {
        this.fbegin = fbegin;
        this.fend = fend;
        this.begin = round(fbegin);
        this.end=round(fend);
        this.width = end-begin;
        this.text = text;
    }

    public RushSpan(float fbegin, float fend, int ruleId) {

        this.fbegin = fbegin;
        this.fend = fend;
        this.ruleId = ruleId;
        this.begin = round(fbegin);
        this.end=round(fend);
        this.width = end-begin;
    }

    public RushSpan(float fbegin, float fend, int ruleId, double score) {
        this.fbegin = fbegin;
        this.fend = fend;
        this.ruleId = ruleId;
        this.score = score;
        this.begin = round(fbegin);
        this.end=round(fend);
        this.width = end-begin;
    }

    public RushSpan(float fbegin, float fend, int ruleId, int width, double score) {
        this.fbegin = fbegin;
        this.fend = fend;
        this.ruleId = ruleId;
        this.score = score;
        this.begin = round(fbegin);
        this.end=round(fend);
        this.width = end-begin;
    }

    public RushSpan(float fbegin, float fend, int ruleId, double score, String text) {
        this.fbegin = fbegin;
        this.fend = fend;
        this.ruleId = ruleId;
        this.score = score;
        this.text = text;
        this.begin = round(fbegin);
        this.end=round(fend);
        this.width = end-begin;
    }

    @Override
    public int compareTo(Span o) {
        if (o == null)
            return -1;

        int res = compareFloat(fbegin, ((RushSpan) o).fbegin);
//        if (res == 0) {
//            res = compareFloat((float) score, (float) o.score);
//            if (res == 0)
//                res = compareFloat(width, o.width);
//        }
        return res;
    }

    public int compareFloat(float a, float b) {
        if (a < b)
            return -1;
        else if (a > b)
            return 1;
        else
            return 0;
    }

    public int getBegin() {
        return begin;
    }

    public float getFloatBegin() {
        return fbegin;
    }


    public void setBegin(float fbegin) {
        this.fbegin = fbegin;
        this.begin = round(fbegin);
    }

    public int getEnd() {
        return end;
    }

    public float getFloatEnd() {
        return fend;
    }


    public void setEnd(float fend) {
        this.fend = fend;
        this.end = round(fend);
        this.width=end-begin;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString() {
        return getText();
    }

    public String serialize() {
        return "(Rule" + this.ruleId + ": " + getBegin() + "-" + getEnd() + ":" + score + "):" + getText();
    }



    public int getPosition() {
        return Math.round(fbegin);
    }
}
