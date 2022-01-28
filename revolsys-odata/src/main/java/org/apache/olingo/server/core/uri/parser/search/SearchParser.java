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
package org.apache.olingo.server.core.uri.parser.search;

import java.util.Iterator;
import java.util.List;

import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.search.SearchBinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.search.SearchExpression;
import org.apache.olingo.server.api.uri.queryoption.search.SearchTerm;
import org.apache.olingo.server.core.uri.parser.search.SearchQueryToken.Token;
import org.apache.olingo.server.core.uri.queryoption.SearchOptionImpl;

/*
 * Rewritten grammar
 *
 * SearchExpr ::= ExprOR
 * ExprOR ::= ExprAnd ('OR' ExprAnd)*
 * ExprAnd ::= Term ('AND'? Term)*
 * Term ::= ('NOT')? (Word | Phrase)
 * | '(' Expr ')'
 */

public class SearchParser {

  private Iterator<SearchQueryToken> tokens;

  private SearchQueryToken token;

  private String getTokenAsString() {
    return this.token == null ? "<EOF>" : this.token.getToken().name();
  }

  private boolean isEof() {
    return this.token == null;
  }

  private boolean isTerm() {
    return isToken(SearchQueryToken.Token.NOT) || isToken(SearchQueryToken.Token.PHRASE)
      || isToken(SearchQueryToken.Token.WORD) || isToken(SearchQueryToken.Token.OPEN);
  }

  private boolean isToken(final SearchQueryToken.Token toCheckToken) {
    return this.token != null && this.token.getToken() == toCheckToken;
  }

  private void nextToken() {
    if (this.tokens.hasNext()) {
      this.token = this.tokens.next();
    } else {
      this.token = null;
    }
  }

  protected SearchExpression parse(final List<SearchQueryToken> tokens)
    throws SearchParserException {
    this.tokens = tokens.iterator();
    nextToken();
    if (this.token == null) {
      throw new SearchParserException("No search String",
        SearchParserException.MessageKeys.NO_EXPRESSION_FOUND);
    }
    final SearchExpression searchExpression = processSearchExpression();
    if (!isEof()) {
      throw new SearchParserException("Token left after end of search query parsing.",
        SearchParserException.MessageKeys.INVALID_END_OF_QUERY, getTokenAsString());
    }
    return searchExpression;
  }

  public SearchOption parse(final String searchQuery)
    throws SearchParserException, SearchTokenizerException {
    final SearchTokenizer tokenizer = new SearchTokenizer();
    SearchExpression searchExpression;
    try {
      searchExpression = parse(tokenizer.tokenize(searchQuery));
    } catch (final SearchTokenizerException e) {
      final String message = e.getMessage();
      throw new SearchParserException("Tokenizer exception with message: " + message, e,
        SearchParserException.MessageKeys.TOKENIZER_EXCEPTION, message);
    }
    final SearchOptionImpl searchOption = new SearchOptionImpl();
    searchOption.setSearchExpression(searchExpression);
    return searchOption;
  }

  private void processClose() throws SearchParserException {
    if (isToken(Token.CLOSE)) {
      nextToken();
    } else {
      throw new SearchParserException("Missing close bracket after open bracket.",
        SearchParserException.MessageKeys.MISSING_CLOSE);
    }
  }

  private SearchExpression processExprAnd() throws SearchParserException {
    SearchExpression left = processTerm();

    while (isToken(Token.AND) || isTerm()) {
      if (isToken(Token.AND)) {
        nextToken(); // Match AND
      }
      final SearchExpression right = processTerm();
      left = new SearchBinaryImpl(left, SearchBinaryOperatorKind.AND, right);
    }

    return left;
  }

  private SearchExpression processExprOr() throws SearchParserException {
    SearchExpression left = processExprAnd();

    while (isToken(Token.OR)) {
      nextToken(); // Match OR
      final SearchExpression right = processExprAnd();
      left = new SearchBinaryImpl(left, SearchBinaryOperatorKind.OR, right);
    }

    return left;
  }

  private SearchExpression processNot() throws SearchParserException {
    nextToken();
    if (isToken(Token.WORD) || isToken(Token.PHRASE)) {
      return new SearchUnaryImpl(processWordOrPhrase());
    }

    final String tokenAsString = getTokenAsString();
    throw new SearchParserException("NOT must be followed by a term not a " + tokenAsString,
      SearchParserException.MessageKeys.INVALID_NOT_OPERAND, tokenAsString);
  }

  private SearchTerm processPhrase() {
    final String literal = this.token.getLiteral();
    nextToken();
    return new SearchTermImpl(literal.substring(1, literal.length() - 1));
  }

  private SearchExpression processSearchExpression() throws SearchParserException {
    return processExprOr();
  }

  private SearchExpression processTerm() throws SearchParserException {
    if (isToken(SearchQueryToken.Token.OPEN)) {
      nextToken(); // Match OPEN
      final SearchExpression expr = processExprOr();
      processClose();

      return expr;
    } else {
      // ('NOT')? (Word | Phrase)
      if (isToken(SearchQueryToken.Token.NOT)) {
        return processNot();
      }
      return processWordOrPhrase();
    }
  }

  private SearchTerm processWord() {
    final String literal = this.token.getLiteral();
    nextToken();
    return new SearchTermImpl(literal);
  }

  private SearchTerm processWordOrPhrase() throws SearchParserException {
    if (isToken(Token.PHRASE)) {
      return processPhrase();
    } else if (isToken(Token.WORD)) {
      return processWord();
    }

    final String tokenName = getTokenAsString();
    throw new SearchParserException("Expected PHRASE||WORD found: " + tokenName,
      SearchParserException.MessageKeys.EXPECTED_DIFFERENT_TOKEN,
      Token.PHRASE.name() + "" + Token.WORD.name(), tokenName);
  }
}
