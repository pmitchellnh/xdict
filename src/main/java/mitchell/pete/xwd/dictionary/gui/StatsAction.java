package mitchell.pete.xwd.dictionary.gui;

import javax.swing.*;
import java.awt.event.*;

public class StatsAction extends AbstractAction
{
    private static final long serialVersionUID = 1L;
    private XDictGui gui = null;

    public StatsAction(XDictGui g)
    {
        this.gui = g;

        putValue(Action.SHORT_DESCRIPTION, "Show Database Statistics");
        putValue(Action.NAME, "Database Stats");
    }

    public void actionPerformed(ActionEvent e)
    {
        if ( gui != null )
        {
            gui.getDatabaseStats();
        }
    }
}