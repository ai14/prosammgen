package com.github.ai14.prosammgen.textgen;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import java.util.function.Function;
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
        TextGenerators.parse("Hello, World!", ImmutableMap
            .<String, Function<ImmutableList<String>, TextGenerator>>of());

    assertEquals(new Constant("Hello, World!"), gen);
  }

  @Test
  public void testRef() throws Exception {
    TextGenerator gen =
        TextGenerators.parse("Hello, #FOO!", ImmutableMap
            .<String, Function<ImmutableList<String>, TextGenerator>>of());

    assertEquals(new Conjunction(new Constant("Hello, "), new Delegation("FOO"), new Constant("!")),
                 gen);
  }

  @Test
  public void testRefEnd() throws Exception {
    TextGenerator gen =
        TextGenerators.parse("Hello, #FOO", ImmutableMap
            .<String, Function<ImmutableList<String>, TextGenerator>>of());

    assertEquals(new Conjunction(new Constant("Hello, "), new Delegation("FOO")),
                 gen);
  }

  @Test
  public void testMacro() throws Exception {
    TextGenerator gen =
        TextGenerators.parse("Hello, %FOO()",
                             ImmutableMap.of("FOO", x -> new Constant("x")));

    assertEquals(new Conjunction(new Constant("Hello, "), new Constant("x")),
                 gen);
  }

  @Test
  public void testMacroArgs() throws Exception {
    TextGenerator gen =
        TextGenerators.parse("Hello, %FOO(a, b, c)",
                             ImmutableMap.of("FOO", x -> new Constant(Joiner.on(',').join(x))));

    assertEquals(new Conjunction(new Constant("Hello, "), new Constant("a,b,c")),
                 gen);
  }

}
