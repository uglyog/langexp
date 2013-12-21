package langexp.interpreter

import langexp.parser.Symbol
import org.apache.commons.lang3.builder.ToStringBuilder

class Variable {
  Symbol type
  def value

  @Override
  public String toString() {
    "type=$type, value=$value"
  }
}
