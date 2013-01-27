package langexp.lex

class Token
{
    enum Type {
        WHITESPACE,
        NEWLINE,
        SYMBOL,
        OPERATOR,
        NUMBER,
        STRING
    }

    Type type
    String matched

    String toString() {
        "$type($matched)"
    }
}
