package translation;

import code.Code;
import grammar.NevelBaseVisitor;
import grammar.NevelParser;
import lang.mutability.Mutability;
import lang.type.PrimitiveType;
import lang.type.Type;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import types.Indexer;

import java.util.HashMap;
import java.util.Map;

import static code.Codes.*;

public class CTranslator extends NevelBaseVisitor<Code> implements Translator {
    private static final Map<Code, Code> TYPE_TRANSLATION_MAP = new HashMap<>();
    private static final Map<Code, Code> TYPE_FORMAT_MAP = new HashMap<>();

    static {
        TYPE_TRANSLATION_MAP.put(VOID, VOID);
        TYPE_TRANSLATION_MAP.put(BOOL, BOOL);
        TYPE_TRANSLATION_MAP.put(BYTE, new Code(SIGNED, SPACE, CHAR));
        TYPE_TRANSLATION_MAP.put(SHORT, new Code(SIGNED, SPACE, SHORT, SPACE, INT));
        TYPE_TRANSLATION_MAP.put(INT, new Code(SIGNED, SPACE, INT));
        TYPE_TRANSLATION_MAP.put(LONG, new Code(SIGNED, SPACE, LONG, SPACE, LONG, SPACE, INT));
        TYPE_TRANSLATION_MAP.put(FLOAT, FLOAT);
        TYPE_TRANSLATION_MAP.put(DOUBLE, DOUBLE);
        TYPE_TRANSLATION_MAP.put(CHAR, new Code(UNSIGNED, SPACE, CHAR));
        TYPE_TRANSLATION_MAP.put(STRING, new Code(UNSIGNED, SPACE, CHAR, ASTERISK));

        TYPE_FORMAT_MAP.put(BOOL, new Code("%s"));
        TYPE_FORMAT_MAP.put(BYTE, new Code("%hhi"));
        TYPE_FORMAT_MAP.put(SHORT, new Code("%hi"));
        TYPE_FORMAT_MAP.put(INT, new Code("%i"));
        TYPE_FORMAT_MAP.put(LONG, new Code("%lli"));
        TYPE_FORMAT_MAP.put(FLOAT, new Code("%f"));
        TYPE_FORMAT_MAP.put(DOUBLE, new Code("%lf"));
        TYPE_FORMAT_MAP.put(CHAR, new Code("%c"));
        TYPE_FORMAT_MAP.put(STRING, new Code("%s"));
    }

    private static final Code[] INCLUDES = {
            new Code("<stdbool.h>"),
            new Code("<stdio.h>"),
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
        for (Code include : INCLUDES) {
            code.addln(C_INCLUDE, SPACE, include);
        }
        code.addln();

        Code slashN = new Code("\\n");
        for (Type generalType : Indexer.GENERAL_TYPES) {
            Code v = new Code("v");
            Code vv = new Code("v");
            if (generalType.equals(PrimitiveType.BOOL)) {
                vv.add(SPACE, QUESTION, SPACE, new Code("\"true\""), SPACE, COLON, SPACE, new Code("\"false\""));
            }
            Code typeCode = new Code(TYPE_TRANSLATION_MAP.get(generalType.getCode()));
            Code formatCode = new Code(TYPE_FORMAT_MAP.get(generalType.getCode()));

            Code niceTypeCode = new Code('_' + generalType.getCode().toString());

            code.add(VOID, SPACE, PRINT, niceTypeCode, OPEN, typeCode, SPACE, v, CLOSE, SPACE, OPENING_BRACE, SPACE);
            code.add(PRINTF, OPEN, DOUBLE_QUOTE, formatCode, DOUBLE_QUOTE, COMMA, SPACE, vv, CLOSE, SEMICOLON);
            code.addln(SPACE, CLOSING_BRACE);

            code.add(VOID, SPACE, PRINTLN, niceTypeCode, OPEN, typeCode, SPACE, v, CLOSE, SPACE, OPENING_BRACE, SPACE);
            code.add(PRINTF, OPEN, DOUBLE_QUOTE, formatCode, slashN, DOUBLE_QUOTE, COMMA, SPACE, vv, CLOSE, SEMICOLON);
            code.addln(SPACE, CLOSING_BRACE);
        }
        code.addln();

        for (NevelParser.FunctionContext functionCtx : ctx.function()) {
            code.addln(visitFunction(functionCtx, false), SEMICOLON);
        }
        code.addln();

        code.add(TYPE_TRANSLATION_MAP.get(BOOL), SPACE, new Code("_tmp"), SPACE, ASSIGN, new Code("0"), SEMICOLON);

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
        return new Code(TYPE_TRANSLATION_MAP.get(ctx.type.getCode()));
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
        Code code = new Code(OPENING_BRACE);
        code.addln();
        blockCount++;
        for (NevelParser.StatementContext statementCtx : ctx.statement()) {
            code.tabulate(blockCount);
            code.addln(visitStatement(statementCtx));
        }
        blockCount--;
        code.tabulate(blockCount);
        code.add(CLOSING_BRACE);
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
        return new Code(visitVariableName(ctx.availableVariable().variableName()), PLUS_PLUS, SEMICOLON);
    }

