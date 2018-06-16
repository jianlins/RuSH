package edu.utah.bmi.nlp.rush.core;

import edu.utah.bmi.nlp.core.Span;
import org.jetbrains.annotations.NotNull;

public class Marker implements Comparable<Marker> {
    //    use integer float to represent begin, integer+0.5 to represent end.
//     So that begins and ends won't overwrite
    public float position;
    public MARKERTYPE type;

    @Override
    public int compareTo(@NotNull Marker o) {
        if (o == null)
            return -1;
        if (position < o.position)
            return -1;
        else if (position == o.position)
            return 0;
        else
            return 1;

    }

    public enum MARKERTYPE {BEGIN, END}

    ;

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
