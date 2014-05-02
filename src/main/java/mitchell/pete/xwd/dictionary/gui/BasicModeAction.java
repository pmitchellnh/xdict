package mitchell.pete.xwd.dictionary.gui;

import java.awt.event.*;

import javax.swing.*;

public class BasicModeAction extends AbstractAction
{
	private static final long serialVersionUID = 1L;
	private XDictGui gui = null;

    public BasicModeAction(XDictGui g) 
    {
        this.gui = g;

        putValue(Action.LONG_DESCRIPTION, "Select basic mode");
        putValue(Action.NAME, "Basic");
    }

    public void actionPerformed(ActionEvent e)
    {
		if ( gui != null )
		{
			gui.setAdvancedMode(false);
			gui.buildControlPanel();
		}
    }
}

