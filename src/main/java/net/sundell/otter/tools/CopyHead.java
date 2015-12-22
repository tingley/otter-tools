package net.sundell.otter.tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import net.sundell.otter.TMXEvent;
import net.sundell.otter.TMXEventType;
import net.sundell.otter.TMXReader;
import net.sundell.otter.TMXWriter;

/**
 * Copy the first n TUs in a TMX to another file. 
 *
 * Usage: CopyHead <src> <tgt> <count>
 *
 * TODO: add controls for filtering properties
 */
public class CopyHead {

    public static void main(String[] args) {
        try {
            new CopyHead().run(Arrays.asList(args));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run(List<String> args) throws Exception {
        int max = Integer.valueOf(args.get(2));
        try (Reader r = new InputStreamReader(new FileInputStream(args.get(0)), StandardCharsets.UTF_8);
             Writer w = new OutputStreamWriter(new FileOutputStream(args.get(1)), StandardCharsets.UTF_8)) {
            
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
            System.out.println("Copied " + count + "/" + max + " TUs, skipped " + skipped);
        }
    }
}
