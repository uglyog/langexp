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
                    case ~/[ \t]/:
                        current = new Token(type: Token.Type.WHITESPACE, matched: ch)
                        break
                    case '\n':
                        result << new Token(type: Token.Type.NEWLINE, matched: ch)
                        break
                    case ~/[A-Za-z_]/:
                        current = new Token(type: Token.Type.SYMBOL, matched: ch)
                        break
                    case ~/[0-9]/:
                        current = new Token(type: Token.Type.NUMBER, matched: ch)
                        break
                    case ~/['"`]/:
                        current = new Token(type: Token.Type.STRING, matched: ch)
                        break
                    default:
                        current = new Token(type: Token.Type.OPERATOR, matched: ch)
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
                }
            }
        }
    }
}
