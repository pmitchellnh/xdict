package mitchell.pete.xwd.dictionary.gui;

import java.awt.event.*;
import java.util.Date;

import javax.swing.*;

public class QueryAction extends AbstractAction implements Runnable
{
	private static final long serialVersionUID = 1L;
	private XDictGui gui = null;
	private boolean queryNext = false;	// if true, get next bunch from existing query

    public QueryAction(XDictGui g, boolean next) 
    {
        this.gui = g;
        queryNext = next;
        if (next) {
	        putValue(Action.LONG_DESCRIPTION, "Next in result set");
	        putValue(Action.NAME, "Next");
        } else {
	        putValue(Action.LONG_DESCRIPTION, "Query word");
	        putValue(Action.NAME, "Query");
        }
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
    		
    		String results = gui.doQuery(queryNext);
    		
    		Date stop = new Date();
    		gui.getStatusLine().showInfo( "Query returned " + results + "; (" + ((stop.getTime() - start.getTime()) / (double) 1000) + " secs)." );
    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
    	}
    }
}

