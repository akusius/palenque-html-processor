package org.jsoup.select;

/**
 *
 * @author Bujdosó Ákos
 */
public class QueryParserProxy {

  public static Evaluator parse(String query) {
    return QueryParser.parse(query);
  }

  private QueryParserProxy() {
  }
}
