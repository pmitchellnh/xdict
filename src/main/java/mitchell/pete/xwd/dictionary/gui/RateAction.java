package mitchell.pete.xwd.dictionary.gui;

import mitchell.pete.xwd.dictionary.XDictConfig;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Date;

public class RateAction extends AbstractAction implements Runnable
{
    ;
	private static final long serialVersionUID = 1L;
	private XDictGui gui = null;
	private XDictConfig.RATINGS rating;

    public RateAction(XDictGui g, XDictConfig.RATINGS r)
    {
        this.gui = g;
        rating = r;

        putValue(Action.SHORT_DESCRIPTION, XDictConfig.getRateButtonDesc(rating));
        putValue(Action.NAME, XDictConfig.getRateButtonName(rating));

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
    		
    		String status = gui.doRate(rating);
    		
    		Date stop = new Date();
    		gui.getStatusLine().showInfo( status + " (" + ((stop.getTime() - start.getTime()) / (double) 1000) + " secs)." );
    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
    	}
    }
}

