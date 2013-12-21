package langexp.command

import langexp.compiler.StackVmCompiler
import langexp.interpreter.StackVmInterpreter

class StackVmCommand extends BaseCommand {

  def type = 'SBVM'
  StackVmInterpreter interpreter
  StackVmCompiler compiler

  StackVmCommand(Object options) {
    super(options)
  }

  void compile() {
    println "Compiling AST to SBVM ..."
    compiler = new StackVmCompiler(ast: ast, inputFile: inputFile)
    if (options.o) {
      compiler.file = options.o
    }
    compiler.compile()
  }

  Object execute() {
    println "Executing SBVM ..."
    interpreter = new StackVmInterpreter(inputFile: compiler.file)
    interpreter.loadFile()
    interpreter.execute()
  }

  void dumpData() {
    interpreter.dumpData()
  }

}
