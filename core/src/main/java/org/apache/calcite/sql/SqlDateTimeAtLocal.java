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
package org.apache.calcite.sql;

import org.apache.calcite.jdbc.CalcitePrepare;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.util.ImmutableNullableList;

import java.util.List;

/**
 * A {@code SqlDateTimeAtLocal} is an AST node that describes
 * the date time expression of At Local.
 */
public class SqlDateTimeAtLocal extends SqlCall implements SqlExecutableStatement {
  public static final SqlSpecialOperator OPERATOR =
      new SqlSpecialOperator("AT LOCAL", SqlKind.OTHER);

  private final SqlNode dateTimePrimary;

  /**
   * Creates a {@code SqlDateTimeAtLocal}.
   *
   * @param pos  Parser position, must not be null
   * @param dateTimePrimary  SqlNode, contains the DateTimePrimary to be transformed
   */
  public SqlDateTimeAtLocal(
      SqlParserPos pos, SqlNode dateTimePrimary) {
    super(pos);
    this.dateTimePrimary = dateTimePrimary;
  }

  @Override public SqlOperator getOperator() {
    return OPERATOR;
  }

  @Override public List<SqlNode> getOperandList() {
    return ImmutableNullableList.of(dateTimePrimary);
  }

  @Override public void unparse(SqlWriter writer, int leftPrec,
      int rightPrec) {
    dateTimePrimary.unparse(writer, leftPrec, rightPrec);
    writer.keyword("AT LOCAL");
  }

  // Intentionally left empty.
  @Override public void execute(CalcitePrepare.Context context) {}
}
