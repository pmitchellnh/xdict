
package mitchell.pete.xdict.gui;

import mitchell.pete.xdict.XDictConfig;
import mitchell.pete.xdict.XDictConfig.RATINGS;

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

        putValue(Action.SHORT_DESCRIPTION, XDictConfig.getRateButtonDesc(rating, -1));
        putValue(Action.NAME, XDictConfig.getRateButtonName(rating));
    }

    public void actionPerformed(ActionEvent e)
    {
        new Thread(this).start();
    }

    public void resetRatingDesc(int wordLength) {
        putValue(Action.SHORT_DESCRIPTION, XDictConfig.getRateButtonDesc(rating, wordLength));
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

