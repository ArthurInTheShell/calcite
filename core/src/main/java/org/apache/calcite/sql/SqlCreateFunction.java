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
import org.apache.calcite.util.Pair;
import org.apache.calcite.util.Util;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Parse tree for {@code CREATE FUNCTION} statement.
 */
public class SqlCreateFunction extends SqlCreate
    implements SqlExecutableStatement {
  public final SqlIdentifier name;
  private final SqlNode className;
  private final SqlNodeList usingList;

  private static final SqlSpecialOperator OPERATOR =
      new SqlSpecialOperator("CREATE FUNCTION", SqlKind.CREATE_FUNCTION);

  /** Creates a SqlCreateFunction. */
  public SqlCreateFunction(SqlParserPos pos,
      SqlCreateSpecifier createSpecifier, boolean ifNotExists,
      SqlIdentifier name, SqlNode className, SqlNodeList usingList) {
    super(OPERATOR, pos, createSpecifier, ifNotExists);
    this.name = Objects.requireNonNull(name);
    this.className = className;
    this.usingList = Objects.requireNonNull(usingList);
    Preconditions.checkArgument(usingList.size() % 2 == 0);
  }

  @Override public void unparse(SqlWriter writer, int leftPrec,
      int rightPrec) {
    writer.keyword(getCreateSpecifier().toString());
    writer.keyword("FUNCTION");
    if (ifNotExists) {
      writer.keyword("IF NOT EXISTS");
    }
    name.unparse(writer, 0, 0);
    writer.keyword("AS");
    className.unparse(writer, 0, 0);
    if (usingList.size() > 0) {
      writer.keyword("USING");
      final SqlWriter.Frame frame =
          writer.startList(SqlWriter.FrameTypeEnum.SIMPLE);
      for (Pair<SqlLiteral, SqlLiteral> using : pairs()) {
        writer.sep(",");
        using.left.unparse(writer, 0, 0); // FILE, URL or ARCHIVE
        using.right.unparse(writer, 0, 0); // e.g. 'file:foo/bar.jar'
      }
      writer.endList(frame);
    }
  }

  @Override public void execute(CalcitePrepare.Context context) {
    throw new UnsupportedOperationException("CREATE FUNCTION is not supported yet.");
  }

  @SuppressWarnings("unchecked")
  private List<Pair<SqlLiteral, SqlLiteral>> pairs() {
    return Util.pairs((List) usingList.getList());
  }

  @Override public SqlOperator getOperator() {
    return OPERATOR;
  }

  @Override public List<SqlNode> getOperandList() {
    return Arrays.asList(name, className, usingList);
  }
}
