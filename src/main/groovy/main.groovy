import langexp.lex.Token
import langexp.lex.Tokeniser
import langexp.parser.Parser

def cli = new CliBuilder(usage:'langexp [options] [targets]', header:'Options:')
cli.help('print this message')
def options = cli.parse(args)

if (!options?.arguments()) {
    cli.usage()
    System.exit(1)
}

options.arguments().each {
    new File(it).withReader { reader ->
        def tokeniser = new Tokeniser(input: new PushbackReader(reader))
        def parser = new Parser(tokeniser: tokeniser)
        def ast = parser.parse()
        ast.printAsciiTree()
        if (tokeniser.errors) {
            println "\nFound the following lexical errors:"
            tokeniser.errors.each { println it }
        }
        if (parser.errors) {
            println "\nFound the following syntax errors:"
            parser.errors.each { println it }
        }
    }
}
