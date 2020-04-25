grammar Nevel;

@header {
    import exception.*;
    import type.*;
    import java.util.HashMap;
    import java.util.HashSet;
    import java.util.ArrayList;
    import java.util.Map;
    import java.util.Set;
    import java.util.List;
}

@members {
    private void error(String message, ParserRuleContext _localctx, ParserRuleContext _ctx) throws ParseException {
        throw new ParseException(
            message,
            _localctx.start.getLine(),
            _localctx.start.getCharPositionInLine(),
            _ctx.start.getText()
        );
    }

    private final Map<String, Type> variables = new HashMap<>();
    private final Map<String, List<Type>> functions = new HashMap<>();
    private final Set<String> constantVariables = new HashSet<>();

    private void ensureSubtype(Type expected, Type actual, ParserRuleContext _localctx, ParserRuleContext _ctx) throws ParseException {
        if ((expected.getCode() & actual.getCode()) != actual.getCode()) {
            error("Expected subtype of " + expected + ", but found " + actual + ".", _localctx, _ctx);
        }
    }

    private void ensureEquals(Type expected, Type actual, ParserRuleContext _localctx, ParserRuleContext _ctx) throws ParseException {
        if (expected.getCode() != actual.getCode()) {
            error("Expected " + expected + ", but found " + actual + ".", _localctx, _ctx);
        }
    }

    private void checkVariable(String name, ParserRuleContext _localctx, ParserRuleContext _ctx) throws ParseException {
        if (!variables.containsKey(name)) {
            error("Cannot resolve symbol " + name + ".", _localctx, _ctx);
        }
        if (constantVariables.contains(name)) {
            error("Cannot assign a value to const variable " + name + ".", _localctx, _ctx);
        }
    }

    private void defineVariable(String name, Type type) {
        variables.put(name, type);
    }

    private void defineFunction(String name, List<Type> types) {
        functions.put(name, types);
    }
}

program : (defineOperator | emptyOperator | function)+;

typeName returns [Type type] :
    ( BOOL | BYTE | SHORT | INT | LONG | FLOAT | DOUBLE | CHAR | STRING | VOID )
    { $type = Type.valueOf(_localctx.getChild(0).getText().toUpperCase()); }
;
variableName returns [String name] : IDENTIFIER { $name = _localctx.getChild(0).getText(); };
functionName returns [String name] : IDENTIFIER { $name = _localctx.getChild(0).getText(); };

function returns [List<Type> types, String name] :
    FUN functionName { $name = $functionName.name; $types = new ArrayList(); $types.add(Type.VOID); }
    OPENING_BRACKET
    (declaration[false] { $types.add($declaration.type); } (COMMA declaration[false] { $types.add($declaration.type); })*)?
    CLOSING_BRACKET
    (COLON typeName { $types.set(0, $typeName.type); })?
    block
    { defineFunction($name, $types); }
;

block : OPENING_BRACE (statement)* CLOSING_BRACE;
statement : ifStatement | whileStatement | operatorStatement;

ifStatement : IF OPENING_BRACKET expression CLOSING_BRACKET (statement | block) (ELSE (statement | block))?;
whileStatement : WHILE OPENING_BRACKET expression CLOSING_BRACKET (statement | block);
operatorStatement :
    incrementOperator | decrementOperator | assignOperator | callOperator | defineOperator |
    continueOperator | breakOperator | returnOperator | emptyOperator
;

incrementOperator : variableName { checkVariable($variableName.name, _localctx, _ctx); } PLUS_PLUS SEMICOLON;
decrementOperator : variableName { checkVariable($variableName.name, _localctx, _ctx); } MINUS_MINUS SEMICOLON;
callOperator : callExpression SEMICOLON;
emptyOperator : SEMICOLON;
continueOperator : CONTINUE SEMICOLON;
breakOperator : BREAK SEMICOLON;
returnOperator : RETURN (expression)? SEMICOLON;
defineOperator : (CONST initialization[true] (COMMA initialization[true])* | VAR definition[false] (COMMA definition[false])*) SEMICOLON;
assignOperator : assignation SEMICOLON;

definition[boolean constant] : declaration[$constant] | initialization[$constant];
declaration[boolean constant] returns [Type type, String name] :
    variableName { $name = $variableName.name; }
    COLON typeName { $type = $typeName.type; defineVariable($name, $type); }
;
initialization[boolean constant] returns [Type type, String name] :
    variableName { $name = $variableName.name; }
    (COLON typeName { $type = $typeName.type; })?
    ASSIGN expression {
        if ($type == null) {
            $type = $expression.type;
        }
        ensureEquals($type, $expression.type, _localctx, _ctx);
        defineVariable($name, $type);
    }
