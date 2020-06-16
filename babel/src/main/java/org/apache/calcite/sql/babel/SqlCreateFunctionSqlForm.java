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

import org.apache.calcite.sql.SqlCreate;
import org.apache.calcite.sql.SqlDataTypeSpec;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSpecialOperator;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.util.ImmutableNullableList;

import java.util.List;

public class SqlCreateFunctionSqlForm extends SqlCreate {
  public final SqlIdentifier functionName;
  public final SqlIdentifier specificFunctionName;
  public List<SqlIdentifier> fieldNames;
  public List<SqlDataTypeSpec> fieldTypes;
  public final SqlDataTypeSpec returnsDataType;
  public final boolean isDeterministic;
  public final boolean canRunOnNullInput;
  public final boolean hasSqlSecurityDefiner;
  public final int typeInt;
  public final SqlNode returnExpression;

  private static final SqlSpecialOperator OPERATOR =
      new SqlSpecialOperator("CREATE FUNCTION", SqlKind.CREATE_FUNCTION);

  /** Creates a SqlCreateFunctionSqlForm.
   * @param pos position
   * @param replace if "or replace" token occurred
   * @param functionName the name of the function
   * @param specificFunctionName an optional specific functionName
   * @param returnsDataType return type of the function
   * @param isDeterministic if "deterministic" is specified
   * @param canRunOnNullInput if "called on null input" or "returns null on null input"
   *                          is specified
   * @param hasSqlSecurityDefiner if "sql security definer" is specified
   * @param typeInt integer value after inline type
   * @param returnExpression the expression that is returned
   * */
  public SqlCreateFunctionSqlForm(final SqlParserPos pos, final boolean replace,
      final SqlIdentifier functionName,
      final SqlIdentifier specificFunctionName, final List<SqlIdentifier> fieldNames,
      final List<SqlDataTypeSpec> fieldTypes,
      final SqlDataTypeSpec returnsDataType, final boolean isDeterministic,
      final boolean canRunOnNullInput, final boolean hasSqlSecurityDefiner,
      final int typeInt, final SqlNode returnExpression) {

    super(OPERATOR, pos, replace, false);
    this.functionName = functionName;
    this.fieldNames = fieldNames;
    this.fieldTypes = fieldTypes;
    this.specificFunctionName = specificFunctionName;
    this.returnsDataType = returnsDataType;
    this.isDeterministic = isDeterministic;
    this.canRunOnNullInput = canRunOnNullInput;
    this.hasSqlSecurityDefiner = hasSqlSecurityDefiner;
    this.typeInt = typeInt;
    this.returnExpression = returnExpression;
  }

  @Override public List<SqlNode> getOperandList() {
    return ImmutableNullableList.of(functionName, specificFunctionName, returnsDataType,
        returnExpression);
  }

  @Override public void unparse(SqlWriter writer, int leftPrec, int rightPrec) {
    writer.keyword("CREATE FUNCTION");
    functionName.unparse(writer, 0,  0);
//    writer.print("(");
//    if (fieldNames.size() != 0){
    SqlWriter.Frame frame = writer.startList("(", ")");
    for (int i = 0; i < fieldNames.size(); i++) {
      writer.sep(",", false);
      fieldNames.get(i).unparse(writer, 0, 0);
      fieldTypes.get(i).unparse(writer, 0, 0);
    }
//    }
    writer.print(") ");
    writer.keyword("RETURNS");
    returnsDataType.unparse(writer, 0, 0);
    writer.keyword("LANGUAGE SQL COLLATION INVOKER INLINE TYPE");
    writer.print(typeInt + " ");
    writer.keyword("RETURN");
    returnExpression.unparse(writer, 0, 0);
  }
}