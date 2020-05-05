package mitchell.pete.xwd.dictionary.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class BrowseAction extends AbstractAction implements Runnable
{
    private static final long serialVersionUID = 1L;
    private XDictGui gui = null;
    private boolean isRestore;

    public BrowseAction(XDictGui g, boolean isRestore)
    {
        this.gui = g;
        this.isRestore = isRestore;

        if (isRestore)
            putValue(Action.SHORT_DESCRIPTION, "Browse for backup file to restore");
        else
            putValue(Action.SHORT_DESCRIPTION, "Browse for word list file to load");
        putValue(Action.NAME, "Browse");
    }

    public void actionPerformed(ActionEvent e)
    {
        new Thread(this).start();
    }

    public void run()
    {
        try
        {
            new Thread() {
                public void run() {
                    gui.fileSelectDialog(isRestore);
                }
            }.start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
        }
    }
}

