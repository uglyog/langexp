package langexp.lex

import langexp.statemachine.StateMachineBuilder
import static langexp.lex.Token.Type.*

class Tokeniser
{
    Reader input
    def errors = []
    int currentLine = 1, currentChar = 1

    def pushback = {
        input.unread(stateMachine.currentEvent.toCharArray())
        stateMachine.clearEvent()
    }

    def pushbackAndTerminate = {
        pushback()
        stateMachine.terminate()
    }

    def stateMachine = StateMachineBuilder.build {
        start {
            event '\n', to: NEWLINE
            event '/', to: 'SLASH'
            event ~/[ \t]/, to:  WHITESPACE
            event ~/[A-Za-z_!@#\$]/, to: SYMBOL
            event ~/['"`]/, to: STRING
        }
        state('SLASH') {
            event '/', to: COMMENT
        }
        state(COMMENT) {
            event ~/[^\n]/
            event '\n', action: pushbackAndTerminate
        }
        state(WHITESPACE) {
            event ~/[ \t]/
            event ~/[^ \t]/, action: pushbackAndTerminate
        }
        state(SYMBOL) {
            onEntry { subject.type = SYMBOL }
            event ~/[A-Za-z_!@#\$]/
            event ~/[^A-Za-z_!@#\$]/, action: pushbackAndTerminate
        }
        state(STRING) {
            event ~/[^'"`]/
            event ~/['"`]/, action: {
                if (stateMachine.currentEvent == subject.firstMatched) {
                    terminate()
                } else {
                    flagError("Expected a closing qoute (${subject.firstMatched}) to terminate a String, found (${stateMachine.currentEvent})")
                    pushbackAndTerminate()
                }
            }
            event EOF, action:  {
                flagError("Expected a closing qoute (${subject.firstMatched}) to terminate a String, found EOF")
                terminate()
            }
        }

        finalState(NEWLINE)
        finalState(EOF)

        onTermination {
            if (stateMachine.currentState instanceof Token.Type && subject.type == UNKNOWN) {
                subject.type = stateMachine.currentState
            }
        }

        action { event ->
            if (event instanceof String) {
                subject.appendToMatched(event)
                if (event == '\n') {
                    currentLine++
                    currentChar = 1
                } else {
                    currentChar++
                }
            }
        }
        event EOF, to: EOF
    }

    Token nextToken() {
        stateMachine.subject = new Token()
        stateMachine.start()
        while (!stateMachine.terminated) {
            def ch = input.read()
            if (ch >= 0) {
                stateMachine.transition(Character.toChars(ch) as String)
            } else {
                stateMachine.transition(EOF)
            }
        }
        stateMachine.currentState ? stateMachine.subject : null
    }

    void flagError(String error) {
        errors << errorMessage(error)
    }

    String errorMessage(String error) {
        "[$currentLine, $currentChar]: $error"
    }
}
