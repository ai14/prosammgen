package com.github.ai14.prosammgen;

import opennlp.tools.postag.POSModel;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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

  public static NLPModel loadFromDBs(URL sentDb, URL tokenDb, URL posDb)
          throws IOException {
    final SentenceModel sent;
    final TokenizerModel tok;
    final POSModel pos;

    InputStream is = null;
    try {
      is = sentDb.openStream();
      sent = new SentenceModel(is);
    } finally {
      if (is != null) {
        is.close();
      }
    }

    try {
      is = tokenDb.openStream();
      tok = new TokenizerModel(is);
    } finally {
      if (is != null) {
        is.close();
      }
    }

    try {
      is = posDb.openStream();
      pos = new POSModel(is);
    } finally {
      if (is != null) {
        is.close();
      }
    }

    return new NLPModel(sent, tok, pos);
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
}
