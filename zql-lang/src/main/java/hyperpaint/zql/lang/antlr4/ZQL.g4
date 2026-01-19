grammar ZQL;

@header {
package hyperpaint.zql.lang.antlr4;
}

GREATER: '>';
GREATER_EQUALS: '>=';
LOWER: '<';
LOWER_EQUALS: '<=';

EQUALS: '=' | '==';
NOT_EQUALS: '<>' | '!=';

LIKE: LIKE_WORD | '=~';
NOT_LIKE: NOT_LIKE_WORD | '!~';

PLUS: '+';
MINUS: '-';
MULTIPLY: '*';
DIVIDE: '/';

SELECT_WORD: 'select';
FROM_WORD: 'from';
WHERE_WORD: 'where';
GROUP_BY_WORD: 'group'[ ]+'by';
HAVING_WORD: 'having';
ORDER_BY_WORD: 'order'[ ]+'by';
ASC_WORD: 'asc';
DESC_WORD: 'desc';

LIST_WORD: 'list' | 'ls';

LIKE_WORD: 'like';
NOT_LIKE_WORD: 'not'[ ]+'like';

AND_WORD: 'and';
OR_WORD: 'or';

AS_WORD: 'as';

SUBSTRING_WORD: 'substr';
JSON_PATH_WORD: 'json_path';
CONCATENATION_WORD: 'concat';

COUNT_WORD: 'count';
SUM_WORD: 'sum';
AVG_WORD: 'avg';
MIN_WORD: 'min';
MAX_WORD: 'max';

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
    |   LIST_WORD
    |   LIKE_WORD
    |   AND_WORD
    |   OR_WORD
    |   AS_WORD
    |   SUBSTRING_WORD
    |   JSON_PATH_WORD
    |   CONCATENATION_WORD
    |   COUNT_WORD
    |   SUM_WORD
    |   AVG_WORD
    |   MIN_WORD
    |   MAX_WORD
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
    // 3 ARGS
    |   expressionSubstring
    // 2 ARGS
    |   expressionJsonPath
    |   expressionConcatenation
    |   expressionArithmetical
    // 1 ARGS GROUP
    |   expressionCount
    |   expressionSum
    |   expressionAvg
    |   expressionMin
    |   expressionMax
    // PRIMITIVE
    |   expressionNumber
    |   expressionString
    |   expressionIdentifier
    ;

fromZnodes
    :   fromZnodes ',' fromZnodes # ZNodesComma
    |   znode # ZNodesBase
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
    :   havingExpressionLeft GREATER havingExpressionRight # WhereConditionGreater
    |   havingExpressionLeft GREATER_EQUALS havingExpressionRight # WhereConditionGreaterEquals
    |   havingExpressionLeft LOWER havingExpressionRight # WhereConditionLower
    |   havingExpressionLeft LOWER_EQUALS havingExpressionRight # WhereConditionLowerEquals
    |   whereExpressionLeft EQUALS whereExpressionRight # WhereConditionEquals
    |   whereExpressionLeft NOT_EQUALS whereExpressionRight # WhereConditionNotEquals
    |   whereExpressionLeft LIKE whereExpressionRight # WhereConditionLike
    |   whereExpressionLeft NOT_LIKE whereExpressionRight # WhereConditionNotLike
    ;

whereExpressionLeft
    // 3 ARGS
    :  expressionSubstring
    // 2 ARGS
    |   expressionJsonPath
    |   expressionConcatenation
    |   expressionArithmetical
    // PRIMITIVE
    |   expressionNumber
    |   expressionString
    |   expressionIdentifier
    ;

whereExpressionRight
    // 3 ARGS
    :  expressionSubstring
    // 2 ARGS
    |   expressionJsonPath
    |   expressionConcatenation
    |   expressionArithmetical
    // PRIMITIVE
    |   expressionNumber
    |   expressionString
    |   expressionIdentifier
    ;

groupByExpressions
    :   groupByExpressions ',' groupByExpressions # GroupByExpressionsComma
    |   groupByExpression # GroupByExpressionsBase
    ;

groupByExpression
    // 3 ARGS
    :  expressionSubstring
    // 2 ARGS
    |   expressionJsonPath
    |   expressionConcatenation
    |   expressionArithmetical
    // PRIMITIVE
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
    :   havingExpressionLeft GREATER havingExpressionRight # HavingConditionGreater
    |   havingExpressionLeft GREATER_EQUALS havingExpressionRight # HavingConditionGreaterEquals
    |   havingExpressionLeft LOWER havingExpressionRight # HavingConditionLower
    |   havingExpressionLeft LOWER_EQUALS havingExpressionRight # HavingConditionLowerEquals
    |   havingExpressionLeft EQUALS havingExpressionRight # HavingConditionEquals
    |   havingExpressionLeft NOT_EQUALS havingExpressionRight # HavingConditionNotEquals
    |   havingExpressionLeft LIKE havingExpressionRight # HavingConditionLike
    |   havingExpressionLeft NOT_LIKE havingExpressionRight # HavingConditionNotLike
    ;

