package langexp.statemachine

import org.apache.commons.lang3.StringUtils

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

    void start(Closure closure = null) {
        addState(null, false, closure)
    }

    void state(def stateKey, Closure closure = null) {
        addState(stateKey, false, closure)
    }

    void finalState(def stateKey, Closure closure = null) {
        addState(stateKey, true, closure)
    }

    private void addState(def stateKey, boolean finalState, Closure closure) {
        if (!stateMachine.stateMachineMap.containsKey(stateKey)) {
            stateMachine.stateMachineMap[stateKey] = new State(state: stateKey, finalState: finalState)
        }
        if (closure) {
            closure.delegate = this
            currentState = stateMachine.stateMachineMap[stateKey]
            closure.call()
            currentState = null
        }
    }

    void event(Map options = [:], Object... args) {
        if (currentState?.finalState) {
            throw new Exception("You are trying to add an event to final state $currentState")
        }

        def eventName = args[0]
        Closure closure = null
        if (args[-1] instanceof Closure) {
            closure = args[-1]
        }
        def evt = [event: eventName, to: options.to ?: currentState?.state]
        if (options.action) {
            evt.action = options.action
        }
        if (options.guard) {
            evt.guard = options.guard
        }

        if (closure) {
            closure.delegate = this
            currentEvent = evt
            closure.call()
            currentEvent = null
        }

        if (currentState) {
            currentState.events << evt
        } else {
            stateMachine.globalEvents << evt
        }
    }

    void transitionTo(def stateKey) {
        currentEvent['to'] = stateKey
    }

    def to = this.&transitionTo

    void action(Closure actionClosure) {
        actionClosure.delegate = stateMachine
        if (currentEvent) {
            currentEvent['action'] = actionClosure
        } else {
            stateMachine.globalActions << actionClosure
        }
    }

    void guard(Closure actionClosure) {
        currentEvent['guard'] = actionClosure
    }

    void onEntry(Closure entryAction) {
        List newStateEntry = currentState.events
        if (newStateEntry && newStateEntry.first() instanceof Closure) {
            newStateEntry[0] = entryAction
        } else {
            newStateEntry.add(0, entryAction)
        }
    }

    void onTermination(Closure terminationAction) {
        stateMachine.terminationAction = terminationAction
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
