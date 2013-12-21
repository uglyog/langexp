package langexp.command

import langexp.interpreter.AstInterpreter

class AstCommand extends BaseCommand {

  def type = 'AST'
  AstInterpreter interpreter

  void compile() {
  }

  Object execute() {
    println "Executing AST ..."
    println "-" * 80
    interpreter = new AstInterpreter(ast: ast, parser: parser)
    def result = interpreter.execute()
    println "-" * 80
    result
  }

  void dumpData() {
    interpreter.dumpData()
  }

}
