import langexp.interpreter.AstInterpreter
import langexp.lex.Token
import langexp.lex.Tokeniser
import langexp.parser.Parser
import org.apache.commons.lang3.time.StopWatch

def cli = new CliBuilder(usage:'langexp [options] [targets]', header:'Options:')
cli.help('print this message')
def options = cli.parse(args)

if (!options?.arguments()) {
    cli.usage()
    System.exit(1)
}

options.arguments().each {
    new File(it).withReader { reader ->
        def stopwatch = new StopWatch()
        stopwatch.start()
        def tokeniser = new Tokeniser(input: new PushbackReader(reader))
        def parser = new Parser(tokeniser: tokeniser)
        def ast = parser.parse()
        stopwatch.split()
        println "Parsing Took: ${stopwatch.toSplitString()}"
        ast.printAsciiTree()
        if (tokeniser.errors) {
            println "\nFound the following lexical errors:"
            tokeniser.errors.each { println it }
        }
        if (parser.errors) {
            println "\nFound the following syntax errors:"
            parser.errors.each { println it }
        }

        if (!tokeniser.errors && !parser.errors) {
            println "Executing AST ..."
            AstInterpreter interpreter = new AstInterpreter(ast: ast)
            def result = interpreter.execute()
            stopwatch.split()
            println "Execution Took: ${stopwatch.toSplitString()}"
            println "Result = $result"
            println "Symbol Table:"
            interpreter.printSymbolTable()
        }
        stopwatch.stop()
    }
}
