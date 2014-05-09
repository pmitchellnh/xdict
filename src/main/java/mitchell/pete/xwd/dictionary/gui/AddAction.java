package mitchell.pete.xwd.dictionary.gui;

import java.awt.event.*;
import java.util.Date;

import javax.swing.*;

import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.WORD_STATUS;

public class AddAction extends AbstractAction implements Runnable
{
	private static final long serialVersionUID = 1L;
	private XDictGui gui = null;

    public AddAction(XDictGui g) 
    {
        this.gui = g;

        putValue(Action.SHORT_DESCRIPTION, "Add this word");
        putValue(Action.NAME, "Add");
    }

    public void actionPerformed(ActionEvent e) 
    {
    	new Thread(this).start();
    }
    
    public void run()
    {
    	try
    	{
    		gui.getStatusLine().showInfo("Processing add...");
    		Date start = new Date();
    		
    		WORD_STATUS status = gui.doAdd();
    		String addStatus = "";
    		if (status == WORD_STATUS.NEW) {
    			addStatus = "Added new word.";
    		} else if (status == WORD_STATUS.EXISTS) {
    			addStatus = "Existing word modified.";
    		} else {
    			addStatus = "Duplicate word. No changes.";
    		}
    		
    		Date stop = new Date();
    		gui.getStatusLine().showInfo( addStatus + " (" + ((stop.getTime() - start.getTime()) / (double) 1000) + " secs)." );
    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
    	}
    }
}

