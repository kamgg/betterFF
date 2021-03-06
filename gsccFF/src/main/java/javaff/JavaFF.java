/************************************************************************
 * Strathclyde Planning Group,
 * Department of Computer and Information Sciences,
 * University of Strathclyde, Glasgow, UK
 * http://planning.cis.strath.ac.uk/
 * 
 * Copyright 2007, Keith Halsey
 * Copyright 2008, Andrew Coles and Amanda Smith
 * Copyright 2015, David Pattison
 *
 * This file is part of JavaFF.
 * 
 * JavaFF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JavaFF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JavaFF.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ************************************************************************/

package javaff;

import javaff.data.Action;
import javaff.data.Fact;
import javaff.data.GroundFact;
import javaff.data.metric.Metric;
import javaff.data.metric.MetricType;
import javaff.data.DomainRequirements;
import javaff.data.Requirement;
import javaff.data.UngroundProblem;
import javaff.data.GroundProblem;
import javaff.data.Plan;
import javaff.data.TotalOrderPlan;
import javaff.data.TimeStampedPlan;
import javaff.data.metric.NumberFunction;
import javaff.data.strips.And;
import javaff.data.strips.InstantAction;
import javaff.data.strips.Not;
import javaff.data.temporal.DurativeAction;
import javaff.parser.PDDL21parser;
import javaff.parser.ParseException;
import javaff.planning.HelpfulFilter;
import javaff.planning.MetricState;
import javaff.planning.STRIPSState;
import javaff.planning.State;
import javaff.planning.TemporalMetricState;
import javaff.planning.NullFilter;
import javaff.search.BestFirstSearch;
import javaff.search.EnforcedHillClimbingSearch;
import javaff.search.Search;
import javaff.search.UnreachableGoalException;
import javaff.util.Triplet;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import javax.swing.border.StrokeBorder;

import org.graalvm.compiler.nodes.NodeView.Default;

/**
 * An implementation of the FF planner in Java. The planner currently only
 * supports STRIPS/ADL style planning, but as it is a branch of the CRIKEY planner,
 * the components for both Temporal and Metric planning exist, but are unused.
 * 
 * @version 2.2 - This version represents another major overhaul of the JavaFF code from 2.1, which had a massively
 * modified framework structure. Version 2.2 focuses more on the functionality of the system rather than the
 * code-base. This essentially means bringing JavaFF much closer to the original FF functionality, by adding
 * "correct" helpful action usage, reachability analysis, goal-ordering, extended ADL support
 * and speed/memory optimisations. Both temporal and metric planning have been disabled because of 
 * bugs arising from framework modifications, but will be re-introduced at a later date.
 * 
 * @author Keith Halsey, < 2007
 * @author Amanda Coles, 2008
 * @author Andrew Coles, 2008
 * @author David Pattison, 2008-2013
 */
public class JavaFF
{
	protected static double Nanos = 1000000000;
	/**
	 * This flag is used to determine whether the behaviour of JavaFF is
	 * deterministic. This essentially comes down to which action is selected
	 * during search. If false, the underlying order of the applicable actions
	 * is used, if true, actions are sorted based upon a {@link Comparator}
	 * defined elsewhere.
	 * @deprecated Not so much deprecated as not implemented fully yet!
	 */
	@Deprecated
	protected static boolean Deterministic = false;


	/**
	 * This flag determines whether to use goal serialisation on solving the problem.
	 */
	protected static boolean GoalSerialisation = false;

	/**
	 * Returns the hard-coded, static and final PDDL requirements which JavaFF supports.
	 * @see Requirement
	 * @see DomainRequirements
	 */
	public static final DomainRequirements PDDLRequirementsSupported = JavaFF.GetRequirementsSupported();

	public static BigDecimal EPSILON = new BigDecimal(0.01);
	public static BigDecimal MAX_DURATION = new BigDecimal("100000");
	public static Random generator = new Random(1234); //FIXME reintroduce as CLI parameter!

	public static PrintStream planOutput = System.out;
	public static PrintStream parsingOutput = System.out;
	public static PrintStream infoOutput = System.out;
	public static PrintStream errorOutput = System.err;

	protected File domainFile;

	protected File useOutputFile;
	
	protected boolean useEHC, useBFS;
	
