package de.ice09.invoice.verifier.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Input {

    public static Map<String, String> parse(String arg) throws IOException {
        Map<String, String> ard = new HashMap<>();
        //String content = Files.lines(new File("src/main/resources/ard.csv").toPath()).collect(Collectors.joining()).replaceAll("\"", "");
        String[] contents = arg.replaceAll("\"", "").split(",");
        ard.put("id", contents[0]);
        ard.put("seller", contents[1]);
        ard.put("buyer", contents[2]);
        ard.put("duedate", contents[3]);
        ard.put("total", contents[4]);
        ard.put("sigR", contents[5]);
        ard.put("sigS", contents[6]);
        ard.put("sigV", contents[7]);
        return ard;
    }

}