havingExpressionLeft
    // 3 ARGS
    :   expressionSubstring
    // 2 ARGS
    |   expressionJsonPath
    |   expressionConcatenation
    |   expressionArithmetical
    // 1 ARGS GROUP
    |   expressionCount
    |   expressionSum
    |   expressionAvg
    |   expressionMin
    |   expressionMax
    // PRIMITIVE
    |   expressionNumber
    |   expressionString
    |   expressionIdentifier
    ;

havingExpressionRight
    // 3 ARGS
    :   expressionSubstring
    // 2 ARGS
    |   expressionJsonPath
    |   expressionConcatenation
    |   expressionArithmetical
    // 1 ARGS GROUP
    |   expressionCount
    |   expressionSum
    |   expressionAvg
    |   expressionMin
    |   expressionMax
    // PRIMITIVE
    |   expressionNumber
    |   expressionString
    |   expressionIdentifier
    ;

orderByExpressions
    :   orderByExpressions ',' orderByExpressions # OrderByExpressionsComma
    |   orderByExpression # OrderByExpressionsBase
    ;

orderByExpression
    // 3 ARGS
    :   expressionSubstring (ASC_WORD | DESC_WORD)?
    // 2 ARGS
    |   expressionJsonPath (ASC_WORD | DESC_WORD)?
    |   expressionConcatenation (ASC_WORD | DESC_WORD)?
    |   expressionArithmetical (ASC_WORD | DESC_WORD)?
    // 1 ARGS GROUP
    |   expressionCount (ASC_WORD | DESC_WORD)?
    |   expressionSum (ASC_WORD | DESC_WORD)?
    |   expressionAvg (ASC_WORD | DESC_WORD)?
    |   expressionMin (ASC_WORD | DESC_WORD)?
    |   expressionMax (ASC_WORD | DESC_WORD)?
    // PRIMITIVE
    |   expressionNumber (ASC_WORD | DESC_WORD)?
    |   expressionString (ASC_WORD | DESC_WORD)?
    |   expressionIdentifier (ASC_WORD | DESC_WORD)?
    ;

expressionAlias
    // 3 ARGS
    :   expressionSubstring AS_WORD? (expressionString | expressionIdentifier)
    // 2 ARGS
    |   expressionJsonPath AS_WORD? (expressionString | expressionIdentifier)
    |   expressionConcatenation AS_WORD? (expressionString | expressionIdentifier)
    |   expressionArithmetical AS_WORD? (expressionString | expressionIdentifier)
    // 1 ARGS GROUP
    |   expressionCount AS_WORD? (expressionString | expressionIdentifier)
    |   expressionSum AS_WORD? (expressionString | expressionIdentifier)
    |   expressionAvg AS_WORD? (expressionString | expressionIdentifier)
    |   expressionMin AS_WORD? (expressionString | expressionIdentifier)
    |   expressionMax AS_WORD? (expressionString | expressionIdentifier)
    // PRIMITIVE
    |   expressionNumber AS_WORD? (expressionString | expressionIdentifier)
    |   expressionString AS_WORD? (expressionString | expressionIdentifier)
    |   expressionIdentifier AS_WORD? (expressionString | expressionIdentifier)
    ;

expressionJsonPath
    :   JSON_PATH_WORD
        '(' (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            // PRIMITIVE
            | expressionString
            | expressionIdentifier
        ) ',' (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            // PRIMITIVE
            | expressionString
            | expressionIdentifier
        ) ')'
    ;

expressionConcatenation
    :   CONCATENATION_WORD
        '(' (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            | expressionArithmetical
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
        ) ',' (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            | expressionArithmetical
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
        ) ')'
    ;

expressionSubstring
    :   SUBSTRING_WORD
        '(' (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            | expressionArithmetical
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
        ) ',' (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            | expressionArithmetical
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
        ) ',' (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            | expressionArithmetical
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
        ) ')'
    ;

expressionArithmetical
    :   (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
        ) PLUS (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            | expressionArithmetical
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
        ) # ExpressionArithmeticalPlus
    |   (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
    ) MINUS (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            | expressionArithmetical
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
    ) # ExpressionArithmeticalMinus
    |   (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
    ) MULTIPLY (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            | expressionArithmetical
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
    ) # ExpressionArithmeticalMultiply
    |   (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
    ) DIVIDE (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            | expressionArithmetical
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
    ) # ExpressionArithmeticalDivide
    |   '(' expressionArithmetical ')' # ExpressionArithmeticalBrackets
    ;

expressionCount
    :   COUNT_WORD
        '(' (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            | expressionArithmetical
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
        ) ')'
    ;

expressionSum
    :   SUM_WORD '(' (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            | expressionArithmetical
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
        ) ')'
    ;

expressionAvg
    :   AVG_WORD '(' (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            | expressionArithmetical
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
        ) ')'
    ;

expressionMin
    :   MIN_WORD '(' (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            | expressionArithmetical
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
        ) ')'
    ;

expressionMax
    :   MAX_WORD '(' (
            // 3 ARGS
            expressionSubstring
            // 2 ARGS
            | expressionJsonPath
            | expressionConcatenation
            | expressionArithmetical
            // PRIMITIVE
            | expressionNumber
            | expressionString
            | expressionIdentifier
        ) ')'
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
