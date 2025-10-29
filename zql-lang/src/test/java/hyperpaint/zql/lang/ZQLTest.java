package hyperpaint.zql.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZQLTest {
    private static void queryEqualsParseAndCombine(String query) {
        assertDoesNotThrow(() -> assertEquals(query, ZQL.parse(query).text()));
    }

    @Test
    void selectTextSemicolon() {
        queryEqualsParseAndCombine("select \"text\";");
    }

    @Test
    void selectNumberSemicolon() {
        queryEqualsParseAndCombine("select 1;");
    }

    @Test
    void selectIdentifierSemicolon() {
        queryEqualsParseAndCombine("select id;");
    }

    @Test
    void selectCombinedSemicolon() {
        queryEqualsParseAndCombine("select \"text1\", 1, id1, \"text2\", 2, id2, \"text3\" as text, 3 as number, id3 as identifier, select as select;");
    }

    @Test
    void selectCombinedFromSemicolon() {
        queryEqualsParseAndCombine("select \"text1\", 1, id1, \"text2\", 2, id2, \"text3\" as text, 3 as number, id3 as identifier, select as select from /table;");
    }

    @Test
    void selectCombinedFromCombinedSemicolon() {
        queryEqualsParseAndCombine("select \"text1\", 1, id1, \"text2\", 2, id2, \"text3\" as text, 3 as number, id3 as identifier, select as select from /table, /table2, ls//table3, ls/ls/ls//table4;");
    }

    @Test
    void selectCombinedFromCombinedWhereCombinedSemicolon() {
        queryEqualsParseAndCombine("select \"text1\", 1, id1, \"text2\", 2, id2, \"text3\" as text, 3 as number, id3 as identifier, select as select from /table, /table2, ls//table3, ls/ls/ls//table4 where a == b and c != d or e =~ f and g != h;");
    }

    @Test
    void selectCombinedFromCombinedWhereCombinedByCombinedSemicolon() {
        queryEqualsParseAndCombine("select \"text1\", 1, id1, \"text2\", 2, id2, \"text3\" as text, 3 as number, id3 as identifier, select as select from /table, /table2, ls//table3, ls/ls/ls//table4 where a == b and c != d or e =~ f and g != h group by a, b, c, d;");
    }

    @Test
    void selectCombinedFromCombinedWhereCombinedByCombinedHavingCombinedSemicolon() {
        queryEqualsParseAndCombine("select \"text1\", 1, id1, \"text2\", 2, id2, \"text3\" as text, 3 as number, id3 as identifier, select as select from /table, /table2, ls//table3, ls/ls/ls//table4 where a == b and c != d or e =~ f and g != h group by a, b, c, d having count(a) == 0 and sum(b) == 1;");
    }

    @Test
    void selectCombinedFromCombinedWhereCombinedByCombinedHavingCombinedOrderByCombinedSemicolon() {
        queryEqualsParseAndCombine("select \"text1\", 1, id1, \"text2\", 2, id2, \"text3\" as text, 3 as number, id3 as identifier, select as select from /table, /table2, ls//table3, ls/ls/ls//table4 where a == b and c != d or e =~ f and g != h group by a, b, c, d having count(a) == 0 and sum(b) == 1 order by 1 asc, 2 desc;");
    }
}
