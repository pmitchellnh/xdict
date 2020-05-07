package mitchell.pete.xdict.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class BreakdownAction extends AbstractAction
{
    private static final long serialVersionUID = 1L;
    private XDictGui gui = null;

    public BreakdownAction(XDictGui g)
    {
        this.gui = g;

        putValue(Action.SHORT_DESCRIPTION, "Report of rating ranges, broken down by word length");
        putValue(Action.NAME, "Rating Breakdown Report");
    }

    public void actionPerformed(ActionEvent e)
    {
        if ( gui != null )
        {
            new Thread() {
                public void run() {
                    gui.getRatingBreakdown();
                }
            }.start();
        }
    }
}