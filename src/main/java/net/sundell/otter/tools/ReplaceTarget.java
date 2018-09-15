package net.sundell.otter.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;

import net.sundell.cauliflower.Command;
import net.sundell.otter.TMXEvent;
import net.sundell.otter.TMXEventType;
import net.sundell.otter.TMXReader;
import net.sundell.otter.TMXWriter;
import net.sundell.otter.TU;
import net.sundell.otter.TUV;
import net.sundell.otter.TUVContent;
import net.sundell.otter.TextContent;

// Writes foo.tmx to foo.tmx.out, replacing strings in the target
// with some replacement.
public class ReplaceTarget extends Command {

    @Override
    public String getDescription() {
        return "Replace all instances of one target string with another";
    }

    @Override
    public void handle(CommandLine command) {
        String[] args = command.getArgs();
        if (args.length != 3) {
            usage();
        }
        Path p = Paths.get(args[0]);
        if (!Files.exists(p) || Files.isDirectory(p)) {
            System.err.println("Not a file: " + args[0]);
            usage();
        }
        Path output = p.resolveSibling(p.getFileName().toString() + ".out");
        try {
            replace(p, output, args[1], args[2]);
        }
        catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getUsageLine() {
        return getName() + " tmx_file <string> <replacement>";
    }

    void replace(Path input, Path output, final String original, final String replacement) throws XMLStreamException, IOException {
        int count = 0;
        try (TMXReader reader = TMXReader.createTMXReader(Files.newBufferedReader(input, StandardCharsets.UTF_8));
             TMXWriter writer = TMXWriter.createTMXWriter(Files.newBufferedWriter(output, StandardCharsets.UTF_8))) {
            while (reader.hasNext()) {
                TMXEvent event = reader.nextEvent();
                if (event.getEventType() == TMXEventType.TU) {
                    TU tu = event.getTU();
                    for (TUV tuv : tu.getTuvs().values()) {
                        // Skip the source; fix all targets
                        if (tuv.getLocale().equals(tu.getSrcLang())) {
                            continue;
                        }
                        for (TUVContent c : tuv.getContents()) {
                            if (c instanceof TextContent) {
                                TextContent tc = (TextContent)c;
                                String orig = tc.getValue();
                                String modified = tc.getValue().replaceAll(original, replacement);
                                tc.setValue(modified);
                                if (!modified.equals(orig)) {
                                    count++;
                                }
                            }
                        };
                    }
                }
                writer.writeEvent(event);
            }
        }
        System.out.println("Modified " + count + " TUs in " + output.getFileName());
    }
}
