package mitchell.pete.xwd.dictionary.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class StatusLine extends JTextField 
{
	private static final long serialVersionUID = 8188490595623120978L;

	public static final Font PLAIN_FONT = new Font("dialog", Font.PLAIN, 12);
    public static final Font BOLD_FONT = new Font("dialog", Font.BOLD,  12);

    public StatusLine(int preferredWidth) {
        setFont(BOLD_FONT);
        setEditable(false);
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        Dimension d = getPreferredSize();
        d.width = preferredWidth;
        setPreferredSize(d);
    }

    public void showInfo(String message) {
        SwingUtilities.invokeLater(new StatusLineUpdater(this, PLAIN_FONT, Color.black, message, message));
    }

    public void showError(String status) {
        SwingUtilities.invokeLater(new StatusLineUpdater(this, BOLD_FONT, Color.red, status, status));
    }

    public void clear() {
        SwingUtilities.invokeLater(new StatusLineUpdater(this, PLAIN_FONT, Color.black, "", null));
    }
}

class StatusLineUpdater implements Runnable 
{
    private StatusLine statusLine;
    private Font font;
    private Color color;
    private String message;
    private String tooltip;

    public StatusLineUpdater(StatusLine statusLine, Font font, Color color, String message, String tooltip) {
        this.statusLine = statusLine;
        this.font = font;
        this.color = color;
        this.message = message;
        this.tooltip = tooltip;
    }

    public void run() {
        statusLine.setFont(font);
        statusLine.setForeground(color);
        statusLine.setText(message);
        statusLine.setToolTipText(tooltip);
    }
}

