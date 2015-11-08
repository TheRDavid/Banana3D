package general;

import b3dElements.B3D_Element;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author David
 */
public class UAManager implements Serializable
{

    private ArrayList<ElementState> states = new ArrayList<ElementState>();

    public enum State
    {

        DEFAULT, UNDONE
    }

    public void addState(B3D_Element before, B3D_Element after)
    {
        //Remove all undone states
        int index = getLastUndoneIndex();
        for (int i = states.size() - 1; i > index + 1; i--)
            states.remove(i);
        states.add(new ElementState(before, after));
    }

    private int getLastUndoneIndex()
    {
        int index = states.size() - 1;
        boolean done = false;
        while (!done)
            if (states.get(index--).state.equals(State.UNDONE))
                done = true;
        return index - 1;
    }

    public void undo()
    {
        int index = getLastUndoneIndex();
        states.get(index).state = State.UNDONE;
    }

    public class ElementState<T extends B3D_Element> implements Serializable
    {

        private T before, after;
        private State state = State.DEFAULT;

        public ElementState(T before, T after)
        {
            this.before = before;
            this.after = after;
        }

        public T getBefore()
        {
            return before;
        }

        public T getAfter()
        {
            return after;
        }
    }
}
