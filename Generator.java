import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Generator {
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir,
                "package com.craftinginterpreters.jlox.syntax",
                "Expression", Arrays.asList(
                        "Assign: Token name, Expression value",
                        "Binary : Expression left, Token op, Expression right",
                        "Unary : Token op, Expression expr",
                        "Grouping : Expression expr",
                        "Literal : Object value",
                        "CommaSeperated: List<Expression> expressions",
                        "Variable: Token name",
                        "Logical: Expression left, Token op, Expression right",
                        "Call: Expression callee, Token paren, List<Expression> arguments",
                        "FunctionExpr: Token name, List<Token> params, List<Statement> stmts"));
        defineAst(outputDir, 
        "package com.craftinginterpreters.jlox.syntax", 
        "Statement", Arrays.asList(
                "Block: List<Statement> stmts",
                "Expr: Expression expr",
                "Print: Expression expr",
                "Var: Token name, Expression initializer",
                "IfElse: Expression condition, Statement thenBranch, Statement elseBranch",
                "While: Expression codition, Statement body",
                "For: Statement initializer, Expression condition, Statement body, Expression change",
                "Function: Token name, List<Token> params, List<Statement> stmts",
                "Break: ",
                "Continue: ",
                "Return: Token keyword, Expression value"));
    }

    private static void defineAst(
            String outputDirectory,
            String packageName,
            String baseClass,
            List<String> types) throws FileNotFoundException, UnsupportedEncodingException {
        String outputFile = Path.of(outputDirectory, String.format("%s.java", baseClass)).toString();
        PrintWriter writer = new PrintWriter(outputFile, "UTF-8");

        writer.println(packageName + ";");
        writer.println();

        // imports
        writer.println("import java.util.List;");
        // end imports

        writer.println();
        writer.println("public abstract class " + baseClass + " {");

        // define base accept class
        writer.println("\tpublic abstract <R> R accept(Visitor<R> visitor);");

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseClass, className, fields);
        }

        defineVisitor(writer, "Visitor", baseClass, types);

        writer.println("}");
        writer.print("\n\n");

        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String string, String baseClass, List<String> types) {
        writer.println("\tpublic interface Visitor<T> {");
        for (String type : types) {
            writer.println();
            String typeName = type.split(":")[0].trim();
            writer.println(String.format("\t\tT visit%s(%s.%s obj);", typeName, baseClass, typeName));
        }
        writer.println("\t}");
    }

    private static void defineType(PrintWriter writer, String baseClass, String className, String fields) {
        writer.println();

        // class
        writer.println(String.format("\tpublic static class %s extends %s {", className, baseClass));

        // constructor
        writer.println(String.format("\t\tpublic %s(%s) {", className, fields));

        String[] fieldsList = fields.split(",");
        for (String field : fieldsList) {
            if (field.isBlank()) {
                continue;
            }
            String name = field.trim().split(" ")[1].trim();
            writer.println(String.format("\t\t\tthis.%s = %s;", name, name));
        }

        writer.println("\t\t}");
        // constructor ends

        // visitor class
        writer.println();
        writer.println("\t\t@Override");
        writer.println("\t\tpublic <R> R accept(Visitor<R> visitor) {");
        writer.println(String.format("\t\t\treturn visitor.visit%s(this);", className));
        writer.println("\t\t}");
        writer.println();
        // visitor ends

        for (String field : fieldsList) {
            if (field.isBlank()) {
                continue;
            }
            String[] fieldDescription = field.trim().split(" ");
            String type = fieldDescription[0].trim();
            String name = fieldDescription[1].trim();
            writer.println(String.format("\t\tpublic final %s %s;", type, name));
        }

        writer.println("\t}");
        // class ends
    }
}
