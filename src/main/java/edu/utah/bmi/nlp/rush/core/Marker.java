package edu.utah.bmi.nlp.rush.core;


import edu.utah.bmi.nlp.core.Span;

public class Marker extends Span {
    public enum MARKERTYPE {BEGIN, END}

    ;
    //    use integer float to represent begin, integer+0.5 to represent end.
//     So that begins and ends won't overwrite
    public float fbegin, fend;
    public MARKERTYPE type;
    //    still keep the width for priority comparison




    public Marker(int begin, int end, String text) {
        construct(begin, end, -1, null, text);
    }

    public Marker(float fbegin) {
        construct(fbegin, 0,-1, null, "");
    }

    public Marker(float fbegin, float fend) {
        construct(fbegin, fend, width, null, "");
    }

    public Marker(float fbegin, float fend, int width) {
        construct(fbegin, fend, width, null, "");
    }


    public Marker(float fbegin, MARKERTYPE type) {
        construct(fbegin, fbegin+1, -1, type, "");
    }

    protected void construct(float fbegin, float fend, int width, MARKERTYPE type, String text) {
        this.fbegin = fbegin;
        this.fend = fend;
        this.begin = (int) fbegin;
        this.end = (int) fend;
        this.type = null;
        if (width == -1)
            this.width = ((int) fend) - ((int) fbegin) + 1;
        else
            this.width = width;
        this.type = type;
        this.text = text;
    }

    protected void construct(int begin, int end, int width, MARKERTYPE type, String text) {
        this.fbegin = begin;
        this.begin = begin;
        this.end = end;
        this.fend = (float) end + 0.5f;
        if (width == -1)
            this.width = end - begin + 1;
        else
            this.width = width;
        this.type = type;
        this.text = text;
    }

    public int getBegin() {
        return (int) fbegin;
    }

    public int getEnd() {
        return (int) fend;
    }

    public void setBegin(int begin) {
        this.fbegin = begin;
    }

    public void setEnd(int end) {
        this.fend = end;
    }

    public String toString() {
        return fbegin + "-" + type;
    }


}
