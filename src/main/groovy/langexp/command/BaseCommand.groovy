package langexp.command

import langexp.lex.Tokeniser
import langexp.parser.Ast
import langexp.parser.Parser

class BaseCommand {

  Tokeniser tokeniser
  Parser parser
  Ast ast
  Object options
  String inputFile

  BaseCommand(options) {
    this.options = options
  }

  void parse() {
    new File(inputFile).withReader { reader ->
      tokeniser = new Tokeniser(input: new PushbackReader(reader))
      parser = new Parser(tokeniser: tokeniser)
      ast = parser.parse()
    }
  }

}
