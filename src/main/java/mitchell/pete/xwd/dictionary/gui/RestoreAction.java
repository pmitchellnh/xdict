package mitchell.pete.xwd.dictionary.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RestoreAction extends AbstractAction implements Runnable
{
    private static final long serialVersionUID = 1L;
    private XDictGui gui = null;

    public RestoreAction(XDictGui g)
    {
        this.gui = g;

        putValue(Action.SHORT_DESCRIPTION, "Restore dictionary from a backup file");
        putValue(Action.NAME, "Restore...");
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
                    gui.doRestore();
                }
            }.start();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
        }
    }
}

