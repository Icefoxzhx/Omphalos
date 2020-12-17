grammar Mx;

program : programfragment* EOF;

programfragment : varDef | funcDef | classDef;

classDef : Class Identifier '{' (varDef | funcDef | constructFuncDef)* '}' ';';
funcDef : returnType Identifier '(' paramList? ')' suite;
varDef : type singleVarDef (',' singleVarDef)* ';';

constructFuncDef : Identifier '(' ')' suite;
singleVarDef : Identifier ('=' expression)?;

paramList : param (',' param)*;
param : type Identifier;

basicType : Bool | Int | String;
type
    : (Identifier | basicType) ('[' ']')*
    | Void
    ;
returnType : type | Void;

suite : '{' statement* '}';

statement
    : suite                                                 #Block
    | varDef                                                #VarDefStmt
    | If '(' expression ')' trueStmt=statement 
        (Else falseStmt=statement)?                         #IfStmt
    | For '(' init=expression? ';' cond=expression? ';'
                incr=expression? ')' statement              #ForStmt
    | While '(' expression ')' statement                    #WhileStmt
    | Return expression? ';'                                #ReturnStmt
    | Break ';'                                             #BreakStmt
    | Continue ';'                                          #ContinueStmt
    | expression ';'                                        #PureExprStmt
    | ';'                                                   #EmptyStmt
    ;

expressionList : expression (',' expression)*;

expression
    : expression op=('++' | '--')                  #SuffixExpr   // Precedence 1
    | expression '(' expressionList? ')'           #FuncCall
    | expression '[' expression ']'                #Subscript
    | expression '.' Identifier                    #MemberAccess

    | <assoc=right> op=('++'|'--') expression      #UnaryExpr        // Precedence 2
    | <assoc=right> op=('+' | '-') expression      #UnaryExpr
    | <assoc=right> op=('!' | '~') expression      #UnaryExpr
    | <assoc=right> 'new' creator                  #New

    | expression op=('*' | '/' | '%') expression   #BinaryExpr       // Precedence 3
    | expression op=('+' | '-') expression         #BinaryExpr       // Precedence 4
    | expression op=('<<'|'>>') expression         #BinaryExpr       // Precedence 5
    | expression op=('<' | '>') expression         #BinaryExpr       // Precedence 6
    | expression op=('<='|'>=') expression         #BinaryExpr
    | expression op=('=='|'!=') expression         #BinaryExpr       // Precedence 7
    | expression op='&' expression                 #BinaryExpr       // Precedence 8
    | expression op='^' expression                 #BinaryExpr       // Precedence 9
    | expression op='|' expression                 #BinaryExpr       // Precedence 10
    | expression op='&&' expression                #BinaryExpr       // Precedence 11
    | expression op='||' expression                #BinaryExpr       // Precedence 12

    | <assoc=right> expression op='=' expression   #BinaryExpr       // Precedence 14

    | Identifier                                   #Identifier
    | constant                                     #Const
    | '(' expression ')'                           #SubExpression
    | This                                         #This
    ;

constant
    : DecimalInteger
    | StringLiteral
    | boolValue=(True | False)
    | Null
    ;
creator
    : (basicType | Identifier) ('[' expression ']')+ ('[' ']')+ ('[' expression ']')+ #ErrorCreator
    | (basicType | Identifier) ('[' expression ']')+ ('[' ']')* #ArrayCreator
    | (basicType | Identifier) '(' ')'                          #ClassCreator
    | (basicType | Identifier)                                  #BasicCreator
    ;


Int : 'int';
Bool : 'bool';
String : 'string';
Null : 'null';
Void : 'void';
True : 'true';
False : 'false';
If : 'if';
Else : 'else';
For : 'for';
While : 'while';
Break : 'break';
Continue : 'continue';
Return : 'return';
New : 'new';
Class : 'class';
This: 'this';

LeftParen : '(';
RightParen : ')';
LeftBracket : '[';
RightBracket : ']';
LeftBrace : '{';
RightBrace : '}';

Less : '<';
LessEqual : '<=';
Greater : '>';
GreaterEqual : '>=';
LeftShift : '<<';
RightShift : '>>';

Plus : '+';
PlusPlus : '++';
Minus : '-';
MinusMinus : '--';
Star : '*';
Div : '/';
Mod : '%';

And : '&';
Or : '|';
AndAnd : '&&';
OrOr : '||';
Caret : '^';
Not : '!';
Tilde : '~';

Question : '?';
Colon : ':';
Semi : ';';
Comma : ',';

Assign : '=';
Equal : '==';
NotEqual : '!=';

Dot : '.';

StringLiteral
    : '"' SChar* '"'
    ;

fragment
SChar
    : ~["\\\n\r]
    | '\\n'
    | '\\\\'
    | '\\"'
    ;

Identifier
    : [a-zA-Z] [a-zA-Z_0-9]*
    ;

DecimalInteger
    : [1-9] [0-9]*
    | '0'
    ;

Whitespace
    :   [ \t]+
        -> skip
    ;

Newline
    :   (   '\r' '\n'?
        |   '\n'
        )
        -> skip
    ;

BlockComment
    :   '/*' .*? '*/'
        -> skip
    ;

LineComment
    :   '//' ~[\r\n]*
        -> skip
    ;