package mitchell.pete.xwd.dictionary.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class BackupAction extends AbstractAction implements Runnable
{
	private static final long serialVersionUID = 1L;
	private XDictGui gui = null;

    public BackupAction(XDictGui g) 
    {
        this.gui = g;

        putValue(Action.SHORT_DESCRIPTION, "Backup this dictionary to a timestamped file");
        putValue(Action.NAME, "Backup");
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
                    gui.doExport(true);
                }
            }.start();

    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
    	}
    }
}

