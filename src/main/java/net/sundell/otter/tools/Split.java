package net.sundell.otter.tools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import net.sundell.cauliflower.Command;
import net.sundell.otter.TMXEvent;
import net.sundell.otter.TMXEventType;
import net.sundell.otter.TMXReader;
import net.sundell.otter.TMXWriter;

public class Split extends Command {
    public static final int DEFAULT_SPLIT_THRESHOLD = 5000;

    @Override
    public String getDescription() {
        return "Splits a TMX into chunks of <n> TUs";
    }

    @Override
    protected String getUsageLine() {
        return getName() + " [options] source_tmx target_tmx_prefix";
    }

    @Override
    public Options getOptions() {
        Options options = super.getOptions();
        options.addOption(new Option("v", "verbose"));
        options.addOption(new Option("n", true, "Split every <n> TUs. Defaults to " + DEFAULT_SPLIT_THRESHOLD));
        return options;
    }

    private int parseCountOption(String val) {
        try {
            return Integer.valueOf(val);
        }
        catch (NumberFormatException e) {
            usage("Not a number: '" + val + "'");
            return 0; // unreachable
        }
    }

    private String getPartName(String prefix, int partNum) {
        return String.format("%s_PART%04d.tmx", prefix, partNum);
    }

    private Writer getWriter(String filePrefix, int partNum) throws IOException {
        String name = getPartName(filePrefix, partNum);
        System.out.println("Writing " + name + "...");
        return new OutputStreamWriter(new FileOutputStream(name), StandardCharsets.UTF_8);
    }

    @Override
    public void handle(CommandLine command) {
        String[] args = command.getArgs();
        if (args.length < 2) usage();
        boolean verbose = command.hasOption("v");
        int threshold = command.hasOption("n") ? parseCountOption(command.getOptionValue("n")) : DEFAULT_SPLIT_THRESHOLD;
        System.out.println("Splitting every " + threshold + " TUs");
        int partNum = 1;
        String filePrefix = args[1];
        List<TMXEvent> headEvents = new ArrayList<>(); 
        try (Reader r = new InputStreamReader(new FileInputStream(args[0]), StandardCharsets.UTF_8);
             Writer w = getWriter(filePrefix, partNum++)) {
            
            TMXReader reader = TMXReader.createTMXReader(r);
            TMXWriter writer = TMXWriter.createTMXWriter(w);

            // Save everything before we get to TUs
            TMXEvent event = null;
            while (reader.hasNext()) {
                event = reader.nextEvent();
                TMXEventType type = event.getEventType();
                if (type == TMXEventType.TU) {
                    break;
                }
                else {
                    headEvents.add(event);
                }
            }
            // Write the header
            for (TMXEvent e : headEvents) {
                writer.writeEvent(e);;
            }

            // Write the first TU
            if (event != null) {
                writer.writeEvent(event);
            }
            int count = 1;
            while (reader.hasNext()) {
                event = reader.nextEvent();
                TMXEventType type = event.getEventType();
                if (type == TMXEventType.END_TMX) {
                    writer.writeEvent(event);
                    // All done!
                    writer.close();
                    break;
                }
                else if (type == TMXEventType.TU) {
                    if (count >= threshold) {
                        // Finish the file
                        writer.endTMX();
                        writer.close();
                        writer = TMXWriter.createTMXWriter(getWriter(filePrefix, partNum++));
                        for (TMXEvent e : headEvents) {
                            writer.writeEvent(e);;
                        }
                        count = 0;
                    }
                    writer.writeEvent(event);
                    count++;
                }
            }
        }
        catch (IOException | XMLStreamException e) {
            if (verbose) {
                e.printStackTrace(System.err);
                die("Error encountered splitting TMX.");
            }
            die("Error encountered splitting TMX. Re-run with -v for the error.");
        }
    }

}
