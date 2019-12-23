package translation;

import grammar.NevelBaseVisitor;
import grammar.NevelParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.Map;

public class CTranslator extends NevelBaseVisitor<Code> implements Translator {
    private static final Code PLUS = new Code("+");
    private static final Code MINUS = new Code("-");
    private static final Code TILDA = new Code("~");
    private static final Code EXCLAMATION_MARK = new Code("!");
    private static final Code ASTERISK = new Code("*");
    private static final Code SLASH = new Code("/");
    private static final Code PERCENT = new Code("%");
    private static final Code GREATER = new Code(">");
    private static final Code GREATER_OR_EQUALS = new Code(">=");
    private static final Code LESS = new Code("<");
    private static final Code LESS_OR_EQUALS = new Code("<=");
    private static final Code EQUALS = new Code("==");
    private static final Code NOT_EQUALS = new Code("!=");
    private static final Code AMPERSAND = new Code("&");
    private static final Code CARET = new Code("^");
    private static final Code PIPE = new Code("|");
    private static final Code AMPERSAND_AMPERSAND = new Code("&&");
    private static final Code PIPE_PIPE = new Code("||");
    private static final Code ASSIGN = new Code("=");
    private static final Code PLUS_ASSIGN = new Code("+=");
    private static final Code MINUS_ASSIGN = new Code("-=");
    private static final Code ASTERISK_ASSIGN = new Code("*=");
    private static final Code SLASH_ASSIGN = new Code("/=");
    private static final Code PERCENT_ASSIGN = new Code("%=");
    private static final Code AND_ASSIGN = new Code("&=");
    private static final Code OR_ASSIGN = new Code("|=");
    private static final Code XOR_ASSIGN = new Code("^=");
    private static final Code PLUS_PLUS = new Code("++");
    private static final Code MINUS_MINUS = new Code("--");

    private static final Code EMPTY = new Code("");
    private static final Code SPACE = new Code(" ");

    private static final Code OPEN = new Code("(");
    private static final Code CLOSE = new Code(")");
    private static final Code BLOCK = new Code("{");
    private static final Code UNBLOCK = new Code("}");
    private static final Code SEMICOLON = new Code(";");
    private static final Code COMMA = new Code(",");
    private static final Code QUESTION = new Code("?");
    private static final Code COLON = new Code(":");

    private static final Code IF = new Code("if");
    private static final Code ELSE = new Code("else");
    private static final Code WHILE = new Code("while");
    private static final Code CONTINUE = new Code("continue");
    private static final Code BREAK = new Code("break");
    private static final Code RETURN = new Code("return");
    private static final Code CONST = new Code("const");
    private static final Code INCLUDE = new Code("#include");

    private static final Code[] includes = {
            new Code("<stdbool.h>"),
    };

    private static final Code[] commonSymbols = {
            PLUS, MINUS, TILDA, EXCLAMATION_MARK, EXCLAMATION_MARK,
            ASTERISK, SLASH, PERCENT,
            GREATER, GREATER_OR_EQUALS, GREATER_OR_EQUALS, LESS, LESS_OR_EQUALS, LESS_OR_EQUALS,
            EQUALS, NOT_EQUALS,
            AMPERSAND, AMPERSAND,
            CARET, CARET,
            PIPE, PIPE,
            AMPERSAND_AMPERSAND,
            PIPE_PIPE,

            ASSIGN,
            ASTERISK_ASSIGN, SLASH_ASSIGN, PERCENT_ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN,
            AND_ASSIGN, XOR_ASSIGN, OR_ASSIGN,

            PLUS_PLUS, MINUS_MINUS
    };

    private static final Map<Integer, Code> arithmeticTranslations = new HashMap<>();
    static {
        for (int i = 0; i < commonSymbols.length; i++) {
            arithmeticTranslations.put(i + 1, commonSymbols[i]);
        }
    }

    private int blockCount = 0;

    @Override
    public Code translate(NevelParser.ProgramContext ctx) {
        Code code = new Code();
        for (Code include : includes) {
            code.addln(INCLUDE, SPACE, include);
        }
        code.addln();

        for (NevelParser.FunctionContext functionCtx : ctx.function()) {
            code.addln(visitFunction(functionCtx, false), SEMICOLON);
        }
        code.addln();

        code.add(visitProgram(ctx));
        return code;
    }

