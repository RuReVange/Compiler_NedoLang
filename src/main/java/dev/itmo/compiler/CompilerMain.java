package dev.itmo.compiler;

import dev.itmo.compiler.interpreter.Interpreter;
import dev.itmo.compiler.lexer.Lexer;
import dev.itmo.compiler.lexer.Token;
import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.Parser;
import dev.itmo.compiler.semantic.SemanticAnalyzer;
import dev.itmo.compiler.vm.VirtualMachine;
import dev.itmo.compiler.vm.bytecode.Bytecode;
import dev.itmo.compiler.vm.bytecode.Compiler;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CompilerMain {
    public static void main(String[] args) {
        try {
            String code = new String(Files.readAllBytes(Paths.get("src/main/resources/sort.nedolang")));
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.tokenize();
            Parser parser = new Parser(tokens);
            List<ASTNode> nodes = parser.parse();
//            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
//            semanticAnalyzer.analyze(nodes);

            Interpreter interpreter = new Interpreter();
            interpreter.interpret(nodes);

            Compiler compiler = new Compiler();
            Bytecode bytecode = compiler.compile(nodes);
//            bytecode.serialize("factorialByteCode.nedolang");

//            Bytecode bytecode = Bytecode.deserialize("/Users/ioannboltonov/IdeaProjects/Compiler/src/main/resources/factorialByteCode.nedolang");
            VirtualMachine vm = new VirtualMachine(bytecode);
            vm.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
