package com.github.ai14.prosammgen;

import java.util.List;

public class ReportGenerator {
  String heading = "prosamm report heading";

  public ProsammReport generateReport(List<String> questions) {
    ProsammReport report = new ProsammReport(heading);

    for (String question : questions) {

      // report.addQuestionAnswer(
    }

    return report;
  }
}
