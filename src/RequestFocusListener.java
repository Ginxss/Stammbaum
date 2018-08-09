import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class RequestFocusListener implements AncestorListener {
    @Override
    public void ancestorAdded(AncestorEvent e) {
        JComponent component = e.getComponent();
        component.requestFocusInWindow();
        component.removeAncestorListener(this);
    }

    @Override
    public void ancestorRemoved(AncestorEvent e) {}

    @Override
    public void ancestorMoved(AncestorEvent e) {}
}
