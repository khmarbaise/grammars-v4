/*
 [The "BSD licence"]
 Copyright (c) 2014 Vlad Shlosberg
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestBasicCss extends TestBase {
  @Test
  public void testSimple() {
    String[] lines = {
      "body {}",
    };
    assertThat(getSelector(lines).selector(0).element(0).identifier().getText()).isEqualTo("body");
  }

  @Test
  public void testIds() {
    String[] lines = {
      "#id1 {}",
    };
    assertThat(getSelector(lines).selector(0).element(0).identifier().getText()).isEqualTo("id1");
  }

  @Test
  public void testBlockWithNoSpacing() {
    String[] lines = {
      "#id1{}",
    };
    assertThat(getSelector(lines).selector(0).element(0).identifier().getText()).isEqualTo("id1");
  }

  @Test
  public void testClass() {
    String[] lines = {
      ".cls {}",
    };
    assertThat(getSelector(lines).selector(0).element(0).identifier().getText()).isEqualTo("cls");
  }

  @Test
  public void testOneSelectorMultipleParts() {
    String[] lines = {
      "body .cls {}",
    };
    ScssParser.SelectorsContext context = getSelector(lines);
    assertThat(context.selector(0).element(0).getText()).isEqualTo("body");
    assertThat(context.selector(0).element(1).getText()).isEqualTo(".cls");
  }

  @Test
  public void testMultipleSelectorsDifferentSelectors() {
    String[] lines = {
      "body .cls, h1 {}",
    };
    ScssParser.SelectorsContext context = getSelector(lines);
    assertThat(context.selector(0).element(0).identifier().getText()).isEqualTo("body");
    assertThat(context.selector(0).element(1).getText()).isEqualTo(".cls");

    assertThat(context.selector(1).element(0).getText()).isEqualTo("h1");
  }

  @Test
  public void testMultipleSelectorsSameElement() {
    String[] lines = {
      "body .cls, h1.cls1 {}",
    };
    ScssParser.SelectorsContext context = getSelector(lines);
    assertThat(context.selector(0).element(0).identifier().getText()).isEqualTo("body");
    assertThat(context.selector(0).element(1).getText()).isEqualTo(".cls");

    assertThat(context.selector(1).element(0).getText()).isEqualTo("h1");
    assertThat(context.selector(1).element(1).getText()).isEqualTo(".cls1");
  }

  @Test
  public void testMultipleSelectorsMix() {
    String[] lines = {
      ".cls1 .cls2, .cls3, .cls4 > .cls5 {}",
    };
    ScssParser.SelectorsContext context = getSelector(lines);
    assertThat(context.selector(0).element(0).getText()).isEqualTo(".cls1");
    assertThat(context.selector(0).element(1).getText()).isEqualTo(".cls2");

    assertThat(context.selector(1).element(0).getText()).isEqualTo(".cls3");

    assertThat(context.selector(2).element(0).getText()).isEqualTo(".cls4");

    assertThat(context.selector(2).element(1).combinator().getText()).isEqualTo(">");
    assertThat(context.selector(2).element(2).getText()).isEqualTo(".cls5");
  }

  @Test
  public void testNestingWithCombinatorAtStartOfLine() {
    String[] lines = {
      "p {", "  > p {}", "}",
    };
    ScssParser.StylesheetContext context = parse(lines);
    assertThat(context.statement(0).ruleset().selectors().selector(0).element(0).getText())
        .isEqualTo("p");
    assertThat(
            context
                .statement(0)
                .ruleset()
                .block()
                .statement(0)
                .ruleset()
                .selectors()
                .selector(0)
                .element(0)
                .combinator()
                .getText())
        .isEqualTo(">");
    assertThat(
            context
                .statement(0)
                .ruleset()
                .block()
                .statement(0)
                .ruleset()
                .selectors()
                .selector(0)
                .element(1)
                .getText())
        .isEqualTo("p");
  }

  @Test
  public void testProperties() {
    String[] lines = {
      "h1 {", "  display: block", "}",
    };
    ScssParser.BlockContext context = parse(lines).statement(0).ruleset().block();
    assertThat(context.property(0).identifier().getText()).isEqualTo("display");
    assertThat(context.property(0).values().commandStatement(0).getText()).isEqualTo("block");
  }

  @Test
  public void testPropertiesMultivalues() {
    String[] lines = {
      "h1 {", "  background: url('a'), 1px 2px", "}",
    };
    ScssParser.BlockContext context = parse(lines).statement(0).ruleset().block();
    assertThat(context.property(0).identifier().getText()).isEqualTo("background");

    ScssParser.ValuesContext val = context.property(0).values();
    assertThat(val.commandStatement(0).expression(0).url().Url().getText()).isEqualTo("'a'");
    assertThat(val.commandStatement(1).expression(0).measurement().Number().getText())
        .isEqualTo("1");
    assertThat(val.commandStatement(1).expression(0).measurement().Unit().getText())
        .isEqualTo("px");

    assertThat(val.commandStatement(1).expression(1).measurement().Number().getText())
        .isEqualTo("2");
    assertThat(val.commandStatement(1).expression(1).measurement().Unit().getText())
        .isEqualTo("px");
  }

  @Test
  public void testPropertiesMultiLines() {
    String[] lines = {
      "h1 {", "  color: 1px;", "  font-size: #fff", "}",
    };
    ScssParser.BlockContext context = parse(lines).statement(0).ruleset().block();

    assertThat(context.property(0).identifier().getText()).isEqualTo("color");
    ScssParser.ValuesContext val = context.property(0).values();
    assertThat(val.commandStatement(0).expression(0).measurement().Number().getText())
        .isEqualTo("1");
    assertThat(val.commandStatement(0).expression(0).measurement().Unit().getText())
        .isEqualTo("px");

    assertThat(context.property(1).identifier().getText()).isEqualTo("font-size");
    val = context.property(1).values();
    assertThat(val.commandStatement(0).expression(0).Color().getText()).isEqualTo("#fff");
  }

  @Test
  public void testPropertyMeasurement() {
    ScssParser.ExpressionContext exp = createProperty("p1: 1;");
    assertThat(exp.measurement().Number().getText()).isEqualTo("1");
    assertThat(exp.measurement().Unit()).isNull();
  }

  @Test
  public void testPropertyMeasurementAndUnit() {
    ScssParser.ExpressionContext exp = createProperty("p1: 1px;");
    assertThat(exp.measurement().Number().getText()).isEqualTo("1");
    assertThat(exp.measurement().Unit().getText()).isEqualTo("px");
  }

  @Test
  public void testPropertyShortColor() {
    ScssParser.ExpressionContext exp = createProperty("p1: #fff;");
    assertThat(exp.Color().getText()).isEqualTo("#fff");
  }

  @Test
  public void testPropertyLongColor() {
    ScssParser.ExpressionContext exp = createProperty("p1: #ababab;");
    assertThat(exp.Color().getText()).isEqualTo("#ababab");
  }

  @Test
  public void testPropertyIdentifier() {
    ScssParser.ExpressionContext exp = createProperty("p1: solid;");
    assertThat(exp.identifier().getText()).isEqualTo("solid");
  }

  @Test
  public void testPropertyUrlString() {
    ScssParser.ExpressionContext exp = createProperty("p1: url(\"hello\");");
    assertThat(exp.url().Url().getText()).isEqualTo("\"hello\"");
  }

  @Test
  public void testPropertyUrl() {
    ScssParser.ExpressionContext exp = createProperty("p1: url(hello);");
    assertThat(exp.url().Url().getText()).isEqualTo("hello");
  }

  @Test
  public void testPropertyString() {
    ScssParser.ExpressionContext exp = createProperty("p1: \"hello\";");
    assertThat(exp.StringLiteral().getText()).isEqualTo("\"hello\"");
  }

  @Test
  public void testPropertyVariable() {
    ScssParser.ExpressionContext exp = createProperty("p1: $hello;");
    assertThat(exp.variableName().getText()).isEqualTo("$hello");
  }

  @Test
  public void testPropertyFunctionMath() {
    ScssParser.ExpressionContext exp = createProperty("p1: calc(100% / 3);");
    assertThat(exp.functionCall().Identifier().getText()).isEqualTo("calc");
    assertThat(exp.functionCall().values().commandStatement(0).expression(0).getText())
        .isEqualTo("100%");
    assertThat(
            exp.functionCall()
                .values()
                .commandStatement(0)
                .mathStatement()
                .mathCharacter()
                .getText())
        .isEqualTo("/");
    assertThat(
            exp.functionCall()
                .values()
                .commandStatement(0)
                .mathStatement()
                .commandStatement()
                .expression(0)
                .measurement()
                .Number()
                .getText())
        .isEqualTo("3");
  }

  @Test
  public void testPropertyFunctionMathMinus() {
    ScssParser.ExpressionContext exp = createProperty("p1: calc(100% - 80px);");
    assertThat(exp.functionCall().Identifier().getText()).isEqualTo("calc");
    assertThat(
            exp.functionCall()
                .values()
                .commandStatement(0)
                .expression(0)
                .measurement()
                .Number()
                .getText())
        .isEqualTo("100");
    assertThat(
            exp.functionCall()
                .values()
                .commandStatement(0)
                .expression(0)
                .measurement()
                .Unit()
                .getText())
        .isEqualTo("%");

    assertThat(
            exp.functionCall()
                .values()
                .commandStatement(0)
                .mathStatement()
                .mathCharacter()
                .getText())
        .isEqualTo("-");

    ScssParser.MeasurementContext measure =
        exp.functionCall()
            .values()
            .commandStatement(0)
            .mathStatement()
            .commandStatement()
            .expression(0)
            .measurement();
    assertThat(measure.Number().getText()).isEqualTo("80");
    assertThat(measure.Unit().getText()).isEqualTo("px");
  }

  @Test
  public void testPropertyFunctionMathVar() {
    ScssParser.ExpressionContext exp = createProperty("p1: calc(100% - $var);");
    assertThat(exp.functionCall().Identifier().getText()).isEqualTo("calc");
    assertThat(
            exp.functionCall()
                .values()
                .commandStatement(0)
                .expression(0)
                .measurement()
                .Number()
                .getText())
        .isEqualTo("100");
    assertThat(
            exp.functionCall()
                .values()
                .commandStatement(0)
                .expression(0)
                .measurement()
                .Unit()
                .getText())
        .isEqualTo("%");

    assertThat(
            exp.functionCall()
                .values()
                .commandStatement(0)
                .mathStatement()
                .mathCharacter()
                .getText())
        .isEqualTo("-");

    assertThat(
            exp.functionCall()
                .values()
                .commandStatement(0)
                .mathStatement()
                .commandStatement()
                .expression(0)
                .variableName()
                .getText())
        .isEqualTo("$var");
  }

  @Test
  public void testPropertyFunctionMathParen() {
    ScssParser.ExpressionContext exp = createProperty("p1: calc(((100%)));");
    assertThat(exp.functionCall().Identifier().getText()).isEqualTo("calc");
    assertThat(
            exp.functionCall()
                .values()
                .commandStatement(0)
                .commandStatement()
                .commandStatement()
                .expression(0)
                .measurement()
                .Number()
                .getText())
        .isEqualTo("100");
  }

  @Test
  public void testPropertyWithNegatedMathValue() {
    String[] lines = {"h1 { p1: -$my-var; }"};

    ScssParser.BlockContext context = parse(lines).statement(0).ruleset().block();
    assertThat(context.property(0).identifier().getText()).isEqualTo("p1");
    assertThat(context.property(0).values().commandStatement(0).getText()).isEqualTo("-$my-var");
    assertThat(context.property(0).values().commandStatement(0).MINUS().getText()).isEqualTo("-");
    assertThat(context.property(0).values().commandStatement(0).expression(0).getText())
        .isEqualTo("$my-var");
  }

  @Test
  public void testPropertyWithPlusPrefix() {
    String[] lines = {"h1 { p1: +(400px - 200px); }"};

    ScssParser.BlockContext context = parse(lines).statement(0).ruleset().block();
    assertThat(context.property(0).identifier().getText()).isEqualTo("p1");
    assertThat(context.property(0).values().commandStatement(0).getText())
        .isEqualTo("+(400px-200px)");
    assertThat(context.property(0).values().commandStatement(0).PLUS().getText()).isEqualTo("+");
    assertThat(
            context
                .property(0)
                .values()
                .commandStatement(0)
                .commandStatement()
                .expression(0)
                .getText())
        .isEqualTo("400px");
    assertThat(
            context
                .property(0)
                .values()
                .commandStatement(0)
                .commandStatement()
                .mathStatement()
                .mathCharacter()
                .MINUS()
                .getText())
        .isEqualTo("-");
    assertThat(
            context
                .property(0)
                .values()
                .commandStatement(0)
                .commandStatement()
                .mathStatement()
                .commandStatement()
                .expression(0)
                .getText())
        .isEqualTo("200px");
  }

  @Test
  public void testInterpolation() {
    String[] lines = {"p.#{$name} > select2 {}"};
    ScssParser.SelectorsContext context = getSelector(lines);
    assertThat(context.selector(0).element(0).identifier().getText()).isEqualTo("p");
    assertThat(context.selector(0).element(1).identifier().identifierVariableName().getText())
        .isEqualTo("$name");

    assertThat(context.selector(0).element(2).getText()).isEqualTo(">");
    assertThat(context.selector(0).element(3).getText()).isEqualTo("select2");
  }

  @Test
  public void testInterpolationSpace() {
    String[] lines = {"p #{$name} {}"};
    ScssParser.SelectorsContext context = getSelector(lines);
    assertThat(context.selector(0).element(0).identifier().getText()).isEqualTo("p");
    assertThat(context.selector(0).element(1).identifier().identifierVariableName().getText())
        .isEqualTo("$name");
  }

  @Test
  public void testPseudoClassWithClassParameter() {
    String[] lines = {":host(.fullscreen) {}"};

    ScssParser.SelectorsContext context = getSelector(lines);
    assertThat(context.selector(0).element(0).pseudo().getText()).isEqualTo(":host(.fullscreen)");
    assertThat(context.selector(0).element(0).pseudo().selector().getText())
        .isEqualTo(".fullscreen");
  }

  @Test
  public void testPseudoClassWithAttributeParameter() {
    String[] lines = {"button:not([disabled]) {}"};

    ScssParser.SelectorsContext context = getSelector(lines);
    assertThat(context.selector(0).element(0).identifier().getText()).isEqualTo("button");
    assertThat(context.selector(0).element(1).pseudo().getText()).isEqualTo(":not([disabled])");
    assertThat(context.selector(0).element(1).pseudo().selector().getText())
        .isEqualTo("[disabled]");
  }

  @Test
  public void testPseudoClassWithNumberParameter() {
    String[] lines = {"button:nth-child(4) {}"};

    ScssParser.SelectorsContext context = getSelector(lines);
    assertThat(context.selector(0).element(0).identifier().getText()).isEqualTo("button");
    assertThat(context.selector(0).element(1).pseudo().getText()).isEqualTo(":nth-child(4)");
    assertThat(context.selector(0).element(1).pseudo().values().getText()).isEqualTo("4");
  }

  @Test
  public void testAttributeSelector() {
    String[] lines = {"input[type=number] {}"};

    ScssParser.SelectorsContext context = getSelector(lines);
    assertThat(context.selector(0).element(0).getText()).isEqualTo("input");
    assertThat(context.selector(0).element(1).attrib().getText()).isEqualTo("[type=number]");
    assertThat(context.selector(0).element(1).attrib().Identifier(0).getText()).isEqualTo("type");
    assertThat(context.selector(0).element(1).attrib().attribRelate().getText()).isEqualTo("=");
    assertThat(context.selector(0).element(1).attrib().Identifier(1).getText()).isEqualTo("number");
  }

  @Test
  public void testStandAloneAttributeSelector() {
    String[] lines = {"[disabled] {}"};

    ScssParser.SelectorsContext context = getSelector(lines);
    assertThat(context.selector(0).element(0).attrib().getText()).isEqualTo("[disabled]");
    assertThat(context.selector(0).element(0).attrib().Identifier(0).getText())
        .isEqualTo("disabled");
  }

  @Test
  public void testPseudoClassAtEndOfSelector() {
    String[] lines = {"h1:hover { color: blue; }"};

    ScssParser.SelectorsContext context = getSelector(lines);
    assertThat(context.selector(0).element(0).getText()).isEqualTo("h1");
    assertThat(context.selector(0).element(1).pseudo().getText()).isEqualTo(":hover");
  }

  @Test
  public void testPseudoElementAndPseudoClass() {
    // After is a pseudo-element and hover is a pseduo-class.
    // After is supported with 1 or 2 colons.
    String[] lines = {"h1:after:hover, h2:hover::after { color: blue; }"};

    ScssParser.SelectorsContext context = getSelector(lines);
    assertThat(context.selector(0).element(0).getText()).isEqualTo("h1");
    assertThat(context.selector(0).element(1).pseudo().getText()).isEqualTo(":after");
    assertThat(context.selector(0).element(2).pseudo().getText()).isEqualTo(":hover");
    assertThat(context.selector(1).element(0).getText()).isEqualTo("h2");
    assertThat(context.selector(1).element(1).pseudo().getText()).isEqualTo(":hover");
    assertThat(context.selector(1).element(2).pseudo().getText()).isEqualTo("::after");
  }

  @Test
  public void testStandAlonePseudoElement() {
    String[] lines = {"::placeholder { color: blue; }"};

    ScssParser.SelectorsContext context = getSelector(lines);
    assertThat(context.selector(0).element(0).pseudo().getText()).isEqualTo("::placeholder");
  }

  @Test
  public void testDefaultVariableDeclartion() {
    String[] lines = {
      "$black: #000 !default;", "$white: #fff;",
    };

    ScssParser.StylesheetContext context = parse(lines);
    assertThat(context.statement(0).variableDeclaration().POUND_DEFAULT()).isNotNull();
    assertThat(context.statement(1).variableDeclaration().POUND_DEFAULT()).isNull();
  }

  @Test
  public void testImportant() {
    String[] lines = {
      "p {", "  color: blue;", "  background-color: red !important;", "}",
    };

    ScssParser.StylesheetContext context = parse(lines);
    assertThat(context.statement(0).ruleset().block().property(0).IMPORTANT()).isNull();
    assertThat(context.statement(0).ruleset().block().property(1).IMPORTANT()).isNotNull();
  }

  private ScssParser.SelectorsContext getSelector(String... lines) {
    ScssParser.StylesheetContext context = parse(lines);
    return context.statement(0).ruleset().selectors();
  }

  private ScssParser.ExpressionContext createProperty(String... lines) {
    String[] all = new String[lines.length + 2];
    all[0] = "h1 {";
    System.arraycopy(lines, 0, all, 1, lines.length);
    all[all.length - 1] = "}";

    ScssParser.StylesheetContext styleContext = parse(all);
    ScssParser.BlockContext context = styleContext.statement(0).ruleset().block();
    return context.property(0).values().commandStatement(0).expression(0);
  }
}
