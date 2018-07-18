package STGraph;

import IntelMessage.IntelMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eddie on 2018/7/17.
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
