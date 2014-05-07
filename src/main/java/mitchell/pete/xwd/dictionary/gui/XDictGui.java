package mitchell.pete.xwd.dictionary.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mitchell.pete.xwd.dictionary.Word;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.LengthControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.MethodControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.PatternControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.RatingControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.ResearchControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.UsedControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.WORD_STATUS;
import mitchell.pete.xwd.dictionary.db.XDictDB_MySQL;

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
    private static int queryStart = 0;
    private static int LENGTH_DEFAULT = 3;
    private static int RATING_DEFAULT = 10;
    
    private static final String NO_RESULTS_FOUND = "No entries found that match this criteria.\n";

    private JMenuBar          menuBar                = new JMenuBar();
    private JMenu             fileMenu               = new JMenu();
    private JMenu             viewMenu               = new JMenu();
    private JTextArea         queryResultArea        = new JTextArea();
    private JScrollPane       queryScrollPane        = new JScrollPane();
    private JTextArea         addResultArea          = new JTextArea();
    private JScrollPane       addScrollPane          = new JScrollPane();
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
    private JRadioButton queryMethodAll = new JRadioButton("All");
    private JRadioButton queryMethodManual = new JRadioButton("Hand-rated");
    private JRadioButton queryMethodAuto = new JRadioButton("Auto-loaded");

    private JTextField wordEntry            = new JTextField(30);
    private JSlider wordLengthSlider        = new JSlider(3,25,LENGTH_DEFAULT);
    private JLabel wordLengthLabel          = new JLabel(String.valueOf(wordLengthSlider.getValue()));
    private JSlider wordRatingSlider        = new JSlider(0,100,RATING_DEFAULT);
    private JLabel wordRatingLabel          = new JLabel(String.valueOf(wordRatingSlider.getValue()));
    private JCheckBox usedAny               = new JCheckBox("Used Any: ");
    private JCheckBox usedNYT               = new JCheckBox("Used NYT: ");
    private JCheckBox research               = new JCheckBox("Needs Research: ");
    private JButton queryButton				= new JButton(new QueryAction(this, false));
    private JButton nextButton				= new JButton(new QueryAction(this, true));
    private JButton addButton				= new JButton(new AddAction(this));
    private JButton loadButton				= new JButton(new LoadAction(this));
    private JButton exportButton		    = new JButton(new ExportAction(this));
    private JTabbedPane resultPaneTabs 		= new JTabbedPane();
    
    private JTextField loadFile            = new JTextField(50);
    private JTextField exportFile            = new JTextField(50);



    ChangeListener lengthListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    		JSlider source = (JSlider)e.getSource();
    	    wordLengthLabel.setText( PAD + String.valueOf(source.getValue()) + PAD);
    	}
    };
    ChangeListener ratingListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    		JSlider source = (JSlider)e.getSource();
    	    wordRatingLabel.setText( PAD + String.valueOf(source.getValue()) + PAD);
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
    		} else if (source.getSelectedIndex() == USE_MODE.ADD.ordinal()) {
    			queryButton.setEnabled(false);
    			nextButton.setEnabled(false);
    			addButton.setEnabled(true);
    			loadButton.setEnabled(false);
    			exportButton.setEnabled(false);
			} else if (source.getSelectedIndex() == USE_MODE.LOAD.ordinal()) {
				queryButton.setEnabled(false);
				nextButton.setEnabled(false);
				addButton.setEnabled(false);
				loadButton.setEnabled(true);
    			exportButton.setEnabled(false);
			} else if (source.getSelectedIndex() == USE_MODE.EXPORT.ordinal()) {
				queryButton.setEnabled(false);
				nextButton.setEnabled(false);
				addButton.setEnabled(false);
				loadButton.setEnabled(false);
    			exportButton.setEnabled(true);
			}
    	}
    };
    ChangeListener usedAnyListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    		JCheckBox source = (JCheckBox)e.getSource();
    		// If not selected, then NYT cannot be selected
    		if (!source.isSelected())
    			usedNYT.setSelected(false);
    	}
    };
    ChangeListener usedNYTListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    		JCheckBox source = (JCheckBox)e.getSource();
    		// If selected, then "Any" must also be selected
    		if (source.isSelected())
    			usedAny.setSelected(true);
    	}
    };


    public XDictGui() 
    {
        this.setSize(new Dimension(1200, 800));
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
        buildFileMenu();
        buildViewMenu();

        this.setJMenuBar(menuBar);
    }
    
    private void buildFileMenu() {
        menuBar.add(fileMenu);

        fileMenu.setText("File");

//        Action    action;
//        JMenuItem menuItem;
//        JButton   button;

        /*
         * Use as example of adding action to menu
         */
        /*
        action = new DependencyExtractAction(this);
        menuItem = fileMenu.add(action);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK));
        menuItem.setMnemonic('e');
        button = toolbar.add(action);
        button.setToolTipText((String) action.getValue(Action.LONG_DESCRIPTION));

        action = new RefreshDependencyGraphAction(this);
        menuItem = fileMenu.add(action);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK));
        menuItem.setMnemonic('r');
        button = toolbar.add(action);
        button.setToolTipText((String) action.getValue(Action.LONG_DESCRIPTION));

        toolbar.addSeparator();
        fileMenu.addSeparator();
        
        action = new OpenFileAction(this);
        menuItem = fileMenu.add(action);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
        menuItem.setMnemonic('o');
        button = toolbar.add(action);
        button.setToolTipText((String) action.getValue(Action.LONG_DESCRIPTION));
        
        action = new SaveFileAction(this, commandLine.getSingleSwitch("encoding"), commandLine.getSingleSwitch("dtd-prefix"));
        menuItem = fileMenu.add(action);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
        menuItem.setMnemonic('s');
        button = toolbar.add(action);
        button.setToolTipText((String) action.getValue(Action.LONG_DESCRIPTION));

        if (commandLine.isPresent("indent-text")) {
            ((SaveFileAction) action).setIndentText(commandLine.getSingleSwitch("indent-text"));
        }
        
        toolbar.addSeparator();
        fileMenu.addSeparator();
        
        action = new NewDependencyGraphAction(this);
        menuItem = fileMenu.add(action);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));
        menuItem.setMnemonic('n');
        button = toolbar.add(action);
        button.setToolTipText((String) action.getValue(Action.LONG_DESCRIPTION));

        toolbar.addSeparator();
        fileMenu.addSeparator();
        
        action = new DependencyQueryAction(this);
        menuItem = fileMenu.add(action);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK));
        menuItem.setMnemonic('d');
        button = toolbar.add(action);
        button.setToolTipText((String) action.getValue(Action.LONG_DESCRIPTION));

        action = new ClosureQueryAction(this);
        menuItem = fileMenu.add(action);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));
        menuItem.setMnemonic('c');
        button = toolbar.add(action);
        button.setToolTipText((String) action.getValue(Action.LONG_DESCRIPTION));

        action = new MetricsQueryAction(this);
        menuItem = fileMenu.add(action);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Event.CTRL_MASK));
        menuItem.setMnemonic('m');
        button = toolbar.add(action);
        button.setToolTipText((String) action.getValue(Action.LONG_DESCRIPTION));

        action = new AllQueriesAction(this);
        menuItem = fileMenu.add(action);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK));
        menuItem.setMnemonic('a');
        button = toolbar.add(action);
        button.setToolTipText((String) action.getValue(Action.LONG_DESCRIPTION));

        toolbar.addSeparator();
        fileMenu.addSeparator();

        action = new ExitAction(this);
        menuItem = fileMenu.add(action);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));
        menuItem.setMnemonic('x');
        */
    }
    
    private void buildViewMenu() 
    {
        menuBar.add(viewMenu);

        viewMenu.setText("View");

//        ButtonGroup group = new ButtonGroup();
        JMenuItem menuItem;

//        menuItem = new JRadioButtonMenuItem(new BasicModeAction(this));
//        menuItem.setSelected(true);
//        group.add(menuItem);
//        viewMenu.add(menuItem);
        
//        menuItem = new JRadioButtonMenuItem(new AdvancedModeAction(this));
//        group.add(menuItem);
//        viewMenu.add(menuItem);
        menuItem = new JMenuItem(new ResetQueryAction(this));
        viewMenu.add(menuItem);
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
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0.25;
        c.gridwidth = 5;
        JComponent b1 = buildRadioButton3(queryEntryEquals, queryEntryStarts, queryEntryContains, 1 );
        JComponent entryMatch = buildGenericCombo2("Entry", b1, wordEntry);
        controlPanel.add(entryMatch);
        gbl.setConstraints(entryMatch, c);
        c.gridwidth = 1;	// reset

        // Rating 
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 5;
        c.gridy = 2;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 5;
        JComponent b2 = buildRadioButton2(queryRatingAtLeast, queryRatingAtMost, 1 );
        JComponent entryRating = buildComboSlider("Rating", b2, wordRatingLabel, wordRatingSlider);
        controlPanel.add(entryRating);
        gbl.setConstraints(entryRating, c);
        c.gridwidth = 1;

        // Method Modified
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        c.weighty = 0.25;
        c.gridwidth = 5;
        JComponent b3 = buildRadioButton3(queryMethodAll, queryMethodManual, queryMethodAuto, 1 );
        controlPanel.add(b3);
        gbl.setConstraints(b3, c);
        c.gridwidth = 1;

        // Length
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 5;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 5;
        Component entryLength = buildComboSlider("Length", buildLengthRadioButton(), wordLengthLabel, wordLengthSlider);
        controlPanel.add(entryLength);
        gbl.setConstraints(entryLength, c);
        c.gridwidth = 1;

        // UsedAny Checkbox
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        c.weighty = 0.25;
        controlPanel.add(usedAny);
        gbl.setConstraints(usedAny, c);
        
        // UsedNYT Checkbox
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 0;
        c.weighty = 0.25;
        controlPanel.add(usedNYT);
        gbl.setConstraints(usedNYT, c);
        
        // Research Checkbox
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 2;
        c.gridy = 2;
        c.weightx = 0;
        c.weighty = 0.25;
        controlPanel.add(research);
        gbl.setConstraints(research, c);
        
        // Query Button
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0.2;
        c.weighty = 0.25;
        c.gridwidth = 1;
//        JButton bq = new JButton(new QueryAction(this, false));
        controlPanel.add(queryButton);
        gbl.setConstraints(queryButton, c);
       
        // Next Button
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 0.2;
        c.weighty = 0.25;
        c.gridwidth = 1;
        controlPanel.add(nextButton);
        gbl.setConstraints(nextButton, c);
        nextButton.setEnabled(false);

        // Add Button
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 3;
        c.weightx = 0.2;
        c.weighty = 0.25;
        c.gridwidth = 1;
        controlPanel.add(addButton);
        gbl.setConstraints(addButton, c);
        addButton.setEnabled(false);	// init to disabled

        // Load Button
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 3;
        c.weightx = 0.2;
        c.weighty = 0.25;
        c.gridwidth = 1;
        controlPanel.add(loadButton);
        gbl.setConstraints(loadButton, c);
        loadButton.setEnabled(false);	// init to disabled
        
        // Export Button
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 4;
        c.gridy = 3;
        c.weightx = 0.2;
        c.weighty = 0.25;
        c.gridwidth = 1;
        controlPanel.add(exportButton);
        gbl.setConstraints(exportButton, c);
        exportButton.setEnabled(false);	// init to disabled
        
        return controlPanel;
    }
    
