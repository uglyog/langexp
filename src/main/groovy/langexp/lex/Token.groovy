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
    StringBuilder matched = new StringBuilder()

    String toString() {
        "$type($matched)"
    }
}
