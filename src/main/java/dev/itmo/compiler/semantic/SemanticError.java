package dev.itmo.compiler.semantic;

class SemanticError extends RuntimeException {
    public SemanticError(String message) {
        super(message);
    }
}