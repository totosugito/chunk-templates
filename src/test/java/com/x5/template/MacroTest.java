package com.x5.template;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Test;
import static org.junit.Assert.*;

public class MacroTest
{
    @Test
    public void simpleMacroTest()
    {
        Theme theme = new Theme("test/base");

        Chunk c = theme.makeChunk("macro_test#simple_test");
        c.set("host_name", "Bob");

        assertEquals("Once I ate a burrito and drank some sangria at Bob's house and I had awful indigestion afterwards.\n",
                c.toString());
    }

    @Test
    public void tableTest()
    {
        Theme theme = new Theme("test/base");

        Chunk c = theme.makeChunk("macro_test#table_test");

        assertEquals("Featured: fork\n\nAll:\n<table>\n"
                + "<tr><td>spoon</td><td>$0.20</td></tr>\n"
                + "<tr><td>fork</td><td>$0.30</td></tr>\n"
                + "<tr><td>knife</td><td>$1.03</td></tr>\n"
                + "</table>\n",c.toString());
    }

    @Test
    public void dontFallForTheIfTest()
    {
        Theme theme = new Theme("test/base");

        Chunk c = theme.makeChunk("macro_test#assignment_trick_test");
        c.set("host_name", "Bob");

        assertEquals("Once I ate a burrito and drank some sangria at Jane's house and I had awful indigestion afterwards.\n",
                c.toString());
    }

    @Test
    public void standardArgsTest()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#simplified_syntax_test");
        c.set("host_name", "Harry");

        String targetOutput = theme.fetch("macro_test#std_fmt_expected_output");

