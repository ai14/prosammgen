package com.github.ai14.prosammgen;

import java.util.List;

public class ProsammReport {
  private String heading;
  private List<QuestionAnswer> parts;

  public ProsammReport(String heading) {
    this.heading = heading;
  }

  public void addQuestionAnswer(QuestionAnswer qa) {
    parts.add(qa);
  }

  public void generateLatex() {
    // TODO
  }
}
