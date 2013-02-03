package langexp.lex

import langexp.statemachine.StateMachineBuilder

class Tokeniser
{
    Reader input

    def pushback = {
        input.unread(stateMachine.currentEvent.toCharArray())
        stateMachine.clearEvent()
    }

    def pushbackAndTerminate = {
        pushback()
        terminate()
    }

    def stateMachine = StateMachineBuilder.build {
        start {
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
            event ~/['"`]/, action: { terminate() }
        }

        finalState(Token.Type.NEWLINE)

        onTermination {
            if (stateMachine.currentState instanceof Token.Type) {
                subject.type = stateMachine.currentState
            }
        }
        action { event -> if (event) { subject.matched.append(event) } }
    }

    Token nextToken() {
        stateMachine.subject = new Token()
        stateMachine.start()
        while (!stateMachine.terminated) {
            def ch = input.read()
            if (ch >= 0) {
                stateMachine.transition(Character.toChars(ch) as String)
            } else {
                stateMachine.terminate()
            }
        }
        stateMachine.currentState ? stateMachine.subject : null
    }

    void advanceState(String ch) {
        if (ch == null) {
            result = current
            current = null
        } else {
            if (current == null) {
                switch (ch) {
                    case '\n':
                        result = new Token(type: Token.Type.NEWLINE, matched: ch, firstMatched: ch)
                        break
                    case ';':
                        current = new Token(type: Token.Type.COMMENT, matched: ch, firstMatched: ch)
                        break
                    case ~/[ \t]/:
                        current = new Token(type: Token.Type.WHITESPACE, matched: ch, firstMatched: ch)
                        break
                    case ~/[A-Za-z_]/:
                        current = new Token(type: Token.Type.SYMBOL, matched: ch, firstMatched: ch)
                        break
                    case ~/[0-9]/:
                        current = new Token(type: Token.Type.NUMBER, matched: ch, firstMatched: ch)
                        break
                    case ~/['"`]/:
                        current = new Token(type: Token.Type.STRING, matched: ch, firstMatched: ch)
                        break
                    default:
                        current = new Token(type: Token.Type.OPERATOR, matched: ch, firstMatched: ch)
                }
            } else {
                switch (current.type) {
                    case Token.Type.WHITESPACE:
                        switch (ch) {
                            case ~/[ \t]/:
                                current.matched += ch
                                break
                            default:
                                result = current
                                current = null
                                advanceState(ch)
                        }
                        break
                    case Token.Type.SYMBOL:
                        switch (ch) {
                            case ~/[a-zA-Z0-9_]/:
                                current.matched += ch
                                break
                            default:
                                result = current
                                current = null
                                advanceState(ch)
                        }
                        break
                    case Token.Type.COMMENT:
                        if (ch == '\n') {
                            result = current
                            current = null
                            input.unread(ch.toCharArray())
                        } else {
                            current.matched += ch
                        }
                        break
                    case Token.Type.STRING:
                        if (ch == current.firstMatched) {
                            current.matched += ch
                            result = current
                            current = null
                        } else {
                            current.matched += ch
                        }
                        break
                }
            }
        }
    }
}
