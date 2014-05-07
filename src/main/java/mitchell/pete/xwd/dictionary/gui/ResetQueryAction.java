package mitchell.pete.xwd.dictionary.gui;

import java.awt.event.*;

import javax.swing.*;

public class ResetQueryAction extends AbstractAction
{
	private static final long serialVersionUID = 1L;
	private XDictGui gui = null;

    public ResetQueryAction(XDictGui g) 
    {
        this.gui = g;

        putValue(Action.LONG_DESCRIPTION, "Reset query fields to default values");
        putValue(Action.NAME, "Reset Query");
    }

    public void actionPerformed(ActionEvent e)
    {
		if ( gui != null )
		{
			gui.resetQuery();
		}
    }
}

