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
NUMBER: ('-')? ([0] | [1-9][0-9]*) ('.'[0-9]+)?;
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
    :   SELECT_WORD select_expressions (FROM_WORD znodes)? (WHERE_WORD conditions)? (GROUP_BY_WORD group_by_expressions)? (HAVING_WORD conditions)? (ORDER_BY_WORD order_by_expressions)?
    ;

select_expressions
    :   select_expressions ',' select_expressions # SelectExpressionsCommaExpressions
    |   select_expression # SelectExpressionsBase
    ;

select_expression
    :   expression_alias # SelectExpressionAlias
    |   expression_function # SelectExpressionFunction
    |   expression_primitive # SelectExpressionPrimitive
    ;

condition_expression
    :   expression_function # ConditionExpressionFunction
    |   expression_primitive # ConditionExpressionPrimitive
    ;

group_by_expressions
    :   group_by_expressions ',' group_by_expressions # GroupByExpressionsCommaGroupByExpressions
    |   group_by_expression # GroupByExpressionsBase
    ;

group_by_expression
    :   expression_function # GroupByExpressionFunction
    |   expression_primitive # GroupByExpressionPrimitive
    ;

order_by_expressions
    :   order_by_expressions ',' order_by_expressions # OrderByExpressionsCommaOrderByExpressions
    |   order_by_expression (ASC_WORD | DESC_WORD)? # OrderByExpressionsBase
    ;

order_by_expression
    :   expression_function # OrderByExpressionFunction
    |   expression_primitive # OrderByExpressionPrimitive
    ;

expression_alias
    :   (expression_function | expression_primitive) AS_WORD TEXT # ExpressionAsText
    |   (expression_function | expression_primitive) AS_WORD identifier # ExpressionAsIdentifier
    ;

expression_function
    :   COUNT_WORD '(' (expression_function | expression_primitive) ')' # ExpressionCount
    |   SUM_WORD '(' (expression_function | expression_primitive) ')' # ExpressionSum
    |   AVG_WORD '(' (expression_function | expression_primitive) ')' # ExpressionAvg
    |   MIN_WORD '(' (expression_function | expression_primitive) ')' # ExpressionMin
    |   MAX_WORD '(' (expression_function | expression_primitive) ')' # ExpressionMax
    |   JSON_WORD '(' (expression_function | expression_primitive) ',' (expression_function | expression_primitive) ')' # ExpressionJsonPath
    ;

expression_primitive
    :   TEXT # ExpressionText
    |   NUMBER # ExpressionNumber
    |   identifier # ExpressionIdentifier
    ;

znodes
    :   znodes ',' znodes # ZnodesCommaZnodes
    |   'ls' '/' znodes # ZnodesList
    |   znode # ZnodesBase
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
    :   condition_expression EQUALS condition_expression # ConditionEquals
    |   condition_expression NOT_EQUALS condition_expression # ConditionNotEquals
    |   condition_expression LIKE condition_expression # ConditionLike
    |   condition_expression NOT_LIKE condition_expression # ConditionNotLike
    ;

LINE_COMMENT: '--' ~[\r\n]* -> skip;
MULTI_LINE_COMMENT: '/*' .*? '*/' -> skip;
WHITESPACE: [ \t\r\n] -> skip;
