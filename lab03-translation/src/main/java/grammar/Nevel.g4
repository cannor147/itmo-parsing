grammar Nevel;

@header {
    import exception.*;
    import types.*;
    import context.*;
    import scope.*;
    import lang.mutability.*;
    import lang.type.*;
    import lang.object.*;
    import java.util.HashMap;
    import java.util.HashSet;
    import java.util.ArrayList;
    import java.util.Map;
    import java.util.Set;
    import java.util.List;
}

@members {
    private Typer typer;
    private Indexer indexer;
    private Scope scope;
    private Stage stage;

    private ParseContext getContext(ParserRuleContext _localctx) {
        return new ParseContext(_localctx, scope, stage);
    }
}

program[Typer myTyper, Indexer myIndexer, Scope myScope, Stage myStage] :
    {
        typer = myTyper;
        indexer = myIndexer;
        scope = myScope;
        stage = myStage;
    }
    (defineOperator | emptyOperator | function | LINE_COMMENT | BLOCK_COMMENT)+
;

typeName returns [Type type] :
    (
        ( BOOL | BYTE | SHORT | INT | LONG | FLOAT | DOUBLE | CHAR | VOID ) {
            $type = PrimitiveType.valueOf(_localctx.getChild(0).getText().toUpperCase());
        }
        |
        STRING {
            $type = StringType.getInstance();
        }
    )
    (
        OPENING_SQUARE_BRACKET CLOSING_SQUARE_BRACKET {
            $type = new ArrayType($type);
        }
    )*
;
variableName returns [String name] : IDENTIFIER { $name = _localctx.getChild(0).getText(); };
functionName returns [String name] : IDENTIFIER { $name = _localctx.getChild(0).getText(); };

function returns [List<Type> argumentTypes, Type resultType, String name] :
    FUN functionName {
        $name = $functionName.name;
        $argumentTypes = new ArrayList();
        $resultType = PrimitiveType.VOID;
        scope.in("@FUNCTION_" + $functionName.name);
    }
    OPENING_BRACKET
    (
        declaration[Mutability.MUTABLE] {
            $argumentTypes.add($declaration.type);
        }
        (
            COMMA declaration[Mutability.MUTABLE] {
                $argumentTypes.add($declaration.type);
            }
        )*
    )?
    CLOSING_BRACKET
    (
        COLON typeName {
            $resultType = $typeName.type;
        }
    )?
    block[null] {
        scope.out();
        indexer.indexFunction($name, $argumentTypes, $resultType, getContext(_localctx));
    }
;

block[String scopeName] :
    OPENING_BRACE {
        if ($scopeName != null) {
            scope.in($scopeName);
        }
    }
    (statement)*
    CLOSING_BRACE {
        if ($scopeName != null) {
            scope.out();
        }
    }
;
statement : ifStatement | whileStatement | operatorStatement;

ifStatement : IF OPENING_BRACKET expression CLOSING_BRACKET (statement | block["$@IF"]) (ELSE (statement | block["@ELSE"]))?;
whileStatement : WHILE OPENING_BRACKET expression CLOSING_BRACKET (statement | block["@WHILE"]);
operatorStatement :
    incrementOperator | decrementOperator | assignOperator | callOperator | defineOperator |
    continueOperator | breakOperator | returnOperator | emptyOperator | swapOperator
;

incrementOperator : availableVariable[PrimitiveType.NUMBER] PLUS_PLUS SEMICOLON;
decrementOperator : availableVariable[PrimitiveType.NUMBER] MINUS_MINUS SEMICOLON;
callOperator : callExpression SEMICOLON;
emptyOperator : SEMICOLON;
continueOperator : CONTINUE { typer.ensureInsideCycle("continue", getContext(_localctx)); } SEMICOLON;
breakOperator : BREAK { typer.ensureInsideCycle("break", getContext(_localctx)); } SEMICOLON;
returnOperator : RETURN { typer.ensureInsideFunction("return", getContext(_localctx)); } (expression)? SEMICOLON;
assignOperator : assignation SEMICOLON;
defineOperator :
    (
        CONST initialization[Mutability.IMMUTABLE] (COMMA initialization[Mutability.IMMUTABLE])*
        |
        VAR definition[Mutability.MUTABLE] (COMMA definition[Mutability.MUTABLE])*
    )
    SEMICOLON
;
swapOperator locals [Type type1, Type type2]:
    OPENING_BRACKET
    availableVariable[PrimitiveType.BOOL] { $type1 = $availableVariable.type; }
    COMMA
    availableVariable[PrimitiveType.BOOL] { $type2 = $availableVariable.type; }
    CLOSING_BRACKET
    ASSIGN
    OPENING_BRACKET
    availableVariable[PrimitiveType.BOOL] { typer.ensureEquals($type1, $availableVariable.type, getContext(_localctx)); }
    COMMA
    availableVariable[PrimitiveType.BOOL] { typer.ensureEquals($type2, $availableVariable.type, getContext(_localctx)); }
    CLOSING_BRACKET
    SEMICOLON
