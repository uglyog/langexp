package langexp.lex

class Tokeniser
{
    Reader input
    Token current, result

    Token nextToken() {
        result = null
        while (!result) {
            def ch = input.read()
            if (ch >= 0) {
                advanceState(Character.toChars(ch) as String)
            } else {
                advanceState(null)
                return result
            }
        }
        result
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
