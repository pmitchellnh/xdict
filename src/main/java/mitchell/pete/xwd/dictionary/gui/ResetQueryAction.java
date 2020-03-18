package mitchell.pete.xwd.dictionary.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ResetQueryAction extends AbstractAction
{
	private static final long serialVersionUID = 1L;
	private XDictGui gui = null;
	private boolean ratingQuery = false;

    public ResetQueryAction(XDictGui g) 
    {
        this.gui = g;

        putValue(Action.SHORT_DESCRIPTION, "Reset query fields to default values");
        putValue(Action.NAME, "Reset Query");
    }
    
//    public void setRating(boolean rating) {
//    	ratingQuery = rating;
//    }

    public void actionPerformed(ActionEvent e)
    {
		if ( gui != null )
		{
            if (gui.isQueryEnabled())
                gui.resetQuery(false);
            else if (gui.isRatingEnabled())
                gui.resetQuery(true);
            else if (gui.isAddEnabled())
                gui.resetAdd();
            else if (gui.isExportEnabled())
                gui.resetExport();
            else if (gui.isLoadEnabled())
                gui.resetLoad();
		}
    }
}

