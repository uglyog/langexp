package langexp.compiler

class StackVmUtils {

  static int pushData(List data, byte[] d) {
    def index = data.size()
    data.addAll(d)
    index
  }

  static int pushString(List data, String sdata) {
    def index = data.size()
    data.add(StackVmCompiler.DataType.STRING_DATA.ordinal())
    data.addAll(toByteArray(sdata.length()))
    data.addAll(sdata.getBytes())
    index
  }

  static int pushSymbol(List data, String symbol) {
    def index = data.size()
    data.add(StackVmCompiler.DataType.SYMBOL_DATA.ordinal())
    data.add(symbol.length())
    data.addAll(symbol.getBytes())
    index
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