//    private JComponent buildAdvancedQueryControlPanel()
//    {
//        controlPanel.setBorder(BorderFactory.createTitledBorder(""));
//        GridBagLayout      gbl = new GridBagLayout();
//        GridBagConstraints c   = new GridBagConstraints();
//        c.insets = new Insets(0, 2, 0, 2);
//        controlPanel.setLayout(gbl);
//
//        // Entry
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.NONE;
//        c.gridx = 0;
//        c.gridy = 0;
//        c.weightx = 1;
//        c.weighty = 0;
//        c.gridwidth = 10;
//        JComponent b1 = buildRadioButton3(queryEntryEquals, queryEntryStarts, queryEntryContains, 1 );
//        JComponent entryMatch = buildGenericCombo2("Entry", b1, wordEntry);
//        controlPanel.add(entryMatch);
//        gbl.setConstraints(entryMatch, c);
//        c.gridwidth = 1;	// reset
//
//        // Rating 
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.NONE;
//        c.gridx = 0;
//        c.gridy = 1;
//        c.weightx = 0.5;
//        c.weighty = 0;
//        c.gridwidth = 5;
//        JComponent b2 = buildRadioButton2(queryRatingAtLeast, queryRatingAtMost, 1 );
//        JComponent entryRating = buildComboSlider("Rating", b2, wordRatingLabel, wordRatingSlider);
//        controlPanel.add(entryRating);
//        gbl.setConstraints(entryRating, c);
//        c.gridwidth = 1;
//
//        // Length
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.NONE;
//        c.gridx = 5;
//        c.gridy = 1;
//        c.weightx = 0.5;
//        c.weighty = 0;
//        c.gridwidth = 5;
//        Component entryLength = buildComboSlider("Length", buildLengthRadioButton(), wordLengthLabel, wordLengthSlider);
//        controlPanel.add(entryLength);
//        gbl.setConstraints(entryLength, c);
//        c.gridwidth = 1;
//
//        // Sparkle 
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.NONE;
//        c.gridx = 0;
//        c.gridy = 2;
//        c.weightx = 0.5;
//        c.weighty = 0;
//        c.gridwidth = 5;
//        JComponent b3 = buildRadioButton2(querySparkleAtLeast, querySparkleAtMost, 1 );
//        JComponent entrySparkle = buildComboSlider("Sparkle", b3, wordSparkleLabel, wordSparkleSlider);
//        controlPanel.add(entrySparkle);
//        gbl.setConstraints(entrySparkle, c);
//        c.gridwidth = 1;
//
//        // Facility 
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.NONE;
//        c.gridx = 5;
//        c.gridy = 2;
//        c.weightx = 0.5;
//        c.weighty = 0;
//        c.gridwidth = 5;
//        JComponent b4 = buildRadioButton2(queryFacilityAtLeast, queryFacilityAtMost, 1 );
//        JComponent entryFacility = buildComboSlider("Facility", b4, wordFacilityLabel, wordFacilitySlider);
//        controlPanel.add(entryFacility);
//        gbl.setConstraints(entryFacility, c);
//        c.gridwidth = 1;
//
//        // Currency 
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.NONE;
//        c.gridx = 0;
//        c.gridy = 3;
//        c.weightx = 0.5;
//        c.weighty = 0;
//        c.gridwidth = 5;
//        JComponent b5 = buildRadioButton2(queryCurrencyAtLeast, queryCurrencyAtMost, 1 );
//        JComponent entryCurrency = buildComboSlider("Currency", b5, wordCurrencyLabel, wordCurrencySlider);
//        controlPanel.add(entryCurrency);
//        gbl.setConstraints(entryCurrency, c);
//        c.gridwidth = 1;
//
//        // Taste 
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.NONE;
//        c.gridx = 5;
//        c.gridy = 3;
//        c.weightx = 0.5;
//        c.weighty = 0;
//        c.gridwidth = 5;
//        JComponent b6 = buildRadioButton2(queryTasteAtLeast, queryTasteAtMost, 1 );
//        JComponent entryTaste = buildComboSlider("Taste", b6, wordTasteLabel, wordTasteSlider);
//        controlPanel.add(entryTaste);
//        gbl.setConstraints(entryTaste, c);
//        c.gridwidth = 1;
//
//        // UsedAny Checkbox
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.NONE;
//        c.gridx = 0;
//        c.gridy = 4;
//        c.weightx = 0;
//        c.weighty = 0;
//        controlPanel.add(usedAny);
//        gbl.setConstraints(usedAny, c);
//        
//        // UsedNYT Checkbox
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.NONE;
//        c.gridx = 1;
//        c.gridy = 4;
//        c.weightx = 0;
//        c.weighty = 0;
//        controlPanel.add(usedNYT);
//        gbl.setConstraints(usedNYT, c);
//        
//        // Research Checkbox
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.NONE;
//        c.gridx = 2;
//        c.gridy = 4;
//        c.weightx = 0;
//        c.weighty = 0;
//        controlPanel.add(research);
//        gbl.setConstraints(research, c);
//        
//        // Query Button
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.gridx = 0;
//        c.gridy = 5;
//        c.weightx = 0;
//        c.weighty = 0;
//        c.gridwidth = 1;
//        JButton bq = new JButton(new QueryAction(this, false));
//        controlPanel.add(bq);
//        gbl.setConstraints(bq, c);
//        
//        // Query Button
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.gridx = 1;
//        c.gridy = 5;
//        c.weightx = 0;
//        c.weighty = 0;
//        c.gridwidth = 1;
//        JButton bn = new JButton(new QueryAction(this, true));
//        controlPanel.add(bn);
//        gbl.setConstraints(bn, c);
//        
//        return controlPanel;
//    }
    
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
        //result.setBorder(BorderFactory.createTitledBorder(""));

    	// Add radio buttons to control useMode
        ButtonGroup group = new ButtonGroup();
        
        queryLengthAtLeast.setSelected(true);
        group.add(queryLengthEquals);
        group.add(queryLengthAtMost);
        group.add(queryLengthAtLeast);
        
        //queryLengthEquals.setHorizontalAlignment(SwingConstants.LEFT);
        //queryLengthAtMost.setHorizontalAlignment(SwingConstants.LEFT);
        //queryLengthAtLeast.setHorizontalAlignment(SwingConstants.LEFT);
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
    	//result.add(buildAddControlPanel(), BorderLayout.NORTH);
    	result.add(buildAddDisplayPanel(), BorderLayout.CENTER);
    	return result;
    }
    
    private JComponent buildAddDisplayPanel() 
    {
        addScrollPane = new JScrollPane(addResultArea);
        addResultArea.setText("Add Result Area");
        return addScrollPane;
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
        result.setBorder(BorderFactory.createTitledBorder("Control"));
        //queryPanel.removeAll();
        //queryPanel.setLayout(new GridLayout(1, 2));
        //queryPanel.add(buildSimpleScopePanel());
        //queryPanel.add(buildSimpleFilterPanel());
        //queryPanel.revalidate();
        JLabel test = new JLabel("Rate Control Label");
        result.add(test);
        return result;
    }

    private JComponent buildRateDisplayPanel() 
    {
    	JPanel result = new JPanel();
        result.setBorder(BorderFactory.createTitledBorder("Results"));

        rateResultArea.setText("Rate Result Area");
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
        result.setBorder(BorderFactory.createTitledBorder("Control"));
        result.add(new JLabel("File to Load:"), BorderLayout.WEST);
        result.add(loadFile, BorderLayout.CENTER);
        loadFile.setText("");
        
        return result;
    }
    
    private JComponent buildLoadDisplayPanel() 
    {
        loadScrollPane = new JScrollPane(loadResultArea);
        loadResultArea.setText("Load Result Area");
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
        result.setBorder(BorderFactory.createTitledBorder("Control"));
        result.add(new JLabel("File to Export:"), BorderLayout.WEST);
        result.add(exportFile, BorderLayout.CENTER);
        exportFile.setText("");
        
        return result;
    }
    
    private JComponent buildExportDisplayPanel() 
    {
        exportScrollPane = new JScrollPane(exportResultArea);
        exportResultArea.setText("Export Result Area");
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
    
    private JComponent buildRadioButton2(JRadioButton b1, JRadioButton b2, int defaultButton)
    {
    	JPanel result = new JPanel();
        result.setLayout(new GridLayout(1, 2));
        //result.setBorder(BorderFactory.createTitledBorder(""));

    	// Add radio buttons to control useMode
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
    
    private JComponent buildRadioButton3(JRadioButton b1, JRadioButton b2, JRadioButton b3, int defaultButton)
    {
    	JPanel result = new JPanel();
        result.setLayout(new GridLayout(1, 3));
        //result.setBorder(BorderFactory.createTitledBorder(""));

    	// Add radio buttons to control useMode
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
    
    public void resetQuery() {
        wordEntry.setText("");
        wordLengthSlider.setValue(LENGTH_DEFAULT);
        wordRatingSlider.setValue(RATING_DEFAULT);
        usedAny.setSelected(false);
        usedNYT.setSelected(false);
        research.setSelected(false);
        queryMethodAll.setSelected(true);
    }
    
    public WORD_STATUS doAdd()
    {
    	addResultArea.setText("");
    	String key = wordEntry.getText();
    	int rat = wordRatingSlider.getValue();
    	UsedControl useCtrl = UsedControl.ALL;
    	ResearchControl resCtrl = ResearchControl.NO_RESEARCH;
    	
    	if ( usedNYT.isSelected() )
    		useCtrl = UsedControl.USED_NYT;
    	else if ( usedAny.isSelected() )
        		useCtrl = UsedControl.USED_ANY;
    	
    	if ( research.isSelected() )
    		resCtrl = ResearchControl.NEEDS_RESEARCH;
    	
    	Word w = new Word.Builder(key).rating((byte)rat).usedAny(useCtrl == UsedControl.USED_ANY).usedNYT(useCtrl == UsedControl.USED_NYT).needsResearch(resCtrl == ResearchControl.NEEDS_RESEARCH).manuallyRated(true).build();

    	WORD_STATUS status = dict.putWord(w);
    	Word w1 = dict.getWord(w.getEntry());
		addResultArea.append(w1.getEntry() + " : " + w1.getRating() + "\n");
		
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

    	if ( usedNYT.isSelected() )
    		useCtrl = UsedControl.USED_NYT;
    	else if ( usedAny.isSelected() )
        		useCtrl = UsedControl.USED_ANY;
    	
    	if ( research.isSelected() )
    		resCtrl = ResearchControl.NEEDS_RESEARCH;
    	
    	if (queryMethodManual.isSelected()) {
    		methCtrl = MethodControl.MANUAL;
    	} else if (queryMethodAuto.isSelected()) {
    		methCtrl = MethodControl.AUTOMATIC;
    	}
    	
    	resultSetSize = dict.getCount(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl, methCtrl);
    	if (next) {
    		queryStart += QUERY_LIMIT;
    		queryStart = (queryStart > resultSetSize ? resultSetSize : queryStart);
    	} else {
    		queryStart = 0;
    	}
    	list = dict.getWords(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl, methCtrl, queryStart, QUERY_LIMIT);
		
		if ( list == null || list.isEmpty() )
		{
			queryResultArea.setText(NO_RESULTS_FOUND);
			nextButton.setEnabled(false);
			return "0";
		}
		else
		{
			if (resultSetSize > (queryStart + QUERY_LIMIT)) {	// More data left to display
				nextButton.setEnabled(true);
			} else {
				nextButton.setEnabled(false);
			}
			for ( Word w : list )
				queryResultArea.append(w.toStringQuery() + "\n");
		}
		
		return "" + (queryStart + 1) + "-" + (queryStart + QUERY_LIMIT > resultSetSize ? resultSetSize : (queryStart + QUERY_LIMIT)) + " of " + resultSetSize + (resultSetSize == 1 ? " entry" : " entries");
    }
    
    public void doRate()
    {
    	rateResultArea.setText("Rating...[Add logic here]");
    }
    
    public String doLoad()
    {
    	String filename = loadFile.getText();
    	loadResultArea.setText("Loading from file: " + filename + "\n");
    	BufferedReader br;
    	int newCount = 0;
    	int existCount = 0;
    	int dupCount = 0;
    	
		try {
			br = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			loadResultArea.append("File not found.\n");
			loadResultArea.append(e.toString());
			return "Error.";
		}
    	String line;
    	int count = 0;
    	try {
			while ((line = br.readLine()) != null) {
				if (line.length() < 3) {
					continue;
				}
				// TODO: ==> PARSE LINE HERE!!

				Word w = new Word.Builder(line).rating((byte)wordRatingSlider.getValue()).usedAny(usedAny.isSelected()).usedNYT(usedNYT.isSelected()).build();
				WORD_STATUS status = dict.putWord(w);
				String statText = "";
				if (status == WORD_STATUS.NEW) {
					statText = " (New)";
					newCount++;
				} else if (status == WORD_STATUS.EXISTS) {
					statText = " (Modified)";
					existCount++;
				} else {
					statText = " (Duplicate)";
					dupCount++;
				}
			   loadResultArea.append("Adding word: " + w.getEntry() + statText + "\n");
			   count++;
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
    	String status = "" + count + " words processed. New: " + newCount + ", Modified: " + existCount + ", Duplicate: " + dupCount;
    	return status;
    }
    
    public String doExport()
    {
    	String filename = exportFile.getText();
    	exportResultArea.setText("");
    	FileWriter fw;

    	try {
			fw = new FileWriter(filename);
		} catch (IOException e) {
			exportResultArea.append("Error opening file: " + filename + ".\n");
			exportResultArea.append(e.toString());
			return "Error.";
		}

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

    	if ( usedNYT.isSelected() )
    		useCtrl = UsedControl.USED_NYT;
    	else if ( usedAny.isSelected() )
        		useCtrl = UsedControl.USED_ANY;
    	
    	if ( research.isSelected() )
    		resCtrl = ResearchControl.NEEDS_RESEARCH;
    	
    	if (queryMethodManual.isSelected()) {
    		methCtrl = MethodControl.MANUAL;
    	} else if (queryMethodAuto.isSelected()) {
    		methCtrl = MethodControl.AUTOMATIC;
    	}
    	
    	resultSetSize = dict.getCount(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl, methCtrl);
    	
    	for (int start = 0; start < resultSetSize; start += QUERY_LIMIT) {
        	list = dict.getWords(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl, methCtrl, start, QUERY_LIMIT);
			for ( Word w : list ) {
				exportResultArea.append(w.toString() + "\n");
				try {
					fw.write(w.toString() + "\n");
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
		
		return "Exported " + resultSetSize + (resultSetSize == 1 ? " entry" : " entries");
    }
        
    
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
        
    }

    public void setupListeners()
    {
        wordLengthSlider.addChangeListener(lengthListener);
        wordRatingSlider.addChangeListener(ratingListener);
        usedNYT.addChangeListener(usedNYTListener);
        usedAny.addChangeListener(usedAnyListener);
        resultPaneTabs.addChangeListener(tabListener);
    }

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
    
    public static void main(String[] args) 
    {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // Ignore
        }

		dict.connect();
        
        XDictGui gui = new XDictGui();
        gui.setVisible(true);
        
    }
}
