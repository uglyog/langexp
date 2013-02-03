package langexp.lex

import langexp.statemachine.StateMachineBuilder

class Tokeniser
{
    Reader input
    def errors = []
    int currentLine = 1, currentChar = 1

    enum State { EOF }

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
            event State.EOF, to: Token.Type.EOF
            event '\n', to: Token.Type.NEWLINE
            event '/', to: 'SLASH'
            event ~/[ \t]/, to:  Token.Type.WHITESPACE
            event ~/[A-Za-z_!@#\$]/, to: Token.Type.SYMBOL
            event ~/['"`]/, to: Token.Type.STRING
        }
        state('SLASH') {
            event '/', to: Token.Type.COMMENT
        }
        state(Token.Type.COMMENT) {
            event ~/[^\n]/
            event '\n', action: pushbackAndTerminate
        }
        state(Token.Type.WHITESPACE) {
            event ~/[ \t]/
            event ~/[^ \t]/, action: pushbackAndTerminate
        }
        state(Token.Type.SYMBOL) {
            event ~/[A-Za-z_!@#\$]/
            event ~/[^A-Za-z_!@#\$]/, action: pushbackAndTerminate
        }
        state(Token.Type.STRING) {
            event ~/[^'"`]/
            event ~/['"`]/, action: {
                if (stateMachine.currentEvent == subject.firstMatched) {
                    terminate()
                } else {
                    flagError("Expected a closing qoute (${subject.firstMatched}) to terminate a String, found (${stateMachine.currentEvent})")
                    pushbackAndTerminate()
                }
            }
            event State.EOF, action:  {
                flagError("Expected a closing qoute (${subject.firstMatched}) to terminate a String, found EOF")
                terminate()
            }
        }

        finalState(Token.Type.NEWLINE)
        finalState(Token.Type.EOF)

        onTermination {
            if (stateMachine.currentState instanceof Token.Type) {
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
        event State.EOF, action: { terminate() }
    }

    Token nextToken() {
        stateMachine.subject = new Token()
        stateMachine.start()
        while (!stateMachine.terminated) {
            def ch = input.read()
            if (ch >= 0) {
                stateMachine.transition(Character.toChars(ch) as String)
            } else {
                stateMachine.transition(State.EOF)
            }
        }
        stateMachine.currentState ? stateMachine.subject : null
    }

    void flagError(String error) {
        errors << "[$currentLine, $currentChar]: $error"
    }
}
