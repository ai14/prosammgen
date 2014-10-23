package com.github.ai14.prosammgen.textgen;

import com.google.common.base.MoreObjects;

public class Constant implements TextGenerator {

  private final String content;

  public Constant(String content) {
    this.content = content;
  }

  @Override
  public void generateText(Context context) {
    context.getBuilder().append(content);
  }

  @Override
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

  @Override
  public int hashCode() {
    return content.hashCode();
  }

  @Override
  public String toString() {
    return "\"" + content + "\"";
  }

}
