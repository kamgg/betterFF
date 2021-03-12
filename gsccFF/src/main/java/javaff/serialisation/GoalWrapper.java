package javaff.serialisation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javaff.data.Fact;
import javaff.data.strips.And;


import javaff.serialisation.Heuristic;

public class GoalWrapper implements Iterable<Fact> {
    private ArrayList<Fact> goals;
    private Heuristic heuristic;

    public GoalWrapper(And goal, Heuristic heuristic) {
        this.goals = new ArrayList<Fact>(goal.getFacts());
        this.heuristic = heuristic;
    }

    @Override
    public Iterator<Fact> iterator() {
        Iterator<Fact> it;

        switch(this.heuristic) {
            case NONE: {
                it = goals.iterator();
                break;
            }
            
            case RANDOM: {
                Collections.shuffle(this.goals);
                it = goals.iterator();
                break;
            }
            
            case GROUPED: {
                it = goals.iterator();
                break;
            }
            default:
                it = goals.iterator();
        }


        return it;
    }
}