package langexp.parser

class Ast
{
    AstNode root = new AstNode()

    void printAsciiTree() {
        println "AST -> "
        root.children.eachWithIndex { node, index ->
            printNode(node, 1, root.isLast(index))
        }
    }

    void printNode(AstNode node, int depth, boolean last) {
        println("${'  ' * depth} ${last ? '└─' : '├─'} $node")
        node.children.eachWithIndex { child, index ->
            printNode(child, depth + 1, node.isLast(index))
        }
    }
}
