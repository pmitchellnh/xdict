package mitchell.pete.xwd.dictionary.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExportAction extends AbstractAction implements Runnable
{
	private static final long serialVersionUID = 1L;
	private XDictGui gui = null;

    public ExportAction(XDictGui g) 
    {
        this.gui = g;

        putValue(Action.SHORT_DESCRIPTION, "Export to a dictionary file");
        putValue(Action.NAME, "Export");
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
                    gui.doExport(false);
                }
            }.start();

    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
    	}
    }
}

