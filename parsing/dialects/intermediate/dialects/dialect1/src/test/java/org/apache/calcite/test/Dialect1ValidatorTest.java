/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.test;

import org.apache.calcite.sql.SqlBeginEndCall;
import org.apache.calcite.sql.SqlConditionalStmt;
import org.apache.calcite.sql.SqlConditionalStmtListPair;
import org.apache.calcite.sql.SqlCreateProcedure;
import org.apache.calcite.sql.SqlLeaveStmt;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlWhileStmt;
import org.apache.calcite.sql.parser.dialect1.Dialect1ParserImpl;
import org.apache.calcite.sql.test.SqlTestFactory;
import org.apache.calcite.sql.test.SqlTester;
import org.apache.calcite.sql.test.SqlValidatorTester;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.sql.validate.SqlValidator;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

public class Dialect1ValidatorTest extends SqlValidatorTestCase {

  @Override public SqlTester getTester() {
    return new SqlValidatorTester(
        SqlTestFactory.INSTANCE
            .with("parserFactory", Dialect1ParserImpl.FACTORY)
            .with("conformance", SqlConformanceEnum.LENIENT)
            .with("identifierExpansion", true)
            .with("allowUnknownTables", true));
  }

  public SqlNode parseAndValidate(String sql) {
    SqlValidator validator = getTester().getValidator();
    return getTester().parseAndValidate(validator, sql);
  }

  @Test public void testSel() {
    String sql = "sel a from abc";
    String expected = "SELECT `ABC`.`A`\n"
        + "FROM `ABC` AS `ABC`";
    sql(sql).rewritesTo(expected);
  }

  @Test public void testFirstValue() {
    String sql = "SELECT FIRST_VALUE (foo) OVER (PARTITION BY (foo)) FROM bar";
    String expected = "SELECT FIRST_VALUE(`BAR`.`FOO`) OVER (PARTITION BY "
        + "`BAR`.`FOO`)\n"
        + "FROM `BAR` AS `BAR`";
    sql(sql).rewritesTo(expected);
  }

  @Test public void testLastValue() {
    String sql = "SELECT LAST_VALUE (foo) OVER (PARTITION BY (foo)) FROM bar";
    String expected = "SELECT LAST_VALUE(`BAR`.`FOO`) OVER (PARTITION BY "
        + "`BAR`.`FOO`)\n"
        + "FROM `BAR` AS `BAR`";
    sql(sql).rewritesTo(expected);
  }

  @Test public void testFirstValueIgnoreNulls() {
    final String sql = "SELECT FIRST_VALUE (foo IGNORE NULLS) OVER"
        + " (PARTITION BY (foo)) FROM bar";
    final String expected = "SELECT FIRST_VALUE(`BAR`.`FOO` IGNORE NULLS)"
        + " OVER (PARTITION BY `BAR`.`FOO`)\n"
        + "FROM `BAR` AS `BAR`";
    sql(sql).rewritesTo(expected);
  }

  @Test public void testFirstValueRespectNulls() {
    final String sql = "SELECT FIRST_VALUE (foo RESPECT NULLS) OVER"
        + " (PARTITION BY (foo)) FROM bar";
    final String expected = "SELECT FIRST_VALUE(`BAR`.`FOO` RESPECT NULLS)"
        + " OVER (PARTITION BY `BAR`.`FOO`)\n"
        + "FROM `BAR` AS `BAR`";
    sql(sql).rewritesTo(expected);
  }

  // The sql() call removes "^" symbols in the query, so this test calls
  // checkRewrite() which does not remove the caret operator.
  @Test public void testCaretNegation() {
    String sql = "select a from abc where ^a = 1";
    String expected = "SELECT `ABC`.`A`\n"
        + "FROM `ABC` AS `ABC`\n"
        + "WHERE ^`ABC`.`A` = 1";
    getTester().checkRewrite(sql, expected);
  }

  @Test public void testHostVariable() {
    String sql = "select :a from abc";
    String expected = "SELECT :A\n"
        + "FROM `ABC` AS `ABC`";
    sql(sql).rewritesTo(expected);
  }

