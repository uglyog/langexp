package langexp.statemachine

import org.apache.commons.lang3.builder.ToStringBuilder

class State
{
    def state
    boolean finalState = false
    def events = []

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
            append("state", state).
            append("finalState", finalState).
            append("events", events).
            toString()
    }
}
