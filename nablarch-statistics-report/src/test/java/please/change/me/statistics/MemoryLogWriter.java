package please.change.me.statistics;

import java.util.ArrayList;
import java.util.List;

import nablarch.core.log.basic.LogWriterSupport;

public class MemoryLogWriter extends LogWriterSupport {

    public static List<String> outputs = new ArrayList<String>();

    @Override
    protected void onWrite(String formattedMessage) {
        outputs.add(formattedMessage);
    }
}