	protected JavaFF()
	{
		this.domainFile = null;
		this.useOutputFile = null;
		this.useEHC = true;
		this.useBFS = true;
	}


	/**
	 * Initialise JavaFF with the specified domain file.
	 * @param domain The domain file which will have an associated problem file provided at planning time.
	 */
	public JavaFF(String domain)
	{
		this(domain, null);
	}

	/**
	 * Initialise JavaFF with the specified domain file.
	 * @param domain The domain file which will have an associated problem file provided at planning time.
	 */
	public JavaFF(File domain)
	{
		this(domain, null);
	}

	/**
	 * Initialise JavaFF with the specified domain file.
	 * @param domain The domain file which will have an associated problem file provided at planning time.
	 * @param solutionFile A file to output any solution found to. May be null of no solution is wanted.
	 */
	public JavaFF(String domain, File solutionFile)
	{
		this();
		
		this.domainFile = new File(domain);
		this.useOutputFile = solutionFile;
	}

	/**
	 * Initialise JavaFF with the specified domain file.
	 * @param domain The domain file which will have an associated problem file provided at planning time.
	 * @param solutionFile A file to output any solution found to. May be null of no solution is wanted.
	 */
	public JavaFF(File domain, File solutionFile)
	{
		this();
		
		this.domainFile = domain;
		this.useOutputFile = solutionFile;
	}

	/**
	 * Constructs and returns a DomainRequirements object which contains flags
	 * for the functionality currently available in JavaFF.
	 * @return
	 */
	public static DomainRequirements GetRequirementsSupported()
	{
		DomainRequirements req = new DomainRequirements();
		req.addRequirement(Requirement.Typing);
		req.addRequirement(Requirement.Strips);
		req.addRequirement(Requirement.Equality);
		req.addRequirement(Requirement.ADL);
		req.addRequirement(Requirement.NegativePreconditions);
		req.addRequirement(Requirement.QuantifiedPreconditions);
		req.addRequirement(Requirement.ExistentialPreconditions);
		req.addRequirement(Requirement.UniversalPreconditions);
		req.addRequirement(Requirement.ActionCosts);

		return req;
	}

	protected boolean checkRequirements(DomainRequirements problemRequirments)
	{
		return JavaFF.PDDLRequirementsSupported.subsumes(problemRequirments);
	}

