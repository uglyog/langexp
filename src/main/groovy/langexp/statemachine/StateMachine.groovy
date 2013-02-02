package langexp.statemachine

import groovy.util.logging.Log4j

@Log4j
class StateMachine {

    def stateMachineMap = [:], currentState, subject, globalActions = []
    Closure terminationAction
    boolean terminated

    void transition(event) {

        if (terminated) {
            log.error('State machine has terminated')
            return
        }

        def transition = stateMachineMap[currentState].events.find { it instanceof Map && it.event == event && (!it.guard || it.guard(this)) }
        if (transition) {
            if (transition.action) {
                transition.action()
            }
            globalActions.each { action -> action(event) }
            currentState = transition.to
            def newStateEntry = stateMachineMap[currentState]
            if (newStateEntry) {
                if (newStateEntry.events && newStateEntry.events.first() instanceof Closure) {
                    newStateEntry.events.first().call(this)
                }
                if (newStateEntry.finalState) {
                    terminate()
                }
            }
        } else {
            log.warn("For state='$currentState' the event='$event' does not apply")
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
}
