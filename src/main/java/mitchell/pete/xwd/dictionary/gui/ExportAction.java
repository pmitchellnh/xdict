package mitchell.pete.xwd.dictionary.gui;

import java.awt.event.*;
import java.util.Date;

import javax.swing.*;

public class ExportAction extends AbstractAction implements Runnable
{
	private static final long serialVersionUID = 1L;
	private XDictGui gui = null;

    public ExportAction(XDictGui g) 
    {
        this.gui = g;

        putValue(Action.LONG_DESCRIPTION, "Export words");
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
    		
    		gui.doExport();
    		
    		Date stop = new Date();
    		gui.getStatusLine().showInfo( "Export completed (" + ((stop.getTime() - start.getTime()) / (double) 1000) + " secs)." );
    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
    	}
    }
}

