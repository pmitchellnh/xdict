package mitchell.pete.xwd.dictionary.gui;

import java.awt.event.*;
import java.util.Date;

import javax.swing.*;

public class LoadAction extends AbstractAction implements Runnable
{
	private static final long serialVersionUID = 1L;
	private XDictGui gui = null;

    public LoadAction(XDictGui g) 
    {
        this.gui = g;

        putValue(Action.SHORT_DESCRIPTION, "Load words from an external file");
        putValue(Action.NAME, "Load");
    }

    public void actionPerformed(ActionEvent e) 
    {
    	new Thread(this).start();
    }
    
    public void run()
    {
    	try
    	{
    		gui.getStatusLine().showInfo("Processing load...");
    		Date start = new Date();
    		
    		String status = gui.doLoad();
    		
    		Date stop = new Date();
    		gui.getStatusLine().showInfo( status + " (" + ((stop.getTime() - start.getTime()) / (double) 1000) + " secs)." );
    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
    	}
    }
}

