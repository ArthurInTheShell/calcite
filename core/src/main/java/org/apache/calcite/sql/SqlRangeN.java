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

import org.apache.calcite.sql.parser.SqlParserPos;

import java.util.List;

public class SqlRangeN extends SqlCall{

  final SqlNodeList rangeList;
  final SqlNode testIdentifier;

  public SqlRangeN(final SqlParserPos pos, final SqlNode testIdentifier
      , final SqlNodeList rangeList) {
    super(pos);
    this.testIdentifier = testIdentifier;
    this.rangeList = rangeList;
  }

  @Override public SqlOperator getOperator() {
    return null;
  }

  @Override public List<SqlNode> getOperandList() {
    return null;
  }

  @Override public void unparse(final SqlWriter writer, final int leftPrec
      , final int rightPrec) {
    writer.keyword("RANGE_N");
    SqlWriter.Frame funcCallFrame = writer.startList(SqlWriter.FrameTypeEnum.FUN_CALL
        , "(", ")");
    testIdentifier.unparse(writer, leftPrec, rightPrec);
    writer.keyword("BETWEEN");
    SqlWriter.Frame frame = writer.startList(SqlWriter.FrameTypeEnum.SIMPLE
        , "", "");
    for (SqlNode range: rangeList.getList()) {
      writer.sep(",");
      range.unparse(writer, leftPrec, rightPrec);
    }
    writer.endList(frame);
    writer.endList(funcCallFrame);
  }
}