        assertEquals(targetOutput, c.toString());
    }

    @Test
    public void jsonBadArgsTest()
    {
        Theme theme = new Theme("test/base");

        // log to myerr
        OutputStream myErr = new ByteArrayOutputStream();

        theme.setErrorHandling(true, new PrintStream(myErr));
        Chunk c = theme.makeChunk("macro_test#bad_json");
        c.toString();

        String targetOutput = "Error processing template: exec expected JSON object, not String.\n";
        String err = myErr.toString();
        // strip timestamp
        int errStart = err.indexOf("Error");
        if (errStart > 0) {
            err = err.substring(errStart);
        }

        assertEquals(targetOutput,err);
    }

    @Test
    public void jsonArrayArgsTest()
    {
        Theme theme = new Theme("test/base");

        // log to myErr
        OutputStream myErr = new ByteArrayOutputStream();

        theme.setErrorHandling(true, new PrintStream(myErr));
        Chunk c = theme.makeChunk("macro_test#json_array");
        c.toString();

        String targetOutput = "Error processing template: exec expected JSON object, not JSON array.\n";
        String err = myErr.toString();
        // strip timestamp
        int errStart = err.indexOf("Error");
        if (errStart > 0) {
            err = err.substring(errStart);
        }

        assertEquals(targetOutput,err);
    }

    @Test
    public void jsonArgsTest()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#simplified_syntax_test_json");
        c.set("host_name", "Bob");

        String targetOutput = theme.fetch("macro_test#json_test_expected_output");

        assertEquals(targetOutput, c.toString());
    }

    @Test
    public void jsonStrictArgsTest()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#simplified_syntax_test_json_strict");
        c.set("host_name", "Bob");

        String targetOutput = theme.fetch("macro_test#json_test_expected_output");

        assertEquals(targetOutput, c.toString());
    }

    @Test
    public void xmlArgsTest()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#simplified_syntax_test_xml");
        c.set("host_name", "Bob");

        String targetOutput = theme.fetch("macro_test#xml_test_expected_output");

        assertEquals(targetOutput, c.toString());
    }

    @Test
    public void xmlArgsWithAttributesTest()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#xml_with_attributes");

        String targetOutput = theme.fetch("macro_test#xml_with_attrs_expected_output");

        assertEquals(targetOutput, c.toString());
    }

    @Test
    public void tableOfMapsTest()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#table_of_maps_test");

        String targetOutput = theme.fetch("macro_test#table_of_maps_expected_output");

        assertEquals(targetOutput, c.toString());
    }

    @Test
    public void inlineBodyTest()
    {
        Chunk c = new Chunk();
        c.append("{^exec}{~a=2}{~x=3}{^body}a = {~a}.  x = {~x}.{/exec}");

        assertEquals("a = 2.  x = 3.", c.toString());
    }

    @Test
    public void inlineBodyWithEndTagTest()
    {
        // also tests smart trim...
        Chunk c = new Chunk();
        c.append("{^exec}{~a=2}{~x=3}{^body}\na = {~a}.  x = {~x}.\n{/body} alksdjflaksjdf {/exec}");

        assertEquals("a = 2.  x = 3.\n", c.toString());
    }

    @Test
    public void includeSnippetFromOverrideMacro()
    {
        Theme theme = new Theme("test/base,test/override");

        Chunk c = theme.makeChunk();
        // Well, the bug was that theme.fetch("...") no longer
        // auto-expands unqualified references to fully-qualified refs.
        //
        // But really, you can't expect this.  It's far more valuable
        // to have access to the original template *verbatim* via .fetch(),
        // and just direct folks to use getSnippet() (which works fine)
        // for all other contexts.
        //////c.append(theme.fetch("macro_test#bug29")); <-- bad, breaks!
        c.append(theme.getSnippet("macro_test#bug29"));

        String targetOutput = theme.fetch("macro_test#bug29_target_output");

        assertEquals(targetOutput, c.toString());
    }

    @Test
    public void emptyExecTest()
    {
        Chunk c = new Chunk();
        c.append("{.exec DOES_NOT_EXIST}{/exec}");

        assertEquals("",c.toString());
    }

    @Test
    public void execIndentTest()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#bug55");
        assertEquals("  a\n  b\n  c\n  d\n\n", c.toString());
    }

    @Test
    public void execScopeTest()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#scope_test");
        assertEquals("<div class=\"yum\">apple fruit</div>\n<div class=\"yum\">banana fruit</div>\n<div class=\"yum\">carrot vegetable</div>\n<div class=\"huh\">durian wat</div>\n", c.toString());
    }
    
    @Test
    public void execFilterTest()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#filter_exec");

        c.set("some_tag", "Rumpelstiltskin");
        c.set("some_num", "24999");

        assertEquals("Rmesitkn\n$24,999.00\n", c.toString());
    }

    @Test
    public void execFilterNullTest()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#filter_exec");

        assertEquals("\n\n", c.toString());
    }

    @Test
    public void execDataBlockFormal()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#xml_datablock_formal");

        assertEquals("I said hello.\n", c.toString());
    }

    @Test
    public void execDataBlockFormalUnquoted()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#xml_datablock_formal_unquoted");

        assertEquals("I said hello.\n", c.toString());
    }

    @Test
    public void execDataBlockInformal()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#xml_datablock_informal");

        assertEquals("I said hello.\n", c.toString());
    }

    @Test
    public void execDataBlockShorthand()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#xml_datablock_shorthand");

        assertEquals("I said hello.\n", c.toString());
    }

    @Test
    public void execDataBlockBodyFirst()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#xml_datablock_body_first");

        assertEquals("I said hello.\n", c.toString());
    }

    @Test
    public void execDataBlockBodyFirstOptionalEndMarker()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#xml_datablock_body_first_optional_endmarker");

        assertEquals("I said hello.\n", c.toString());
    }

    @Test
    public void execDataBlockUnmarkedBody()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#xml_datablock_unmarked_body");

        assertEquals("I said hello.\n", c.toString());
    }

    @Test
    public void execDataBlockUnmarkedBodyFirst()
    {
        Theme theme = new Theme("test/base");
        Chunk c = theme.makeChunk("macro_test#xml_datablock_unmarked_body_first");

        assertEquals("I said hello.\n", c.toString());
    }
}