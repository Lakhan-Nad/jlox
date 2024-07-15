package com.craftinginterpreters.jlox.syntax;

import com.craftinginterpreters.jlox.scanner.Token;

abstract class Expression {

	static class Binary extends Expression {
		Binary(Expression left, Token op, Expression right) {
			this.left = left;
			this.op = op;
			this.right = right;
		}
		final Expression left;
		final Token op;
		final Expression right;
	}

	static class Unary extends Expression {
		Unary(Token op, Expression expr) {
			this.op = op;
			this.expr = expr;
		}
		final Token op;
		final Expression expr;
	}

	static class Grouping extends Expression {
		Grouping(Expression expr) {
			this.expr = expr;
		}
		final Expression expr;
	}

	static class Literal extends Expression {
		Literal(Object value) {
			this.value = value;
		}
		final Object value;
	}
}
