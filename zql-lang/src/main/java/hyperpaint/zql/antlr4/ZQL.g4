grammar ZQL;

@header {
package hyperpaint.zql.antlr4;
}

PATH: 'path';
DATA: 'data';

EQUALS: '=' | '==';
NOT_EQUALS: '<>' | '!=';
LIKE: 'like' | '=~';
NOT_LIKE: 'not like' | '!~';

TEXT: [']~[']*['] | ["]~["]*["];
NUMBER: [1-9]([0-9]+)?('.'[0-9]+)?;

zql
    :   statement ';' EOF
    |   statement EOF
    ;

statement
    :   'select' expressions 'from' znodes # SelectFrom
    |   'select' expressions 'from' znodes 'where' conditions # SelectFromWhere
    |   'select' expressions 'from' znodes 'where' conditions 'group' 'by' groups # SelectFromWhereGroupBy
//    |   'select' expressions 'from' znodes 'where' conditions 'group' 'by' groups 'having' ... # SelectFromWhereGroupByHaving
    |   'select' expressions 'from' znodes 'group' 'by' groups # SelectFromGroupBy
//    |   'select' expressions 'from' znodes 'group' 'by' groups 'having' ... # SelectFromGroupByHaving
    ;

expressions
    :   expressions ',' expressions # ExpressionsCommaExpressions
    |   expression # ExpressionsBase
    ;

expression
    :   expression 'as' TEXT # ExpressionAlias
    |   'count' '(' expression ')' # ExpressionCount
    |   'sum' '(' expression ')' # ExpressionSum
    |   'avg' '(' expression ')' # ExpressionAvg
    |   'min' '(' expression ')' # ExpressionMin
    |   'max' '(' expression ')' # ExpressionMax
    |   'json' '(' expression ',' expression ')' # ExpressionJson
    |   PATH # ExpressionPath
    |   DATA # ExpressionData
    |   TEXT # ExpressionText
    |   NUMBER # ExpressionNumber
    ;

znodes
    :   znodes ',' znodes # ZnodesCommaZnodes
    |   'list' '(' znode ')' # ZnodesList
    |   znode # ZnodesBase
    ;

znode
    :   TEXT # ZnodePath
    ;

conditions
    :   conditions 'and' conditions # ConditionsAndConditions
    |   conditions 'or' conditions # ConditionsOrConditions
    |   '(' conditions ')' # ConditionsInBrackets
    |   condition # ConditionsBase
    ;

condition
    :   expression EQUALS expression # ConditionExpressionEqualsExpression
    |   expression NOT_EQUALS expression # ConditionExpressionNotEqualsExpression
    |   expression LIKE expression # ConditionExpressionLikeExpression
    |   expression NOT_LIKE expression # ConditionExpressionNotLikeExpression
    ;

groups
    :   groups ',' groups # GroupsCommaGroups
    |   group # GroupsBase
    ;

group
    :   expression # GroupExpression
    ;

CHAR_SEQUENCE: [A-Za-z0-9]+;

LINE_COMMENT: '--' ~[\r\n]* -> skip;
MULTI_LINE_COMMENT: '/*' .*? '*/' -> skip;
WHITESPACE: [ \t\r\n] -> skip;
