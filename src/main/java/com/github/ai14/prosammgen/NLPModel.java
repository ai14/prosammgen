package com.github.ai14.prosammgen;

import opennlp.tools.postag.POSModel;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class NLPModel {

  private final SentenceModel sentenceModel;
  private final TokenizerModel tokenizerModel;
  private final POSModel posModel;

  public NLPModel(SentenceModel sentenceModel, TokenizerModel tokenizerModel,
                  POSModel posModel) {
    this.sentenceModel = sentenceModel;
    this.tokenizerModel = tokenizerModel;
    this.posModel = posModel;
  }

  public SentenceModel getSentenceModel() {
    return sentenceModel;
  }

  public TokenizerModel getTokenizerModel() {
    return tokenizerModel;
  }

  public POSModel getPosModel() {
    return posModel;
  }

  public static NLPModel loadFromDBs(Path sentDb, Path tokenDb, Path posDb)
      throws IOException {
    final SentenceModel sent;
    final TokenizerModel tok;
    final POSModel pos;

    try (InputStream is = Files.newInputStream(sentDb)) {
      sent = new SentenceModel(is);
    }

    try (InputStream is = Files.newInputStream(tokenDb)) {
      tok = new TokenizerModel(is);
    }

    try (InputStream is = Files.newInputStream(posDb)) {
      pos = new POSModel(is);
    }
    return new NLPModel(sent, tok, pos);
  }
}
