package com.github.ai14.prosammgen.textgen;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import java.text.ParseException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextGenerators {

  public static final Pattern MACRO_PATTERN =
      Pattern.compile("(\\w+)\\(([^)]*)\\).*");
  public static final CharMatcher ESCAPE_CHARS = CharMatcher.anyOf("%#");
  public static final CharMatcher PRODUCTION_CHARS = CharMatcher.JAVA_LETTER;

  private TextGenerators() {
    throw new IllegalAccessError("This class may not be instantiated.");
  }

  public static TextGenerator parse(
      String definition,
      ImmutableMap<String, Function<ImmutableList<String>, TextGenerator>> macros)
      throws ParseException {
    ImmutableList.Builder<TextGenerator> resultBuilder = ImmutableList.builder();

    int i = 0;

    while (i < definition.length()) {
      switch (definition.charAt(i)) {
        case '%':
          Matcher matcher = MACRO_PATTERN.matcher(definition.substring(i + 1));
          if (!matcher.matches()) {
            throw new ParseException("Illegal macro reference", i);
          }

          String macroName = matcher.group(1);
          ImmutableList<String> macroArgs = ImmutableList.copyOf(
              Splitter.on(',').omitEmptyStrings().trimResults().split(matcher.group(2)));

          if (!macros.containsKey(macroName)) {
            throw new ParseException("Unknown macro '" + macroName + "'", i);
          }

          resultBuilder.add(macros.get(macroName).apply(macroArgs));
          i = definition.indexOf(')') + 1;
          break;
        case '#':
          int end = i + 1;
          while (end < definition.length() && PRODUCTION_CHARS.matches(definition.charAt(end))) {
            end++;
          }
          resultBuilder.add(new Delegation(definition.substring(i + 1, end)));
          i = end;
          break;
        default:
          int nextI = ESCAPE_CHARS.indexIn(definition, i);
          if (nextI < 0) {
            resultBuilder.add(new Constant(definition.substring(i)));
            i = definition.length();
          } else {
            resultBuilder.add(new Constant(definition.substring(i, nextI)));
            i = nextI;
          }
      }
    }

    ImmutableList<TextGenerator> result = resultBuilder.build();
    if (result.size() == 1) {
      return Iterables.getOnlyElement(result);
    } else {
      return new Conjunction(result);
    }
  }
}
