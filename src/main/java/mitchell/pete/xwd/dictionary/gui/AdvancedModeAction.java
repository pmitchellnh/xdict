package mitchell.pete.xwd.dictionary.gui;

import java.awt.event.*;

import javax.swing.*;

public class AdvancedModeAction extends AbstractAction
{
	private static final long serialVersionUID = 1L;
	private XDictGui gui = null;

    public AdvancedModeAction(XDictGui g) 
    {
        this.gui = g;

        putValue(Action.LONG_DESCRIPTION, "Select advanced mode - more attributes");
        putValue(Action.NAME, "Advanced");
    }

    public void actionPerformed(ActionEvent e)
    {
		if ( gui != null )
		{
			gui.setAdvancedMode(true);
			gui.buildControlPanel();
		}
    }
}

