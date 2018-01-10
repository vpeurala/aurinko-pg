package org.aurinkopg.testingtools;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JavaTee {
    /**
     * This utility reads an input stream into a list of lines, and at
     * the same time outputs them to standard output like UNIX
     * command line utility program "tee" ('https://en.wikipedia.org/wiki/Tee_(command)').
     * <p>
     * It is always assumed that the encoding of the input stream is UTF-8.
     *
     * @param inputStream an InputStream of valid UTF-8 bytes.
     * @return a list of lines of the input - one String is one line.
     */
    public static List<String> readLines(InputStream inputStream) throws IOException {
        final InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        final BufferedReader bufferedReader = IOUtils.toBufferedReader(reader);
        final List<String> list = new ArrayList<>();
        String line = bufferedReader.readLine();
        while (line != null) {
            // This is the "tee" functionality:
            // the line we just read goes to the returned list of lines...
            list.add(line);
            // ... and at the same time we output it to standard system output.
            System.out.println(line);
            line = bufferedReader.readLine();
        }
        return list;
    }
}
