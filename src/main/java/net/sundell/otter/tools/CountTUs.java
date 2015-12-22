package net.sundell.otter.tools;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import net.sundell.otter.TMXEvent;
import net.sundell.otter.TMXEventType;
import net.sundell.otter.TMXReader;
import net.sundell.otter.TUV;
import net.sundell.otter.TUVContent;
import net.sundell.otter.TextContent;

public class CountTUs {

    public static void main(String[] args) {
        try {
            new CountTUs().run(Arrays.asList(args));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run(List<String> args) throws Exception {
        Reader r = new InputStreamReader(new FileInputStream(args.get(0)), "UTF-8");
        TMXReader reader = TMXReader.createTMXReader(r);
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
        r.close();
        long end = System.currentTimeMillis();
        System.out.println("Parsed " + count + " tus in " + (end - start) + "ms");
        System.out.println("Word count: " + wordCount);
    }
}
