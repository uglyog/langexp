package langexp.interpreter

import langexp.compiler.StackVmCompiler
import org.apache.commons.lang3.builder.ToStringBuilder

class StackVmInterpreter {
  String inputFile

  enum LoadMode {
    LOAD_TYPES,
    LOAD_DATA,
    LOAD_CODE
  }

  class Instruction {
    StackVmCompiler.InstructionCode code
    List<Integer> parameters = []
    int address

    @Override
    public String toString() {
      new ToStringBuilder(this).
        append("code", code).
        append("parameters", parameters).
        append("address", address).
        toString()
    }
  }

  class Reference {
    StackVmCompiler.DataType dataType
    def value
    int address

    @Override
    public String toString() {
      new ToStringBuilder(this).
        append("dataType", dataType).
        append("value", value).
        append("address", address).
        toString()
    }
  }

  Map<Integer, StackVmCompiler.DataType> typeTable = [:]
  List<Byte> data = []
  List<Instruction> code = []
  List<Byte> stack = []
  Map<Integer, String> symbolTable = [:]
  int codePointer = 0

  void loadFile() {
    LoadMode mode = null
    new File(inputFile).eachLine { line ->
      if (!line.startsWith(';')) {
        if (line == '.TYPES') {
          mode = LoadMode.LOAD_TYPES
        } else if (line == '.DATA') {
          mode = LoadMode.LOAD_DATA
        } else if (line == '.CODE') {
          mode = LoadMode.LOAD_CODE
        } else {
          switch (mode) {
            case LoadMode.LOAD_TYPES:
              def lineData = line.split(/\s+/)
              typeTable[Integer.parseInt(lineData[0], 16)] = StackVmCompiler.DataType.valueOf(lineData[1])
              break
            case LoadMode.LOAD_DATA:
              data.addAll(line.decodeHex() as List)
              break
            case LoadMode.LOAD_CODE:
              def lineData = line.split(/\s+/)
              def instruction = new Instruction(code: StackVmCompiler.InstructionCode.valueOf(lineData[0]),
                address: code.size())
              if (lineData.size() > 1) {
                instruction.parameters = lineData[1..-1].collect { Integer.parseInt(it, 16) }
              }
              code << instruction
              break
          }
        }
      }
    }
  }

  def execute() {
    stack = []
    codePointer = 0

    while (codePointer < code.size()) {
      def instruction = code[codePointer++]
      switch (instruction.code) {
        case StackVmCompiler.InstructionCode.PUSHADDR:
          instruction.parameters.each {
            stack.addAll(StackVmCompiler.toByteArray(it) as List)
            stack.add(StackVmCompiler.DataType.POINTER.ordinal() as byte)
          }
          break
        case StackVmCompiler.InstructionCode.PUSHVAL:
          instruction.parameters.each {
            stack.addAll(StackVmCompiler.toByteArray(it) as List)
            stack.add(StackVmCompiler.DataType.INT32.ordinal() as byte)
          }
          break
        case StackVmCompiler.InstructionCode.CALLFUNC:
          callFunction(instruction.parameters.first())
          break
        default:
          throw new Exception("Invalid instruction ${instruction.code} found at address ${StackVmCompiler.intToHex(codePointer)}")
      }
    }

    if (!stack.empty) {
      popValue()
    }
  }

  void callFunction(int address) {
    def dataType = StackVmCompiler.DataType.values()[data[address]]
    def functionReference
    switch (dataType) {
      case StackVmCompiler.DataType.SYMBOL_DATA:
        functionReference = fetchSymbol(address)
        break
      default:
        throw new Exception("Invalid function reference at address ${StackVmCompiler.intToHex(address)}")
    }

    switch (functionReference) {
      case 'print':
        callPrint()
        break
      default:
        throw new Exception("Invalid function reference $functionReference at address ${StackVmCompiler.intToHex(address)}")
    }
  }

  void callPrint() {
    int numParams = popInteger()
    List<Reference> params = []
    numParams.times {
      params << popValue()
    }
    println params.collect() {
      def value = null
      switch (it.dataType) {
        case StackVmCompiler.DataType.POINTER:
          Reference ref = fetchData(it.value)
          value = ref.value
          break
        case StackVmCompiler.DataType.INT32:
          value = it.value
          break
      }
      value
    }.join(' ')
  }

  Reference fetchData(int address) {
    Reference ref = new Reference(address: address)
    ref.dataType = StackVmCompiler.DataType.values()[data[address]]
    switch (ref.dataType) {
      case StackVmCompiler.DataType.POINTER:
        ref = fetchData(fromBytes(data[address+1], data[address+2], data[address+3], data[address+4]))
        break
      case StackVmCompiler.DataType.INT32:
        ref.value = fromBytes(data[address+1], data[address+2], data[address+3], data[address+4])
        break
      case StackVmCompiler.DataType.STRING_DATA:
        int length = fromBytes(data[address+1], data[address+2], data[address+3], data[address+4])
        byte[] sdata = data[(address+5)..(address+4+length)]
        ref.value = new String(sdata)
        break
      case StackVmCompiler.DataType.SYMBOL_DATA:
        byte length = data[address+1]
        byte[] sdata = data[(address+2)..(address+1+length)]
        ref.value = new String(sdata)
        break
    }
    ref
  }

  Reference popValue() {
    Reference ref = new Reference()
    ref.dataType = StackVmCompiler.DataType.values()[stack.pop()]
    switch (ref.dataType) {
      case StackVmCompiler.DataType.POINTER:
      case StackVmCompiler.DataType.INT32:
        byte b1 = stack.pop()
        byte b2 = stack.pop()
        byte b3 = stack.pop()
        byte b4 = stack.pop()
        ref.value = fromBytes(b4, b3, b2, b1)
        break
    }
    ref
  }

  int popInteger() {
    StackVmCompiler.DataType type = StackVmCompiler.DataType.values()[stack.pop()]
    switch (type) {
      case StackVmCompiler.DataType.INT32:
        byte b1 = stack.pop()
        byte b2 = stack.pop()
        byte b3 = stack.pop()
        byte b4 = stack.pop()
        return fromBytes(b4, b3, b2, b1)
      default:
        throw new Exception("Expected an Integer on the stack, found a $type")
    }
  }

  void dumpData() {
    println("Type Table:")
    println(typeTable)
    println("Symbol Table:")
    println(symbolTable)
    println("Data Table:")
    println(data)
    println("Instructions:")
    println(code)
    println("Stack:")
    println(stack)
    println("Code Pointer = $codePointer")
  }

  String fetchSymbol(int address) {
    if (!symbolTable.containsKey(address)) {
      def length = data[address + 1]
      def symbolData = data[(address + 2)..(address + 1 + length)]
      symbolTable[address] = new String(symbolData as byte[])
    }
    symbolTable[address]
  }

  static int fromBytes(byte b1, byte b2, byte b3, byte b4) {
       b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF)
  }
}
