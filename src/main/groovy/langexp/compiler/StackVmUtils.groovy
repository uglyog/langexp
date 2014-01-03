package langexp.compiler

import langexp.parser.AstNode

class StackVmUtils {

  static int pushData(List data, byte[] d) {
    def index = data.size()
    data.addAll(d)
    index
  }

  static SymbolEntry pushString(List data, String sdata) {
    def index = data.size()
    data.add(StackVmCompiler.DataType.STRING_DATA.ordinal())
    data.addAll(toByteArray(sdata.length()))
    data.addAll(sdata.getBytes())
    new SymbolEntry(address: index, type: AstNode.NodeType.STRING)
  }

  static SymbolEntry pushSymbol(List data, String symbol) {
    def index = data.size()
    data.add(StackVmCompiler.DataType.SYMBOL_DATA.ordinal())
    data.add(symbol.length())
    data.addAll(symbol.getBytes())
    new SymbolEntry(address: index, type: AstNode.NodeType.SYMBOL)
  }

  static byte[] toByteArray(int value) {
    [
      (byte) (value >> 24),
      (byte) (value >> 16),
      (byte) (value >> 8),
      (byte) value
    ]
  }
}
