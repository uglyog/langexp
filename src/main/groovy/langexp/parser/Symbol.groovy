package langexp.parser

class Symbol {

  enum Type {
    SYMBOL,
    FUNCTION
  }

  Type type

  @Override
  public String toString() {
    type.toString()
  }
}
