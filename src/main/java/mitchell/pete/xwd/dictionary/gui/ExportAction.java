package mitchell.pete.xwd.dictionary.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Date;

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
    		gui.getStatusLine().showInfo("Processing export...");
    		Date start = new Date();
    		
    		String status = gui.doExport(false);
    		
    		Date stop = new Date();
    		gui.getStatusLine().showInfo( status + " (" + ((stop.getTime() - start.getTime()) / (double) 1000) + " secs)." );
    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
    	}
    }
}

