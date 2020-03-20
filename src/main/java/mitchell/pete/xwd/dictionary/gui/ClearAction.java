package mitchell.pete.xwd.dictionary.gui;

        import javax.swing.*;
        import java.awt.event.ActionEvent;
        import java.util.Date;

public class ClearAction extends AbstractAction implements Runnable
{
    private static final long serialVersionUID = 1L;
    private XDictGui gui = null;

    public ClearAction(XDictGui g)
    {
        this.gui = g;

        putValue(Action.SHORT_DESCRIPTION, "Clear all tables (Start over, or prepare for a full restore)");
        putValue(Action.NAME, "Clear Tables...");
    }

    public void actionPerformed(ActionEvent e)
    {
        new Thread(this).start();
    }

    public void run()
    {
        try
        {
            Date start = new Date();

            String status = gui.doClear();

            Date stop = new Date();
            gui.getStatusLine().showInfo( status + " (" + ((stop.getTime() - start.getTime()) / (double) 1000) + " secs)." );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(gui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
        }
    }
}

