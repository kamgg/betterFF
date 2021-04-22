package javaff.serialisation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.PriorityQueue;

import javaff.data.Fact;
import javaff.data.strips.And;
import javaff.planning.RelaxedPlanningGraph;
import javaff.planning.STRIPSState;
import javaff.data.GroundProblem;
import javaff.data.RelaxedPlan;
import javaff.planning.RelaxedPlanningGraph;

import javaff.serialisation.Heuristic;
import javaff.serialisation.RPGPairComparatorDescending;
import javaff.serialisation.RPGPairComparatorAscending;
import javaff.util.Pair;

/**
 * GoalWrapper used to determine the order in which to iterate a set of goals.
 * @Author Kamal Asmatpoor
 * @version 1.0
 */
public class GoalWrapper implements Iterable<Fact> {
    private And goal;
    private ArrayList<Fact> goals;
    private Heuristic heuristic;
    private GroundProblem problem; 
    
    /**
     * GoalWrapper constructor.
     * @param goal goals to wrap.
     * @param heuristic heuristic to be used for determining goal order.
     * @param problem a reference to the current problem.
     */
    public GoalWrapper(And goal, Heuristic heuristic, GroundProblem problem) {
        this.goal = goal;
        this.goals = new ArrayList<Fact>(goal.getFacts());
        this.heuristic = heuristic;
        this.problem = problem;
    }

    /**
     * Return the relevant iterator according to heuristic specicfied.
     */
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

            // Calculates RPG heuristic once at the start
            case RPGDESCENDING: {
                System.out.println("Ordering goals...");

                // Priority queue to store RPG heuristic for each goal
                PriorityQueue<Pair<Integer, Fact>> queue = new PriorityQueue<Pair<Integer, Fact>>(new RPGPairComparatorDescending());
                
                // RPG used to calculate heuristic
                RelaxedPlanningGraph rpg = new RelaxedPlanningGraph(problem, goal);

                // Get current state & goal in problem
                STRIPSState currentState = (STRIPSState) problem.getState();
                And originalStateGoal = (And) currentState.goal;

                for (Fact fact : goals) {
                    originalStateGoal.add(fact);
                    currentState.goal = originalStateGoal;

                    RelaxedPlan relaxedPlan = rpg.getPlan(currentState);
                    int HValue = relaxedPlan.getPlanLength();
                    System.out.println(fact + ": " +  HValue + " Added to queue.");

                    Pair<Integer, Fact> pair = new Pair(HValue, fact);
                    queue.add(pair);

                    originalStateGoal.remove(fact);
                    currentState.goal = originalStateGoal;
                }

                System.out.println("Done!");

                it = new Iterator<Fact>() {
                    @Override
                    public boolean hasNext() {
                        return !queue.isEmpty();
                    }

                    @Override
                    public Fact next() {
                        Fact fact = queue.remove().y;
                        return fact;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
    
                break;
            }

            // Calculates RPG heuristic once at the start
            case RPGASCENDING: {
                System.out.println("Ordering goals...");
                // Priority queue to store RPG heuristic for each goal
                PriorityQueue<Pair<Integer, Fact>> queue = new PriorityQueue<Pair<Integer, Fact>>(new RPGPairComparatorAscending());
                
                // RPG used to calculate heuristic
                RelaxedPlanningGraph rpg = new RelaxedPlanningGraph(problem, goal);

                // Get current state & goal in problem
                STRIPSState currentState = (STRIPSState) problem.getState();
                And originalStateGoal = (And) currentState.goal;

                for (Fact fact : goals) {
                    originalStateGoal.add(fact);
                    currentState.goal = originalStateGoal;

                    RelaxedPlan relaxedPlan = rpg.getPlan(currentState);
                    int HValue = relaxedPlan.getPlanLength();
                    System.out.println(fact + ": " +  HValue + " Added to queue.");
                    
                    Pair<Integer, Fact> pair = new Pair(HValue, fact);
                    queue.add(pair);
                    
                    originalStateGoal.remove(fact);
                    currentState.goal = originalStateGoal;
                }

                System.out.println("Done!");

                it = new Iterator<Fact>() {
                    @Override
                    public boolean hasNext() {
                        return !queue.isEmpty();
                    }

                    @Override
                    public Fact next() {
                        Fact fact = queue.remove().y;
                        return fact;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
    
                break;
            }

            // Calculates RPG heuristic every iteration
            case INRPGDESCENDING: {
                it = new Iterator<Fact>() {
                    // Open & closed lists to store which goals have already been seen
                    ArrayList<Fact> open = (ArrayList<Fact>) goals.clone();
                    ArrayList<Fact> closed = new ArrayList<Fact>();
                    
                    // RPG used to calculate heuristic
                    RelaxedPlanningGraph rpg = new RelaxedPlanningGraph(problem, goal);

                    @Override
                    public boolean hasNext() {
                        return open.size() > 0;
                    }

                    @Override
                    public Fact next() {
                        // Get current state & goal in problem
                        STRIPSState currentState = (STRIPSState) problem.getState();
                        And originalStateGoal = (And) currentState.goal;

                        // For remaining goals in open list, select goal with smallest RPG heuristic as next node to serialise.
                        int minHValue = Integer.MAX_VALUE;
                        int index = -1;

                        for (int i = 0; i < open.size(); i++) {
                            Fact fact = open.get(i);

                            // Might not be necessary to set currentState.goal again
                            originalStateGoal.add(fact);
                            currentState.goal = originalStateGoal;

                            int HValue = rpg.getPlan(currentState).getPlanLength();

                            if (HValue <= minHValue) {
                                minHValue = HValue;
                                index = i;
                            }

                            originalStateGoal.remove(fact);
                            currentState.goal = originalStateGoal;
                        }
                        
                        Fact fact = open.remove(index);
                        closed.add(fact);
                        return fact;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };

                break;
            }

            case INRPGASCENDING: {
                it = new Iterator<Fact>() {
                    // Open & closed lists to store which goals have already been seen
                    ArrayList<Fact> open = (ArrayList<Fact>) goals.clone();
                    ArrayList<Fact> closed = new ArrayList<Fact>();
                    
                    // RPG used to calculate heuristic
                    RelaxedPlanningGraph rpg = new RelaxedPlanningGraph(problem, goal);

                    @Override
                    public boolean hasNext() {
                        return open.size() > 0;
                    }

                    @Override
                    public Fact next() {
                        // Get current state & goal in problem
                        STRIPSState currentState = (STRIPSState) problem.getState();
                        And originalStateGoal = (And) currentState.goal;

                        // For remaining goals in open list, select goal with largest RPG heuristic as next node to serialise.
                        int maxHValue = Integer.MIN_VALUE;
                        int index = -1;

                        for (int i = 0; i < open.size(); i++) {
                            Fact fact = open.get(i);

                            // Might not be necessary to set currentState.goal again
                            originalStateGoal.add(fact);
                            currentState.goal = originalStateGoal;

                            int HValue = rpg.getPlan(currentState).getPlanLength();

                            if (HValue >= maxHValue) {
                                maxHValue = HValue;
                                index = i;
                            }

                            originalStateGoal.remove(fact);
                            currentState.goal = originalStateGoal;
                        }
                        
                        Fact fact = open.remove(index);
                        closed.add(fact);
                        return fact;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };

                break;
            }

            default:
                it = goals.iterator();
        }

        return it;
    }
}