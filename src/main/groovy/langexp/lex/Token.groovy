package langexp.lex

class Token
{
    enum Type {
        WHITESPACE,
        NEWLINE,
        SYMBOL,
        OPERATOR,
        NUMBER,
        STRING,
        COMMENT
    }

    Type type
    String matched, firstMatched

    String toString() {
        "$type($matched)"
    }
}
