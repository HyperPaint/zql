package hyperpaint.zql.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZQLLangTest {
    private static void queryEqualsParseAndCombine(String query) {
        assertDoesNotThrow(() -> assertEquals(query, ZQL.parse(query).toZql()));
    }

    @Test
    void select1() {
        queryEqualsParseAndCombine("select 1, \"text2\", value, key as \"k\", json_path(\"json\", \"path\");");
    }

    @Test
    void select1From() {
        queryEqualsParseAndCombine("select 1, \"text2\", value, key as \"k\", json_path(\"json\", \"path\") from /, /path/to/node, ls/, ls/path/to/node, ls/ls/, ls/ls/path/to/node;");
    }

    @Test
    void select1Where() {
        queryEqualsParseAndCombine("select 1, \"text2\", value, key as \"k\", json_path(\"json\", \"path\") where (1 == 1 and \"data\" =~ \"data\" or 2 != 2 and \"data\" !~ \"data\");");
    }

    @Test
    void select1FromWhere() {
        queryEqualsParseAndCombine("select 1, \"text2\", value, key as \"k\", json_path(\"json\", \"path\") from /, /path/to/node, ls/, ls/path/to/node, ls/ls/, ls/ls/path/to/node where (1 == 1 and \"data\" =~ \"data\" or 2 != 2 and \"data\" !~ \"data\");");
    }

    @Test
    void select2() {
        queryEqualsParseAndCombine("select 1, \"text2\", value, key as \"k\", json_path(\"json\", \"path\"), count(data), sum(data), avg(data), min(data), max(data) group by 1, \"text2\", value, key, json_path(\"json\", \"path\");");
    }

    @Test
    void select2From() {
        queryEqualsParseAndCombine("select 1, \"text2\", value, key as \"k\", json_path(\"json\", \"path\"), count(data), sum(data), avg(data), min(data), max(data) from /, /path/to/node, ls/, ls/path/to/node, ls/ls/, ls/ls/path/to/node group by 1, \"text2\", value, key, json_path(\"json\", \"path\");");
    }

    @Test
    void select2Where() {
        queryEqualsParseAndCombine("select 1, \"text2\", value, key as \"k\", json_path(\"json\", \"path\"), count(data), sum(data), avg(data), min(data), max(data) where (1 == 1 and \"data\" =~ \"data\" or 2 != 2 and \"data\" !~ \"data\") group by 1, \"text2\", value, key, json_path(\"json\", \"path\");");
    }

    @Test
    void select2FromWhere() {
        queryEqualsParseAndCombine("select 1, \"text2\", value, key as \"k\", json_path(\"json\", \"path\"), count(data), sum(data), avg(data), min(data), max(data) from /, /path/to/node, ls/, ls/path/to/node, ls/ls/, ls/ls/path/to/node where (1 == 1 and \"data\" =~ \"data\" or 2 != 2 and \"data\" !~ \"data\") group by 1, \"text2\", value, key, json_path(\"json\", \"path\");");
    }

    @Test
    void select2Having() {
        queryEqualsParseAndCombine("select 1, \"text2\", value, key as \"k\", json_path(\"json\", \"path\"), count(data), sum(data), avg(data), min(data), max(data) group by 1, \"text2\", value, key, json_path(\"json\", \"path\") having count(data) == 1;");
    }

    @Test
    void select2FromHaving() {
        queryEqualsParseAndCombine("select 1, \"text2\", value, key as \"k\", json_path(\"json\", \"path\"), count(data), sum(data), avg(data), min(data), max(data) from /, /path/to/node, ls/, ls/path/to/node, ls/ls/, ls/ls/path/to/node group by 1, \"text2\", value, key, json_path(\"json\", \"path\") having count(data) == 1;");
    }

    @Test
    void select2WhereHaving() {
        queryEqualsParseAndCombine("select 1, \"text2\", value, key as \"k\", json_path(\"json\", \"path\"), count(data), sum(data), avg(data), min(data), max(data) where (1 == 1 and \"data\" =~ \"data\" or 2 != 2 and \"data\" !~ \"data\") group by 1, \"text2\", value, key, json_path(\"json\", \"path\") having count(data) == 1;");
    }

    @Test
    void select2FromWhereHaving() {
        queryEqualsParseAndCombine("select 1, \"text2\", value, key as \"k\", json_path(\"json\", \"path\"), count(data), sum(data), avg(data), min(data), max(data) from /, /path/to/node, ls/, ls/path/to/node, ls/ls/, ls/ls/path/to/node where (1 == 1 and \"data\" =~ \"data\" or 2 != 2 and \"data\" !~ \"data\") group by 1, \"text2\", value, key, json_path(\"json\", \"path\") having count(data) == 1;");
    }
}
