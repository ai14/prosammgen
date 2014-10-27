package com.github.ai14.prosammgen.textgen;

import org.apache.commons.lang.StringEscapeUtils;

public class Constant implements TextGenerator {

  private final String content;

  public Constant(String content) {
    this.content = content;
  }

  public void generateText(Context context) {
    context.getBuilder().append(content);
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Constant constant = (Constant) o;

    if (!content.equals(constant.content)) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    return content.hashCode();
  }

  public String toString() {
    return "\"" + StringEscapeUtils.escapeJava(content) + "\"";
  }

}
