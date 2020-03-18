package com.revolsys.swing.builder;

public abstract class AbstractUiBuilder implements UiBuilder {
  public final static String escapeHTML(final String value, final boolean escapeSpaces,
    final boolean escapeNewlines) {
    if (value == null) {
      return null;
    }

    final char[] content = new char[value.length()];
    value.getChars(0, value.length(), content, 0);

    final StringBuilder result = new StringBuilder();

    for (final char element : content) {
      switch (element) {
        case ' ':
          result.append(escapeSpaces ? "&#32;" : " ");

        break;

        // Added \n [Jon Aquino]
        case '\n':
          result.append(escapeNewlines ? "<BR>" : "\n");

        break;

        case '!':
          result.append("&#33;");

        break;

        case '"':
          result.append("&#34;");

        break;

        case '#':
          result.append("&#35;");

        break;

        case '$':
          result.append("&#36;");

        break;

        case '%':
          result.append("&#37;");

        break;

        case '&':
          result.append("&#38;");

        break;

        case '\'':
          result.append("&#39;");

        break;

        case '(':
          result.append("&#40;");

        break;

        case ')':
          result.append("&#41;");

        break;

        case '*':
          result.append("&#42;");

        break;

        case '+':
          result.append("&#43;");

        break;

        case ',':
          result.append("&#44;");

        break;

        case '-':
          result.append("&#45;");

        break;

        case '.':
          result.append("&#46;");

        break;

        case '/':
          result.append("&#47;");

        break;

        case ':':
          result.append("&#58;");

        break;

        case ';':
          result.append("&#59;");

        break;

        case '<':
          result.append("&#60;");

        break;

        case '=':
          result.append("&#61;");

        break;

        case '>':
          result.append("&#62;");

        break;

        case '?':
          result.append("&#63;");

        break;

        case '@':
          result.append("&#64;");

        break;

        case '[':
          result.append("&#91;");

        break;

        case '\\':
          result.append("&#92;");

        break;

        case ']':
          result.append("&#93;");

        break;

        case '^':
          result.append("&#94;");

        break;

        case '_':
          result.append("&#95;");

        break;

        case '`':
          result.append("&#96;");

        break;

        case '{':
          result.append("&#123;");

        break;

        case '|':
          result.append("&#124;");

        break;

        case '}':
          result.append("&#125;");

        break;

        case '~':
          result.append("&#126;");

        break;

        default:
          result.append(element);
      }
    }

    return result.toString();
  }

  private UiBuilderRegistry registry;

  /**
   * @return the registry
   */
  @Override
  public UiBuilderRegistry getRegistry() {
    return this.registry;
  }

  /**
   * @param registry the registry to set
   */
  @Override
  public void setRegistry(final UiBuilderRegistry registry) {
    this.registry = registry;
  }

  @Override
  public String toHtml(final Object object) {
    final StringBuilder s = new StringBuilder();
    appendHtml(s, object);
    return s.toString();
  }
}
