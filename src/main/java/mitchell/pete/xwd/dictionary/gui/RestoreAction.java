package mitchell.pete.xwd.dictionary.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Date;

public class RestoreAction extends AbstractAction implements Runnable
{
    private static final long serialVersionUID = 1L;
    private XDictGui gui = null;

    public RestoreAction(XDictGui g)
    {
        this.gui = g;

        putValue(Action.SHORT_DESCRIPTION, "Restore dictionary from a file.");
        putValue(Action.NAME, "Restore");
    }

    public void actionPerformed(ActionEvent e)
    {
        new Thread(this).start();
    }

    public void run()
    {
        try
        {
            gui.getStatusLine().showInfo("Processing restore...");
            Date start = new Date();

            String status = gui.doRestore();

            Date stop = new Date();
            gui.getStatusLine().showInfo( status + " (" + ((stop.getTime() - start.getTime()) / (double) 1000) + " secs)." );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
        }
    }
}

