package net.sundell.otter.tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import net.sundell.cauliflower.Command;
import net.sundell.otter.TMXEvent;
import net.sundell.otter.TMXEventType;
import net.sundell.otter.TMXReader;
import net.sundell.otter.TMXWriter;

/**
 * Copy the first n TUs in a TMX to another file. 
 *
 * TODO: add controls for filtering properties
 */
public class CopyHead extends Command {

    @Override
    public String getDescription() {
        return "Copies the first <n> TUs of a TMX to a new TMX";
    }

    @Override
    protected String getUsageLine() {
        return getName() + " [options] source_tmx target_tmx";
    }

    @Override
    public Options getOptions() {
        Options options = super.getOptions();
        options.addOption(new Option("v", "verbose"));
        options.addOption(new Option("n", true, "Only copy the first <arg> TUs"));
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

    @Override
    public void handle(CommandLine command) {
        String[] args = command.getArgs();
        if (args.length != 2) usage();
        boolean verbose = command.hasOption("v");
        int max = command.hasOption("n") ? parseCountOption(command.getOptionValue("n")) : Integer.MAX_VALUE;
        try (Reader r = new InputStreamReader(new FileInputStream(args[0]), StandardCharsets.UTF_8);
             Writer w = new OutputStreamWriter(new FileOutputStream(args[1]), StandardCharsets.UTF_8)) {
            
            TMXReader reader = TMXReader.createTMXReader(r);
            TMXWriter writer = TMXWriter.createTMXWriter(w);

            int count = 0;
            int skipped = 0;
            while (reader.hasNext()) {
                TMXEvent event = reader.nextEvent();
                TMXEventType type = event.getEventType();
                if (type == TMXEventType.END_TMX) {
                    writer.writeEvent(event);
                    break;
                }
                else if (type == TMXEventType.START_TMX) {
                    writer.writeEvent(event);
                }
                else if (type == TMXEventType.TU) {
                    if (count < max) {
                        writer.writeEvent(event);
                        count++;
                    }
                    else {
                        skipped++;
                    }
                }
            }
            if (max == Integer.MAX_VALUE) {
                max = count;
            }
            if (max <= count) {
                System.out.println("Copied " + count + "/" + max + " TUs, skipped " + skipped);
            }
            else {
                System.out.println("Copied " + count + "/" + max + " TUs");
            }
        }
        catch (IOException | XMLStreamException e) {
            if (verbose) {
                e.printStackTrace(System.err);
                die("Error encountered copying TMX.");
            }
            die("Error encountered copying TMX. Re-run with -v for the error.");
        }
    }
}
