package mitchell.pete.xwd.dictionary.gui;

        import javax.swing.*;
        import java.awt.event.*;

public class ProgressAction extends AbstractAction
{
    private static final long serialVersionUID = 1L;
    private XDictGui gui = null;

    public ProgressAction(XDictGui g)
    {
        this.gui = g;

        putValue(Action.SHORT_DESCRIPTION, "Report of progress manually rating words");
        putValue(Action.NAME, "Rating Progress Report");
    }

    public void actionPerformed(ActionEvent e)
    {
        if ( gui != null )
        {
            new Thread() {
                public void run() {
                    gui.getRatingProgress();
                }
            }.start();
        }
    }
}