;
assignation returns [Type type] :
    variableName { checkVariable($variableName.name, _localctx, _ctx); $type = variables.get($variableName.name); }
    (
        ASSIGN
        |
        (PLUS_ASSIGN | MINUS_ASSIGN | ASTERISK_ASSIGN | SLASH_ASSIGN | PERCENT_ASSIGN)
        { ensureSubtype(Type.NUMBER, $type, _localctx, _ctx); }
        |
        (AND_ASSIGN | OR_ASSIGN | XOR_ASSIGN)
        { ensureSubtype(Type.ALGEBRAIC, $type, _localctx, _ctx); }
    )
    expression { ensureEquals($type, $expression.type, _localctx, _ctx); }
;

expression returns [Type type] :
    ternaryExpression {
        $type = $ternaryExpression.type;
    }
;

ternaryExpression returns [Type type] :
    smartOrBinaryExpression {
        $type = $smartOrBinaryExpression.type;
    }
    (
        QUESTION_MARK smartOrBinaryExpression {
            ensureSubtype(Type.BOOL, $type, _localctx, _ctx);
            $type = $smartOrBinaryExpression.type;
        }
        COLON ternaryExpression {
            ensureEquals($type, $ternaryExpression.type, _localctx, _ctx);
        }
    )?
;

smartOrBinaryExpression returns [Type type] :
    smartAndBinaryExpression {
        $type = $smartAndBinaryExpression.type;
    }
    (
        PIPE_PIPE smartAndBinaryExpression {
            ensureEquals($type, $smartAndBinaryExpression.type, _localctx, _ctx);
            ensureSubtype(Type.BOOL, $smartAndBinaryExpression.type, _localctx, _ctx);
        }
    )*
;

smartAndBinaryExpression returns [Type type] :
    orBinaryExpression {
        $type = $orBinaryExpression.type;
    }
    (
        AMPERSAND_AMPERSAND orBinaryExpression {
            ensureEquals($type, $orBinaryExpression.type, _localctx, _ctx);
            ensureSubtype(Type.BOOL, $orBinaryExpression.type, _localctx, _ctx);
        }
    )*
;

orBinaryExpression returns [Type type] :
    xorBinaryExpression {
        $type = $xorBinaryExpression.type;
    }
    (
        (OR | PIPE) xorBinaryExpression {
            ensureEquals($type, $xorBinaryExpression.type, _localctx, _ctx);
            ensureSubtype(Type.ALGEBRAIC, $xorBinaryExpression.type, _localctx, _ctx);
        }
    )*
;

xorBinaryExpression returns [Type type] :
    andBinaryExpression {
        $type = $andBinaryExpression.type;
    }
    (
        (XOR | CARET) andBinaryExpression {
            ensureEquals($type, $andBinaryExpression.type, _localctx, _ctx);
            ensureSubtype(Type.ALGEBRAIC, $andBinaryExpression.type, _localctx, _ctx);
        }
    )*
;

andBinaryExpression returns [Type type] :
    equalityBinaryExpression {
        $type = $equalityBinaryExpression.type;
    }
    (
        (AND | AMPERSAND) equalityBinaryExpression {
            ensureEquals($type, $equalityBinaryExpression.type, _localctx, _ctx);
            ensureSubtype(Type.ALGEBRAIC, $equalityBinaryExpression.type, _localctx, _ctx);
        }
    )*
;

equalityBinaryExpression returns [Type type] :
    relationalBinaryExpression {
        $type = $relationalBinaryExpression.type;
    }
    (
        (EQUALS | NOT_EQUALS | SUPER_EQUALS | SUPER_NOT_EQUALS) relationalBinaryExpression {
            ensureEquals($type, $relationalBinaryExpression.type, _localctx, _ctx);
            $type = Type.BOOL;
        }
    )*
;

relationalBinaryExpression returns [Type type] :
    lowArithmeticBinaryExpression {
        $type = $lowArithmeticBinaryExpression.type;
    }
    (
        (GREATER | GREATER_OR_EQUALS | EQUALS_OR_GREATER | LESS | LESS_OR_EQUALS | EQUALS_OR_LESS) lowArithmeticBinaryExpression {
            ensureEquals($type, $lowArithmeticBinaryExpression.type, _localctx, _ctx);
            ensureSubtype(Type.NUMBER, $lowArithmeticBinaryExpression.type, _localctx, _ctx);
            $type = Type.BOOL;
        }
    )?
;

lowArithmeticBinaryExpression returns [Type type] :
    highArithmeticBinaryExpression {
        $type = $highArithmeticBinaryExpression.type;
    }
    (
        (PLUS | MINUS) highArithmeticBinaryExpression {
            ensureEquals($type, $highArithmeticBinaryExpression.type, _localctx, _ctx);
            ensureSubtype(Type.NUMBER, $highArithmeticBinaryExpression.type, _localctx, _ctx);
        }
    )*
;

highArithmeticBinaryExpression returns [Type type] :
    unaryExpression {
        $type = $unaryExpression.type;
    }
    (
        (ASTERISK | SLASH | PERCENT) unaryExpression {
            ensureEquals($type, $unaryExpression.type, _localctx, _ctx);
            ensureSubtype(Type.NUMBER, $unaryExpression.type, _localctx, _ctx);
        }
    )*
