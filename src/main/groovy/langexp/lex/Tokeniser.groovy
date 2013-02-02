package langexp.lex

import langexp.statemachine.StateMachine

class Tokeniser
{
    Reader input
    Token current, result

    def stateMachine = StateMachine.build {
        state {
            event '\n', to: Token.Type.NEWLINE
        }
    }

    Token nextToken() {
        result = [matched: new StringBuilder()]
        while (!stateMachine.terminated) {
            def ch = input.read()
            if (ch >= 0) {
                result.matched.append(ch)
                StateMachine.transition(ch, tokenStateMap, result)
            } else {
                StateMachine.transition(null, tokenStateMap, result)
            }
        }
        new Token(type: result.state, matched: ch, firstMatched: ch)
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
