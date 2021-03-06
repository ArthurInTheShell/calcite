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


/**
 * A {@code SqlColumnAttributeGeneratedMinValue} represents the MINVALUE
 * option of a GENERATED column attribute.
 */
public class SqlColumnAttributeGeneratedMinValue extends
    SqlColumnAttributeGeneratedOption {

  public final SqlLiteral min;
  public final boolean none;

  /**
   * Creates a {@code SqlColumnAttributeGeneratedMinValue}.
   *
   * @param min     The amount specified in the MINVALUE attribute. This
   *                  parameter should only be null when {@code none} is true.
   * @param none    Whether NO MINVALUE was specified.
   */
  public SqlColumnAttributeGeneratedMinValue(SqlLiteral min, boolean none) {
    this.min = min;
    this.none = none;
  }

  @Override public void unparse(SqlWriter writer,
      int leftPrec, int rightPrec) {
    if (none) {
      writer.keyword("NO MINVALUE");
    } else if (min != null) {
      writer.keyword("MINVALUE");
      min.unparse(writer, leftPrec, rightPrec);
    }
  }
}
