package finitestatemachine.model.minimize;

import java.util.Set;

public class Group {

    private static int counter = 0;

    private int number;
    private Set<Character> states;

    public Group(Set<Character> states) {
        number = counter++;
        this.states = states;
    }

    public int getNumber() {
        return number;
    }

    public Set<Character> getStates() {
        return states;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Group group = (Group) other;
        return states.equals(group.states);
    }

    @Override
    public int hashCode() {
        return states.hashCode();
    }
}
