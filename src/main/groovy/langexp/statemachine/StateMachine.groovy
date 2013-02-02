package langexp.statemachine

import groovy.util.logging.Log4j
import org.apache.commons.lang3.StringUtils

@Log4j
class StateMachine {

    def stateMachineMap = [:], currentState

    void transition(event) {
        def transition = stateMachineMap[currentState].find { it instanceof Map && it.event == event && (!it.guard || it.guard(this)) }
        if (transition) {
            if (transition.action) {
                transition.action(this)
            }
            currentState = transition.to
            def newStateEntry = stateMachineMap[currentState]
            if (newStateEntry && newStateEntry.first() instanceof Closure) {
                newStateEntry.first().call(this)
            }
        } else {
            log.warn("For state='$currentState' the event='$event' does not apply")
        }
    }
}