	public static void main(String args[]) {
		EPSILON = EPSILON.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		MAX_DURATION = MAX_DURATION.setScale(2, BigDecimal.ROUND_HALF_EVEN);

		boolean useOutputFile = false;


		// Shoddy arg parser
		ArrayList<String> parameters = new ArrayList<String>();
		
		for (int i = 0; i < args.length; i++) {

			if (args[i].startsWith("-")) {
				switch(args[i]) {
					case "--deterministic": {
						JavaFF.Deterministic = true;
						break;
					}

					case "-D": {
						JavaFF.Deterministic = true;
						break;
					}

					case "--goal-serialisation": {
						JavaFF.GoalSerialisation = true;
						break;
					}

					case "-G": {
						JavaFF.GoalSerialisation = true;
						break;
					}
					default:
					System.out.println("Invalid option: '" + args[i] + "'");

				}
			} else {
				parameters.add(args[i]);
			}

		}

		if (parameters.size() < 2) {
			System.out.println("Parameters needed: [options] domainFile.pddl problemFile.pddl [outputfile.sol]");
		} else {
			File domainFile = new File(parameters.get(0));
			File problemFile = new File(parameters.get(1));
			File solutionFile = null;

			if (parameters.size() > 2) {
				solutionFile = new File(parameters.get(2));
				useOutputFile = true;
			}

			try {
				JavaFF planner = new JavaFF(domainFile, solutionFile);
				Plan p = planner.plan(problemFile);
			} catch (UnreachableGoalException e) {
				System.out.println("Goal " + e.getUnreachables().toString() + " is unreachable");
			} catch (ParseException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	/**
	 * Constructs plans over several problem files.
	 * 
	 * @param path
	 *            The path to the folder containing the problem files.
	 * @param filenamePrefix
	 *            The prefix of each problem file, usually "pfile".
	 * @param pfileStart
	 *            The start index which will be appended to the filenamePrefix.
	 * @param pfileEnd
	 *            The index of the last problem file.
	 * @param usePDDLpostfix
	 *            Whether to use ".pddl" at the end of the problem files.
	 *            Domains are assumed to already have this.
	 * @return A totally ordered plan.
	 */
	public List<Plan> plan(String path, String filenamePrefix, int pfileStart,
			int pfileEnd, boolean usePDDLpostfix)
			throws UnreachableGoalException, ParseException
	{
		List<Plan> plans = new ArrayList<Plan>(pfileEnd - pfileStart);
		for (int i = pfileStart; i < pfileEnd; i++)
		{
			String postfix = "" + i;
			if (i < 10)
				postfix = "0" + i;
			if (usePDDLpostfix)
				postfix = postfix + ".pddl";

			File pfile = new File(path + "/" + filenamePrefix + postfix);
			plans.add(this.plan(pfile));
		}

		return plans;
	}

	/**
	 * Construct a plan from the ground problem provided. This method foregoes
	 * any parsing required by plan(File).
	 * 
	 * @param gproblem
	 *            A grounded problem.
	 * @return A totally ordered plan.
	 */
	public Plan plan(GroundProblem gproblem) throws UnreachableGoalException
	{
		return doPlan(gproblem);
	}

	/**
	 * Construct a plan for the single problem file provided. Obviously this
	 * problem must be intended for the domain associated with this object. @see
	 * JavaFF.getDomainFile(). Note- This method should only be called if there
	 * exists no UngroundProblem or GroundProblem instance in the program.
	 * Otherwise, use plan(GroundProblem, String).
	 * 
	 * @param pFile
	 *            The file to parse.
	 * @return A totally ordered plan.
	 */
	public Plan plan(File pFile) throws UnreachableGoalException,
			ParseException
	{
		Plan plan = this.doFilePlan(pFile);

		if (plan != null)
		{
			if (useOutputFile != null)
			{
				this.writePlanToFile(plan, useOutputFile);
				System.out.println("Plan written to "
						+ useOutputFile.getAbsolutePath());
			}
		}

		return plan;
	}
	
	protected List<GroundFact> getGoalOrderings(GroundFact goal)
	{
		
		//TODO add the goal-ordering extraction as explained on page 21 of the FF paper
		//To do this, need to find out what Jorg means by "the planner looks at all pairs of goals and decides 
		//heuristically whether there is an ordering constraint between them". Quite what "heuristically" means
		//is anyones guess.
		return new ArrayList<GroundFact>(0);
		
	}
			

	protected boolean isGoalValid(GroundProblem problem)
	{
		Set<? extends Fact> facts = problem.getGoal().getFacts();

		for (Fact f : facts)
		{
			if (f instanceof Not)
			{
				// if (problem.reachableFacts.contains(((Not)f).literal) ==
				// false)
				// return false;
				if (problem.getReachableFacts().contains(((Not) f)) == false)
					return false;
			}
			else
			// TODO checks for Quantified facts etc.
			{
				if (problem.getReachableFacts().contains(f) == false)
					return false;
			}
		}

		return true;
	}

	protected Plan doSTRIPSPlan(GroundProblem ground) throws UnreachableGoalException
	{

		if (JavaFF.GoalSerialisation && ground.getGoal() instanceof And) {
			return this.performPlanningGS(ground);
		} else {
			STRIPSState initialState = ground.recomputeSTRIPSInitialState();
			return this.performPlanning(ground, initialState);
		}
	}

	protected TotalOrderPlan performPlanning(GroundProblem ground, State initialState) throws UnreachableGoalException
	{
		long startTime = System.nanoTime();
		long afterBFSPlanning = 0, afterEHCPlanning = 0;

		State originalInitState = (State) initialState.clone(); 
		State goalState = null;

		TotalOrderPlan plan = null;

		System.out.println("Goal is: " + ground.getGoal().toString());

		double planningEHCTime = 0;
		double planningBFSTime = 0;

		if (this.isUseEHC())
		{
			System.out.println("Running FF with EHC...");
			goalState = this.performFFSearch(initialState, true);
			afterEHCPlanning = System.nanoTime();
			planningEHCTime = (afterEHCPlanning - startTime) / JavaFF.Nanos;
		}

		if (goalState != null)
		{
			System.out.println("Found EHC plan: ");
			plan = (TotalOrderPlan) goalState.getSolution();
			plan.setFinalState(goalState);
		}
		else if (this.isUseBFS())
		{
			initialState = (State) originalInitState.clone();
			System.out.println("Running FF with BFS...");
			goalState = this.performFFSearch(initialState, false);
			afterBFSPlanning = System.nanoTime();
			planningBFSTime = (afterBFSPlanning - afterEHCPlanning) / JavaFF.Nanos;
			
			if (goalState != null)
			{
				plan = (TotalOrderPlan) goalState.getSolution();
				System.out.println("Found BFS plan: ");

			}

		}

		TimeStampedPlan tsp = null;
		if (plan != null)
		{
			System.out.println("Final plan...");
			int actionCounter = 0;
			tsp = new TimeStampedPlan(ground.getGoal());
			for (Object a : plan.getActions())
				tsp.addAction((Action) a, new BigDecimal(actionCounter++));


			if (tsp != null)
			{
				tsp.print(planOutput);
				System.out.println("Final plan length is " + tsp.actions.size());
			}

			infoOutput.println("EHC Plan Time = " + planningEHCTime + "sec");
			infoOutput.println("BFS Plan Time = " + planningBFSTime + "sec");
		}
		else
		{
			System.out.println("No plan found");
		}

		plan.setFinalState(goalState);
		return plan;
	}

	protected Triplet<TotalOrderPlan, Double, Double> performSinglePlanGS(GroundProblem ground, State initialState) throws UnreachableGoalException 
	{
		long startTime = System.nanoTime();
		long afterBFSPlanning, afterEHCPlanning = 0;
		double planningEHCTime = 0;
		double planningBFSTime = 0;

		State originalInitState = (State) initialState.clone();
		State goalState = null;
		TotalOrderPlan plan = null;
		
		if (this.isUseEHC())
		{
			System.out.println("Running FF with EHC...");
			goalState = this.performFFSearch(initialState, true);
			afterEHCPlanning = System.nanoTime();
			planningEHCTime = (afterEHCPlanning - startTime) / JavaFF.Nanos;
		}

		if (goalState != null)
		{
			System.out.println("Found EHC plan: ");
			plan = (TotalOrderPlan) goalState.getSolution();
			plan.setFinalState(goalState);
		}
		else if (this.isUseBFS())
		{
			initialState = (State) originalInitState.clone();
			System.out.println("Running FF with BFS...");
			goalState = this.performFFSearch(initialState, false);
			afterBFSPlanning = System.nanoTime();
			planningBFSTime = (afterBFSPlanning - afterEHCPlanning) / JavaFF.Nanos;
			
			if (goalState != null)
			{
				plan = (TotalOrderPlan) goalState.getSolution();
				plan.setFinalState(goalState);
				System.out.println("Found BFS plan: ");
			}
		}

		return new Triplet<TotalOrderPlan, Double, Double>(plan, planningEHCTime, planningBFSTime);
	}
	protected TotalOrderPlan performPlanningGS(GroundProblem ground) throws UnreachableGoalException
	{
		// Time variables
		double totalPlanningEHCTime = 0;
		double totalPlanningBFSTime = 0;

		// Goals
		And goals = (And) ground.getGoal();
		
		And currentGoal = new And();

		System.out.print("Serialising goal(s): ");
		System.out.println(goals);
		
		// Clone problem
		GroundProblem currentProblem = (GroundProblem) ground.clone();

		// List of plans
		ArrayList<TotalOrderPlan> plans = new ArrayList<TotalOrderPlan>();

		// Iterate through every fact in goal
		for (Fact fact : goals.getFacts()) {
			currentGoal.add(fact);
			System.out.println("Running planner on goal(s): " + currentGoal);
			currentProblem.setGoal(currentGoal);
			
			// If there has been a prior plan, update the initial state to be the final state of the previous plan
			if (plans.size() != 0) {
				TotalOrderPlan currentPlan = plans.get(plans.size() - 1);
				STRIPSState finalState = (STRIPSState) currentPlan.getFinalState();
				Set<Fact> newInitialState = finalState.getFacts();
				currentProblem.setInitial(newInitialState);
			}

			STRIPSState initialState = currentProblem.recomputeSTRIPSInitialState();
			Triplet<TotalOrderPlan, Double, Double> planData = this.performSinglePlanGS(currentProblem, initialState);
			totalPlanningEHCTime += planData.y;
			totalPlanningBFSTime += planData.z;
			plans.add(planData.x);
		}
		TotalOrderPlan completePlan = new TotalOrderPlan(plans.get(plans.size() - 1).getGoal());

		for (TotalOrderPlan plan : plans) {
			completePlan.addActions(plan.getActions());
		}

		// Update final state of plan with final state of last plan
		completePlan.setFinalState(plans.get(plans.size() - 1).getFinalState());
		
		// Check whether final state goal is achieved
		STRIPSState finalState = (STRIPSState) completePlan.getFinalState();

		Set<Fact> achievedGoals = finalState.getFacts();

		boolean goalsAchieved = true;

		for (Fact goal : goals.getFacts()) {
			boolean goalAchieved = false;

			for (Fact achieved : achievedGoals) {
				if (achieved.toString().equals(goal.toString())) {
					goalAchieved = true;
					break;
				}
			}

			if (goalAchieved == false) {
				goalsAchieved = false;
				break;
			}
		}

		if (goalsAchieved)
		{
			System.out.println("Final plan...");
			
			TimeStampedPlan tsp = null;
			int actionCounter = 0;
			tsp = new TimeStampedPlan(ground.getGoal());
			for (Object a : completePlan.getActions())
				tsp.addAction((Action) a, new BigDecimal(actionCounter++));

			if (tsp != null)
			{
				tsp.print(planOutput);
				System.out.println("Final plan length is " + tsp.actions.size());
			}

			infoOutput.println("EHC Plan Time = " + totalPlanningEHCTime + "sec");
			infoOutput.println("BFS Plan Time = " + totalPlanningBFSTime + "sec");
		}
		else
		{
			System.out.println("No plan found");
		}

		return completePlan;
	}

	protected Plan doPlan(GroundProblem ground) throws UnreachableGoalException
	{
		if (ground.getRequirements().contains(Requirement.ADL))
		{
			System.out.println("Decompiling ADL...");
			int previousActionCount = ground.getActions().size();
			int naiveAdlActionCount = ground.decompileADL();
			int adlActionCount = ground.getActions().size();
			System.out.println(previousActionCount + " actions before ADL, "
					+ adlActionCount + " after ("+naiveAdlActionCount+" generated in total)");
		}
		 
		//filtering has to be done after decompiling the ADL because the RPG method used only understands
		//STRIPS facts
		System.out.println("Performing RPG reachability analysis...");
		ground.filterReachableFacts();

		//Select the correct problem type to generate -- doing a STRIPS only domain using a Temporal approach will
		//cause massive overheads which may stop the problem being solvable. This is because the type of RPG constructed
		//is irectly linked to the type of the initial state. That is, STRIPS states will produce RPGs and further STRIPSStates. 
		//Metric states produce MetricRPGs and more Metric states, etc.
		if (ground.isMetric() == false && ground.isTemporal() == false)
		{
			return this.doSTRIPSPlan(ground);
		}
		else if (ground.isMetric() == true && ground.isTemporal() == false)
		{
			throw new IllegalArgumentException(
					"Metric planning currently not supported");
			// return this.doMetricPlan(ground);
		}
		else
		// temporal subsumes metric so no need to check for metric == false &&
		// temporal == true
		{
			throw new IllegalArgumentException(
					"Temporal planning currently not supported");
			// return this.doTemporalPlan(ground);
		}
	}

	protected Plan doFilePlan(File pFile) throws UnreachableGoalException,
			ParseException
	{
		// ********************************
		// Parse and Ground the Problem
		// ********************************
		UngroundProblem unground = PDDL21parser.parseFiles(this.domainFile,
				pFile);

		if (unground == null)
		{
			System.err.println("Parsing error - see console for details");
			return null;
		}

		if (this.checkRequirements(unground.requirements) == false)
			throw new ParseException(
					"Domain contains unsupported PDDL requirements. JavaFF currently"
							+ " supports the following\n"
							+ JavaFF.PDDLRequirementsSupported.toString());

		System.out.println("Grounding...");
		GroundProblem ground = unground.ground();
		System.out.println("Grounding complete");

		return this.doPlan(ground);
	}

//	/**
//	 * This is a rough RPG for detecting unreachable facts and unused actions.
//	 * Anything which does not appear before the graph stabilises is eliminated
//	 * from the ground problem and search.
//	 * 
//	 * @param gproblem
//	 */
//	protected void filterReachableFacts(GroundProblem gproblem)
//			throws UnreachableGoalException
//	{
//		int prevActionCount = 0;
//		int currActionCount = 0;
//
//		HashSet<Action> usedActions = new HashSet<Action>();
//		Queue<Action> unusedActions = new LinkedList<Action>(gproblem.actions);
//
//		// //we have to clone because if we don't, the referenced state will be
//		// updated and eventually have the goal in it!
//		// STRIPSState state = (STRIPSState)
//		// gproblem.igetSTRIPSInitialState().clone();
//		int layers = 0;
//
//		// STRIPSState curr = new STRIPSState(gproblem.actions, new
//		// HashSet<Fact>(gproblem.initial), gproblem.goal);
//		STRIPSState curr = gproblem.getSTRIPSInitialState();
//
//		do
//		{
//			STRIPSState next = (STRIPSState) curr.clone();
//			prevActionCount = currActionCount;
//			// System.out.println("Layer "+layers);
//			for (Action a : unusedActions)
//			{
//				// if (a.isApplicable(curr))
//				if (this.isActionReachable(curr, a))
//				{
//					// System.out.println(a);
//
//					++currActionCount;
//					usedActions.add(a);
//
//					for (Fact add : a.getAddPropositions())
//					{
//						next.addFact(add);
//					}
//					if (gproblem.requirements.contains(Requirements.ADL)
//							|| gproblem.requirements
//									.contains(Requirements.NegativePreconditions))
//					{
//						for (Not del : a.getDeletePropositions())
//						{
//							next.addFact(del);
//						}
//					}
//
//				}
//			}
//			if (currActionCount != prevActionCount)
//				++layers;
//
//			unusedActions.removeAll(usedActions);
//			curr = next;
//		}
//		while (currActionCount != prevActionCount);
//
//		System.out.println("Lower-bound plan length is " + layers);
//		if (curr.goalReached() == false)
//		{
//			HashSet<Fact> unreachable = new HashSet<Fact>();
//			for (Fact g : gproblem.goal.getFacts())
//			{
//				if (curr.getFacts().contains(g) == false)
//					unreachable.add(g);
//			}
//
//			throw new UnreachableGoalException(unreachable,
//					"Goal is unreachable");
//		}
//
//		if (gproblem.requirements.contains(Requirements.ADL)
//				|| gproblem.requirements
//						.contains(Requirements.NegativePreconditions))
//		{
//			System.out.println("Found " + curr.getFacts().size()
//					+ " reachable facts (" + curr.getTrueFacts().size()
//					+ " positive facts, " + curr.getFalseFacts().size()
//					+ " negated facts) from "
//					+ gproblem.groundedPropositions.size()
//					+ " original positive facts.");
//		}
//		else
//		{
//			System.out.println("Found " + curr.getTrueFacts().size()
//					+ " reachable facts from "
//					+ gproblem.groundedPropositions.size()
//					+ " original positive facts.");
//		}
//		System.out.println("Found " + currActionCount
//				+ " applicable actions from " + gproblem.actions.size()
//				+ " original actions");
//
//		gproblem.actions = usedActions;
//		gproblem.reachableFacts.clear();
//
//		for (Fact f : curr.getFacts())
//		{
//			if (f instanceof Proposition)
//				gproblem.reachableFacts.add((Proposition) f);
//			if (f instanceof Not)
//				gproblem.reachableFacts.add(f);
//		}
//	}

	/**
	 * This performs the same job as Action.isApplicable() intuitively would --
	 * but now that ADL is supported, the isApplicable() method inside
	 * propositions and in particular, Nots, assumes that the state is valid in
	 * the context of the domain model. As we want to use the RPG, this is of no
	 * use, because relaxed states are not reachable.
	 * 
	 * @param curr
	 * @return
	 */
	protected boolean isActionReachable(STRIPSState curr, Action a)
	{
		for (Fact pc : a.getPreconditions())
		{
			if (pc instanceof Not)
			{
				Not npc = (Not) pc;
				if (npc.getLiteral() instanceof Not == false)
				{
//					if (curr.getFalseFacts().contains(pc) == false)
//						return false;

					if (!curr.isTrue(pc) == false)
						return false;
				}
				else //PC is a Not, and its internal literal is also a Not
				{
					// only tests 2-nested Nots
					if (curr.getTrueFacts().contains(
							((Not) ((Not) pc).getLiteral()).getLiteral()) == false)
						return false;
				}
			}
			else
			{
				if (curr.isTrue(pc) == false)
					return false;
			}

		}

		return true;
	}

	/**
	 * This performs the same job as Action.isApplicable() intuitively would --
	 * but now that ADL is supported, the isApplicable() method inside
	 * propositions and in particular, Nots, assumes that the state is valid in
	 * the context of the domain model. As we want to use the RPG, this is of no
	 * use, because relaxed states are not reachable.
	 * 
	 * @param curr
	 * @return
	 */
	protected boolean isActionReachable(MetricState curr, Action a)
	{
		if (this.isActionReachable((STRIPSState) curr, a) == false)
			return false;
		
		//TODO add check for functions
		return true;
	}

	/**
	 * This performs the same job as Action.isApplicable() intuitively would --
	 * but now that ADL is supported, the isApplicable() method inside
	 * propositions and in particular, Nots, assumes that the state is valid in
	 * the context of the domain model. As we want to use the RPG, this is of no
	 * use, because relaxed states are not reachable.
	 * 
	 * @param curr
	 * @return
	 */
	protected boolean isActionReachable(TemporalMetricState curr, Action a)
	{
		if (this.isActionReachable((MetricState) curr, a) == false)
			return false;
		
		//TODO add check for temporal aspects
		return true;
	}

	/**
	 * Perform search using (normally) EHC with helpful actions.
	 * 
	 * @param initialState
	 * @param useEHC
	 *            Whether to use EHC or best-first-search.
	 * @return
	 */
	protected State performFFSearch(State initialState, boolean useEHC)
	{
		State goalState = null;

		// System.out.println("INIT "+initialState.getTrueFacts() +
		// "\nGOAL "+initialState.goal);
		if (useEHC)
		{
			infoOutput
					.println("Performing search using EHC with standard helpful action filter");

			Search EHCS = new EnforcedHillClimbingSearch(initialState);

			// EHCS.setFilter(NullFilter.getInstance());
			EHCS.setFilter(HelpfulFilter.getInstance()); // and use the helpful
															// actions
															// neighbourhood

			// Try and find a plan using EHC
			goalState = EHCS.search();

			if (goalState != null)
				return goalState;
			else
				infoOutput.println("Failed to find solution using EHC");
		}
		else
		{
			infoOutput.println("Performing search using BFS");
			// create a Best-First Searcher
			BestFirstSearch BFS = new BestFirstSearch(initialState);
			BFS.setFilter(NullFilter.getInstance());
			goalState = BFS.search();

			if (goalState == null)
				infoOutput.println("Failed to find solution using BFS");
		}
		return goalState;
	}

	protected void writePlanToFile(Plan plan, File fileOut)
	{
		try
		{
			// System.out.println("plan is "+plan+", file is "+fileOut);
			fileOut.delete();
			fileOut.createNewFile();

			FileOutputStream outputStream = new FileOutputStream(fileOut);
			PrintWriter printWriter = new PrintWriter(outputStream);
			plan.print(printWriter);
			printWriter.close();
		}
		catch (FileNotFoundException e)
		{
			errorOutput.println(e);
			e.printStackTrace();
		}
		catch (IOException e)
		{
			errorOutput.println(e);
			e.printStackTrace();
		}

	}

	public File getDomainFile()
	{
		return domainFile;
	}

	public void setDomainFile(File domainFile)
	{
		this.domainFile = domainFile;
	}

	public boolean isUseEHC()
	{
		return useEHC;
	}

	public void setUseEHC(boolean useEHC)
	{
		this.useEHC = useEHC;
	}

	public boolean isUseBFS()
	{
		return useBFS;
	}

	public void setUseBFS(boolean useBFS)
	{
		this.useBFS = useBFS;
	}
}
