package mitchell.pete.xwd.dictionary.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Date;

public class QueryAction extends AbstractAction implements Runnable
{
	private static final long serialVersionUID = 1L;
	private XDictGui gui = null;
	private boolean queryNext = false;	// if true, get next bunch from existing query
	private boolean ratingQuery = false;	// if true, changes the way usedAny and usedNYT are handled

    public QueryAction(XDictGui g, boolean next, boolean rating) 
    {
        this.gui = g;
        queryNext = next;
        ratingQuery = rating;
        if (next) {
	        putValue(Action.SHORT_DESCRIPTION, "Get next group in result set");
	        putValue(Action.NAME, "Next");
        } else if (rating) {
	        putValue(Action.SHORT_DESCRIPTION, "Get words to rate that match current criteria");
	        putValue(Action.NAME, "Query");
        } else {
	        putValue(Action.SHORT_DESCRIPTION, "Get words matching current criteria");
	        putValue(Action.NAME, "Query");
        }
    }

    public void setRating(boolean rating) {
    	ratingQuery = rating;
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
    		
    		String results;
    		if (ratingQuery)
    			results = gui.doRatingQuery(queryNext);
    		else
    			results = gui.doQuery(queryNext);
    		
    		Date stop = new Date();
    		gui.getStatusLine().showInfo( "Query returned " + results + "; (" + ((stop.getTime() - start.getTime()) / (double) 1000) + " secs)." );
    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
    	}
    }
}

