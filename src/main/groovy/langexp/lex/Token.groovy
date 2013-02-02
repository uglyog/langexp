package langexp.lex

class Token
{
    enum Type {
        UNKNOWN,
        WHITESPACE,
        NEWLINE,
        SYMBOL,
        OPERATOR,
        NUMBER,
        STRING,
        COMMENT
    }

    Type type = Type.UNKNOWN
    String matched, firstMatched

    String toString() {
        "$type($matched)"
    }
}
