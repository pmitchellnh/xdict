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

        putValue(Action.LONG_DESCRIPTION, "Load words");
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
    		
    		gui.doLoad();
    		
    		Date stop = new Date();
    		gui.getStatusLine().showInfo( "Load completed (" + ((stop.getTime() - start.getTime()) / (double) 1000) + " secs)." );
    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
    	}
    }
}
