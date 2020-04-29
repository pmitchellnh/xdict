package mitchell.pete.xwd.dictionary.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

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
            new Thread() {
                public void run() {
                    gui.doLoad();
                }
            }.start();

    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
    	}
    }
}