  @Test public void testInlineModOperatorWithExpressions() {
    String sql = "select (select a from abc) mod (select d from def) from ghi";
    String expected = "SELECT MOD(((SELECT `ABC`.`A`\n"
        + "FROM `ABC` AS `ABC`)), ((SELECT `DEF`.`D`\n"
        + "FROM `DEF` AS `DEF`)))\n"
        + "FROM `GHI` AS `GHI`";
    sql(sql).rewritesTo(expected);
  }

  @Test public void testCreateProcedureBeginEndLabel() {
    String sql = "create procedure foo()\n"
        + "label1: begin\n"
        + "leave label1;\n"
        + "end";
    SqlCreateProcedure node = (SqlCreateProcedure) parseAndValidate(sql);
    SqlBeginEndCall beginEnd = (SqlBeginEndCall) node.statement;
    SqlLeaveStmt leaveStmt = (SqlLeaveStmt) beginEnd.statements.get(0);
    assertThat(beginEnd, sameInstance(leaveStmt.labeledBlock));
  }

  @Test public void testCreateProcedureBeginEndNestedOuterLabel() {
    String sql = "create procedure foo()\n"
        + "label1: begin\n"
        + "label2: begin\n"
        + "leave label1;\n"
        + "end;\n"
        + "end";
    SqlCreateProcedure node = (SqlCreateProcedure) parseAndValidate(sql);
    SqlBeginEndCall beginEnd = (SqlBeginEndCall) node.statement;
    SqlBeginEndCall nestedBeginEnd
        = (SqlBeginEndCall) beginEnd.statements.get(0);
    SqlLeaveStmt leaveStmt = (SqlLeaveStmt) nestedBeginEnd.statements.get(0);
    assertThat(beginEnd, sameInstance(leaveStmt.labeledBlock));
  }

  @Test public void testCreateProcedureBeginEndNestedInnerLabel() {
    String sql = "create procedure foo()\n"
        + "label1: begin\n"
        + "label2: begin\n"
        + "leave label2;\n"
        + "end;\n"
        + "end";
    SqlCreateProcedure node = (SqlCreateProcedure) parseAndValidate(sql);
    SqlBeginEndCall beginEnd = (SqlBeginEndCall) node.statement;
    SqlBeginEndCall nestedBeginEnd
        = (SqlBeginEndCall) beginEnd.statements.get(0);
    SqlLeaveStmt leaveStmt = (SqlLeaveStmt) nestedBeginEnd.statements.get(0);
    assertThat(nestedBeginEnd, sameInstance(leaveStmt.labeledBlock));
  }

  @Test public void testCreateProcedureBeginEndNestedSameNameLabel() {
    String sql = "create procedure foo()\n"
        + "label1: begin\n"
        + "label1: begin\n"
        + "leave label1;\n"
        + "end;\n"
        + "end";
    SqlCreateProcedure node = (SqlCreateProcedure) parseAndValidate(sql);
    SqlBeginEndCall beginEnd = (SqlBeginEndCall) node.statement;
    SqlBeginEndCall nestedBeginEnd
        = (SqlBeginEndCall) beginEnd.statements.get(0);
    SqlLeaveStmt leaveStmt = (SqlLeaveStmt) nestedBeginEnd.statements.get(0);
    assertThat(nestedBeginEnd, sameInstance(leaveStmt.labeledBlock));
  }

  @Test public void testCreateProcedureBeginEndSameLevelLabel() {
    String sql = "create procedure foo()\n"
        + "label1: begin\n"
        + "label1: begin\n"
        + "select a from abc;\n"
        + "end;\n"
        + "label2: begin\n"
        + "leave label1;\n"
        + "end;\n"
        + "end";
    SqlCreateProcedure node = (SqlCreateProcedure) parseAndValidate(sql);
    SqlBeginEndCall beginEnd = (SqlBeginEndCall) node.statement;
    SqlBeginEndCall nestedBeginEnd
        = (SqlBeginEndCall) beginEnd.statements.get(1);
    SqlLeaveStmt leaveStmt = (SqlLeaveStmt) nestedBeginEnd.statements.get(0);
    assertThat(beginEnd, sameInstance(leaveStmt.labeledBlock));
  }

