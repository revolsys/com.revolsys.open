/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.server.core.uri.parser;

import java.util.Collection;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.core.edm.Edm;
import org.apache.olingo.server.api.uri.queryoption.AliasQueryOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.core.uri.queryoption.FilterOptionImpl;
import org.apache.olingo.server.core.uri.validator.UriValidationException;

public class FilterParser {

  public static Expression parseExpression(final Edm edm, final UriTokenizer tokenizer,
    final EdmType referencedType, final Collection<String> crossjoinEntitySetNames,
    final Map<String, AliasQueryOption> aliases) throws UriParserException, UriValidationException {
    final Expression filterExpression = new ExpressionParser(edm).parse(tokenizer, referencedType,
      crossjoinEntitySetNames, aliases);
    final EdmType type = ExpressionParser.getType(filterExpression);
    if (type == null || type.equals(EdmPrimitiveTypeKind.Boolean.getInstance())) {
      return filterExpression;
    } else {
      throw new UriParserSemanticException("Filter expressions must be boolean.",
        UriParserSemanticException.MessageKeys.TYPES_NOT_COMPATIBLE, "Edm.Boolean",
        type.getFullQualifiedName().getFullQualifiedNameAsString());
    }
  }

  private final Edm edm;

  public FilterParser(final Edm edm) {
    this.edm = edm;
  }

  public FilterOption parse(final UriTokenizer tokenizer, final EdmType referencedType,
    final Collection<String> crossjoinEntitySetNames, final Map<String, AliasQueryOption> aliases)
    throws UriParserException, UriValidationException {
    final Expression filterExpression = parseExpression(this.edm, tokenizer, referencedType,
      crossjoinEntitySetNames, aliases);
    return new FilterOptionImpl().setExpression(filterExpression);
  }
}
