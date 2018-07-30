package STGraph;

import IntelMessage.IntelMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a helper class that denote the information in real log message
 */
public class LogComposite extends STComponent {
    List<IntelMessage> groupedIntelMessage;

    public LogComposite() {
        groupedIntelMessage = new ArrayList<>();
    }

    @Override
    public void addIntelMessage(IntelMessage message) {
        super.addIntelMessage(message);
        groupedIntelMessage.add(message);
    }
}
