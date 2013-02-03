package langexp.statemachine

import java.util.regex.Pattern

class StateMachine {

    def stateMachineMap = [:], currentState, currentEvent, subject, globalActions = [], globalEvents = []
    Closure terminationAction
    boolean terminated

    def findEvent = { it instanceof Map && matches(it.event, currentEvent) && (!it.guard || it.guard(this)) }

    void transition(event) {

        if (terminated) {
            println('State machine has terminated')
            return
        }

        currentEvent = event

        def transition = stateMachineMap[currentState]?.events?.find(findEvent)
        if (!transition) {
            transition = globalEvents.find(findEvent)
        }
        if (transition) {
            if (transition.action) {
                transition.action.delegate = this
                transition.action()
            }
            globalActions.each { action -> action(currentEvent) }
            currentState = transition.to
            def newStateEntry = stateMachineMap[currentState]
            if (newStateEntry) {
                if (newStateEntry.events && newStateEntry.events.first() instanceof Closure) {
                    def action = newStateEntry.events.first()
                    action.delegate = this
                    action()
                }
                if (newStateEntry.finalState) {
                    terminate()
                }
            }
        } else {
            println("For state='$currentState' the event='$event' does not apply, terminating the state machine")
            terminate()
        }
    }

    void start(state = null) {
        currentState = state
        terminated = false
    }

    void terminate() {
        if (terminationAction) {
            terminationAction.delegate = this
            terminationAction()
        }
        terminated = true
    }

    static boolean matches(event1, event2) {
        if (event1 instanceof Pattern && event2 instanceof String) {
            event1.matcher(event2).matches()
        } else {
            event1 == event2
        }
    }

    void clearEvent() {
        currentEvent = null
    }
}
