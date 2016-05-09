package uk.co.transputersystems.transputer.assembler;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

public class AssembleConfigTest {

    Properties properties = new Properties();

    @Before
    public void before() throws Exception {
        properties.load(this.getClass().getResourceAsStream("/basedir.properties"));
        File dir = new File(properties.getProperty("project.build.directory") + "/test-output/assemble-config-test/assembler-configs");
        dir.mkdirs();
    }

    public InputStream getResource(String name) {
        return getClass().getResourceAsStream(name);
    }

    /**
     * Given a input stream, attempt to assemble it.
     * TODO: find a way to test the output other than checking nullity
     * @param inputStream The file to be assembled
     * @param verbose Whether to print debug output or not.
     * @return true if the compilation appears to have succeeded, otherwise false.
     */
    public File testAssemblingFile(InputStream inputStream, InputStream configStream, String filePath, boolean verbose) throws Exception {
        PrintStream out = new PrintStream(System.out);

        if (verbose) {
            out.println("TEST: " + filePath);
            out.flush();
        }

        String input = new Scanner(inputStream, "utf-8").useDelimiter("\\Z").next();
        List<String> output = Assembler.assemble(input, Assembler.loadConfig(new InputStreamReader(configStream)), out, true);

        assertTrue(output != null);
        assertTrue(output.size() > 0);

        File outputFile = new File(properties.getProperty("project.build.directory")+ "/test-output/assemble-config-test" + filePath.replace(".s", ".auto.o"));
        FileWriter outputWriter = new FileWriter(outputFile);
        for (String line : output) {
            outputWriter.append(line);
            outputWriter.append('\n');
        }
        outputWriter.flush();
        outputWriter.close();

        return outputFile;
    }

    @Test
    public void testConfig1() throws Exception {
        String path = "/assembler-configs/config1.s";
        String configPath = "/assembler-configs/config1.yaml";
        testAssemblingFile(getResource(path), getResource(configPath), path, true);
    }
}
