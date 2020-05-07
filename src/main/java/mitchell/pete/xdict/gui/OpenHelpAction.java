package mitchell.pete.xdict.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class OpenHelpAction extends AbstractAction implements Runnable
{
    private static final long serialVersionUID = 1L;
    private XDictGui gui = null;

    public OpenHelpAction(XDictGui g)
    {
        this.gui = g;

        putValue(Action.SHORT_DESCRIPTION, "Open help files in a browser");
        putValue(Action.NAME, "View Help Documentation");
    }

    public void actionPerformed(ActionEvent e)
    {
        new Thread(this).start();
    }

    public void run()
    {
        try
        {
            gui.doHelp();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
        }
    }
}
