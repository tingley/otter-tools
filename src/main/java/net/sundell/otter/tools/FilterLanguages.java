package net.sundell.otter.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class FilterLanguages extends Command {

    void filter(Path file, Path output, Set<String> languageCodes) {
        try (TMXReader reader = TMXReader.createTMXReader(Files.newBufferedReader(file, StandardCharsets.UTF_8));
             TMXWriter writer = TMXWriter.createTMXWriter(Files.newBufferedWriter(output, StandardCharsets.UTF_8))) {

            while (reader.hasNext()) {
                TMXEvent event = reader.nextEvent();
                if (event.getEventType() == TMXEventType.TU) {
                    TU tu = event.getTU();
                    for (Iterator<Map.Entry<String, TUV>> it = tu.getTuvs().entrySet().iterator(); it.hasNext(); ) {
                        Map.Entry<String, TUV> e = it.next();
                        if (!languageCodes.contains(e.getKey())) {
                           it.remove();
                        }
                    }
                }
                writer.writeEvent(event);
            }
        }
        catch (IOException | XMLStreamException e) {
            e.printStackTrace(System.err);
            die("Aborting due to parsing error.");
        }
    }

    @Override
    public String getDescription() {
        return "Filters TUVs that don't match one of the specified languages";
    }

    @Override
    protected String getUsageLine() {
        return getName() + " tmx_file output_file langCode1 langCode2";
    }

    @Override
    public void handle(CommandLine command) {
        String[] args = command.getArgs();
        if (args.length != 4) {
            usage();
        }
        Path input = Paths.get(args[0]);
        if (!Files.exists(input)) {
            System.err.println("File does not exist: " + input);
            usage();
        }
        Path output = Paths.get(args[1]);
        Set<String> langs = new HashSet<>();
        langs.add(args[2]);
        langs.add(args[3]);
        
        filter(input, output, langs);
    }
}
