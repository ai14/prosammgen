package com.github.ai14.prosammgen.textgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import java.util.regex.Matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TextGeneratorsTest {

  @Test
  public void testNormalMacro() throws Exception {
    Matcher matcher = TextGenerators.MACRO_PATTERN.matcher("FOO()");
    assertTrue(matcher.matches());
    assertEquals("FOO", matcher.group(1));
  }

  @Test
  public void testOneArgMacro() throws Exception {
    Matcher matcher = TextGenerators.MACRO_PATTERN.matcher("FOO(x)");
    assertTrue(matcher.matches());
    assertEquals("FOO", matcher.group(1));
    assertEquals("x", matcher.group(2));
  }

  @Test
  public void testOneArgMacroSpaces() throws Exception {
    Matcher matcher = TextGenerators.MACRO_PATTERN.matcher("FOO(\tx  )");
    assertTrue(matcher.matches());
    assertEquals("FOO", matcher.group(1));
    assertEquals("\tx  ", matcher.group(2));
  }

  @Test
  public void testTwoArgMacro() throws Exception {
    Matcher matcher = TextGenerators.MACRO_PATTERN.matcher("FOO(x,y)");
    assertTrue(matcher.matches());
    assertEquals("FOO", matcher.group(1));
    assertEquals("x,y", matcher.group(2));
  }

  @Test
  public void testTwoArgMacroSpaces() throws Exception {
    Matcher matcher = TextGenerators.MACRO_PATTERN.matcher("FOO(\tx , y  )");
    assertTrue(matcher.matches());
    assertEquals("FOO", matcher.group(1));
    assertEquals("\tx , y  ", matcher.group(2));
  }

  @Test
  public void testConstant() throws Exception {
    TextGenerator gen =
        TextGenerators.parse("Hello, World!");

    assertEquals(new Constant("Hello, World!"), gen);
  }

  @Test
  public void testRef() throws Exception {
    TextGenerator gen =
        TextGenerators.parse("Hello, #FOO!");

    assertEquals(new Conjunction(new Constant("Hello, "), new Delegation("FOO"), new Constant("!")),
                 gen);
  }

  @Test
  public void testRefEnd() throws Exception {
    TextGenerator gen =
        TextGenerators.parse("Hello, #FOO");

    assertEquals(new Conjunction(new Constant("Hello, "), new Delegation("FOO")),
                 gen);
  }

  @Test
  public void testMacro() throws Exception {
    TextGenerator gen =
        TextGenerators.parse("Hello, %FOO()");

    assertEquals(
        new Conjunction(new Constant("Hello, "), new Macro("FOO", ImmutableList.<String>of())),
        gen);
  }

  @Test
  public void testMacroArgs() throws Exception {
    TextGenerator gen =
        TextGenerators.parse("Hello, %FOO(a, b, c)");

    assertEquals(new Conjunction(new Constant("Hello, "),
                                 new Macro("FOO", ImmutableList.of("a", "b", "c"))),
                 gen);
  }

  @Test
  public void testGrammarParse() throws Exception {
    ImmutableList<String> lines =
        ImmutableList.of("#FOO Bar");

    ImmutableMap<String, TextGenerator> grammar =
        TextGenerators.parseGrammar(lines);

    assertTrue(grammar.containsKey("FOO"));
    assertEquals(new Constant("Bar"), grammar.get("FOO"));
  }

  @Test
  public void testGrammarParseTwo() throws Exception {
    ImmutableList<String> lines =
        ImmutableList.of("#FOO Bar #BAZ",
                         "#BAZ xyz");

    ImmutableMap<String, TextGenerator> grammar =
        TextGenerators.parseGrammar(lines);

    assertTrue(grammar.containsKey("FOO"));
    assertTrue(grammar.containsKey("BAZ"));
    assertEquals(new Conjunction(new Constant("Bar "), new Delegation("BAZ")), grammar.get("FOO"));
    assertEquals(new Constant("xyz"), grammar.get("BAZ"));
  }

  @Test
  public void testGrammarParseTwoComments() throws Exception {
    ImmutableList<String> lines =
        ImmutableList.of("// Comment!",
                         "#FOO Bar #BAZ",
                         "    // Comment again!",
                         "",
                         "#BAZ xyz");

    ImmutableMap<String, TextGenerator> grammar =
        TextGenerators.parseGrammar(lines);

    assertTrue(grammar.containsKey("FOO"));
    assertTrue(grammar.containsKey("BAZ"));
    assertEquals(new Conjunction(new Constant("Bar "), new Delegation("BAZ")), grammar.get("FOO"));
    assertEquals(new Constant("xyz"), grammar.get("BAZ"));
  }
}