;

definition[Mutability mutability] : declaration[$mutability] | initialization[$mutability];
declaration[Mutability mutability] returns [Type type, String name] :
    variableName {
        $name = $variableName.name;
    }
    COLON typeName {
        $type = $typeName.type;
        indexer.indexVariable($name, $type, $mutability, getContext(_localctx));
    }
;
initialization[Mutability mutability] returns [Type type, String name] :
    variableName {
        $name = $variableName.name;
    }
    (
        COLON typeName {
            $type = $typeName.type;
        }
    )?
    ASSIGN expression {
        if ($type == null) {
            $type = $expression.type;
        } else {
            typer.ensureEquals($type, $expression.type, getContext(_localctx));
        }
        indexer.indexVariable($name, $type, $mutability, getContext(_localctx));
    }
;
assignation returns [Type type] :
    availableVariable[AnyType.getInstance()] {
        $type = $availableVariable.type;
    }
    (
        ASSIGN
        |
        (PLUS_ASSIGN | MINUS_ASSIGN | ASTERISK_ASSIGN | SLASH_ASSIGN | PERCENT_ASSIGN) {
            typer.ensureSubtype(PrimitiveType.NUMBER, $type, getContext(_localctx));
        }
        |
        (AND_ASSIGN | OR_ASSIGN | XOR_ASSIGN) {
            typer.ensureSubtype(PrimitiveType.ALGEBRAIC, $type, getContext(_localctx));
        }
    )
    expression {
        typer.ensureEquals($type, $expression.type, getContext(_localctx));
    }
;

availableVariable[Type superType] returns [Type type] :
    variableName {
        NevelVariable variable = indexer.findVariable($variableName.name, getContext(_localctx));
        if (variable != null) {
            $type = variable.getType();
            typer.ensureMutable($variableName.name, variable.getMutability(), getContext(_localctx));
            typer.ensureSubtype($superType, $type, getContext(_localctx));
        }
    }
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
            typer.ensureSubtype(PrimitiveType.BOOL, $type, getContext(_localctx));
            $type = $smartOrBinaryExpression.type;
        }
        COLON ternaryExpression {
            typer.ensureEquals($type, $ternaryExpression.type, getContext(_localctx));
        }
    )?
;

smartOrBinaryExpression returns [Type type] :
    smartAndBinaryExpression {
        $type = $smartAndBinaryExpression.type;
    }
    (
        PIPE_PIPE smartAndBinaryExpression {
            typer.ensureEquals($type, $smartAndBinaryExpression.type, getContext(_localctx));
            typer.ensureSubtype(PrimitiveType.BOOL, $smartAndBinaryExpression.type, getContext(_localctx));
        }
    )*
;

smartAndBinaryExpression returns [Type type] :
    orBinaryExpression {
        $type = $orBinaryExpression.type;
    }
    (
        AMPERSAND_AMPERSAND orBinaryExpression {
            typer.ensureEquals($type, $orBinaryExpression.type, getContext(_localctx));
            typer.ensureSubtype(PrimitiveType.BOOL, $orBinaryExpression.type, getContext(_localctx));
        }
    )*
;

orBinaryExpression returns [Type type] :
    xorBinaryExpression {
        $type = $xorBinaryExpression.type;
    }
    (
        (OR | PIPE) xorBinaryExpression {
            typer.ensureEquals($type, $xorBinaryExpression.type, getContext(_localctx));
            typer.ensureSubtype(PrimitiveType.ALGEBRAIC, $xorBinaryExpression.type, getContext(_localctx));
        }
    )*
;

xorBinaryExpression returns [Type type] :
    andBinaryExpression {
        $type = $andBinaryExpression.type;
    }
    (
        (XOR | CARET) andBinaryExpression {
            typer.ensureEquals($type, $andBinaryExpression.type, getContext(_localctx));
            typer.ensureSubtype(PrimitiveType.ALGEBRAIC, $andBinaryExpression.type, getContext(_localctx));
        }
    )*
;

andBinaryExpression returns [Type type] :
    equalityBinaryExpression {
        $type = $equalityBinaryExpression.type;
    }
    (
        (AND | AMPERSAND) equalityBinaryExpression {
            typer.ensureEquals($type, $equalityBinaryExpression.type, getContext(_localctx));
            typer.ensureSubtype(PrimitiveType.ALGEBRAIC, $equalityBinaryExpression.type, getContext(_localctx));
        }
    )*
;

equalityBinaryExpression returns [Type type] :
    relationalBinaryExpression {
        $type = $relationalBinaryExpression.type;
    }
    (
        (EQUALS | NOT_EQUALS | SUPER_EQUALS | SUPER_NOT_EQUALS) relationalBinaryExpression {
            typer.ensureEquals($type, $relationalBinaryExpression.type, getContext(_localctx));
            $type = PrimitiveType.BOOL;
        }
    )*