    @Override
    public Code visitDecrementOperator(NevelParser.DecrementOperatorContext ctx) {
        return new Code(visitVariableName(ctx.availableVariable().variableName()), MINUS_MINUS, SEMICOLON);
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

    /*
    t = a
    a = b
    b = t

    t = 4
    1 = 3
    2 = t
     */

    @Override
    public Code visitAssignOperator(NevelParser.AssignOperatorContext ctx) {
        return new Code(visitAssignation(ctx.assignation()), SEMICOLON);
    }

    @Override
    public Code visitSwapOperator(NevelParser.SwapOperatorContext ctx) {
        Code first = visitChildren(ctx.availableVariable(0));
        Code second = visitChildren(ctx.availableVariable(1));
        Code third = visitChildren(ctx.availableVariable(2));
        Code fourth = visitChildren(ctx.availableVariable(3));
        Code tmp = new Code("_tmp");

        Code code = new Code(tmp, SPACE, ASSIGN, SPACE, fourth, SEMICOLON);
        code.add(first, SPACE, ASSIGN, SPACE, third, SEMICOLON);
        code.add(second, SPACE, ASSIGN, SPACE, tmp, SEMICOLON);

        return code;
    }

    @Override
    public Code visitDefinition(NevelParser.DefinitionContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Code visitDeclaration(NevelParser.DeclarationContext ctx) {
        Code code = ctx.mutability == Mutability.IMMUTABLE ? new Code(CONST, SPACE) : new Code();
        code.add(visitTypeName(ctx.typeName()), SPACE, visitVariableName(ctx.variableName()));
        return code;
    }

    @Override
    public Code visitInitialization(NevelParser.InitializationContext ctx) {
        Code code = ctx.mutability == Mutability.IMMUTABLE ? new Code(CONST, SPACE) : new Code();
        code.add(TYPE_TRANSLATION_MAP.get(ctx.type.getCode()), SPACE, visitVariableName(ctx.variableName()));
        code.add(SPACE, ASSIGN, SPACE, visitExpression(ctx.expression()));
        return code;
    }

    @Override
    public Code visitAssignation(NevelParser.AssignationContext ctx) {
        Code code = new Code(visitVariableName(ctx.availableVariable.variableName()));
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
            return new Code(OPEN, OPEN, visitTypeName(ctx.typeName()), CLOSE, SPACE, OPEN, visitUnaryExpression(ctx.unaryExpression()), CLOSE, CLOSE);
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

        Code functionName = visitFunctionName(ctx.functionName());
        Code code = new Code(OPEN);
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

        if (ctx.expression().size() == 1 && ("print".equals(functionName.toString()) || "println".equals(functionName.toString()))) {
            String typeName = ctx.expression(0).type.getCode().toString();
            functionName.add(new Code('_' + ctx.expression(0).type.getCode().toString()));
        }

        return new Code(functionName, code);
    }

    @Override
    public Code visitTerminalExpression(NevelParser.TerminalExpressionContext ctx) {
        return new Code(ctx.getChild(0).toString());
    }

    private Code visitFunction(NevelParser.FunctionContext ctx, boolean printBody) {
        Code typeCode = TYPE_TRANSLATION_MAP.get(ctx.resultType.getCode());
        Code code = new Code(typeCode);
        code.add(SPACE, visitFunctionName(ctx.functionName()), OPEN);
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
