package langexp.statemachine

import groovy.util.logging.Log4j
import org.apache.commons.lang3.StringUtils

@Log4j
class StateMachineBuilder
{
    private StateMachine stateMachine = new StateMachine()
    def currentState, currentEvent

    static StateMachine build(Closure closure) {
        def smb = new StateMachineBuilder()

        closure.delegate = smb
        closure.call()

        smb.stateMachine
    }

    void state(def stateKey = null, Closure closure = null) {
        if (!stateMachine.stateMachineMap.containsKey(stateKey)) {
            stateMachine.stateMachineMap[stateKey] = []
        }
        currentState = stateKey
        if (closure) {
            closure.delegate = this
            closure.call()
        }
    }

    void event(Map options = [:], Object... args) {
        def eventName = args[0]
        Closure closure = null
        if (args[-1] instanceof Closure) {
            closure = args[-1]
        }
        currentEvent = [event: eventName, to: options.to ?: currentState]
        if (options.action) {
            currentEvent.action = options.action
        }
        if (options.guard) {
            currentEvent.guard = options.guard
        }

        if (closure) {
            closure.delegate = this
            closure.call()
        }

        stateMachine.stateMachineMap[currentState] << currentEvent
    }

    void transitionTo(def stateKey) {
        currentEvent['to'] = stateKey
    }

    def to = this.&transitionTo

    void action(Closure actionClosure) {
        currentEvent['action'] = actionClosure
    }

    void guard(Closure actionClosure) {
        currentEvent['guard'] = actionClosure
    }

    void onEntry(Closure entryAction) {
        List newStateEntry = stateMachine.stateMachineMap[currentState]
        if (newStateEntry && newStateEntry.first() instanceof Closure) {
            newStateEntry[0] = entryAction
        } else {
            newStateEntry.add(0, entryAction)
        }
    }

    def methodMissing(String name, args) {
        if (name.startsWith('on')) {
            switch (args.length) {
                case 1:
                    if (args[0] instanceof Map) {
                        event(StringUtils.uncapitalize(name[2..-1]), *:args[0])
                    } else {
                        event(StringUtils.uncapitalize(name[2..-1]), args[0])
                    }
                    break
                case 2:
                    event(StringUtils.uncapitalize(name[2..-1]), *:args[0], args[1])
                    break
                default:
                    event(StringUtils.uncapitalize(name[2..-1]))
            }
        } else {
            throw new MissingMethodException(name, this.class, args)
        }
    }
}
