package mitchell.pete.xdict.gui;

        import javax.swing.*;
        import java.awt.event.*;

public class DatabaseInfoAction extends AbstractAction
{
    private static final long serialVersionUID = 1L;
    private XDictGui gui = null;

    public DatabaseInfoAction(XDictGui g)
    {
        this.gui = g;

        putValue(Action.SHORT_DESCRIPTION, "List of all tables and number of entries");
        putValue(Action.NAME, "Database Info");
    }

    public void actionPerformed(ActionEvent e)
    {
        if ( gui != null )
        {
            gui.getDatabaseInfo();
        }
    }
}