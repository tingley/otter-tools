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
import net.sundell.otter.TUV;
import net.sundell.otter.TUVContent;
import net.sundell.otter.TextContent;

public class CountTUs extends Command {

    void count(Path file) {
        try (TMXReader reader = TMXReader.createTMXReader(Files.newBufferedReader(file, StandardCharsets.UTF_8))) {
            int count = 0;
            long start = System.currentTimeMillis();
            int wordCount = 0;

            while (reader.hasNext()) {
                TMXEvent event = reader.nextEvent();
                if (event.getEventType() == TMXEventType.TU) {
                    count++;
                    TUV srcTuv = event.getTU().getTuvs().get(event.getTU().getSrcLang());
                    if (srcTuv != null) {
                        for (TUVContent content : srcTuv.getContents()) {
                            if (content instanceof TextContent) {
                                String s = ((TextContent)content).getValue();
                                wordCount += s.split("\\s+").length;
                            }
                        }
                    }
                    else {
                        System.out.println("Missing TUV for lang " + event.getTU().getSrcLang());
                    }
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("TUs " + count + ", Words " + wordCount + ", Time " + (end - start) + "ms");
        }
        catch (IOException | XMLStreamException e) {
            e.printStackTrace(System.err);
            die("Aborting due to parsing error.");
        }
    }

    @Override
    public String getDescription() {
        return "Count TUs and words in one or more TMX files";
    }

    @Override
    protected String getUsageLine() {
        return getName() + " tmx_file [tmx_file2] [tmx_file3] ...";
    }

    @Override
    public void handle(CommandLine command) {
        String[] args = command.getArgs();
        if (args.length == 0) {
            usage();
        }
        for (String arg : args) {
            Path p = Paths.get(arg);
            if (!Files.exists(p)) {
                System.err.println("File does not exist: " + p);
                continue;
            }
            count(p);
        }
    }
}
