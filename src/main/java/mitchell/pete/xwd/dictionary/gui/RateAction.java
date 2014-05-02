package mitchell.pete.xwd.dictionary.gui;

import java.awt.event.*;
import java.util.Date;

import javax.swing.*;

public class RateAction extends AbstractAction implements Runnable
{
	private static final long serialVersionUID = 1L;
	private XDictGui gui = null;

    public RateAction(XDictGui g) 
    {
        this.gui = g;

        putValue(Action.LONG_DESCRIPTION, "Rate word");
        putValue(Action.NAME, "Rate");
    }

    public void actionPerformed(ActionEvent e) 
    {
    	new Thread(this).start();
    }
    
    public void run()
    {
    	try
    	{
    		gui.getStatusLine().showInfo("Processing rate...");
    		Date start = new Date();
    		
    		gui.doRate();
    		
    		Date stop = new Date();
    		gui.getStatusLine().showInfo( "Rate completed (" + ((stop.getTime() - start.getTime()) / (double) 1000) + " secs)." );
    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
    	}
    }
}

