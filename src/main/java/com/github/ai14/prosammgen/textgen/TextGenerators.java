package com.github.ai14.prosammgen.textgen;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextGenerators {

  public static final Pattern MACRO_PATTERN =
      Pattern.compile("([A-Z]+)\\(([^)]*)\\).*");
  public static final CharMatcher ESCAPE_CHARS = CharMatcher.anyOf("%#");
  public static final CharMatcher PRODUCTION_CHARS = CharMatcher.JAVA_LETTER;

  private TextGenerators() {
    throw new IllegalAccessError("This class may not be instantiated.");
  }

  public static TextGenerator parse(String definition)
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

          resultBuilder.add(new Macro(macroName, macroArgs));
          i = definition.indexOf(')', i) + 1;
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

  public static ImmutableMap<String, TextGenerator> parseGrammar(List<String> lines)
      throws ParseException {
    Multimap<String, TextGenerator> productions = HashMultimap.create();
    for (String line : lines) {
      if (line.matches("\\s*(//.*)?")) {
        continue;
      }
      String[] parts = line.split("\\s+", 2);
      if (parts.length != 2 || !parts[0].startsWith("#")) {
        throw new RuntimeException("Not a valid definition: " + line);
      }
      String name = parts[0].substring(1);
      String definition = parts[1];

      productions.put(name, parse(definition));
    }

    ImmutableMap.Builder<String, TextGenerator> generatorsBuilder = ImmutableMap.builder();

    for (Map.Entry<String, Collection<TextGenerator>> entry : productions.asMap().entrySet()) {
      if (entry.getValue().size() == 1) {
        generatorsBuilder.put(entry.getKey(), Iterables.getOnlyElement(entry.getValue()));
      } else {
        generatorsBuilder
            .put(entry.getKey(), new Disjunction(ImmutableSet.copyOf(entry.getValue())));
      }
    }
    return generatorsBuilder.build();
  }
}