    @Override
    public Code visitProgram(NevelParser.ProgramContext ctx) {
        Code code = new Code();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            code.addln(visit(ctx.getChild(i)));
        }
        return code;
    }

    @Override
    public Code visitTypeName(NevelParser.TypeNameContext ctx) {
        return ctx.type.getCCode();
    }

    @Override
    public Code visitVariableName(NevelParser.VariableNameContext ctx) {
        return new Code(ctx.IDENTIFIER().getText());
    }

    @Override
    public Code visitFunctionName(NevelParser.FunctionNameContext ctx) {
        return new Code(ctx.IDENTIFIER().getText());
    }

    @Override
    public Code visitFunction(NevelParser.FunctionContext ctx) {
        return visitFunction(ctx, true);
    }

    @Override
    public Code visitBlock(NevelParser.BlockContext ctx) {
        Code code = new Code(BLOCK);
        code.addln();
        blockCount++;
        for (NevelParser.StatementContext statementCtx : ctx.statement()) {
            code.tabulate(blockCount);
            code.addln(visitStatement(statementCtx));
        }
        blockCount--;
        code.tabulate(blockCount);
        code.add(UNBLOCK);
        return code;

    }

    @Override
    public Code visitStatement(NevelParser.StatementContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Code visitIfStatement(NevelParser.IfStatementContext ctx) {
        Code code = new Code(IF, SPACE, OPEN, visitExpression(ctx.expression()), CLOSE, SPACE);
        code.add(visit(ctx.getChild(4)));
        if (ctx.getChildCount() > 5) {
            code.add(SPACE, ELSE, visit(ctx.getChild(6)));
        }
        return code;
    }

    @Override
    public Code visitWhileStatement(NevelParser.WhileStatementContext ctx) {
        return new Code(WHILE, SPACE, OPEN, visitExpression(ctx.expression()), CLOSE, SPACE, visit(ctx.getChild(4)));
    }

    @Override
    public Code visitOperatorStatement(NevelParser.OperatorStatementContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Code visitIncrementOperator(NevelParser.IncrementOperatorContext ctx) {
        return new Code(visitVariableName(ctx.variableName()), PLUS_PLUS, SEMICOLON);
    }

    @Override
    public Code visitDecrementOperator(NevelParser.DecrementOperatorContext ctx) {
        return new Code(visitVariableName(ctx.variableName()), MINUS_MINUS, SEMICOLON);
    }

    @Override
    public Code visitCallOperator(NevelParser.CallOperatorContext ctx) {
        return new Code(visitCallExpression(ctx.callExpression()), SEMICOLON);
    }

    @Override
    public Code visitEmptyOperator(NevelParser.EmptyOperatorContext ctx) {
        return EMPTY;
    }

    @Override
    public Code visitContinueOperator(NevelParser.ContinueOperatorContext ctx) {
        return new Code(CONTINUE, SEMICOLON);
    }

    @Override
    public Code visitBreakOperator(NevelParser.BreakOperatorContext ctx) {
        return new Code(BREAK, SEMICOLON);
    }

    @Override
    public Code visitReturnOperator(NevelParser.ReturnOperatorContext ctx) {
        Code code = new Code(RETURN);
        if (ctx.expression() != null) {
            code.add(SPACE, visitExpression(ctx.expression()));
        }
        code.add(SEMICOLON);
        return code;
    }

    @Override
    public Code visitDefineOperator(NevelParser.DefineOperatorContext ctx) {
        Code code = new Code();
        boolean first = true;
        for (NevelParser.DefinitionContext definitionCtx : ctx.definition()) {
            if (first) {
                first = false;
            } else {
                code.addln();
                code.tabulate(blockCount);
            }
            code.add(visitDefinition(definitionCtx), SEMICOLON);
        }
        for (NevelParser.InitializationContext initializationCtx : ctx.initialization()) {
            if (first) {
                first = false;
            } else {
                code.addln();
                code.tabulate(blockCount);
            }
            code.add(visitInitialization(initializationCtx), SEMICOLON);
        }
        return code;
    }

    @Override
    public Code visitAssignOperator(NevelParser.AssignOperatorContext ctx) {
        return new Code(visitAssignation(ctx.assignation()), SEMICOLON);
    }

    @Override
    public Code visitDefinition(NevelParser.DefinitionContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Code visitDeclaration(NevelParser.DeclarationContext ctx) {
        Code code = ctx.constant ? new Code(CONST, SPACE) : new Code();
        code.add(visitTypeName(ctx.typeName()), SPACE, visitVariableName(ctx.variableName()));
        return code;
    }

    @Override
    public Code visitInitialization(NevelParser.InitializationContext ctx) {
        Code code = ctx.constant ? new Code(CONST, SPACE) : new Code();
        code.add(ctx.type.getCCode(), SPACE, visitVariableName(ctx.variableName()));
        code.add(SPACE, ASSIGN, SPACE, visitExpression(ctx.expression()));
        return code;
    }

    @Override
    public Code visitAssignation(NevelParser.AssignationContext ctx) {
        Code code = new Code(visitVariableName(ctx.variableName()));
        TerminalNode sign = (TerminalNode) ctx.getChild(1);
        code.add(SPACE, arithmeticTranslations.get(sign.getSymbol().getType()), SPACE, visitExpression(ctx.expression()));
        return code;
    }

    @Override
    public Code visitExpression(NevelParser.ExpressionContext ctx) {
        return visit(ctx.ternaryExpression());
    }

    @Override
    public Code visitTernaryExpression(NevelParser.TernaryExpressionContext ctx) {
        if (ctx.getChildCount() == 1) {
            return new Code(visitSmartOrBinaryExpression(ctx.smartOrBinaryExpression(0)));
        }

        Code code = new Code(OPEN, visitSmartOrBinaryExpression(ctx.smartOrBinaryExpression(0)), SPACE);
        code.add(QUESTION, SPACE, visitSmartOrBinaryExpression(ctx.smartOrBinaryExpression(1)), SPACE);
        code.add(COLON, SPACE, visitTernaryExpression(ctx.ternaryExpression()), CLOSE);
        return code;
    }

    @Override
    public Code visitSmartOrBinaryExpression(NevelParser.SmartOrBinaryExpressionContext ctx) {
        return visitBinaryContext(ctx);
    }

    @Override
    public Code visitSmartAndBinaryExpression(NevelParser.SmartAndBinaryExpressionContext ctx) {
        return visitBinaryContext(ctx);
    }

    @Override
    public Code visitOrBinaryExpression(NevelParser.OrBinaryExpressionContext ctx) {
        return visitBinaryContext(ctx);
    }

    @Override
    public Code visitXorBinaryExpression(NevelParser.XorBinaryExpressionContext ctx) {
        return visitBinaryContext(ctx);
    }

    @Override
    public Code visitAndBinaryExpression(NevelParser.AndBinaryExpressionContext ctx) {
        return visitBinaryContext(ctx);
    }

    @Override
    public Code visitEqualityBinaryExpression(NevelParser.EqualityBinaryExpressionContext ctx) {
        return visitBinaryContext(ctx);
    }

    @Override
    public Code visitRelationalBinaryExpression(NevelParser.RelationalBinaryExpressionContext ctx) {
        return visitBinaryContext(ctx);
    }

    @Override
    public Code visitLowArithmeticBinaryExpression(NevelParser.LowArithmeticBinaryExpressionContext ctx) {
        return visitBinaryContext(ctx);
    }

    @Override
    public Code visitHighArithmeticBinaryExpression(NevelParser.HighArithmeticBinaryExpressionContext ctx) {
        return visitBinaryContext(ctx);
    }

    @Override
    public Code visitUnaryExpression(NevelParser.UnaryExpressionContext ctx) {
        if (ctx.callExpression() != null) {
            return visitCallExpression(ctx.callExpression());
        } else if (ctx.AS() != null) {
            return new Code(OPEN, OPEN, visitTypeName(ctx.typeName()), OPEN, SPACE, CLOSE, visitUnaryExpression(ctx.unaryExpression()), CLOSE, CLOSE);
        } else if (ctx.OPENING_BRACKET() != null) {
            return new Code(OPEN, visitExpression(ctx.expression()), CLOSE);
        } else {
            TerminalNode sign = (TerminalNode) ctx.getChild(0);
            return new Code(OPEN, arithmeticTranslations.get(sign.getSymbol().getType()), visitUnaryExpression(ctx.unaryExpression()), CLOSE);
        }
    }

    @Override
    public Code visitCallExpression(NevelParser.CallExpressionContext ctx) {
        if (ctx.getChildCount() == 1) {
            return visit(ctx.getChild(0));
        }

        Code code = new Code(visitFunctionName(ctx.functionName()), OPEN);
        boolean first = true;
        for (NevelParser.ExpressionContext expressionCtx : ctx.expression()) {
            if (first) {
                first = false;
            } else {
                code.add(COMMA, SPACE);
            }
            code.add(visitExpression(expressionCtx));
        }
        code.add(CLOSE);
        return code;
    }

    @Override
    public Code visitTerminalExpression(NevelParser.TerminalExpressionContext ctx) {
        return new Code(ctx.getChild(0).toString());
    }

    private Code visitFunction(NevelParser.FunctionContext ctx, boolean printBody) {
        Code code = new Code(ctx.types.get(0).getCCode());
        code.add(SPACE, visitFunctionName(ctx.functionName()), SPACE, OPEN);
        boolean first = true;
        for (NevelParser.DeclarationContext declarationCtx : ctx.declaration()) {
            if (first) {
                first = false;
            } else {
                code.add(COMMA, SPACE);
            }
            code.add(visitDeclaration(declarationCtx));
        }
        code.add(CLOSE);
        if (printBody) {
            code.addln(SPACE, visitBlock(ctx.block()));
        }
        return code;
    }

    private Code visitBinaryContext(ParserRuleContext ctx) {
        if (ctx.getChildCount() == 1) {
            return visit(ctx.getChild(0));
        }

        Code code = new Code(OPEN, visit(ctx.getChild(0)));
        int arguments = ctx.getChildCount() / 2 + 1;
        for (int i = 1; i < arguments; i++) {
            TerminalNode sign = (TerminalNode) ctx.getChild(2 * i - 1);
            code.add(SPACE, arithmeticTranslations.get(sign.getSymbol().getType()), SPACE, visit(ctx.getChild(2 * i)));
        }
        code.add(CLOSE);
        return code;
    }

    @Override
    public Code visit(ParseTree tree) {
        return tree.accept(this);
    }

}
