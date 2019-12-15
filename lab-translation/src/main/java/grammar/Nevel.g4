grammar Nevel;

@header {
    import exception.*;
}

@members {
    private void error(String message, ParserRuleContext _localctx, ParserRuleContext _ctx) throws ParseException {
        throw new exception.ParseException(
            message,
            _localctx.start.getLine(),
            _localctx.start.getCharPositionInLine(),
            _ctx.getStart().getText()
        );
    }

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

    enum Type {
        BOOL(1),
        BYTE(2),
        SHORT(4),
        INT(8),
        LONG(16),
        FLOAT(32),
        DOUBLE(64),
        CHAR(128),
        STRING(256),

        ALGEBRAIC(31),
        INTEGER(30),
        NUMBER(126),
        PRIMITIVE(254),
        ANY(Integer.MAX_VALUE);

        private final int code;

        Type(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}

program : (statement)+;

function : FUN IDENTIFIER OPENING_BRACKET CLOSING_BRACKET OPENING_BRACE (statement)* CLOSING_BRACKET;

statement : (DEF | VAR) IDENTIFIER ASIGN expression SEMICOLON+;

expression returns [Type type] :
    expression_ternary {
        $type = $expression_ternary.type;
    }
;

expression_ternary returns [Type type] :
    expression_binary_r {
        ensureSubtype(Type.BOOL, $expression_binary_r.type, _localctx, _ctx);
    }
    (
        QUESTION expression_binary_r {
            $type = $expression_binary_r.type;
        }
        COLON expression_binary_r {
            ensureEquals($type, $expression_binary_r.type, _localctx, _ctx);
        }
    )*
;

expression_binary_r returns [Type type] :
    expression_binary_s {
        $type = $expression_binary_s.type;
    }
    (
        SMART_OR expression_binary_s {
            ensureEquals($type, $expression_binary_s.type, _localctx, _ctx);
            ensureSubtype(Type.BOOL, $expression_binary_s.type, _localctx, _ctx);
        }
    )*
;

expression_binary_s returns [Type type] :
    expression_binary_t {
        $type = $expression_binary_t.type;
    }
    (
        SMART_AND expression_binary_t {
            ensureEquals($type, $expression_binary_t.type, _localctx, _ctx);
            ensureSubtype(Type.BOOL, $expression_binary_t.type, _localctx, _ctx);
        }
    )*
;

expression_binary_t returns [Type type] :
    expression_binary_u {
        $type = $expression_binary_u.type;
    }
    (
        OR expression_binary_u {
            ensureEquals($type, $expression_binary_u.type, _localctx, _ctx);
            ensureSubtype(Type.ALGEBRAIC, $expression_binary_u.type, _localctx, _ctx);
        }
    )*
;

expression_binary_u returns [Type type] :
    expression_binary_v {
        $type = $expression_binary_v.type;
    }
    (
        XOR expression_binary_v {
            ensureEquals($type, $expression_binary_v.type, _localctx, _ctx);
            ensureSubtype(Type.ALGEBRAIC, $expression_binary_v.type, _localctx, _ctx);
        }
    )*
;

expression_binary_v returns [Type type] :
    expression_binary_w {
        $type = $expression_binary_w.type;
    }
    (
        AND expression_binary_w {
            ensureEquals($type, $expression_binary_w.type, _localctx, _ctx);
            ensureSubtype(Type.ALGEBRAIC, $expression_binary_w.type, _localctx, _ctx);
        }
    )*
;

expression_binary_w returns [Type type] :
    expression_binary_x {
        $type = $expression_binary_x.type;
    }
    (
        EQUALS expression_binary_x {
            ensureEquals($type, $expression_binary_x.type, _localctx, _ctx);
            $type = Type.BOOL;
        }
        |
        NOT_EQUALS expression_binary_x {
            ensureEquals($type, $expression_binary_x.type, _localctx, _ctx);
            $type = Type.BOOL;
        }
        |
        SUPER_EQUALS expression_binary_x {
            ensureEquals($type, $expression_binary_x.type, _localctx, _ctx);
            $type = Type.BOOL;
        }
        |
        SUPER_NOT_EQUALS expression_binary_x {
            ensureEquals($type, $expression_binary_x.type, _localctx, _ctx);
            $type = Type.BOOL;
        }
    )*
;

expression_binary_x returns [Type type] :
    expression_binary_y {
        $type = $expression_binary_y.type;
    }
    |
    expression_binary_y {$type = $expression_binary_y.type;} GREATER expression_binary_y {
        ensureEquals($type, $expression_binary_y.type, _localctx, _ctx);
        ensureSubtype(Type.NUMBER, $expression_binary_y.type, _localctx, _ctx);
        $type = Type.BOOL;
    }
    |
    expression_binary_y {$type = $expression_binary_y.type;} GREATER_OR_EQUALS expression_binary_y {
        ensureEquals($type, $expression_binary_y.type, _localctx, _ctx);
        ensureSubtype(Type.NUMBER, $expression_binary_y.type, _localctx, _ctx);
        $type = Type.BOOL;
    }
    |
    |
    expression_binary_y {$type = $expression_binary_y.type;} LESS expression_binary_y {
        ensureEquals($type, $expression_binary_y.type, _localctx, _ctx);
        ensureSubtype(Type.NUMBER, $expression_binary_y.type, _localctx, _ctx);
        $type = Type.BOOL;
    }
    |
    expression_binary_y {$type = $expression_binary_y.type;} LESS_OR_EQUALS expression_binary_y {
        ensureEquals($type, $expression_binary_y.type, _localctx, _ctx);
        ensureSubtype(Type.NUMBER, $expression_binary_y.type, _localctx, _ctx);
        $type = Type.BOOL;
    }
;

expression_binary_y returns [Type type] :
    expression_binary_z {
        $type = $expression_binary_z.type;
    }
    (
        PLUS expression_binary_z {
            ensureEquals($type, $expression_binary_z.type, _localctx, _ctx);
            ensureSubtype(Type.NUMBER, $expression_binary_z.type, _localctx, _ctx);
        }
        MINUS expression_binary_z {
            ensureEquals($type, $expression_binary_z.type, _localctx, _ctx);
            ensureSubtype(Type.NUMBER, $expression_binary_z.type, _localctx, _ctx);
        }
    )*
;

expression_binary_z returns [Type type] :
    expression_unary {
        $type = $expression_unary.type;
    }
    (
        ASTERISK expression_unary {
            ensureEquals($type, $expression_unary.type, _localctx, _ctx);
            ensureSubtype(Type.NUMBER, $expression_unary.type, _localctx, _ctx);
        }
        |
        SLASH expression_unary {
            ensureEquals($type, $expression_unary.type, _localctx, _ctx);
            ensureSubtype(Type.NUMBER, $expression_unary.type, _localctx, _ctx);
        }
        |
        PERCENT expression_unary {
            ensureEquals($type, $expression_unary.type, _localctx, _ctx);
            ensureSubtype(Type.NUMBER, $expression_unary.type, _localctx, _ctx);
        }
    )*
;

expression_unary returns [Type type] :
    PLUS expression_term {
        ensureSubtype(Type.NUMBER, $expression_term.type, _localctx, _ctx);
        $type = $expression_term.type;
    }
    |
    MINUS expression_term {
        ensureSubtype(Type.NUMBER, $expression_term.type, _localctx, _ctx);
        $type = $expression_term.type;
    }
    |
    INVERSE expression_term {
        ensureSubtype(Type.NUMBER, $expression_term.type, _localctx, _ctx);
        $type = $expression_term.type;
    }
    |
    NOT expression_term {
        ensureSubtype(Type.BOOL, $expression_term.type, _localctx, _ctx);
        $type = $expression_term.type;
    }
    |
    expression_term {
        $type = $expression_term.type;
    }
;

expression_term returns [Type type] :
    TRUE {
        $type = Type.BOOL;
    }
    |
    FALSE {
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
    |
    OPENING_BRACKET expression CLOSING_BRACKET {
        $type = $expression.type;
    }
;

OPENING_BRACKET : '(';
CLOSING_BRACKET : ')';
OPENING_BRACE : '{';
CLOSING_BRACE : '}';
OPENING_SQUARE_BRACKET : '[';
CLOSING_SQUARE_BRACKET : ']';

DOT : '.';
COMMA : ',';
COLON : ':';
SEMICOLON : ';';
QUOTE : '\'';
DOUBLE_QUOTE : '"';
QUESTION : '?';
PLUS : '+';
MINUS : '-';
ASTERISK : '*';
SLASH : '/';
PERCENT : '%';

ASIGN : '=';
PLUS_ASIGN : '+=';
MINUS_ASIGN : '-=';
ASTERISK_ASIGN : '*=';
SLASH_ASIGN : '/=';
PERCENT_ASIGN : '%=';
AND_ASIGN : '&=';
OR_ASIGN : '|=';
XOR_ASIGN : '^=';

EQUALS : '==';
NOT_EQUALS : '!=';
LESS : '<';
GREATER : '>';
LESS_OR_EQUALS : '<=' | '=<';
GREATER_OR_EQUALS : '>=' | '=>';
SUPER_EQUALS : '===';
SUPER_NOT_EQUALS : '!==';

INVERSE : '~';
NOT : 'not' | '!';
AND : 'and' | '&';
OR : 'or' | '|';
XOR : 'xor' | '^';
SMART_AND : '&&';
SMART_OR : '||';

FALSE : 'false';
TRUE : 'true';
NULL : 'null';

BOOL : 'bool';
BYTE : 'byte';
SHORT : 'short';
INT : 'int';
LONG : 'long';
FLOAT : 'float';
DOUBLE : 'double';
CHAR : 'char';
STRING : 'string';

IF : 'if';
ELSE : 'else';
FOR : 'for';
WHILE : 'while';
DO : 'do';
AS : 'as';
WHEN : 'when';
CONTINUE : 'continue';
BREAK : 'break';
RETURN : 'retur';

VAR : 'var';
DEF : 'def';
FUN : 'fun';

SLASHN : '\\n';
SLASHSLASH : '\\\\';
SLASHDOLLAR : '\\$';

NUMBER_VALUE : [0-9]+ ;
DOUBLE_NUMBER_VALUE : NUMBER_VALUE DOT NUMBER_VALUE ;
CHARACTER_VALUE : QUOTE . QUOTE;
STRINGVALUE : '"' (~('\\' | '$' |'"') | SLASHN | SLASHSLASH | SLASHDOLLAR)* '"';

IDENTIFIER : '_'*[A-Za-z][_A-Za-z0-9]*;

WHITESPACE: [ \n\t\r]+ -> skip;
