grammar ZQL;

@header {
package hyperpaint.zql.lang.antlr4;
}

EQUALS: '=' | '==';
NOT_EQUALS: '<>' | '!=';

LIKE_WORD: 'like';
NOT_LIKE_WORD: 'not'[ ]+'like';

LIKE: LIKE_WORD | '=~';
NOT_LIKE: NOT_LIKE_WORD | '!~';

SELECT_WORD: 'select';
FROM_WORD: 'from';
WHERE_WORD: 'where';
GROUP_BY_WORD: 'group'[ ]+'by';
HAVING_WORD: 'having';
ORDER_BY_WORD: 'order'[ ]+'by';
ASC_WORD: 'asc';
DESC_WORD: 'desc';

AS_WORD: 'as';

COUNT_WORD: 'count';
SUM_WORD: 'sum';
AVG_WORD: 'avg';
MIN_WORD: 'min';
MAX_WORD: 'max';
JSON_WORD: 'json';

AND_WORD: 'and';
OR_WORD: 'or';

LS_WORD: 'ls';

TEXT: [']~[']*['] | ["]~["]*["];
NUMBER: [1-9][0-9]*('.'[0-9]+)?;
IDENTIFIER: [A-Za-z\\\-_.]+[A-Za-z0-9\\\-_.]*;

zql
    :   statement ';' EOF
    |   statement EOF
    ;

identifier
    :   IDENTIFIER
    // Keywords
    |   LIKE_WORD
    |   SELECT_WORD
    |   FROM_WORD
    |   WHERE_WORD
    |   HAVING_WORD
    |   ASC_WORD
    |   DESC_WORD
    |   AS_WORD
    |   COUNT_WORD
    |   SUM_WORD
    |   AVG_WORD
    |   MIN_WORD
    |   MAX_WORD
    |   JSON_WORD
    |   AND_WORD
    |   OR_WORD
    |   LS_WORD
    ;

statement
    :   select
    ;

select
    :   SELECT_WORD expressions (FROM_WORD znodes)? (WHERE_WORD conditions)? (GROUP_BY_WORD expressions)? (HAVING_WORD conditions)? (ORDER_BY_WORD expression)? (ASC_WORD | DESC_WORD)?
    ;

expressions
    :   expressions ',' expressions # ExpressionsCommaExpressions
    |   expression # ExpressionsBase
    ;

expression
    :   expression AS_WORD TEXT # ExpressionAsText
    |   expression AS_WORD identifier # ExpressionAsIdentifier
    |   COUNT_WORD '(' expression ')' # ExpressionCount
    |   SUM_WORD '(' expression ')' # ExpressionSum
    |   AVG_WORD '(' expression ')' # ExpressionAvg
    |   MIN_WORD '(' expression ')' # ExpressionMin
    |   MAX_WORD '(' expression ')' # ExpressionMax
    |   JSON_WORD '(' expression ',' expression ')' # ExpressionJsonPath
    |   TEXT # ExpressionText
    |   NUMBER # ExpressionNumber
    |   identifier # ExpressionIdentifier
    ;

znodes
    :   znodes ',' znodes # ZnodesCommaZnodes
    |   znode # ZnodesBase
    |   'ls' znodes # ZnodesList
    |   'ls' '/' znodes # ZnodesList
    ;

znode
    :   ('/'identifier?)+
    ;

conditions
    :   conditions AND_WORD conditions # ConditionsAndConditions
    |   conditions OR_WORD conditions # ConditionsOrConditions
    |   '(' conditions ')' # ConditionsInBrackets
    |   condition # ConditionsBase
    ;

condition
    :   expression EQUALS expression # ConditionEquals
    |   expression NOT_EQUALS expression # ConditionNotEquals
    |   expression LIKE expression # ConditionLike
    |   expression NOT_LIKE expression # ConditionNotLike
    ;

LINE_COMMENT: '--' ~[\r\n]* -> skip;
MULTI_LINE_COMMENT: '/*' .*? '*/' -> skip;
WHITESPACE: [ \t\r\n] -> skip;
