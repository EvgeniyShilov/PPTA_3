package finitestatemachine.util;

import finitestatemachine.model.FiniteStateMachine;
import finitestatemachine.model.TransitionFunction;
import finitestatemachine.model.minimize.Group;
import finitestatemachine.model.transitionfunction.TransitionFunctionInput;

import java.util.*;

public class FSMMinimizeUtil {

    public static FiniteStateMachine minimize(FiniteStateMachine finiteStateMachine) {
        Set<Character> unreachableStates = getUnreachableStates(finiteStateMachine);
        removeStates(finiteStateMachine, unreachableStates);
        List<Set<Character>> equalStates = getEqualStates(finiteStateMachine);
        Map<Set<Character>, Character> newNotations = NewNotationsProvider.forEqualStates(equalStates, finiteStateMachine.getStates());
        List<TransitionFunction> transitionFunctions = replaceWithNewNotations(finiteStateMachine, newNotations);
        FiniteStateMachine result = new FiniteStateMachine();
        result.setFiniteStates(FSMBuilder.getFiniteStates(transitionFunctions));
        result.setTransitionFunctions(transitionFunctions);
        result.setStates(FSMBuilder.getStates(transitionFunctions));
        result.setInitialStates(finiteStateMachine.getInitialStates());
        result.setInputSymbols(finiteStateMachine.getInputSymbols());
        return result;
    }

    private static Set<Character> getUnreachableStates(FiniteStateMachine finiteStateMachine) {
        Set<Character> reachableStates = new HashSet<Character>() {{
            addAll(finiteStateMachine.getInitialStates());
        }};
        Set<Character> prevReachableStates = new HashSet<Character>() {{
            addAll(finiteStateMachine.getInitialStates());
        }};
        Set<Character> newReachableStatesStep = new HashSet<>();
        while (true) {
            for (Character state : prevReachableStates)
                for (TransitionFunction function : finiteStateMachine.getTransitionFunctions())
                    if (function.getIn().getState().equals(state)) newReachableStatesStep.add(function.getOut());
            prevReachableStates = newReachableStatesStep;
            newReachableStatesStep.removeAll(reachableStates);
            if (newReachableStatesStep.size() == 0) break;
            else {
                reachableStates.addAll(prevReachableStates);
                newReachableStatesStep = new HashSet<>();
            }
        }
        Set<Character> unreachableStates = new HashSet<Character>() {{
            addAll(finiteStateMachine.getStates());
        }};
        unreachableStates.removeAll(reachableStates);
        return unreachableStates;
    }

    private static void removeStates(FiniteStateMachine finiteStateMachine, Set<Character> states) {
        finiteStateMachine.getStates().removeAll(states);
        finiteStateMachine.getFiniteStates().removeAll(states);
        List<TransitionFunction> unneededFunctions = new ArrayList<>();
        for (TransitionFunction function : finiteStateMachine.getTransitionFunctions())
            if (states.contains(function.getIn().getState()) || states.contains(function.getOut()))
                unneededFunctions.add(function);
        finiteStateMachine.getTransitionFunctions().removeAll(unneededFunctions);
    }

    private static List<Set<Character>> getEqualStates(FiniteStateMachine finiteStateMachine) {
        Set<Group> groups = new HashSet<>();
        groups.add(new Group(finiteStateMachine.getFiniteStates()));
        Set<Character> otherStates = finiteStateMachine.getStates();
        otherStates.removeAll(finiteStateMachine.getFiniteStates());
        groups.add(new Group(otherStates));
        Set<Group> groupsStep;
        while (true) {
            Map<TransitionFunctionInput, Group> table = getTransitionTable(finiteStateMachine, groups);
            groupsStep = splitIntoGroups(table);
            if (groups.equals(groupsStep)) break;
            else groups = groupsStep;
        }
        List<Set<Character>> equalStatesList = new ArrayList<>();
        for (Group group : groups)
            equalStatesList.add(group.getStates());
        return equalStatesList;
    }

    private static Set<Group> splitIntoGroups(Map<TransitionFunctionInput, Group> table) {
        Set<Group> groups = new HashSet<>();
        for (Map.Entry<TransitionFunctionInput, Group> outer : table.entrySet()) {
            Set<Character> states = new HashSet<>();
            states.add((Character) outer.getKey().getState());
            for (Map.Entry<TransitionFunctionInput, Group> inner : table.entrySet()) {
                if (outer.equals(inner)) continue;
                if (inner.getKey().getSignal().equals(outer.getKey().getSignal()) &&
                        inner.getValue().getNumber() == (outer.getValue().getNumber()))
                    states.add((Character) inner.getKey().getState());
            }
            if (states.size() != 1) groups.add(new Group(states));
        }
        return groups;
    }

    private static Map<TransitionFunctionInput, Group> getTransitionTable(FiniteStateMachine finiteStateMachine,
                                                                          Set<Group> groups) {
        Map<TransitionFunctionInput, Group> table = new HashMap<>();
        for (TransitionFunction function : finiteStateMachine.getTransitionFunctions()) {
            for (Group group : groups)
                if (group.getStates().contains(function.getOut())) table.put(function.getIn(), group);
        }
        return table;
    }

    private static List<TransitionFunction> replaceWithNewNotations(FiniteStateMachine finiteStateMachine,
                                                                    Map<Set<Character>, Character> newNotations) {
        List<TransitionFunction> transitionFunctions = finiteStateMachine.getTransitionFunctions();
        for (TransitionFunction transitionFunction : transitionFunctions)
            for (Set<Character> equalStates : newNotations.keySet()) {
                if (equalStates.contains(transitionFunction.getIn().getState()))
                    transitionFunction.getIn().setState(newNotations.get(equalStates));
                if (equalStates.contains(transitionFunction.getOut()))
                    transitionFunction.setOut(newNotations.get(equalStates));
            }
        Set<TransitionFunction> uniqueFunctions = new HashSet<>(transitionFunctions);
        return new ArrayList<>(uniqueFunctions);
    }
}
