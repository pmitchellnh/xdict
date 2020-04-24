package mitchell.pete.xwd.dictionary.gui;

import mitchell.pete.xwd.dictionary.LoadAndExportUtilities;
import mitchell.pete.xwd.dictionary.Word;
import mitchell.pete.xwd.dictionary.XDictConfig;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.LengthControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.MethodControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.PatternControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.RatingControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.ResearchControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.UsedControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.WORD_STATUS;
import mitchell.pete.xwd.dictionary.db.XDictDB_MySQL;
import mitchell.pete.xwd.dictionary.XDictConfig.RATINGS;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class XDictGui extends JFrame implements WindowListener 
{

	private static final long serialVersionUID = 2093964455516510191L;

	// This is the DB...
	private static XDictDB_MySQL dict = new XDictDB_MySQL( "xdict" );

    // "Use modes" are basically just different use case tasks
    public enum USE_MODE { QUERY, ADD, RATE, LOAD, EXPORT };
    private USE_MODE useMode = USE_MODE.QUERY;
    private static final String PAD = "  ";
    private static final int QUERY_LIMIT = 1000;
    private static final int RATING_QUERY_LIMIT = 20;
    private static int queryStart = 0;
    private static int LENGTH_DEFAULT = 3;
    private static int RATING_DEFAULT = 1;
    private static int EXPORT_RATING_DEFAULT = 10;
    private static int LOAD_RATING_DEFAULT = 50;

    // Use this list to drive the manual rating process.
	private ArrayList<Word> listToRate = null;

    
    private static final String NO_RESULTS_FOUND = "No entries found that match this criteria.\n";

    private JMenuBar          menuBar                = new JMenuBar();
    private JMenu             xDictMenu              = new JMenu();
    private JMenu             databaseMenu           = new JMenu();
    private JMenu             viewMenu               = new JMenu();
    private JMenu             reportMenu             = new JMenu();
    private JMenu             helpMenu               = new JMenu();
    private JMenuItem		  resetQueryMenuItem	 = new JMenuItem(new ResetQueryAction(this));
    private JMenuItem         progressStatsMenuItem  = new JMenuItem(new ProgressAction(this));
    private JMenuItem         breakdownStatsMenuItem = new JMenuItem(new BreakdownAction(this));
    private JMenuItem         databaseInfoMenuItem   = new JMenuItem(new DatabaseInfoAction(this));
    private JMenuItem		  backupMenuItem	 	 = new JMenuItem(new BackupAction(this));
    private JMenuItem		  restoreMenuItem	 	 = new JMenuItem(new RestoreAction(this));
    private JMenuItem         clearMenuItem          = new JMenuItem(new ClearAction(this));
    private JMenuItem         helpMenuItem           = new JMenuItem(new OpenHelpAction(this));
    private JMenuItem         aboutMenuItem          = new JMenuItem(new AboutAction(this));
    private JTextArea         queryResultArea        = new JTextArea();
    private JScrollPane       queryScrollPane        = new JScrollPane();
    private JTextArea         addResultArea          = new JTextArea();
    private JTextArea         rateResultArea         = new JTextArea();
    private JTextArea         loadResultArea         = new JTextArea();
    private JScrollPane       loadScrollPane         = new JScrollPane();
    private JTextArea         exportResultArea       = new JTextArea();
    private JScrollPane       exportScrollPane       = new JScrollPane();
    private StatusLine        statusLine             = new StatusLine(420);
    private JProgressBar      progressBar            = new JProgressBar();
    
    //  Control components
    private JPanel controlPanel				= new JPanel();
    
    private JRadioButton queryEntryEquals = new JRadioButton("Equals");
    private JRadioButton queryEntryStarts = new JRadioButton("Starts with");
    private JRadioButton queryEntryContains = new JRadioButton("Contains");
    private JRadioButton queryLengthEquals = new JRadioButton("Equals");
    private JRadioButton queryLengthAtMost = new JRadioButton("At Most");
    private JRadioButton queryLengthAtLeast = new JRadioButton("At Least");
    private JRadioButton queryRatingAtMost = new JRadioButton("At Most");
    private JRadioButton queryRatingAtLeast = new JRadioButton("At Least");
    private JRadioButton queryRatingEquals = new JRadioButton("Equals");
    private JRadioButton queryMethodAll = new JRadioButton("All");
    private JRadioButton queryMethodManual = new JRadioButton("Hand-rated");
    private JRadioButton queryMethodAuto = new JRadioButton("Auto-loaded");

    private JTextField wordEntry            = new JTextField(30);
    private JTextField wordComment          = new JTextField(99);
    private JLabel wordToRate           	= new JLabel();
    private JSlider wordLengthSlider        = new JSlider(3,25,LENGTH_DEFAULT);
    private JLabel wordLengthLabel          = new JLabel(String.valueOf(wordLengthSlider.getValue()));
    private JSlider wordRatingSlider        = new JSlider(0,100,RATING_DEFAULT);
    private JLabel wordRatingLabel          = new JLabel(String.valueOf(wordRatingSlider.getValue()));
    private JSlider manualRatingSlider		= new JSlider(0,100,RATING_DEFAULT);
    private JLabel manualRatingLabel        = new JLabel(String.valueOf(manualRatingSlider.getValue()));
    private JCheckBox usedAny               = new JCheckBox("Used Any");
    private JCheckBox usedNYT               = new JCheckBox("Used NYT");
    private JCheckBox notUsed               = new JCheckBox("Not Used");
    private JCheckBox research              = new JCheckBox("Needs Research");
    private JButton queryButton				= new JButton(new QueryAction(this, false, false));
    private JButton nextButton				= new JButton(new QueryAction(this, true, false));
    private JButton rateQueryButton			= new JButton(new QueryAction(this, false, true));
    private JButton addButton				= new JButton(new AddAction(this));
    private JButton loadButton				= new JButton(new LoadAction(this));
    private JButton exportButton		    = new JButton(new ExportAction(this));
    private JButton killButton		        = new JButton(new RateAction(this, XDictConfig.RATINGS.KILL));
    private JButton terribleButton		    = new JButton(new RateAction(this, XDictConfig.RATINGS.TERRIBLE));
    private JButton poorButton		        = new JButton(new RateAction(this, XDictConfig.RATINGS.POOR));
    private JButton lameButton		        = new JButton(new RateAction(this, XDictConfig.RATINGS.LAME));
    private JButton okButton   		        = new JButton(new RateAction(this, XDictConfig.RATINGS.OK));
    private JButton goodButton		        = new JButton(new RateAction(this, XDictConfig.RATINGS.GOOD));
    private JButton excellentButton		    = new JButton(new RateAction(this, XDictConfig.RATINGS.EXCELLENT));
    private JButton terribleButton2		    = new JButton(new SetRatingAction(this, XDictConfig.RATINGS.TERRIBLE));
    private JButton poorButton2		        = new JButton(new SetRatingAction(this, XDictConfig.RATINGS.POOR));
    private JButton lameButton2		        = new JButton(new SetRatingAction(this, XDictConfig.RATINGS.LAME));
    private JButton okButton2   		    = new JButton(new SetRatingAction(this, XDictConfig.RATINGS.OK));
    private JButton goodButton2		        = new JButton(new SetRatingAction(this, XDictConfig.RATINGS.GOOD));
    private JButton excellentButton2		= new JButton(new SetRatingAction(this, XDictConfig.RATINGS.EXCELLENT));
    private JButton researchButton		    = new JButton(new RateAction(this, XDictConfig.RATINGS.RESEARCH));
    private JButton skipButton		    	= new JButton(new RateAction(this, XDictConfig.RATINGS.SKIP));
    private JButton manualButton		    = new JButton(new RateAction(this, XDictConfig.RATINGS.MANUAL));
    private JTabbedPane resultPaneTabs 		= new JTabbedPane();
    
    private JTextField loadFile            = new JTextField(50);
    private JTextField exportFile            = new JTextField(50);



    ChangeListener lengthListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    		JSlider source = (JSlider)e.getSource();
    	    wordLengthLabel.setText( PAD + String.valueOf(source.getValue()) + PAD);
    	    nextButton.setEnabled(false);
            if (queryButton.isEnabled())
                getRootPane().setDefaultButton(queryButton);
            else if (rateQueryButton.isEnabled())
                getRootPane().setDefaultButton(rateQueryButton);

        }
    };
    ChangeListener ratingListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    		JSlider source = (JSlider)e.getSource();
    	    wordRatingLabel.setText( PAD + String.valueOf(source.getValue()) + PAD);
    	    nextButton.setEnabled(false);
            if (queryButton.isEnabled())
                getRootPane().setDefaultButton(queryButton);
            else if (rateQueryButton.isEnabled())
                getRootPane().setDefaultButton(rateQueryButton);
    	}
    };
    ChangeListener manualRatingListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    		JSlider source = (JSlider)e.getSource();
    	    manualRatingLabel.setText(PAD + String.valueOf(source.getValue()) + PAD);
    	}
    };
    ChangeListener tabListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    		JTabbedPane source = (JTabbedPane)e.getSource();
    		if (source.getSelectedIndex() == USE_MODE.QUERY.ordinal()) {
    			queryButton.setEnabled(true);
    			nextButton.setEnabled(false);
    			addButton.setEnabled(false);
    			loadButton.setEnabled(false);
    			exportButton.setEnabled(false);
    			rateQueryButton.setEnabled(false);
                queryButton.setVisible(true);
                nextButton.setVisible(true);
                addButton.setVisible(false);
                loadButton.setVisible(false);
                exportButton.setVisible(false);
                rateQueryButton.setVisible(false);
                usedNYT.removeChangeListener(usedNYT.getChangeListeners()[0]);
                usedAny.removeChangeListener(usedAny.getChangeListeners()[0]);
                usedNYT.addChangeListener(usedNYTListener);
                usedAny.addChangeListener(usedAnyListener);
    			resetQuery(false);
                getRootPane().setDefaultButton(queryButton);
    		} else if (source.getSelectedIndex() == USE_MODE.ADD.ordinal()) {
    			queryButton.setEnabled(false);
    			nextButton.setEnabled(false);
    			addButton.setEnabled(true);
    			loadButton.setEnabled(false);
    			exportButton.setEnabled(false);
    			rateQueryButton.setEnabled(false);
                queryButton.setVisible(false);
                nextButton.setVisible(false);
                addButton.setVisible(true);
                loadButton.setVisible(false);
                exportButton.setVisible(false);
                rateQueryButton.setVisible(false);
                usedNYT.removeChangeListener(usedNYT.getChangeListeners()[0]);
                usedAny.removeChangeListener(usedAny.getChangeListeners()[0]);
                usedNYT.addChangeListener(usedNYTListenerAddOrLoad);
                usedAny.addChangeListener(usedAnyListenerAddOrLoad);
                resetAdd();
                getRootPane().setDefaultButton(addButton);
			} else if (source.getSelectedIndex() == USE_MODE.LOAD.ordinal()) {
				queryButton.setEnabled(false);
				nextButton.setEnabled(false);
				addButton.setEnabled(false);
				loadButton.setEnabled(true);
    			exportButton.setEnabled(false);
    			rateQueryButton.setEnabled(false);
                queryButton.setVisible(false);
                nextButton.setVisible(false);
                addButton.setVisible(false);
                loadButton.setVisible(true);
                exportButton.setVisible(false);
                rateQueryButton.setVisible(false);
                usedNYT.removeChangeListener(usedNYT.getChangeListeners()[0]);
                usedAny.removeChangeListener(usedAny.getChangeListeners()[0]);
                usedNYT.addChangeListener(usedNYTListenerAddOrLoad);
                usedAny.addChangeListener(usedAnyListenerAddOrLoad);
                resetLoad();
                getRootPane().setDefaultButton(loadButton);
			} else if (source.getSelectedIndex() == USE_MODE.EXPORT.ordinal()) {
				queryButton.setEnabled(false);
				nextButton.setEnabled(false);
				addButton.setEnabled(false);
				loadButton.setEnabled(false);
    			exportButton.setEnabled(true);
    			rateQueryButton.setEnabled(false);
                queryButton.setVisible(false);
                nextButton.setVisible(false);
                addButton.setVisible(false);
                loadButton.setVisible(false);
                exportButton.setVisible(true);
                rateQueryButton.setVisible(false);
                usedNYT.removeChangeListener(usedNYT.getChangeListeners()[0]);
                usedAny.removeChangeListener(usedAny.getChangeListeners()[0]);
                usedNYT.addChangeListener(usedNYTListener);
                usedAny.addChangeListener(usedAnyListener);
                resetExport();
                getRootPane().setDefaultButton(exportButton);
			} else if (source.getSelectedIndex() == USE_MODE.RATE.ordinal()) {
				queryButton.setEnabled(false);
				nextButton.setEnabled(false);
				addButton.setEnabled(false);
				loadButton.setEnabled(false);
				exportButton.setEnabled(false);
				rateQueryButton.setEnabled(true);
                queryButton.setVisible(false);
                nextButton.setVisible(true);
                addButton.setVisible(false);
                loadButton.setVisible(false);
                exportButton.setVisible(false);
                rateQueryButton.setVisible(true);
                usedNYT.removeChangeListener(usedNYT.getChangeListeners()[0]);
                usedAny.removeChangeListener(usedAny.getChangeListeners()[0]);
                usedNYT.addChangeListener(usedNYTListener);
                usedAny.addChangeListener(usedAnyListener);
    			resetQuery(true);
                getRootPane().setDefaultButton(rateQueryButton);
			}
    	}
    };
    ChangeListener usedAnyListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    	    nextButton.setEnabled(false);
            if (queryButton.isEnabled())
                getRootPane().setDefaultButton(queryButton);
            else if (rateQueryButton.isEnabled())
                getRootPane().setDefaultButton(rateQueryButton);
    	}
    };
    ChangeListener usedAnyListenerAddOrLoad = new ChangeListener()
    {
        public void stateChanged(ChangeEvent e)
        {
            JCheckBox source = (JCheckBox)e.getSource();
            // If not selected, then NYT cannot be selected
            if (!source.isSelected()) {
                usedNYT.setSelected(false);
            }
            nextButton.setEnabled(false);
            if (queryButton.isEnabled())
                getRootPane().setDefaultButton(queryButton);
            else if (rateQueryButton.isEnabled())
                getRootPane().setDefaultButton(rateQueryButton);
        }
    };
    ChangeListener usedNYTListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    	    nextButton.setEnabled(false);
            if (queryButton.isEnabled())
                getRootPane().setDefaultButton(queryButton);
            else if (rateQueryButton.isEnabled())
                getRootPane().setDefaultButton(rateQueryButton);
    	}
    };
    ChangeListener usedNYTListenerAddOrLoad = new ChangeListener()
    {
        public void stateChanged(ChangeEvent e)
        {
            JCheckBox source = (JCheckBox)e.getSource();
            // If selected, then "Any" must also be selected
            if (source.isSelected()) {
                usedAny.setSelected(true);
            }
            nextButton.setEnabled(false);
            if (queryButton.isEnabled())
                getRootPane().setDefaultButton(queryButton);
            else if (rateQueryButton.isEnabled())
                getRootPane().setDefaultButton(rateQueryButton);
        }
    };

    ChangeListener notUsedListener = new ChangeListener()
    {
        public void stateChanged(ChangeEvent e)
        {
            nextButton.setEnabled(false);
            if (queryButton.isEnabled())
                getRootPane().setDefaultButton(queryButton);
            else if (rateQueryButton.isEnabled())
                getRootPane().setDefaultButton(rateQueryButton);
        }
    };

    ChangeListener queryChangedListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    	    nextButton.setEnabled(false);
            if (queryButton.isEnabled())
                getRootPane().setDefaultButton(queryButton);
            else if (rateQueryButton.isEnabled())
                getRootPane().setDefaultButton(rateQueryButton);
    	}
    };

    public XDictGui() 
    {
        this.setSize(new Dimension(XDictConfig.APP_WIDTH, XDictConfig.APP_HEIGHT));
        this.setTitle("XDict");
        //this.setIconImage(new ImageIcon(getClass().getResource("icons/logoicon.gif")).getImage());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //this.addWindowListener(new WindowKiller());
        this.addWindowListener(this);

        /*
         * Use the following as model to set tool tips
         */
        //packageScope.setToolTipText("Select packages");
        //packageFilter.setToolTipText("Show dependencies to/from packages");
        //packageFilterIncludes.setToolTipText("Package at the other end of the dependency must match any these expressions. E.g., /^com.mycompany/, /\\.get\\w+\\(/");

        setupSliders();
        setupListeners();
        
        buildMenus();
        buildUI();

        statusLine.showInfo("Ready.");
    }

    /*
     ************  MENUS ************
     */
    private void buildMenus() {
        buildXDictMenu();
        buildViewMenu();
        buildReportMenu();
        buildDatabaseMenu();
        buildHelpMenu();

        this.setJMenuBar(menuBar);
    }

    private void buildXDictMenu() {
        menuBar.add(xDictMenu);

        Font f = xDictMenu.getFont();
        xDictMenu.setFont(new Font(f.getFontName(), Font.BOLD, f.getSize()));

        xDictMenu.setText("XDict");
        xDictMenu.add(aboutMenuItem);
    }

    private void buildDatabaseMenu() {
        menuBar.add(databaseMenu);

        databaseMenu.setText("Database");
        databaseMenu.add(databaseInfoMenuItem);
        databaseMenu.add(backupMenuItem);
        databaseMenu.add(restoreMenuItem);
        databaseMenu.add(clearMenuItem);
    }
    
    private void buildViewMenu() 
    {
        menuBar.add(viewMenu);

        viewMenu.setText("View");

        viewMenu.add(resetQueryMenuItem);
    }

    private void buildReportMenu()
    {
        menuBar.add(reportMenu);

        reportMenu.setText("Reports");

        reportMenu.add(progressStatsMenuItem);
        reportMenu.add(breakdownStatsMenuItem);
    }

    private void buildHelpMenu()
    {
        menuBar.add(helpMenu);
        helpMenu.setText("Help");
        helpMenu.add(helpMenuItem);
    }

    /*
     ************  UI ************
     */
    private void buildUI() {
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(buildControlPanel(), BorderLayout.NORTH);
        this.getContentPane().add(buildDisplayPanel(), BorderLayout.CENTER);
        this.getContentPane().add(buildStatusPanel(), BorderLayout.SOUTH);
    }
    
    private JComponent buildDisplayPanel() 
    {
        resultPaneTabs.setBorder(BorderFactory.createTitledBorder(""));

        resultPaneTabs.addTab("Query", buildQueryDisplayPanel());
        resultPaneTabs.addTab("Add", buildAddPanel());
        resultPaneTabs.addTab("Rate", buildRatePanel());
        resultPaneTabs.addTab("Load", buildLoadPanel());
        resultPaneTabs.addTab("Export", buildExportPanel());
        
        return resultPaneTabs;
    }

    /*
     *	Build control panel in either basic or advanced mode 
     */
    public JComponent buildControlPanel() 
    {
    	controlPanel.removeAll();
            buildSimpleQueryControlPanel();
        controlPanel.revalidate();
        
        return controlPanel;
    }
    
    
    /*
     ************  QUERY PANELS ************
     */
    private JComponent buildSimpleQueryControlPanel()
    {
        controlPanel.setBorder(BorderFactory.createTitledBorder(""));
        GridBagLayout      gbl = new GridBagLayout();
        GridBagConstraints c   = new GridBagConstraints();
        c.insets = new Insets(0, 2, 0, 2);
        controlPanel.setLayout(gbl);

        // Entry
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 6;
        JComponent b1 = buildRadioButton3(queryEntryEquals, queryEntryStarts, queryEntryContains, 1 );
        JComponent entryMatch = buildGenericCombo2("Entry", b1, wordEntry);
        controlPanel.add(entryMatch);
        gbl.setConstraints(entryMatch, c);
        c.gridwidth = 1;	// reset

        // Length
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 6;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        Component entryLength = buildComboSlider("Length", buildLengthRadioButton(), wordLengthLabel, wordLengthSlider);
        controlPanel.add(entryLength);
        gbl.setConstraints(entryLength, c);
        c.gridwidth = 1;

        // Method Modified
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 3;
        JComponent b3 = buildRadioButton3(queryMethodAll, queryMethodManual, queryMethodAuto, 1 );
        controlPanel.add(b3);
        gbl.setConstraints(b3, c);
        c.gridwidth = 1;

        // UsedNYT Checkbox
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        c.weighty = 0;
        controlPanel.add(usedNYT);
        gbl.setConstraints(usedNYT, c);

        // UsedAny Checkbox
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 0;
        c.weighty = 0;
        controlPanel.add(usedAny);
        gbl.setConstraints(usedAny, c);
        
        // NotUsed Checkbox
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 2;
        c.gridy = 2;
        c.weightx = 0;
        c.weighty = 0;
        controlPanel.add(notUsed);
        gbl.setConstraints(notUsed, c);

        // Research Checkbox
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 3;
        c.gridy = 2;
        c.weightx = 0;
        c.weighty = 0;
        controlPanel.add(research);
        gbl.setConstraints(research, c);
        
        // Rating 
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 6;
        c.gridy = 2;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        JComponent b2 = buildRadioButton3(queryRatingAtLeast, queryRatingAtMost, queryRatingEquals, 1);
        JComponent entryRating = buildComboSlider("Rating", b2, wordRatingLabel, wordRatingSlider);
        controlPanel.add(entryRating);
        gbl.setConstraints(entryRating, c);
        c.gridwidth = 1;

        // Comment Label
        JLabel lab = new JLabel("Comment");
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 4;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        controlPanel.add(lab);
        gbl.setConstraints(lab, c);
        c.gridwidth = 1;

        // Comment
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 4;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 6;
        controlPanel.add(wordComment);
        gbl.setConstraints(wordComment, c);
        c.gridwidth = 1;

        // Query Button
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        controlPanel.add(queryButton);
        gbl.setConstraints(queryButton, c);
       
        // Rating Query Button
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        controlPanel.add(rateQueryButton);
        gbl.setConstraints(rateQueryButton, c);
        rateQueryButton.setEnabled(false);	// init to disabled
        rateQueryButton.setVisible(false);

        // Next Button
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 5;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        controlPanel.add(nextButton);
        gbl.setConstraints(nextButton, c);
        nextButton.setEnabled(false);

        // Add Button
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        controlPanel.add(addButton);
        gbl.setConstraints(addButton, c);
        addButton.setEnabled(false);	// init to disabled
        addButton.setVisible(false);

        // Load Button
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        controlPanel.add(loadButton);
        gbl.setConstraints(loadButton, c);
        loadButton.setEnabled(false);	// init to disabled
        loadButton.setVisible(false);
        
        // Export Button
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        controlPanel.add(exportButton);
        gbl.setConstraints(exportButton, c);
        exportButton.setEnabled(false);	// init to disabled
        exportButton.setVisible(false);

        return controlPanel;
    }
    
    private JComponent buildQueryDisplayPanel() 
    {
        queryScrollPane = new JScrollPane(queryResultArea);
        queryResultArea.setText("Query Result Area");
        return queryScrollPane;
    }

    private JComponent buildLengthRadioButton()
    {
    	JPanel result = new JPanel();
        result.setLayout(new GridLayout(1, 3));

    	// Add radio buttons to control useMode
        ButtonGroup group = new ButtonGroup();
        
        queryLengthAtLeast.setSelected(true);
        group.add(queryLengthEquals);
        group.add(queryLengthAtMost);
        group.add(queryLengthAtLeast);
        
    	queryLengthEquals.setVerticalAlignment(SwingConstants.TOP);
    	queryLengthAtMost.setVerticalAlignment(SwingConstants.TOP);
    	queryLengthAtLeast.setVerticalAlignment(SwingConstants.TOP);
    	
        result.add(queryLengthEquals);
        result.add(queryLengthAtLeast);
        result.add(queryLengthAtMost);

        return result;
    }
    
    /*
     ************  ADD PANELS ************
     */
    private JComponent buildAddPanel()
    {
    	JPanel result = new JPanel();
    	result.setLayout(new BorderLayout());
    	result.add(buildAddControlPanel(), BorderLayout.NORTH);
    	result.add(buildAddDisplayPanel(), BorderLayout.CENTER);
    	return result;
    }
    
    private JComponent buildAddDisplayPanel() 
    {
        JPanel result = new JPanel();
        result.setBorder(BorderFactory.createTitledBorder(""));

        addResultArea.setText("Add Result Area");
        result.add(addResultArea);

        return result;
    }

    private JComponent buildAddControlPanel()
    {
        JPanel result = new JPanel();
        result.setLayout(new BorderLayout());
        result.setBorder(BorderFactory.createTitledBorder("Rating"));
        result.add(buildAddControlButtons(), BorderLayout.CENTER);
        return result;
    }

    /*
     ************  RATE PANELS ************
     */
    private JComponent buildRatePanel()
    {
    	JPanel result = new JPanel();
    	result.setLayout(new BorderLayout());
    	result.add(buildRateControlPanel(), BorderLayout.NORTH);
    	result.add(buildRateDisplayPanel(), BorderLayout.CENTER);
    	
    	return result;
    }
    
    private JComponent buildRateControlPanel() 
    {
    	JPanel result = new JPanel();
    	result.setLayout(new BorderLayout());
        result.setBorder(BorderFactory.createTitledBorder("Rating"));
    	result.add(wordToRate, BorderLayout.NORTH);
    	wordToRate.setHorizontalAlignment(SwingConstants.CENTER);
    	result.add(buildRateControlButtons(), BorderLayout.CENTER);
        return result;
    }
    
    private JComponent buildRateControlButtons()
    {
    	JPanel result = new JPanel();
        result.add(killButton);
        result.add(terribleButton);
        result.add(poorButton);
        result.add(lameButton);
        result.add(okButton);
        result.add(goodButton);
        result.add(excellentButton);
        result.add(manualButton);
        JComponent entryRating = buildGenericCombo2("Manual Rating", manualRatingLabel, manualRatingSlider);
        result.add(entryRating);
        result.add(researchButton);
        result.add(skipButton);
        setRatingButtons(false);
        return result;
    }

    private JComponent buildAddControlButtons()
    {
        JPanel result = new JPanel();
        result.add(terribleButton2);
        result.add(poorButton2);
        result.add(lameButton2);
        result.add(okButton2);
        result.add(goodButton2);
        result.add(excellentButton2);
        setAddRatingButtons(true);
        return result;
    }

    private void setRatingButtons(boolean state) {
        killButton.setEnabled(state);
    	terribleButton.setEnabled(state);
    	poorButton.setEnabled(state);
    	lameButton.setEnabled(state);
    	okButton.setEnabled(state);
    	goodButton.setEnabled(state);
    	excellentButton.setEnabled(state);
    	manualButton.setEnabled(state);
    	researchButton.setEnabled(state);
    	skipButton.setEnabled(state);
    }

    private void setAddRatingButtons(boolean state) {
        terribleButton2.setEnabled(state);
        poorButton2.setEnabled(state);
        lameButton2.setEnabled(state);
        okButton2.setEnabled(state);
        goodButton2.setEnabled(state);
        excellentButton2.setEnabled(state);
    }

    private JComponent buildRateDisplayPanel()
    {
    	JPanel result = new JPanel();
        result.setBorder(BorderFactory.createTitledBorder(""));

        rateResultArea.setText("");
        result.add(rateResultArea);
        
        return result;
    }

    /*
     ************  LOAD PANELS ************
     */
    private JComponent buildLoadPanel()
    {
    	JPanel result = new JPanel();
    	result.setLayout(new BorderLayout());
    	result.add(buildLoadControlPanel(), BorderLayout.NORTH);
    	result.add(buildLoadDisplayPanel(), BorderLayout.CENTER);
    	
    	return result;
    }
    
    private JComponent buildLoadControlPanel() 
    {
    	JPanel result = new JPanel();
    	result.setLayout(new BorderLayout());
        result.setBorder(BorderFactory.createTitledBorder(""));
        result.add(new JLabel("File to Load:"), BorderLayout.WEST);
        result.add(loadFile, BorderLayout.CENTER);
        loadFile.setText("");
        
        return result;
    }
    
    private JComponent buildLoadDisplayPanel() 
    {
        loadScrollPane = new JScrollPane(loadResultArea);
        loadResultArea.setText("");
        return loadScrollPane;
    }
    
    /*
     ************  EXPORT PANELS ************
     */
    private JComponent buildExportPanel()
    {
    	JPanel result = new JPanel();
    	result.setLayout(new BorderLayout());
    	result.add(buildExportControlPanel(), BorderLayout.NORTH);
    	result.add(buildExportDisplayPanel(), BorderLayout.CENTER);
    	
    	return result;
    }
    
    private JComponent buildExportControlPanel() 
    {
    	JPanel result = new JPanel();
    	result.setLayout(new BorderLayout());
        result.setBorder(BorderFactory.createTitledBorder(""));
        result.add(new JLabel("File to Export:"), BorderLayout.WEST);
        result.add(exportFile, BorderLayout.CENTER);
        exportFile.setText("export/");
        
        return result;
    }
    
    private JComponent buildExportDisplayPanel() 
    {
        exportScrollPane = new JScrollPane(exportResultArea);
        exportResultArea.setText("");
        return exportScrollPane;
    }

    /*
     ************  GUI CONTROL UTILITIES ************
     */
    private JComponent buildComboSlider(String title, JComponent buttons, JLabel label, JSlider slider )
    {
    	JPanel result = new JPanel();
        result.setBorder(BorderFactory.createTitledBorder(title));
        result.setLayout(new BorderLayout());

        // Radio Button
        result.add(buttons, BorderLayout.WEST);
        
        // Text (Value of Slider)
        label.setText(PAD + String.valueOf(slider.getValue()) + PAD);
        result.add(label, BorderLayout.CENTER);
        
        // Slider
        result.add(slider, BorderLayout.EAST);
        
        return result;
    }

    // Radio button with 2 options
    private JComponent buildRadioButton2(JRadioButton b1, JRadioButton b2, int defaultButton)
    {
    	JPanel result = new JPanel();
        result.setLayout(new GridLayout(1, 2));

        ButtonGroup group = new ButtonGroup();

        switch (defaultButton)
        {
        case 1:
        	b1.setSelected(true);
        	break;
        case 2:
        	b2.setSelected(true);
        	break;
       	default:
       		break;
        }
        group.add(b1);
        group.add(b2);
        
    	b1.setVerticalAlignment(SwingConstants.TOP);
    	b2.setVerticalAlignment(SwingConstants.TOP);
    	
        result.add(b1);
        result.add(b2);

        return result;
    }

    // Radio button with 3 options
    private JComponent buildRadioButton3(JRadioButton b1, JRadioButton b2, JRadioButton b3, int defaultButton)
    {
    	JPanel result = new JPanel();
        result.setLayout(new GridLayout(1, 3));

        ButtonGroup group = new ButtonGroup();

        switch (defaultButton)
        {
        case 1:
        	b1.setSelected(true);
        	break;
        case 2:
        	b2.setSelected(true);
        	break;
        case 3:
        	b3.setSelected(true);
        	break;
       	default:
       		break;
        }
        group.add(b1);
        group.add(b2);
        group.add(b3);
        
    	b1.setVerticalAlignment(SwingConstants.TOP);
    	b2.setVerticalAlignment(SwingConstants.TOP);
    	b3.setVerticalAlignment(SwingConstants.TOP);
    	
        result.add(b1);
        result.add(b2);
        result.add(b3);

        return result;
    }
    
    private JComponent buildGenericCombo2(String title, JComponent j1, JComponent j2 )
    {
    	JPanel result = new JPanel();
        result.setBorder(BorderFactory.createTitledBorder(title));
        result.setLayout(new BorderLayout());

        // First component
        result.add(j1, BorderLayout.WEST);
        
        // Second component
        result.add(j2, BorderLayout.CENTER);
        
        return result;
    }
    
    /*
     * This table implementation looks good -- need to research it
     */
    /*
    private JComponent buildMetricsChartPanel() {
        JComponent result;

        JTable table = new JTable(metricsChartModel);

        table.setCellSelectionEnabled(true);
        table.setColumnSelectionAllowed(true);
        
        result = new JScrollPane(table);

        return result;
    }
    */
        
	public USE_MODE getUseMode() {
		return useMode;
	}

	public void setUseMode(USE_MODE useMode) {
		this.useMode = useMode;
	}
	
    StatusLine getStatusLine() {
        return statusLine;
     }
         
     JProgressBar getProgressBar() {
         return progressBar;
     }

    private JComponent buildStatusPanel() {
        JPanel result = new JPanel();

        Dimension size = getProgressBar().getPreferredSize();
        size.width = 100;
        getProgressBar().setPreferredSize(size);
        getProgressBar().setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        getStatusLine().setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        
        result.setLayout(new BorderLayout());
        result.add(getStatusLine(),  BorderLayout.CENTER);
        result.add(getProgressBar(), BorderLayout.EAST);
        
        return result;
    }

    public boolean isQueryEnabled() { return queryButton.isEnabled(); }
    public boolean isRatingEnabled() { return rateQueryButton.isEnabled(); }
    public boolean isAddEnabled() { return addButton.isEnabled(); }
    public boolean isExportEnabled() { return exportButton.isEnabled(); }
    public boolean isLoadEnabled() { return loadButton.isEnabled(); }

    public void setupSliders()
    {
        wordLengthSlider.setMajorTickSpacing(2);
        wordLengthSlider.setMinorTickSpacing(1);
        wordLengthSlider.setPaintTicks(true);
        wordLengthSlider.setPaintLabels(true);
        wordLengthSlider.setSnapToTicks(true);

        wordRatingSlider.setMajorTickSpacing(10);
        wordRatingSlider.setMinorTickSpacing(2);
        wordRatingSlider.setPaintTicks(true);
        wordRatingSlider.setPaintLabels(true);

        manualRatingSlider.setMajorTickSpacing(10);
        manualRatingSlider.setMinorTickSpacing(2);
        manualRatingSlider.setPaintTicks(true);
        manualRatingSlider.setPaintLabels(true);
    }

    public void setupListeners()
    {
        wordLengthSlider.addChangeListener(lengthListener);
        wordRatingSlider.addChangeListener(ratingListener);
        usedNYT.addChangeListener(usedNYTListener);
        usedAny.addChangeListener(usedAnyListener);
        notUsed.addChangeListener(notUsedListener);
        research.addChangeListener(queryChangedListener);
        queryEntryEquals.addChangeListener(queryChangedListener);
        queryEntryStarts.addChangeListener(queryChangedListener);
        queryEntryContains.addChangeListener(queryChangedListener);
        queryLengthEquals.addChangeListener(queryChangedListener);
        queryLengthAtMost.addChangeListener(queryChangedListener);
        queryLengthAtLeast.addChangeListener(queryChangedListener);
        queryRatingAtMost.addChangeListener(queryChangedListener);
        queryRatingAtLeast.addChangeListener(queryChangedListener);
        queryMethodAll.addChangeListener(queryChangedListener);
        queryMethodManual.addChangeListener(queryChangedListener);
        queryMethodAuto.addChangeListener(queryChangedListener);
        resultPaneTabs.addChangeListener(tabListener);
        manualRatingSlider.addChangeListener(manualRatingListener);
    }


    public void resetQuery(boolean rating) {
//        wordEntry.setText("");
        wordLengthSlider.setValue(LENGTH_DEFAULT);
        wordRatingSlider.setValue(RATING_DEFAULT);
        manualRatingSlider.setValue(RATING_DEFAULT);
        usedAny.setSelected(true);
        usedNYT.setSelected(true);
        notUsed.setSelected(true);
        research.setSelected(false);
        if (rating) {
            queryMethodAuto.setSelected(true);
            queryLengthAtLeast.setSelected(true);
        } else {
            queryMethodAll.setSelected(true);
            queryLengthAtLeast.setSelected(true);
        }
        queryEntryEquals.setSelected(true);
        queryRatingAtLeast.setSelected(true);
        wordComment.setText("");
        queryResultArea.setText("");

    }

    public void resetExport() {
        wordEntry.setText("");
        wordLengthSlider.setValue(LENGTH_DEFAULT);
        wordRatingSlider.setValue(EXPORT_RATING_DEFAULT);
        manualRatingSlider.setValue(EXPORT_RATING_DEFAULT);
        usedAny.setSelected(true);
        usedNYT.setSelected(true);
        notUsed.setSelected(true);
        research.setSelected(false);
        queryMethodAll.setSelected(true);
        queryLengthAtLeast.setSelected(true);
        queryEntryEquals.setSelected(true);
        queryRatingAtLeast.setSelected(true);
        wordComment.setText("");
        exportResultArea.setText("");

    }

    public void resetLoad() {
        wordEntry.setText("");
        wordLengthSlider.setValue(LENGTH_DEFAULT);
        wordRatingSlider.setValue(LOAD_RATING_DEFAULT);
        usedAny.setSelected(false);
        usedNYT.setSelected(false);
        notUsed.setSelected(true);
        research.setSelected(false);
        queryMethodAuto.setSelected(true);
        queryLengthAtLeast.setSelected(true);
        queryEntryEquals.setSelected(true);
        queryRatingEquals.setSelected(true);
        wordComment.setText("");
        loadFile.setText("");
        loadResultArea.setText("");
    }

    public void resetAdd() {
        wordLengthSlider.setValue(LENGTH_DEFAULT);
        wordRatingSlider.setValue(RATING_DEFAULT);
        manualRatingSlider.setValue(RATING_DEFAULT);
        usedAny.setSelected(false);
        usedNYT.setSelected(false);
        notUsed.setSelected(true);
        research.setSelected(false);
        queryMethodManual.setSelected(true);
        queryLengthAtLeast.setSelected(true);
        queryEntryEquals.setSelected(true);
        queryRatingAtLeast.setSelected(true);
        wordComment.setText("");
        addResultArea.setText("");

    }

    /*
     ************  ACTIONS ************
     */

    public WORD_STATUS doAdd()
    {
    	addResultArea.setText("");
    	String key = wordEntry.getText();
    	int rat = wordRatingSlider.getValue();
    	UsedControl useCtrl = UsedControl.ALL;
    	ResearchControl resCtrl = ResearchControl.NO_RESEARCH;
    	WORD_STATUS status;

    	if ( usedNYT.isSelected() )
    		useCtrl = UsedControl.NYT;
    	else if ( usedAny.isSelected() )
        		useCtrl = UsedControl.ANY;
    	
    	if ( research.isSelected() )
    		resCtrl = ResearchControl.NEEDS_RESEARCH;
    	
    	Word w = new Word.Builder(key).rating((byte)rat).usedAny(useCtrl == UsedControl.ANY).usedNYT(useCtrl == UsedControl.NYT).needsResearch(resCtrl == ResearchControl.NEEDS_RESEARCH).manuallyRated(true).build();
    	if (!wordComment.getText().isEmpty()) {
    		w.setComment(wordComment.getText());
    	}
    	if (w.length() < 3) {
    		status = WORD_STATUS.ERROR;
    		addResultArea.setText("Error: " + w.getEntry() + " is less than 3 characters.");
    	} else if (w.length() > 25) {
    		status = WORD_STATUS.ERROR;
    		addResultArea.setText("Error: " + w.getEntry() + " is more than 25 characters.");
    	} else {
	    	status = dict.putWord(w);
	    	Word w1 = dict.getWord(w.getEntry());
			addResultArea.append(w1.getEntry() + " : " + w1.getRating() + "\n");
    	}
		
		return status;
    }
    
    public String doQuery(boolean next)
    {
    	queryResultArea.setText("");

    	ArrayList<Word> list = null;
    	int resultSetSize = 0;
    	
    	String key = wordEntry.getText();
    	int length = wordLengthSlider.getValue();
    	int rat = wordRatingSlider.getValue();
    	LengthControl lenCtrl = LengthControl.ALL;
    	PatternControl patCtrl = PatternControl.ALL;
    	RatingControl ratCtrl = RatingControl.ALL;
    	UsedControl useCtrl = UsedControl.ALL;
    	ResearchControl resCtrl = ResearchControl.ALL;
    	MethodControl methCtrl = MethodControl.ALL;
    	
    	if ( queryRatingAtMost.isSelected() )
    		ratCtrl = RatingControl.ATMOST;
    	else if ( queryRatingAtLeast.isSelected() )
    		ratCtrl = RatingControl.ATLEAST;
        else if ( queryRatingEquals.isSelected() )
            ratCtrl = RatingControl.EQUALS;

    	if ( key.length() == 0 )	// no pattern selected
    		patCtrl = PatternControl.ALL;
    	else if ( queryEntryEquals.isSelected() )
    		patCtrl = PatternControl.EQUALS;
    	else if ( queryEntryStarts.isSelected() )
    		patCtrl = PatternControl.STARTSWITH;
    	else if ( queryEntryContains.isSelected() )
    		patCtrl = PatternControl.CONTAINS;

    	if ( queryLengthEquals.isSelected() )
    		lenCtrl = LengthControl.EQUALS;
    	else if ( queryLengthAtMost.isSelected() )
    		lenCtrl = LengthControl.ATMOST;
    	else if ( queryLengthAtLeast.isSelected() )
    		lenCtrl = LengthControl.ATLEAST;

        if ( usedNYT.isSelected() && usedAny.isSelected() && notUsed.isSelected())
            useCtrl = UsedControl.ALL;
        else if ( usedNYT.isSelected() && usedAny.isSelected() && !notUsed.isSelected())
            useCtrl = UsedControl.ANY;
        else if ( usedNYT.isSelected() && !usedAny.isSelected() && notUsed.isSelected())
            useCtrl = UsedControl.NOT_OTHER;
        else if ( !usedNYT.isSelected() && usedAny.isSelected() && notUsed.isSelected())
            useCtrl = UsedControl.NOT_NYT;
        else if ( usedNYT.isSelected() && !usedAny.isSelected() && !notUsed.isSelected())
            useCtrl = UsedControl.NYT;
        else if ( !usedNYT.isSelected() && usedAny.isSelected() && !notUsed.isSelected())
            useCtrl = UsedControl.OTHER;
        else if ( !usedNYT.isSelected() && !usedAny.isSelected() && notUsed.isSelected())
            useCtrl = UsedControl.NOT_USED;
        else
            useCtrl = UsedControl.NONE;

    	if ( research.isSelected() )
    		resCtrl = ResearchControl.NEEDS_RESEARCH;
    	
    	if (queryMethodManual.isSelected()) {
    		methCtrl = MethodControl.MANUAL;
    	} else if (queryMethodAuto.isSelected()) {
    		methCtrl = MethodControl.AUTOMATIC;
    	}
    	
    	resultSetSize = dict.getCount(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl, methCtrl, false);
    	if (next) {
    		queryStart += QUERY_LIMIT;
    		queryStart = (queryStart > resultSetSize ? resultSetSize : queryStart);
    	} else {
    		queryStart = 0;
    	}
    	list = dict.getWords(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl, methCtrl, queryStart, QUERY_LIMIT, false);
		
		if ( list == null || list.isEmpty() )
		{
			queryResultArea.setText(NO_RESULTS_FOUND);
			nextButton.setEnabled(false);
            getRootPane().setDefaultButton(queryButton);

			wordComment.setText("");
			return "0";
		}
		else
		{
			if (resultSetSize > (queryStart + QUERY_LIMIT)) {	// More data left to display
				((QueryAction)(nextButton.getAction())).setRating(false);	// set the right "next" action
				nextButton.setEnabled(true);
                getRootPane().setDefaultButton(nextButton);
            } else {
				nextButton.setEnabled(false);
                getRootPane().setDefaultButton(queryButton);
			}
			for ( Word w : list ) {
				queryResultArea.append(w.toStringQuery() + "\n");
			}
			if (resultSetSize == 1) {
				wordComment.setText(list.get(0).getComment());
			} else {
				wordComment.setText("");
			}
		}
		
		return "" + (queryStart + 1) + "-" + (queryStart + QUERY_LIMIT > resultSetSize ? resultSetSize : (queryStart + QUERY_LIMIT)) + " of " + resultSetSize + (resultSetSize == 1 ? " entry" : " entries");
    }
    
    public String doRatingQuery(boolean next)
    {
    	rateResultArea.setText("");

    	int resultSetSize = 0;
    	
    	String key = wordEntry.getText();
    	int length = wordLengthSlider.getValue();
    	int rat = wordRatingSlider.getValue();
    	LengthControl lenCtrl = LengthControl.ALL;
    	PatternControl patCtrl = PatternControl.ALL;
    	RatingControl ratCtrl = RatingControl.ALL;
    	UsedControl useCtrl = UsedControl.ALL;
    	ResearchControl resCtrl = ResearchControl.ALL;
    	MethodControl methCtrl = MethodControl.ALL;
    	
    	if ( queryRatingAtMost.isSelected() )
    		ratCtrl = RatingControl.ATMOST;
    	else if ( queryRatingAtLeast.isSelected() )
    		ratCtrl = RatingControl.ATLEAST;
        else if ( queryRatingEquals.isSelected() )
            ratCtrl = RatingControl.EQUALS;

    	if ( key.length() == 0 )	// no pattern selected
    		patCtrl = PatternControl.ALL;
    	else if ( queryEntryEquals.isSelected() )
    		patCtrl = PatternControl.EQUALS;
    	else if ( queryEntryStarts.isSelected() )
    		patCtrl = PatternControl.STARTSWITH;
    	else if ( queryEntryContains.isSelected() )
    		patCtrl = PatternControl.CONTAINS;

    	if ( queryLengthEquals.isSelected() )
    		lenCtrl = LengthControl.EQUALS;
    	else if ( queryLengthAtMost.isSelected() )
    		lenCtrl = LengthControl.ATMOST;
    	else if ( queryLengthAtLeast.isSelected() )
    		lenCtrl = LengthControl.ATLEAST;

        if ( usedNYT.isSelected() && usedAny.isSelected() && notUsed.isSelected())
            useCtrl = UsedControl.ALL;
        else if ( usedNYT.isSelected() && usedAny.isSelected() && !notUsed.isSelected())
            useCtrl = UsedControl.ANY;
        else if ( usedNYT.isSelected() && !usedAny.isSelected() && notUsed.isSelected())
            useCtrl = UsedControl.NOT_OTHER;
        else if ( !usedNYT.isSelected() && usedAny.isSelected() && notUsed.isSelected())
            useCtrl = UsedControl.NOT_NYT;
        else if ( usedNYT.isSelected() && !usedAny.isSelected() && !notUsed.isSelected())
            useCtrl = UsedControl.NYT;
        else if ( !usedNYT.isSelected() && usedAny.isSelected() && !notUsed.isSelected())
            useCtrl = UsedControl.OTHER;
        else if ( !usedNYT.isSelected() && !usedAny.isSelected() && notUsed.isSelected())
            useCtrl = UsedControl.NOT_USED;
        else
            useCtrl = UsedControl.NONE;

        if ( research.isSelected() )
    		resCtrl = ResearchControl.NEEDS_RESEARCH;
    	else
    		resCtrl = ResearchControl.NO_RESEARCH;
    	
    	if (queryMethodManual.isSelected()) {
    		methCtrl = MethodControl.MANUAL;
    	} else if (queryMethodAuto.isSelected()) {
    		methCtrl = MethodControl.AUTOMATIC;
    	}
    	
    	resultSetSize = dict.getCount(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl, methCtrl, true);
    	if (next) {
    		queryStart += RATING_QUERY_LIMIT;
    		queryStart = (queryStart > resultSetSize ? resultSetSize : queryStart);
    	} else {
    		queryStart = 0;
    	}
    	
    	if (listToRate != null) {
    		listToRate.clear();		// clear the list
    	}
    	
    	listToRate = dict.getWords(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl, methCtrl, queryStart, RATING_QUERY_LIMIT, true);
		
		if ( listToRate == null || listToRate.isEmpty() )
		{
			rateResultArea.setText(NO_RESULTS_FOUND);
			nextButton.setEnabled(false);
            getRootPane().setDefaultButton(rateQueryButton);
            wordComment.setText("");
			setRatingButtons(false);
			return "0";
		}
		else
		{
			if (resultSetSize > (queryStart + RATING_QUERY_LIMIT)) {	// More data left to display
				((QueryAction)(nextButton.getAction())).setRating(true);	// set the right "next" action
				nextButton.setEnabled(true);
                getRootPane().setDefaultButton(nextButton);
			} else {
				nextButton.setEnabled(false);
                getRootPane().setDefaultButton(rateQueryButton);
			}
			for ( Word w : listToRate ) {
				if (w.equals(listToRate.get(0))) {
                    rateResultArea.append(w.toString());
				} else {
					rateResultArea.append("\n" + w.toString());
				}
			}
			
			wordToRate.setText(listToRate.get(0).getEntry());
	    	manualRatingSlider.setValue(listToRate.get(0).getRating());
	    	wordComment.setText(listToRate.get(0).getComment());

			setRatingButtons(true);
		}
		
		return "" + (queryStart + 1) + "-" + (queryStart + RATING_QUERY_LIMIT > resultSetSize ? resultSetSize : (queryStart + RATING_QUERY_LIMIT)) + " of " + resultSetSize + (resultSetSize == 1 ? " entry" : " entries");
    }

    public String doRate(RATINGS r) {
        String status = "";
        Word w = listToRate.get(0);
        int rat = manualRatingSlider.getValue();
        if (!wordComment.getText().isEmpty()) {
            w.setComment(wordComment.getText());
        }

        if (r == XDictConfig.RATINGS.RESEARCH) {
            status = w.getEntry() + ": Needs research";
            w.setNeedsResearch(true);
            dict.putWord(w);
        } else if (r == XDictConfig.RATINGS.MANUAL) {
            status = w.getEntry() + ": " + rat + " (Manual)";
            w.setRating(rat);
            w.setManuallyRated(true);
            w.setNeedsResearch(false);    // rated manually; research complete
            dict.putWord(w);
        } else if (r == XDictConfig.RATINGS.KILL) {
            rat = XDictConfig.KILL;
            status = w.getEntry() + ": " + rat + " (Killed)";
            w.setRating(rat);
            w.setManuallyRated(true);
            w.setNeedsResearch(false);    // rated manually; research complete
            dict.putWord(w);
        } else if (r == XDictConfig.RATINGS.SKIP) {
            status = w.getEntry() + ": Skipped";
        } else {
            rat = XDictConfig.getRating(r, w.length());
            status = w.getEntry() + ": " + rat + " (" + r.toString() + ")";
            w.setRating(rat);
            w.setManuallyRated(true);
            w.setNeedsResearch(false);    // rated manually; research complete
            dict.putWord(w);
        }

        listToRate.remove(0);	// Remove rated (or skipped) item from list

        rateResultArea.setText("");
        for ( Word w1 : listToRate ) {
            if (w1.equals(listToRate.get(0))) {
                rateResultArea.append(w1.toString());
            } else {
                rateResultArea.append("\n" + w1.toString());
            }
        }

        if (listToRate.isEmpty()) {
            wordToRate.setText("");
            wordComment.setText("");
            setRatingButtons(false);
        } else {
            wordToRate.setText(listToRate.get(0).getEntry());
            wordComment.setText(listToRate.get(0).getComment());
            manualRatingSlider.setValue(listToRate.get(0).getRating());
            setRatingButtons(true);
        }

        return status;
    }

    public String doSetAddRate(RATINGS r)
    {
    	String status = "";
        String w = wordEntry.getText();

        int rat = XDictConfig.getRating(r, w.length());

        wordRatingSlider.setValue(rat);

    	return status;
    }
    
    public String doLoad()
    {
    	String filename = loadFile.getText();
    	loadResultArea.setText("Loading from file: " + filename + "\n");
    	BufferedReader br;
    	int newCount = 0;
    	int existCount = 0;
    	int dupCount = 0;
    	int skipCount = 0;
    	
		try {
			br = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			loadResultArea.append("File not found.\n");
			loadResultArea.append(e.toString());
			return "Error.";
		}
    	String line;
    	int count = 0;
    	WORD_STATUS status;
//		String statText = "";
		byte rating = 0;

    	try {
			while ((line = br.readLine()) != null) {
				if (line.length() < 3) {
					continue;
				}
				Word wTmp = LoadAndExportUtilities.parseWordAndRating(line, XDictConfig.LOAD_FILE_DELIMITERS);
				// If it has a rating, use it; else grab the rating setting from the UI.
				rating = ((wTmp.getRating() > 0) ? wTmp.getRating() : (byte)wordRatingSlider.getValue());
//				rating = LoadAndExportUtilities.normalizeRating(wTmp.getEntry(), rating);

				Word w = new Word.Builder(wTmp.getEntry()).rating(rating).usedAny(usedAny.isSelected()).usedNYT(usedNYT.isSelected()).manuallyRated(queryMethodManual.isSelected()).build();

		    	if (w.length() < 3) {
		    		status = WORD_STATUS.ERROR;
		    		loadResultArea.append(w.getEntry() + " is less than 3 characters.\n");
//					statText = " (Skipped)";
					skipCount++;
		    	} else if (w.length() > 25) {
		    		status = WORD_STATUS.ERROR;
		    		loadResultArea.append(w.getEntry() + " is more than 25 characters.\n");
//					statText = " (Skipped)";
					skipCount++;
		    	} else {
					status = dict.putWord(w);
					if (status == WORD_STATUS.NEW) {
//						statText = " (New)";
						newCount++;
					} else if (status == WORD_STATUS.EXISTS) {
//						statText = " (Modified)";
						existCount++;
					} else {
//						statText = " (Duplicate)";
						dupCount++;
					}
//				   loadResultArea.setText("Adding word: " + w.getEntry() + statText + "\n");
				   count++;
				   if (count % 1000 == 0) {
					   getStatusLine().showInfo("Processing load..." + count + " records processed.");

				   }
		    	}
			}
		} catch (IOException e) {
			loadResultArea.append("Error reading file.\n");
			loadResultArea.append(e.toString());
		}
    	try {
			br.close();
		} catch (IOException e) {
			loadResultArea.append("Error closing file.\n");
			loadResultArea.append(e.toString());
		}    	
    	String retStatus = "" + count + " words processed. New: " + newCount + ", Modified: " + existCount + ", Duplicate: " + dupCount + "(" + skipCount + " skipped.)";
    	return retStatus;
    }

    public String doRestore()
    {
        resultPaneTabs.setSelectedIndex(3);     // set to load result pane to display results
        if (loadFile.getText().isEmpty())       // if blank, preset with directory, but don't override what's there
            loadFile.setText("backups/");

        String filename = loadFile.getText();
        if ( !filename.startsWith("backups/bkup") ) {
            loadResultArea.setText("Can only restore files starting with \"backups/bkup\"; Filename: [" + filename + "]\n");
            loadResultArea.append("Enter the backup file name in the File to Load field and then retry the Restore action.\n");
            loadResultArea.append("WARNING: Do NOT restore with the LOAD button, as any non-rating data will not be restored!\n");
            return "Error.";
        }
        if (filename.contains(XDictConfig.TEST_MODE_SUFFIX) && !XDictConfig.testMode) {
            loadResultArea.setText("WARNING: You are trying to restore a TEST MODE database but are NOT in TEST MODE!!!\n");
            loadResultArea.append("If you really mean to do this, you must rename the backup file to remove the " + XDictConfig.TEST_MODE_SUFFIX + "from the name.\n");
            loadResultArea.append("This is for your own safety.\n");
            return "Error.";
        }
        loadResultArea.setText("Restoring from file: " + filename + "\n");
        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            loadResultArea.append("File not found.\n");
            loadResultArea.append(e.toString());
            return "Error.";
        }
        String line;
        int count = 0;
        WORD_STATUS status;
