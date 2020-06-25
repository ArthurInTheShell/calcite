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
package org.apache.calcite.sql.babel;

import org.apache.calcite.sql.SqlColumnAttribute;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.parser.SqlParserPos;

/**
 * A {@code SqlColumnAttributeDateFormat} is an AST node that gives
 * a Date column of a specific format.
 */
public class SqlColumnAttributeDateFormat extends SqlColumnAttribute {

  private final SqlNode formatString;

  /**
   * Creates a {@code SqlColumnAttributeDateFormat}.
   *
   * @param pos  Parser position, must not be null
   */
  public SqlColumnAttributeDateFormat(SqlParserPos pos, SqlNode formatString) {
    super(pos);
    this.formatString = formatString;
  }

  @Override public void unparse(final SqlWriter writer, final int leftPrec,
      final int rightPrec) {
    writer.keyword("FORMAT");
    formatString.unparse(writer, 0, 0);
  }
}