;

unaryExpression returns [Type type] :
    callExpression {
        $type = $callExpression.type;
    }
    |
    unaryExpression AS typeName {
        $type = $typeName.type;
    }
    |
    OPENING_BRACKET expression CLOSING_BRACKET {
        $type = $expression.type;
    }
    |
    (PLUS | MINUS | TILDA) unaryExpression {
        ensureSubtype(Type.NUMBER, $unaryExpression.type, _localctx, _ctx);
        $type = $unaryExpression.type;
    }
    |
    (NOT | EXCLAMATION_MARK) unaryExpression {
        ensureSubtype(Type.BOOL, $unaryExpression.type, _localctx, _ctx);
        $type = $unaryExpression.type;
    }
;

callExpression returns [Type type] :
    terminalExpression {
        $type = $terminalExpression.type;
    }
    |
    variableName {
        checkVariable($variableName.name, _localctx, _ctx);
        $type = variables.get($variableName.name);
    }
    |
    functionName OPENING_BRACKET (expression (COMMA expression)*)? CLOSING_BRACKET {
        $type = Type.INT;
    }
;

terminalExpression returns [Type type] :
    (TRUE | FALSE) {
        $type = Type.BOOL;
    }
    |
    NUMBER_VALUE {
        long value = Long.parseLong($NUMBER_VALUE.getText());
        if (Integer.MIN_VALUE <= value && value <= Integer.MAX_VALUE) {
            $type = Type.INT;
        } else {
            $type = Type.LONG;
        }
    }
    |
    DOUBLE_NUMBER_VALUE {
        double value = Double.parseDouble($DOUBLE_NUMBER_VALUE.getText());
        $type = Type.DOUBLE;
    }
    |
    CHARACTER_VALUE {
        $type = Type.CHAR;
    }
    |
    STRINGVALUE {
        $type = Type.STRING;
    }
;

PLUS : '+';
MINUS : '-';
TILDA : '~';
EXCLAMATION_MARK : '!';
NOT : 'not';
ASTERISK : '*';
SLASH : '/';
PERCENT : '%';
GREATER : '>';
GREATER_OR_EQUALS : '>=';
EQUALS_OR_GREATER : '=>';
LESS : '<';
LESS_OR_EQUALS : '<=';
EQUALS_OR_LESS : '=<';
EQUALS : '==';
NOT_EQUALS : '!=';
AMPERSAND : '&';
AND : 'and';
CARET : '^';
XOR : 'xor';
PIPE : '|';
OR : 'or';
AMPERSAND_AMPERSAND : '&&';
PIPE_PIPE : '||';
ASSIGN : '=';
ASTERISK_ASSIGN : '*=';
SLASH_ASSIGN : '/=';
PERCENT_ASSIGN : '%=';
PLUS_ASSIGN : '+=';
MINUS_ASSIGN : '-=';
AND_ASSIGN : '&=';
XOR_ASSIGN : '^=';
OR_ASSIGN : '|=';
PLUS_PLUS : '++';
MINUS_MINUS : '--';

OPENING_BRACKET : '(';
CLOSING_BRACKET : ')';
OPENING_BRACE : '{';
CLOSING_BRACE : '}';
OPENING_SQUARE_BRACKET : '[';
CLOSING_SQUARE_BRACKET : ']';
DOT : '.';
COMMA : ',';
SEMICOLON : ';';
QUESTION_MARK : '?';
COLON : ':';
QUOTE : '\'';
DOUBLE_QUOTE : '"';

SUPER_EQUALS : '===';
SUPER_NOT_EQUALS : '!==';

BOOL : 'bool';
BYTE : 'byte';
SHORT : 'short';
INT : 'int';
LONG : 'long';
FLOAT : 'float';
DOUBLE : 'double';
CHAR : 'char';
STRING : 'string';
VOID : 'void';

FALSE : 'false';
TRUE : 'true';
NULL : 'null';

VAR : 'var';
CONST : 'const';
FUN : 'fun';
IF : 'if';
ELSE : 'else';
FOR : 'for';
WHILE : 'while';
DO : 'do';
AS : 'as';
WHEN : 'when';
CONTINUE : 'continue';
BREAK : 'break';
RETURN : 'return';

SLASHN : '\\n';
SLASHSLASH : '\\\\';
SLASHDOLLAR : '\\$';

NUMBER_VALUE : [0-9]+ ;
DOUBLE_NUMBER_VALUE : NUMBER_VALUE DOT NUMBER_VALUE ;
CHARACTER_VALUE : QUOTE . QUOTE;
STRINGVALUE : '"' (~('\\' | '$' |'"') | SLASHN | SLASHSLASH | SLASHDOLLAR)* '"';

IDENTIFIER : [A-Za-z][A-Za-z0-9]*;

WHITESPACE: [ \n\t\r]+ -> skip;
