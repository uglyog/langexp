package langexp.compiler

import langexp.parser.Ast
import langexp.parser.AstNode
import static langexp.parser.AstNode.NodeType.*
import org.apache.commons.io.FilenameUtils

class StackVmCompiler {
  static final int VERSION = 0

  enum DataType {
    STRING_DATA,
    SYMBOL_DATA
  }

  Ast ast
  String file
  String inputFile
  def symbolTable = [:]
  def instructions = []
  def data = []

  void compile() {
    def outputFile = file ?: (FilenameUtils.removeExtension(inputFile) + '.sbvm')
    println("Compiling to $outputFile")
    new File(outputFile).withPrintWriter { pw ->
      pw.println("; Stack based virtual machine")
      pw.println("; VERSION $VERSION")
      compileAst(ast.root)
      pw.println(".TYPES")
      DataType.each {
        pw.println("${it.ordinal()} $it")
      }
      pw.println(".DATA")
      def ldata = data.take(64)
      data = data.drop(64)
      while (!ldata.empty) {
        pw.println((ldata as byte[]).encodeHex().toString())
        ldata = data.take(64)
        data = data.drop(64)
      }
      pw.println(".CODE")
      instructions.each {
        pw.println(it)
      }
    }
  }

  void compileAst(AstNode node) {
    node.children.each() { compileAst(it) }
    switch (node.type) {
      case FUNCTION:
        def address = lookup(node.value.tokenValue())
        instructions << "CALLFUNC ${address}"
        break
      case STRING:
        def address = pushString(node.value.tokenValue())
        instructions << "PUSHADDR ${Integer.toHexString(address).toUpperCase()}"
        break
      case SYMBOL:
        def address = lookup(node.value.tokenValue())
        instructions << "PUSHADDR ${Integer.toHexString(address).toUpperCase()}"
        break
    }
  }

  int lookup(String symbol) {
    if (!symbolTable.containsKey(symbol)) {
      symbolTable[symbol] = pushSymbol(symbol)
    }
    symbolTable[symbol]
  }

  int pushData(byte[] d) {
    def index = data.size()
    data.addAll(d)
    index
  }

  int pushString(String sdata) {
    def index = data.size()
    data.add(DataType.STRING_DATA.ordinal())
    data.addAll(toByteArray(sdata.length()))
    data.addAll(sdata.getBytes())
    index
  }

  int pushSymbol(String symbol) {
    def index = data.size()
    data.add(DataType.SYMBOL_DATA.ordinal())
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