//		String statText = "";

        try {
            while ((line = br.readLine()) != null) {
                if (line.length() < 3) {
                    continue;
                }
                Word w = LoadAndExportUtilities.parseBackupEntry(line);
//                loadResultArea.append(w.fullInfo() + "\n");

                status = dict.putWord(w);
                count++;
                if (count % 1000 == 0) {
                    getStatusLine().showInfo("Processing load..." + count + " records processed.");

                }
            }
        } catch (IOException e) {
            loadResultArea.append("Error reading file.\n");
            loadResultArea.append(e.toString());
        }
        try {
            br.close();
        } catch (IOException e) {
            loadResultArea.append("Error closing file.\n");
            loadResultArea.append(e.toString());
        }
        String retStatus = "" + count + " words processed.";
        return retStatus;
    }

    public String doExport(boolean isBackup)
    {
    	String filename = exportFile.getText();
    	if (isBackup) {
    		Timestamp t = new Timestamp(new Date().getTime());
    		filename = "backups/bkup" + XDictConfig.DB_MODE_SUFFIX + "_" + t.toString();
    	}
    	exportResultArea.setText("");
    	FileWriter fw;


    	try {
            File file = new File(filename);
            file.getParentFile().mkdirs();
			fw = new FileWriter(filename);
		} catch (IOException e) {
			exportResultArea.append("Error opening file: " + filename + ".\n");
			exportResultArea.append(e.toString());
			return "Error.";
		}

    	ArrayList<Word> list = null;
    	int resultSetSize = 0;
    	
    	String key = (isBackup ? "" : wordEntry.getText());
    	int length = (isBackup ? 3 : wordLengthSlider.getValue());
    	int rat = (isBackup ? 0 : wordRatingSlider.getValue());
    	LengthControl lenCtrl = LengthControl.ALL;
    	PatternControl patCtrl = PatternControl.ALL;
    	RatingControl ratCtrl = RatingControl.ALL;
    	UsedControl useCtrl = UsedControl.ALL;
    	ResearchControl resCtrl = ResearchControl.ALL;
    	MethodControl methCtrl = MethodControl.ALL;
    	
    	// For backup, we take everything regardless of selections...
    	if (!isBackup) {
	    	if ( queryRatingAtMost.isSelected() )
	    		ratCtrl = RatingControl.ATMOST;
	    	else if ( queryRatingAtLeast.isSelected() )
	    		ratCtrl = RatingControl.ATLEAST;
            else if ( queryRatingEquals.isSelected() )
                ratCtrl = RatingControl.EQUALS;

	    	if ( key.length() == 0 )	// no pattern selected
	    		patCtrl = PatternControl.ALL;
	    	else if ( queryEntryEquals.isSelected() )
	    		patCtrl = PatternControl.EQUALS;
	    	else if ( queryEntryStarts.isSelected() )
	    		patCtrl = PatternControl.STARTSWITH;
	    	else if ( queryEntryContains.isSelected() )
	    		patCtrl = PatternControl.CONTAINS;
	
	    	if ( queryLengthEquals.isSelected() )
	    		lenCtrl = LengthControl.EQUALS;
	    	else if ( queryLengthAtMost.isSelected() )
	    		lenCtrl = LengthControl.ATMOST;
	    	else if ( queryLengthAtLeast.isSelected() )
	    		lenCtrl = LengthControl.ATLEAST;
	
            if ( usedNYT.isSelected() ) {
                if (notUsed.isSelected())
                    useCtrl = UsedControl.ALL;
                else
                    useCtrl = UsedControl.NYT;
            }
            else if ( usedAny.isSelected() )
                useCtrl = UsedControl.ANY;
            else if ( notUsed.isSelected())
                useCtrl = UsedControl.NOT_USED;

	    	if ( research.isSelected() )
	    		resCtrl = ResearchControl.NEEDS_RESEARCH;
	    	
	    	if (queryMethodManual.isSelected()) {
	    		methCtrl = MethodControl.MANUAL;
	    	} else if (queryMethodAuto.isSelected()) {
	    		methCtrl = MethodControl.AUTOMATIC;
	    	}
    	}
    	resultSetSize = dict.getCount(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl, methCtrl, false);
    	
    	for (int start = 0; start < resultSetSize; start += QUERY_LIMIT) {
			getStatusLine().showInfo("Processing " + (isBackup ? "backup..." : "export...") + start + " records processed.");

        	list = dict.getWords(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl, methCtrl, start, QUERY_LIMIT, false);
			for ( Word w : list ) {
//				exportResultArea.append(w.toString() + "\n");
				try {
					if (isBackup) {
						fw.write(w.fullInfo() + "\n");
					} else {
						fw.write(w.toString() + "\n");
					}
				} catch (IOException e) {
					exportResultArea.append("Error writing to file: " + filename + ".\n");
					exportResultArea.append(e.toString());
					return "Error.";
				}
			}
    	}
    	try {
			fw.close();
		} catch (IOException e) {
			exportResultArea.append("Error closing file: " + filename + ".\n");
			exportResultArea.append(e.toString());
			return "Error.";
		}
		
		return (isBackup ? "Backed up " : "Exported ") + resultSetSize + (resultSetSize == 1 ? " entry" : " entries");
    }

    public void getDatabaseInfo() {
        resultPaneTabs.setSelectedIndex(0);     // set to query result pane to display results
        queryResultArea.setText("Database Info:" + "\n\n");

        ArrayList<String> tables = dict.showAllTables();

        if (XDictConfig.testMode) {
            queryResultArea.append("Active Tables (TEST MODE)\n");
            queryResultArea.append("------------------------- \n");
            for (String t : tables) {
                if (t.contains(XDictConfig.TEST_MODE_SUFFIX)) {
                    int size = dict.getTableSize(t);
                    queryResultArea.append(t + " : " + size + " entries\n");
                }
            }
            queryResultArea.append("\nInactive Tables \n");
            queryResultArea.append("--------------- \n");
            for (String t : tables) {
                if (!t.contains(XDictConfig.TEST_MODE_SUFFIX)) {
                    int size = dict.getTableSize(t);
                    queryResultArea.append(t + " : " + size + " entries\n");
                }
            }
        } else {
            queryResultArea.append("Active Tables \n");
            queryResultArea.append("------------- \n");
            for (String t : tables) {
                if (!t.contains(XDictConfig.TEST_MODE_SUFFIX)) {
                    int size = dict.getTableSize(t);
                    queryResultArea.append(t + " : " + size + " entries\n");
                }
            }
            queryResultArea.append("\nInactive Tables (TEST MODE)\n");
            queryResultArea.append("--------------------------- \n");
            for (String t : tables) {
                if (t.contains(XDictConfig.TEST_MODE_SUFFIX)) {
                    int size = dict.getTableSize(t);
                    queryResultArea.append(t + " : " + size + " entries\n");
                }
            }
        }

        return;
    }

    public String doClear() {
        String validation = "YES I REALLY MEAN TO DO THIS";
        resultPaneTabs.setSelectedIndex(0);     // set to query result pane to display results
        if (!wordComment.getText().contains(validation)) {
            queryResultArea.setText("You are requesting to clear all tables in your " + (XDictConfig.testMode ? "TEST MODE " : "") + "database!\n");
            queryResultArea.append("If you really mean to do this, you must enter \"YES I REALLY MEAN TO DO THIS\" in the Comment field and then retry.");
            return "Are you sure?";
        }

        dict.clear_YesIReallyMeanToDoThis();

        queryResultArea.setText("Tables cleared.");
        wordComment.setText("");        // clear the validation field

        return "Tables cleared.";
    }

    public void getRatingProgress() {
        resultPaneTabs.setSelectedIndex(0);     // set to query result pane to display results
        queryResultArea.setText("Rating Progress:" + "\n");

        int totalRated = 0;
        int totalUnrated = 0;

        String key = "";
        PatternControl patCtrl = PatternControl.ALL;

        if ( key.length() == 0 )	// no pattern selected
            patCtrl = PatternControl.ALL;
        else if ( queryEntryEquals.isSelected() )
            patCtrl = PatternControl.EQUALS;
        else if ( queryEntryStarts.isSelected() )
            patCtrl = PatternControl.STARTSWITH;
        else if ( queryEntryContains.isSelected() )
            patCtrl = PatternControl.CONTAINS;

        for ( int length = 3; length < 26; length++ ) {

            int ratedCount = dict.getCount(LengthControl.EQUALS, length, patCtrl, key, RatingControl.ALL, 0, UsedControl.ALL, ResearchControl.ALL, MethodControl.MANUAL, false);
            int unratedCount = dict.getCount(LengthControl.EQUALS, length, patCtrl, key, RatingControl.ALL, 0, UsedControl.ALL, ResearchControl.ALL, MethodControl.AUTOMATIC, false);

            if (ratedCount + unratedCount == 0 )
                continue;

            double percent = (double)ratedCount / ( (double)ratedCount + (double)unratedCount);
            DecimalFormat df = new DecimalFormat("##.##%");
            String formattedPercent = df.format(percent);
            totalRated += ratedCount;
            totalUnrated += unratedCount;

            queryResultArea.append("Length: " + length + "  Total: " + (ratedCount + unratedCount) + "  Rated: " + ratedCount + "  Unrated: " + unratedCount + "  Percent: " + formattedPercent + "\n");

        }

        double percent = (double)totalRated / ( (double)totalRated + (double)totalUnrated);
        DecimalFormat df = new DecimalFormat("##.##%");
        String formattedPercent = df.format(percent);
        queryResultArea.append("TOTAL:   Total: " + (totalRated + totalUnrated) + "  Rated: " + totalRated + "  Unrated: " + totalUnrated + "  Percent: " + formattedPercent + "\n");

        return;
    }

    public void getRatingBreakdown() {
        resultPaneTabs.setSelectedIndex(0);     // set to query result pane to display results
        queryResultArea.setText("Rating Breakdown:" + "\n");

        int totalHorrible = 0;   // 0 to 25
        int totalBad = 0;        // 26 to 50
        int totalMedium = 0;     // 51 to 60
        int totalGood = 0;       // 61 to 80
        int totalGreat = 0;      // 81 to 100

        String key = "";
        PatternControl patCtrl = PatternControl.ALL;

        if ( key.length() == 0 )	// no pattern selected
            patCtrl = PatternControl.ALL;
        else if ( queryEntryEquals.isSelected() )
            patCtrl = PatternControl.EQUALS;
        else if ( queryEntryStarts.isSelected() )
            patCtrl = PatternControl.STARTSWITH;
        else if ( queryEntryContains.isSelected() )
            patCtrl = PatternControl.CONTAINS;

        for ( int length = 3; length < 26; length++ ) {

            int horribleCount = dict.getCount(LengthControl.EQUALS, length, patCtrl, key, 0, 20, UsedControl.ALL, ResearchControl.ALL, MethodControl.ALL);
            int badCount = dict.getCount(LengthControl.EQUALS, length, patCtrl, key, 21, 40, UsedControl.ALL, ResearchControl.ALL, MethodControl.ALL);
            int mediumCount = dict.getCount(LengthControl.EQUALS, length, patCtrl, key, 41, 60, UsedControl.ALL, ResearchControl.ALL, MethodControl.ALL);
            int goodCount = dict.getCount(LengthControl.EQUALS, length, patCtrl, key, 61, 80, UsedControl.ALL, ResearchControl.ALL, MethodControl.ALL);
            int greatCount = dict.getCount(LengthControl.EQUALS, length, patCtrl, key, 81, 100, UsedControl.ALL, ResearchControl.ALL, MethodControl.ALL);

            int combinedCount = horribleCount + badCount + mediumCount + goodCount + greatCount;
            if (combinedCount == 0 )
                continue;

            double horriblePercent = (double)horribleCount / (double)combinedCount;
            double badPercent = (double)badCount / (double)combinedCount;
            double mediumPercent = (double)mediumCount / (double)combinedCount;
            double goodPercent = (double)goodCount / (double)combinedCount;
            double greatPercent = (double)greatCount / (double)combinedCount;
            DecimalFormat df = new DecimalFormat("##.#%");
            String formattedHorriblePercent = df.format(horriblePercent);
            String formattedBadPercent = df.format(badPercent);
            String formattedMediumPercent = df.format(mediumPercent);
            String formattedGoodPercent = df.format(goodPercent);
            String formattedGreatPercent = df.format(greatPercent);
            totalHorrible += horribleCount;
            totalBad += badCount;
            totalMedium += mediumCount;
            totalGood += goodCount;
            totalGreat += greatCount;

            queryResultArea.append("Length: " + length + "  Total: " + combinedCount + "   (0-20): " + horribleCount + " (" + formattedHorriblePercent + ") " +
                    "  (21-40): " + badCount + " (" + formattedBadPercent + ") " +
                    "  (41-60): " + mediumCount + " (" + formattedMediumPercent + ") " +
                    "  (61-80): " + goodCount + " (" + formattedGoodPercent + ") " +
                    "  (81-100): " + greatCount + " {" + formattedGreatPercent + ")\n");

        }

        int totalCount = totalHorrible + totalBad + totalMedium + totalGood + totalGreat;

        double horriblePercent = (double)totalHorrible / (double)totalCount;
        double badPercent = (double)totalBad / (double)totalCount;
        double mediumPercent = (double)totalMedium / (double)totalCount;
        double goodPercent = (double)totalGood / (double)totalCount;
        double greatPercent = (double)totalGreat / (double)totalCount;
        DecimalFormat df = new DecimalFormat("##.#%");
        String formattedHorriblePercent = df.format(horriblePercent);
        String formattedBatPercent = df.format(badPercent);
        String formattedMediumPercent = df.format(mediumPercent);
        String formattedGoodPercent = df.format(goodPercent);
        String formattedGreatPercent = df.format(greatPercent);

        queryResultArea.append("TOTAL:   Total: " + totalCount + "   (0-20): " + totalHorrible + " (" + formattedHorriblePercent + ") " +
                "  (21-40): " + totalBad + " (" + formattedBatPercent + ") " +
                "  (41-60): " + totalMedium + " (" + formattedMediumPercent + ") " +
                "  (61-80): " + totalGood + " (" + formattedGoodPercent + ") " +
                "  (81-100): " + totalGreat + " (" + formattedGreatPercent + ")\n");

        return;
    }

    public void doHelp() {
        try {
            File file = new java.io.File("help/XDictHelp.html").getAbsoluteFile();
            Desktop.getDesktop().open(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doAbout() {
        Label copyrightL = new Label("\u00a9");
        String aboutMessage = "This is XDict, a Crossword Dictionary Maintenance Program by Pete Mitchell\n\n" +
                copyrightL.getText() + " 2020";
        JOptionPane.showMessageDialog(this, aboutMessage,
                "About XDict",
                JOptionPane.PLAIN_MESSAGE);
    }
    /*
     ************  WINDOW CONTROL ************
     */

	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		dict.disconnect();
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}
    
    /*
     ************  MAIN ************
     */
	
    public static void main(String[] args) 
    {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // Ignore
        }

        if ( !XDictConfig.processConfigFile())
            return;

		dict.connect();
        
        XDictGui gui = new XDictGui();
        if (XDictConfig.testMode) {
            gui.setTitle("***** TEST MODE ***** TEST MODE ***** TEST MODE ***** TEST MODE ***** TEST MODE ***** TEST MODE ***** TEST MODE *****");
            gui.queryResultArea.setText("YOU ARE IN TEST MODE!" + "\n\n");
            gui.queryResultArea.append("All actions while in TEST MODE will apply to TEST tables." + "\n");
            gui.queryResultArea.append("This allows you to play with the functionality of the system without concern for the actual data." + "\n");
            gui.queryResultArea.append("To switch out of TEST MODE, comment out the TEST_MODE line in config.txt." + "\n");
        } else {
            gui.setTitle("XDict - A Crossword Dictionary Maintenance Tool by Pete Mitchell");
        }
        gui.setVisible(true);
        
    }
}
