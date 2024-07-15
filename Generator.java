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
                        "Binary : Expression left, Token op, Expression right",
                        "Unary : Token op, Expression expr",
                        "Grouping : Expression expr",
                        "Literal : Object value"));
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
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseClass + " {");
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseClass, className, fields);
        }
        writer.println("}");
        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseClass, String className, String fields) {
        writer.println();

        // class
        writer.println(String.format("\tstatic class %s extends %s {", className, baseClass));

        // constructor
        writer.println(String.format("\t\t%s(%s) {", className, fields));

        String[] fieldsList = fields.split(",");
        for (String field: fieldsList) {
            String name = field.trim().split(" ")[1].trim();
            writer.println(String.format("\t\t\tthis.%s = %s;", name, name));
        }

        writer.println("\t\t}");
        // constructor ends

        for (String field: fieldsList) {
            String[] fieldDescription = field.trim().split(" ");
            String type = fieldDescription[0].trim();
            String name = fieldDescription[1].trim();
            writer.println(String.format("\t\tfinal %s %s;", type, name));
        }

        writer.println("\t}");
        //class ends
    }
}