  @Test public void testCreateProcedureBeginEndNullLabel() {
    String sql = "create procedure foo()\n"
        + "begin\n"
        + "leave label1;\n"
        + "end";
    SqlCreateProcedure node = (SqlCreateProcedure) parseAndValidate(sql);
    SqlBeginEndCall beginEnd = (SqlBeginEndCall) node.statement;
    SqlLeaveStmt leaveStmt = (SqlLeaveStmt) beginEnd.statements.get(0);
    assertThat(leaveStmt.labeledBlock, nullValue());
  }

  @Test public void testCreateProcedureIterationStatementLabel() {
    String sql = "create procedure foo()\n"
        + "begin\n"
        + "label1: while bar = 1 do\n"
        + "leave label1;\n"
        + "end while label1;\n"
        + "end";
    SqlCreateProcedure node = (SqlCreateProcedure) parseAndValidate(sql);
    SqlBeginEndCall beginEnd = (SqlBeginEndCall) node.statement;
    SqlWhileStmt whileLoop = (SqlWhileStmt) beginEnd.statements.get(0);
    SqlLeaveStmt leaveStmt = (SqlLeaveStmt) whileLoop.statements.get(0);
    assertThat(whileLoop, sameInstance(leaveStmt.labeledBlock));
  }

  @Test public void testCreateProcedureConditionalStatementLeaveCall() {
    String sql = "create procedure foo()\n"
        + "label1: begin\n"
        + "if a = 3 then\n"
        + "leave label1;\n"
        + "end if;\n"
        + "end";
    SqlCreateProcedure node = (SqlCreateProcedure) parseAndValidate(sql);
    SqlBeginEndCall beginEnd = (SqlBeginEndCall) node.statement;
    SqlConditionalStmt conditionalStmt
        = (SqlConditionalStmt) beginEnd.statements.get(0);
    SqlConditionalStmtListPair listPair
        = (SqlConditionalStmtListPair) conditionalStmt
        .conditionalStmtListPairs.get(0);
    SqlLeaveStmt leaveStmt = (SqlLeaveStmt) listPair.stmtList.get(0);
    assertThat(beginEnd, sameInstance(leaveStmt.labeledBlock));
  }

  @Test public void testCreateTableNoColumns() {
    String ddl = "create table foo";
    String query = "select * from foo";
    sql(ddl).ok();
    sql(query).type("RecordType() NOT NULL");
  }

  @Test public void testCreateTableSelectInteger() {
    String ddl = "create table foo(x int, y varchar)";
    String query = "select x from foo";
    sql(ddl).ok();
    sql(query).type("RecordType(INTEGER NOT NULL X) NOT NULL");
  }

  @Test public void testCreateTableSelectVarchar() {
    String ddl = "create table foo(x int, y varchar)";
    String query = "select y from foo";
    sql(ddl).ok();
    sql(query).type("RecordType(VARCHAR NOT NULL Y) NOT NULL");
  }

  @Test public void testCreateTableInsert() {
    String ddl = "create table foo(x int, y varchar)";
    String query = "insert into foo values (1, 'str')";
    sql(ddl).ok();
    sql(query).type("RecordType(INTEGER NOT NULL X, VARCHAR NOT NULL Y)"
        + " NOT NULL");
  }

  @Test public void testCreateTableSelectNonExistentColumnFails() {
    String ddl = "create table foo(x int, y varchar)";
    String query = "select ^z^ from foo";
    sql(ddl).ok();
    sql(query).fails("Column 'Z' not found in any table");
  }

  @Test public void testCreateTableInsertTooManyValuesFails() {
    String ddl = "create table foo(x int, y varchar)";
    String query = "insert into foo values (1, 'str', 1)";
    sql(ddl).ok();
    sql(query).fails("end index \\(3\\) must not be greater than size \\(2\\)");
  }
}
