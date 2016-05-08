package uk.co.transputersystems.occam;

import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Stream;

public class ParserTest {
/*
    @Test
    public void testParsingFiles() {
        File dir;
        URL url = getClass().getResource("feature-tests");
        try {
            dir = new File(url.toURI());
        } catch(URISyntaxException e) {
            dir = new File(url.getPath());
        }

        OccamCompiler compiler = new OccamCompiler();
        ErrorListener errorListener = new ErrorListener();

        Stream<ParseTree> parseTrees = Arrays.stream(dir.listFiles((f, n) -> n.endsWith(".occ")))
                .sorted((f1, f2) -> f1.getName().compareTo(f2.getName()))
                .map(f -> compiler.makeParseTree(f, OccamParser::file_input, errorListener));

        assert(parseTrees.allMatch(t -> t != null));
        assert(errorListener.errors == 0);
    }

    @Test
    public void testParsingFunctionFiles() {
        File dir;
        URL url = getClass().getResource("function-tests");
        try {
            dir = new File(url.toURI());
        } catch(URISyntaxException e) {
            dir = new File(url.getPath());
        }

        OccamCompiler compiler = new OccamCompiler();
        ErrorListener errorListener = new ErrorListener();

        Stream<ParseTree> parseTrees = Arrays.stream(dir.listFiles((f, n) -> n.endsWith(".occ")))
                .sorted((f1, f2) -> f1.getName().compareTo(f2.getName()))
                .map(f -> compiler.makeParseTree(f, OccamParser::file_input, errorListener));

        assert(parseTrees.allMatch(t -> t != null));
        assert(errorListener.errors == 0);
    }*/
}
