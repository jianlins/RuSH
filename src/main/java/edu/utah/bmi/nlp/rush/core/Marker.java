package edu.utah.bmi.nlp.rush.core;


public class Marker implements Comparable<Marker> {
    public enum MARKERTYPE {BEGIN, END}

    ;
    //    use integer float to represent begin, integer+0.5 to represent end.
//     So that begins and ends won't overwrite
    public float position, end;
    public MARKERTYPE type;
    //    still keep the width for priority comparison
    public int ruleId, width;
    public double score;

    @Override
    public int compareTo(Marker o) {
        if (o == null)
            return -1;
        if (position < o.position)
            return -1;
        else if (position == o.position)
            return 0;
        else
            return 1;
    }


    public Marker(float position) {
        this.position = position;
        this.type = null;
    }

    public Marker(float position, float end, int width) {
        this.position = position;
        this.end = end;
        this.type = null;
        this.width = width;
    }


    public Marker(float position, MARKERTYPE type) {
        this.position = position;
        this.type = type;
    }

    public int getPosition() {
        return Math.round(position);
    }

    public String toString() {
        return position + "-" + type;
    }


}
