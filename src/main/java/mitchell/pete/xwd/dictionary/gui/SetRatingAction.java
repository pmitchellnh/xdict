
package mitchell.pete.xwd.dictionary.gui;

import mitchell.pete.xwd.dictionary.gui.RateAction.RATINGS;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SetRatingAction extends AbstractAction implements Runnable
{
    private static final long serialVersionUID = 1L;
    private XDictGui gui = null;
    private RATINGS rating;

    public SetRatingAction(XDictGui g, RATINGS r)
    {
        this.gui = g;
        rating = r;

        switch (rating) {
            case TERRIBLE:
                putValue(Action.SHORT_DESCRIPTION, "You're joking, right?.");
                putValue(Action.NAME, "Terrible");
                break;
            case POOR:
                putValue(Action.SHORT_DESCRIPTION, "I suppose. In a pinch.");
                putValue(Action.NAME, "Poor");
                break;
            case LAME:
                putValue(Action.SHORT_DESCRIPTION, "Not crazy about it.");
                putValue(Action.NAME, "Lame");
                break;
            case OK:
                putValue(Action.SHORT_DESCRIPTION, "Nothing wrong with it.");
                putValue(Action.NAME, "Ok");
                break;
            case GOOD:
                putValue(Action.SHORT_DESCRIPTION, "Yeah, that's pretty good.");
                putValue(Action.NAME, "Good");
                break;
            case EXCELLENT:
                putValue(Action.SHORT_DESCRIPTION, "Now that's what I'm talking about.");
                putValue(Action.NAME, "Excellent");
                break;
            case MANUAL:
                putValue(Action.SHORT_DESCRIPTION, "Rate manually using slider value.");
                putValue(Action.NAME, "Manual");
                break;
            case RESEARCH:
                putValue(Action.SHORT_DESCRIPTION, "Better check this one out.");
                putValue(Action.NAME, "Research");
                break;
            case SKIP:
                putValue(Action.SHORT_DESCRIPTION, "Skip for now.");
                putValue(Action.NAME, "Skip");
                break;
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        new Thread(this).start();
    }

    public void run()
    {
        try
        {
            String status = gui.doSetAddRate(rating);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
        }
    }
}

