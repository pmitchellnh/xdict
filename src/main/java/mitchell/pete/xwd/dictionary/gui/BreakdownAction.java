package mitchell.pete.xwd.dictionary.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class BreakdownAction extends AbstractAction
{
    private static final long serialVersionUID = 1L;
    private XDictGui gui = null;

    public BreakdownAction(XDictGui g)
    {
        this.gui = g;

        putValue(Action.SHORT_DESCRIPTION, "Rating Breakdown Report");
        putValue(Action.NAME, "Breakdown Report");
    }

    public void actionPerformed(ActionEvent e)
    {
        if ( gui != null )
        {
            gui.getRatingBreakdown();
        }
    }
}