package edu.utah.bmi.nlp.rush.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

public class MarkerTest {
    @Test
    public void testMarker(){
        ArrayList<Marker>markers=new ArrayList<>();
        for (int i=0;i<5;i++){
            markers.add(new Marker((int)(Math.random()*100), Marker.MARKERTYPE.BEGIN));
        }
        for (int i=0;i<7;i++){
            markers.add(new Marker((int)(Math.random()*100)+0.5f, Marker.MARKERTYPE.END));
        }

        System.out.println(markers);
        Collections.sort(markers);
        System.out.println(markers);
        for(Marker marker:markers){
            System.out.println(marker.getPosition()+"--"+marker.type);
        }

        markers.get(markers.size()-1).position=55;
        System.out.println(markers);

    }

}