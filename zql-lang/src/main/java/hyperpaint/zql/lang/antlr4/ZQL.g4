grammar ZQL;

@header {
package hyperpaint.zql.lang.antlr4;
}

EQUALS: '=' | '==';
NOT_EQUALS: '<>' | '!=';

LIKE: LIKE_WORD | '=~';
NOT_LIKE: NOT_LIKE_WORD | '!~';

LIKE_WORD: 'like';
NOT_LIKE_WORD: 'not'[ ]+'like';

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
JSON_PATH_WORD: 'json_path';

AND_WORD: 'and';
OR_WORD: 'or';

LIST_WORD: 'list' | 'ls';

STRING: [']~[']*['] | ["]~["]*["];
NUMBER: ('-')? ([0] | [1-9][0-9]*) ('.'[0-9]+)?;
IDENTIFIER: [A-Za-z\-_.\\]+[A-Za-z0-9\-_.\\]*;

zql
    :   statement EOF
    ;

identifier
    :   IDENTIFIER
    // Keywords
    |   SELECT_WORD
    |   FROM_WORD
    |   WHERE_WORD
    |   HAVING_WORD
    |   ASC_WORD
    |   DESC_WORD
    |   AS_WORD
    |   JSON_PATH_WORD
    |   COUNT_WORD
    |   SUM_WORD
    |   AVG_WORD
    |   MIN_WORD
    |   MAX_WORD
    |   AND_WORD
    |   OR_WORD
    |   LIST_WORD
    |   LIKE_WORD
    ;

statement
    :   select ';'?
    ;

select
    :   SELECT_WORD selectExpressions?
        (FROM_WORD fromZnodes)?
        (WHERE_WORD whereConditions)?
        (GROUP_BY_WORD groupByExpressions)?
        (HAVING_WORD havingConditions)?
        (ORDER_BY_WORD orderByExpressions)?
    ;

selectExpressions
    :   selectExpressions ',' selectExpressions # SelectExpressionsComma
    |   selectExpression # SelectExpressionsBase
    ;

selectExpression
    :   expressionAlias
    |   expressionJsonPath
    |   expressionCount
    |   expressionSum
    |   expressionAvg
    |   expressionMin
    |   expressionMax
    |   expressionNumber
    |   expressionString
    |   expressionIdentifier
    ;

fromZnodes
    :   fromZnodes ',' fromZnodes # ZnodesComma
    |   znode # ZnodesBase
    ;

znode
    :   znodeList
    |   znodePath
    |   znodeRoot
    ;

znodeList
    :   LIST_WORD '/' znodeList
    |   LIST_WORD znodePath
    |   LIST_WORD znodeRoot
    ;

znodePath
    :   znodePath '/' identifier
    |   znodeRoot identifier
    ;

znodeRoot
    :   '/'
    ;


whereConditions
    :   whereConditions AND_WORD whereConditions # WhereConditionsAnd
    |   whereConditions OR_WORD whereConditions # WhereConditionsOr
    |   '(' whereConditions ')' # WhereConditionsBrackets
    |   whereCondition # WhereConditionsBase
    ;

whereCondition
    :   whereExpressionLeft EQUALS whereExpressionRight # WhereConditionEquals
    |   whereExpressionLeft NOT_EQUALS whereExpressionRight # WhereConditionNotEquals
    |   whereExpressionLeft LIKE whereExpressionRight # WhereConditionLike
    |   whereExpressionLeft NOT_LIKE whereExpressionRight # WhereConditionNotLike
    ;

whereExpressionLeft
    :   expressionJsonPath
    |   expressionNumber
    |   expressionString
    |   expressionIdentifier
    ;

whereExpressionRight
    :   expressionJsonPath
    |   expressionNumber
    |   expressionString
    |   expressionIdentifier
    ;

groupByExpressions
    :   groupByExpressions ',' groupByExpressions # GroupByExpressionsComma
    |   groupByExpression # GroupByExpressionsBase
    ;

groupByExpression
    :   expressionJsonPath
    |   expressionNumber
    |   expressionString
    |   expressionIdentifier
    ;

havingConditions
    :   havingConditions AND_WORD havingConditions # HavingConditionsAnd
    |   havingConditions OR_WORD havingConditions # HavingConditionsOr
    |   '(' havingConditions ')' # HavingConditionsBrackets
    |   havingCondition # HavingConditionsBase
    ;

havingCondition
    :   havingExpressionLeft EQUALS havingExpressionRight # HavingConditionEquals
    |   havingExpressionLeft NOT_EQUALS havingExpressionRight # HavingConditionNotEquals
    |   havingExpressionLeft LIKE havingExpressionRight # HavingConditionLike
    |   havingExpressionLeft NOT_LIKE havingExpressionRight # HavingConditionNotLike
    ;

havingExpressionLeft
    :   expressionCount
    |   expressionSum
    |   expressionAvg
    |   expressionMin
    |   expressionMax
    ;

havingExpressionRight
    :   expressionCount
    |   expressionSum
    |   expressionAvg
    |   expressionMin
    |   expressionMax
    |   expressionNumber
    |   expressionString
    |   expressionIdentifier
    ;

orderByExpressions
    :   orderByExpressions ',' orderByExpressions # OrderByExpressionsComma
    |   orderByExpression # OrderByExpressionsBase
    ;

orderByExpression
    :   expressionJsonPath (ASC_WORD | DESC_WORD)?
    |   expressionNumber (ASC_WORD | DESC_WORD)?
    |   expressionString (ASC_WORD | DESC_WORD)?
    |   expressionIdentifier (ASC_WORD | DESC_WORD)?
    ;

expressionAlias
    :   expressionJsonPath AS_WORD? (expressionString | expressionIdentifier)
    |   expressionCount AS_WORD? (expressionString | expressionIdentifier)
    |   expressionSum AS_WORD? (expressionString | expressionIdentifier)
    |   expressionAvg AS_WORD? (expressionString | expressionIdentifier)
    |   expressionMin AS_WORD? (expressionString | expressionIdentifier)
    |   expressionMax AS_WORD? (expressionString | expressionIdentifier)
    |   expressionNumber AS_WORD? (expressionString | expressionIdentifier)
    |   expressionString AS_WORD? (expressionString | expressionIdentifier)
    |   expressionIdentifier AS_WORD? (expressionString | expressionIdentifier)
    ;

expressionJsonPath
    :   JSON_PATH_WORD '(' (expressionJsonPath | expressionString | expressionIdentifier) ',' (expressionJsonPath | expressionString | expressionIdentifier) ')'
    ;

expressionCount
    :   COUNT_WORD '(' expressionJsonPath ')'
    |   COUNT_WORD '(' expressionNumber ')'
    |   COUNT_WORD '(' expressionString ')'
    |   COUNT_WORD '(' expressionIdentifier ')'
    ;

expressionSum
    :   SUM_WORD '(' expressionJsonPath ')'
    |   SUM_WORD '(' expressionNumber ')'
    |   SUM_WORD '(' expressionString ')'
    |   SUM_WORD '(' expressionIdentifier ')'
    ;

expressionAvg
    :   AVG_WORD '(' expressionJsonPath ')'
    |   AVG_WORD '(' expressionNumber ')'
    |   AVG_WORD '(' expressionString ')'
    |   AVG_WORD '(' expressionIdentifier ')'
    ;

expressionMin
    :   MIN_WORD '(' expressionJsonPath ')'
    |   MIN_WORD '(' expressionNumber ')'
    |   MIN_WORD '(' expressionString ')'
    |   MIN_WORD '(' expressionIdentifier ')'
    ;

expressionMax
    :   MAX_WORD '(' expressionJsonPath ')'
    |   MAX_WORD '(' expressionNumber ')'
    |   MAX_WORD '(' expressionString ')'
    |   MAX_WORD '(' expressionIdentifier ')'
    ;

expressionNumber
    :   NUMBER
    ;

expressionString
    :   STRING
    ;

expressionIdentifier
    :   identifier
    ;

LINE_COMMENT: '--' ~[\r\n]* -> skip;
MULTI_LINE_COMMENT: '/*' .*? '*/' -> skip;
WHITESPACE: [ \t\r\n] -> skip;
