package mitchell.pete.xwd.dictionary.gui;

import java.awt.event.*;
import java.util.Date;

import javax.swing.*;

public class QueryAction extends AbstractAction implements Runnable
{
	private static final long serialVersionUID = 1L;
	private XDictGui gui = null;

    public QueryAction(XDictGui g) 
    {
        this.gui = g;

        putValue(Action.LONG_DESCRIPTION, "Query word");
        putValue(Action.NAME, "Query");
    }

    public void actionPerformed(ActionEvent e) 
    {
    	new Thread(this).start();
    }
    
    public void run()
    {
    	try
    	{
    		gui.getStatusLine().showInfo("Processing query...");
    		Date start = new Date();
    		
    		int results = gui.doQuery();
    		
    		Date stop = new Date();
    		gui.getStatusLine().showInfo( "Query returned " + results + " " + (results == 1 ? "entry" : "entries") + "; (" + ((stop.getTime() - start.getTime()) / (double) 1000) + " secs)." );
    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
    	}
    }
}