;

relationalBinaryExpression returns [Type type] :
    lowArithmeticBinaryExpression {
        $type = $lowArithmeticBinaryExpression.type;
    }
    (
        (GREATER | GREATER_OR_EQUALS | EQUALS_OR_GREATER | LESS | LESS_OR_EQUALS | EQUALS_OR_LESS) lowArithmeticBinaryExpression {
            typer.ensureEquals($type, $lowArithmeticBinaryExpression.type, getContext(_localctx));
            typer.ensureSubtype(PrimitiveType.NUMBER, $lowArithmeticBinaryExpression.type, getContext(_localctx));
            $type = PrimitiveType.BOOL;
        }
    )?
;

lowArithmeticBinaryExpression returns [Type type] :
    highArithmeticBinaryExpression {
        $type = $highArithmeticBinaryExpression.type;
    }
    (
        (PLUS | MINUS) highArithmeticBinaryExpression {
            typer.ensureEquals($type, $highArithmeticBinaryExpression.type, getContext(_localctx));
            typer.ensureSubtype(PrimitiveType.NUMBER, $highArithmeticBinaryExpression.type, getContext(_localctx));
        }
    )*
;

highArithmeticBinaryExpression returns [Type type] :
    unaryExpression {
        $type = $unaryExpression.type;
    }
    (
        (ASTERISK | SLASH | PERCENT) unaryExpression {
            typer.ensureEquals($type, $unaryExpression.type, getContext(_localctx));
            typer.ensureSubtype(PrimitiveType.NUMBER, $unaryExpression.type, getContext(_localctx));
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
        typer.ensureSubtype(PrimitiveType.NUMBER, $unaryExpression.type, getContext(_localctx));
        $type = $unaryExpression.type;
    }
    |
    (NOT | EXCLAMATION_MARK) unaryExpression {
        typer.ensureSubtype(PrimitiveType.BOOL, $unaryExpression.type, getContext(_localctx));
        $type = $unaryExpression.type;
    }
;

callExpression returns [Type type, List<Type> types, String name] :
    terminalExpression {
        $type = $terminalExpression.type;
    }
    |
    variableName {
        NevelVariable variable = indexer.findVariable($variableName.name, getContext(_localctx));
        if (variable != null) {
            $type = variable.getType();
        }
    }
    |
    (
        functionName {
            $name = $functionName.name;
            $types = new ArrayList<>();
        }
        OPENING_BRACKET
        (
            expression {
                $types.add($expression.type);
            }
            (
                COMMA
                expression {
                    $types.add($expression.type);
                }
            )*
        )?
        CLOSING_BRACKET {
            NevelFunction function = indexer.findFunction($name, $types, getContext(_localctx));
            if (function != null) {
                $type = function.getType();
            }
        }
    )
;

terminalExpression returns [Type type] :
    (FALSE | TRUE) { $type = PrimitiveType.BOOL; }
    |
    INT_VALUE { $type = PrimitiveType.INT; }
    |
    LONG_VALUE { $type = PrimitiveType.LONG; }
    |
    FLOAT_VALUE { $type = PrimitiveType.FLOAT; }
    |
    DOUBLE_VALUE { $type = PrimitiveType.DOUBLE; }
    |
    CHAR_VALUE { $type = PrimitiveType.CHAR; }
    |
    STRING_VALUE { $type = StringType.getInstance(); }
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

PRINT : 'print';
PRINTLN : 'println';
READ : 'read';

FALSE : 'false';
TRUE : 'true';
NULL : 'null';
NUMBER_VALUE : [0-9]+;

BOOL_VALUE : FALSE | TRUE;
INT_VALUE : NUMBER_VALUE;
LONG_VALUE : NUMBER_VALUE 'LL';
FLOAT_VALUE : NUMBER_VALUE DOT NUMBER_VALUE 'F';
DOUBLE_VALUE : NUMBER_VALUE DOT NUMBER_VALUE;
CHAR_VALUE : '\'' (NO_ESCAPE_SYMBOL | '"' | '\\\'' | ESCAPE_MANAGE_SYMBOL) '\'';
STRING_VALUE : '"' (NO_ESCAPE_SYMBOL | '\'' | '\\"' | ESCAPE_MANAGE_SYMBOL) '"';

NO_ESCAPE_SYMBOL : [^\\'"];
ESCAPE_MANAGE_SYMBOL : '\\\\' | '\\r' | '\\n' | '\\f' | '\\t' | '\\v';

IDENTIFIER : [A-Za-z][A-Za-z0-9]*;

WHITESPACE: [ \n\t\r]+ -> skip;
BLOCK_COMMENT : '/*'.*?'*/' -> skip;
LINE_COMMENT : '//' ~[\r\n]* -> skip;
