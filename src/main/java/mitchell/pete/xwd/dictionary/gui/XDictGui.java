package mitchell.pete.xwd.dictionary.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.Action;
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
import javax.swing.JRadioButtonMenuItem;
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
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.PatternControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.ResearchControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.RatingControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.UsedControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_MySQL;

public class XDictGui extends JFrame implements WindowListener 
{

	private static final long serialVersionUID = 2093964455516510191L;

	// This is the DB...
	private static XDictDB_MySQL dict = new XDictDB_MySQL( "xdict" );

    // "Use modes" are basically just different use case tasks
    public enum USE_MODE { QUERY, ADD, RATE, LOAD, EXPORT };
    private USE_MODE useMode = USE_MODE.QUERY;
    private boolean advancedMode = false;
    private static final String PAD = "  ";
    
    private static final String NO_RESULTS_FOUND = "No entries found that match this criteria.\n";

    private JMenuBar          menuBar                = new JMenuBar();
    private JMenu             fileMenu               = new JMenu();
    private JMenu             viewMenu               = new JMenu();
    private JTextArea         queryResultArea        = new JTextArea();
    private JScrollPane       queryScrollPane        = new JScrollPane();
    private JTextArea         addResultArea          = new JTextArea();
    private JTextArea         rateResultArea         = new JTextArea();
    private JTextArea         loadResultArea         = new JTextArea();
    private JTextArea         exportResultArea       = new JTextArea();
    private StatusLine        statusLine             = new StatusLine(420);
    private JProgressBar      progressBar            = new JProgressBar();
    
    //  Control components
    private JPanel controlPanel				= new JPanel();
    
    private JTextField wordEntry            = new JTextField(30);
    private JSlider wordLengthSlider        = new JSlider(3,25,3);
    private JLabel wordLengthLabel          = new JLabel(new Integer(wordLengthSlider.getValue()).toString());
    private JSlider wordRatingSlider        = new JSlider(0,100,50);
    private JLabel wordRatingLabel          = new JLabel(new Integer(wordRatingSlider.getValue()).toString());
    private JSlider wordSparkleSlider        = new JSlider(0,100,50);
    private JLabel wordSparkleLabel          = new JLabel(new Integer(wordSparkleSlider.getValue()).toString());
    private JSlider wordFacilitySlider       = new JSlider(0,100,50);
    private JLabel wordFacilityLabel         = new JLabel(new Integer(wordFacilitySlider.getValue()).toString());
    private JSlider wordCurrencySlider       = new JSlider(0,100,50);
    private JLabel wordCurrencyLabel         = new JLabel(new Integer(wordCurrencySlider.getValue()).toString());
    private JSlider wordTasteSlider          = new JSlider(0,100,50);
    private JLabel wordTasteLabel            = new JLabel(new Integer(wordTasteSlider.getValue()).toString());
    private JCheckBox usedAny               = new JCheckBox("Used Any: ");
    private JCheckBox usedNYT               = new JCheckBox("Used NYT: ");
    private JCheckBox research               = new JCheckBox("Needs Research: ");

    private JRadioButton queryEntryEquals = new JRadioButton("Equals");
    private JRadioButton queryEntryStarts = new JRadioButton("Starts with");
    private JRadioButton queryEntryContains = new JRadioButton("Contains");
    private JRadioButton queryLengthEquals = new JRadioButton("Equals");
    private JRadioButton queryLengthAtMost = new JRadioButton("At Most");
    private JRadioButton queryLengthAtLeast = new JRadioButton("At Least");
    private JRadioButton queryRatingAtMost = new JRadioButton("At Most");
    private JRadioButton queryRatingAtLeast = new JRadioButton("At Least");
    private JRadioButton querySparkleAtMost = new JRadioButton("At Most");
    private JRadioButton querySparkleAtLeast = new JRadioButton("At Least");
    private JRadioButton queryFacilityAtMost = new JRadioButton("At Most");
    private JRadioButton queryFacilityAtLeast = new JRadioButton("At Least");
    private JRadioButton queryCurrencyAtMost = new JRadioButton("At Most");
    private JRadioButton queryCurrencyAtLeast = new JRadioButton("At Least");
    private JRadioButton queryTasteAtMost = new JRadioButton("At Most");
    private JRadioButton queryTasteAtLeast = new JRadioButton("At Least");


    private JCheckBox  packageScope          = new JCheckBox("packages");
    private JCheckBox  classScope            = new JCheckBox("classes");
    private JCheckBox  featureScope          = new JCheckBox("features");
    private JTextField scopeIncludes         = new JTextField();
    private JTextField packageScopeIncludes  = new JTextField();
    private JTextField classScopeIncludes    = new JTextField();
    private JTextField featureScopeIncludes  = new JTextField();
    private JTextField scopeExcludes         = new JTextField();
    private JTextField packageScopeExcludes  = new JTextField();
    private JTextField classScopeExcludes    = new JTextField();
    private JTextField featureScopeExcludes  = new JTextField();
    
    private JCheckBox  packageFilter         = new JCheckBox("packages");
    private JCheckBox  classFilter           = new JCheckBox("classes");
    private JCheckBox  featureFilter         = new JCheckBox("features");
    private JTextField filterIncludes        = new JTextField();
    private JTextField packageFilterIncludes = new JTextField();
    private JTextField classFilterIncludes   = new JTextField();
    private JTextField featureFilterIncludes = new JTextField();
    private JTextField filterExcludes        = new JTextField();
    private JTextField packageFilterExcludes = new JTextField();
    private JTextField classFilterExcludes   = new JTextField();
    private JTextField featureFilterExcludes = new JTextField();

    private JCheckBox  showInbounds          = new JCheckBox("<--");
    private JCheckBox  showOutbounds         = new JCheckBox("-->");
    private JCheckBox  showEmptyNodes        = new JCheckBox("empty elements");
    private JCheckBox  copyOnly              = new JCheckBox("copy only");

    ChangeListener lengthListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    		JSlider source = (JSlider)e.getSource();
    	    wordLengthLabel.setText( PAD + new Integer(source.getValue()).toString() + PAD);
    	}
    };
    ChangeListener ratingListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    		JSlider source = (JSlider)e.getSource();
    	    wordRatingLabel.setText( PAD + new Integer(source.getValue()).toString() + PAD);
    	}
    };
    ChangeListener sparkleListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    		JSlider source = (JSlider)e.getSource();
    	    wordSparkleLabel.setText( PAD + new Integer(source.getValue()).toString() + PAD);
    	}
    };
    ChangeListener facilityListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    		JSlider source = (JSlider)e.getSource();
    	    wordFacilityLabel.setText( PAD + new Integer(source.getValue()).toString() + PAD);
    	}
    };
    ChangeListener currencyListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    		JSlider source = (JSlider)e.getSource();
    	    wordCurrencyLabel.setText( PAD + new Integer(source.getValue()).toString() + PAD);
    	}
    };
    ChangeListener tasteListener = new ChangeListener()
    {
    	public void stateChanged(ChangeEvent e)
    	{
    		JSlider source = (JSlider)e.getSource();
    	    wordTasteLabel.setText( PAD + new Integer(source.getValue()).toString() + PAD );
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

        //setNewDependencyGraph();
        
        /*
         * Use the following as model to set tool tips
         */
        //packageScope.setToolTipText("Select packages");
        //packageFilter.setToolTipText("Show dependencies to/from packages");
        //packageFilterIncludes.setToolTipText("Package at the other end of the dependency must match any these expressions. E.g., /^com.mycompany/, /\\.get\\w+\\(/");

        setupSliders();
        setupListeners();
        /*
         * Use the following as model to set font
         */
        //showInbounds.setFont(getCodeFont(Font.BOLD, 14));
        
        setAdvancedMode(false);
        
        buildMenus();
        buildUI();

        /*
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            Logger.getLogger(DependencyFinder.class).error("Unable to set look and feel", ex);
        }
        */
        
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

        Action    action;
        JMenuItem menuItem;
        JButton   button;

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

        ButtonGroup group = new ButtonGroup();
        JMenuItem menuItem;

        menuItem = new JRadioButtonMenuItem(new BasicModeAction(this));
        menuItem.setSelected(true);
        group.add(menuItem);
        viewMenu.add(menuItem);
        
        menuItem = new JRadioButtonMenuItem(new AdvancedModeAction(this));
        group.add(menuItem);
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
        JTabbedPane result = new JTabbedPane();
        result.setBorder(BorderFactory.createTitledBorder(""));

        result.addTab("Query", buildQueryDisplayPanel());
        result.addTab("Add", buildAddPanel());
        result.addTab("Rate", buildRatePanel());
        result.addTab("Load", buildLoadPanel());
        result.addTab("Export", buildExportPanel());
        
        return result;
    }

    /*
     *	Build control panel in either basic or advanced mode 
     */
    public JComponent buildControlPanel() 
    {
    	controlPanel.removeAll();
        if (isAdvancedMode()) {
            buildAdvancedQueryControlPanel();
        } else {
            buildSimpleQueryControlPanel();
        }
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
        c.weighty = 0;
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
        c.gridy = 1;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 5;
        JComponent b2 = buildRadioButton2(queryRatingAtLeast, queryRatingAtMost, 1 );
        JComponent entryRating = buildComboSlider("Rating", b2, wordRatingLabel, wordRatingSlider);
        controlPanel.add(entryRating);
        gbl.setConstraints(entryRating, c);
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
        c.gridy = 1;
        c.weightx = 0;
        c.weighty = 0;
        controlPanel.add(usedAny);
        gbl.setConstraints(usedAny, c);
        
        // UsedNYT Checkbox
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0;
        c.weighty = 0;
        controlPanel.add(usedNYT);
        gbl.setConstraints(usedNYT, c);
        
        // Research Checkbox
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 0;
        c.weighty = 0;
        controlPanel.add(research);
        gbl.setConstraints(research, c);
        
        // Query Button
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        JButton b = new JButton(new QueryAction(this));
        controlPanel.add(b);
        gbl.setConstraints(b, c);
        
        return controlPanel;
    }
    
    private JComponent buildAdvancedQueryControlPanel()
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
        c.weightx = 1;
        c.weighty = 0;
        c.gridwidth = 10;
        JComponent b1 = buildRadioButton3(queryEntryEquals, queryEntryStarts, queryEntryContains, 1 );
        JComponent entryMatch = buildGenericCombo2("Entry", b1, wordEntry);
        controlPanel.add(entryMatch);
        gbl.setConstraints(entryMatch, c);
        c.gridwidth = 1;	// reset

        // Rating 
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 5;
        JComponent b2 = buildRadioButton2(queryRatingAtLeast, queryRatingAtMost, 1 );
        JComponent entryRating = buildComboSlider("Rating", b2, wordRatingLabel, wordRatingSlider);
        controlPanel.add(entryRating);
        gbl.setConstraints(entryRating, c);
        c.gridwidth = 1;

        // Length
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 5;
        c.gridy = 1;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 5;
        Component entryLength = buildComboSlider("Length", buildLengthRadioButton(), wordLengthLabel, wordLengthSlider);
        controlPanel.add(entryLength);
        gbl.setConstraints(entryLength, c);
        c.gridwidth = 1;

        // Sparkle 
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 5;
        JComponent b3 = buildRadioButton2(querySparkleAtLeast, querySparkleAtMost, 1 );
        JComponent entrySparkle = buildComboSlider("Sparkle", b3, wordSparkleLabel, wordSparkleSlider);
        controlPanel.add(entrySparkle);
        gbl.setConstraints(entrySparkle, c);
        c.gridwidth = 1;

        // Facility 
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 5;
        c.gridy = 2;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 5;
        JComponent b4 = buildRadioButton2(queryFacilityAtLeast, queryFacilityAtMost, 1 );
        JComponent entryFacility = buildComboSlider("Facility", b4, wordFacilityLabel, wordFacilitySlider);
        controlPanel.add(entryFacility);
        gbl.setConstraints(entryFacility, c);
        c.gridwidth = 1;

        // Currency 
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 5;
        JComponent b5 = buildRadioButton2(queryCurrencyAtLeast, queryCurrencyAtMost, 1 );
        JComponent entryCurrency = buildComboSlider("Currency", b5, wordCurrencyLabel, wordCurrencySlider);
        controlPanel.add(entryCurrency);
        gbl.setConstraints(entryCurrency, c);
        c.gridwidth = 1;

        // Taste 
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 5;
        c.gridy = 3;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 5;
        JComponent b6 = buildRadioButton2(queryTasteAtLeast, queryTasteAtMost, 1 );
        JComponent entryTaste = buildComboSlider("Taste", b6, wordTasteLabel, wordTasteSlider);
        controlPanel.add(entryTaste);
        gbl.setConstraints(entryTaste, c);
        c.gridwidth = 1;

        // UsedAny Checkbox
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 0;
        c.weighty = 0;
        controlPanel.add(usedAny);
        gbl.setConstraints(usedAny, c);
        
        // UsedNYT Checkbox
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        c.gridy = 4;
        c.weightx = 0;
        c.weighty = 0;
        controlPanel.add(usedNYT);
        gbl.setConstraints(usedNYT, c);
        
        // Research Checkbox
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 2;
        c.gridy = 4;
        c.weightx = 0;
        c.weighty = 0;
        controlPanel.add(research);
        gbl.setConstraints(research, c);
        
        // Query Button
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        JButton b = new JButton(new QueryAction(this));
        controlPanel.add(b);
        gbl.setConstraints(b, c);
        
        return controlPanel;
    }
    
    private JComponent buildQueryDisplayPanel() 
    {
        queryScrollPane = new JScrollPane(queryResultArea);
        queryResultArea.setText("Query Result Area");

        return queryScrollPane;
    }

    private JComponent buildQueryEntryMatchPanel()
    {
    	JPanel result = new JPanel();
        result.setLayout(new GridLayout(1, 3));
        result.setBorder(BorderFactory.createTitledBorder("Entry"));

    	// Add radio buttons to control useMode
        ButtonGroup group = new ButtonGroup();
        
        queryEntryEquals.setSelected(true);
        group.add(queryEntryEquals);
        group.add(queryEntryStarts);
        group.add(queryEntryContains);
        
    	//queryEntryEquals.setHorizontalAlignment(SwingConstants.LEFT);
    	//queryEntryStarts.setHorizontalAlignment(SwingConstants.LEFT);
    	//queryEntryContains.setHorizontalAlignment(SwingConstants.LEFT);
    	queryEntryEquals.setVerticalAlignment(SwingConstants.TOP);
    	queryEntryStarts.setVerticalAlignment(SwingConstants.TOP);
    	queryEntryContains.setVerticalAlignment(SwingConstants.TOP);
    	
        result.add(queryEntryEquals);
        result.add(queryEntryStarts);
        result.add(queryEntryContains);

        return result;
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
    
    private JComponent buildQueryRatingPanel()
    {
    	JPanel result = new JPanel();
        result.setLayout(new GridLayout(1, 2));
        result.setBorder(BorderFactory.createTitledBorder("Rating"));

    	// Add radio buttons to control useMode
        ButtonGroup group = new ButtonGroup();
        
        queryRatingAtLeast.setSelected(true);
        group.add(queryRatingAtLeast);
        group.add(queryRatingAtMost);
        
        //queryRatingAtLeast.setHorizontalAlignment(SwingConstants.LEFT);
        //queryRatingAtMost.setHorizontalAlignment(SwingConstants.LEFT);
        queryRatingAtLeast.setVerticalAlignment(SwingConstants.TOP);
        queryRatingAtMost.setVerticalAlignment(SwingConstants.TOP);
    	
        result.add(queryRatingAtLeast);
        result.add(queryRatingAtMost);

        return result;
    }
    
    private JComponent buildQuerySparklePanel()
    {
    	JPanel result = new JPanel();
        result.setLayout(new GridLayout(2, 1));
        result.setBorder(BorderFactory.createTitledBorder("Sparkle"));

    	// Add radio buttons to control useMode
        ButtonGroup group = new ButtonGroup();
        
        querySparkleAtLeast.setSelected(true);
        group.add(querySparkleAtLeast);
        group.add(querySparkleAtMost);
        
        querySparkleAtLeast.setHorizontalAlignment(SwingConstants.LEFT);
        querySparkleAtMost.setHorizontalAlignment(SwingConstants.LEFT);
    	
        result.add(querySparkleAtLeast);
        result.add(querySparkleAtMost);

        return result;
    }
    
    private JComponent buildQueryCurrencyPanel()
    {
    	JPanel result = new JPanel();
        result.setLayout(new GridLayout(2, 1));
        result.setBorder(BorderFactory.createTitledBorder("Currency"));

    	// Add radio buttons to control useMode
        ButtonGroup group = new ButtonGroup();
        
        queryCurrencyAtLeast.setSelected(true);
        group.add(queryCurrencyAtLeast);
        group.add(queryCurrencyAtMost);
        
        queryCurrencyAtLeast.setHorizontalAlignment(SwingConstants.LEFT);
        queryCurrencyAtMost.setHorizontalAlignment(SwingConstants.LEFT);
    	
        result.add(queryCurrencyAtLeast);
        result.add(queryCurrencyAtMost);

        return result;
    }
    
    private JComponent buildQueryFacilityPanel()
    {
    	JPanel result = new JPanel();
        result.setLayout(new GridLayout(2, 1));
        result.setBorder(BorderFactory.createTitledBorder("Facility"));

    	// Add radio buttons to control useMode
        ButtonGroup group = new ButtonGroup();
        
        queryFacilityAtLeast.setSelected(true);
        group.add(queryFacilityAtLeast);
        group.add(queryFacilityAtMost);
        
        queryFacilityAtLeast.setHorizontalAlignment(SwingConstants.LEFT);
        queryFacilityAtMost.setHorizontalAlignment(SwingConstants.LEFT);
    	
        result.add(queryFacilityAtLeast);
        result.add(queryFacilityAtMost);

        return result;
    }
    
    private JComponent buildQueryTastePanel()
    {
    	JPanel result = new JPanel();
        result.setLayout(new GridLayout(2, 1));
        result.setBorder(BorderFactory.createTitledBorder("Taste"));

    	// Add radio buttons to control useMode
        ButtonGroup group = new ButtonGroup();
        
        queryTasteAtLeast.setSelected(true);
        group.add(queryTasteAtLeast);
        group.add(queryTasteAtMost);
        
        queryTasteAtLeast.setHorizontalAlignment(SwingConstants.LEFT);
        queryTasteAtMost.setHorizontalAlignment(SwingConstants.LEFT);
    	
        result.add(queryTasteAtLeast);
        result.add(queryTasteAtMost);

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
    	JPanel result = new JPanel();
        result.setBorder(BorderFactory.createTitledBorder("Results"));

        addResultArea.setText("Add Result Area");
        result.add(addResultArea);

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
        result.setBorder(BorderFactory.createTitledBorder("Control"));
        JLabel test = new JLabel("Load Control Label");
        result.add(test);
        
        return result;
    }
    
    private JComponent buildLoadDisplayPanel() 
    {
    	JPanel result = new JPanel();
        result.setBorder(BorderFactory.createTitledBorder("Results"));

        loadResultArea.setText("Load Result Area");
        result.add(loadResultArea);

        return result;
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
        result.setBorder(BorderFactory.createTitledBorder("Control"));
        JLabel test = new JLabel("Export Control Label");
        result.add(test);
        
        return result;
    }
    
    private JComponent buildExportDisplayPanel() 
    {
    	JPanel result = new JPanel();
        result.setBorder(BorderFactory.createTitledBorder("Results"));

        exportResultArea.setText("Export Result Area");
        result.add(exportResultArea);

        return result;
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
        label.setText(PAD + new Integer(wordLengthSlider.getValue()).toString() + PAD);
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
        packageScope.setSelected(true);
        classScope.setSelected(false);
        featureScope.setSelected(false);
        scopeIncludes.setText("//");
        packageScopeIncludes.setText("");
        classScopeIncludes.setText("");
        featureScopeIncludes.setText("");
        scopeExcludes.setText("");
        packageScopeExcludes.setText("");
        classScopeExcludes.setText("");
        featureScopeExcludes.setText("");
    
        packageFilter.setSelected(true);
        classFilter.setSelected(false);
        featureFilter.setSelected(false);
        filterIncludes.setText("//");
        packageFilterIncludes.setText("");
        classFilterIncludes.setText("");
        featureFilterIncludes.setText("");
        filterExcludes.setText("");
        packageFilterExcludes.setText("");
        classFilterExcludes.setText("");
        featureFilterExcludes.setText("");

        showInbounds.setSelected(true);
        showOutbounds.setSelected(true);
        showEmptyNodes.setSelected(true);
        copyOnly.setSelected(false);
    }
    
    public void doAdd()
    {
    	addResultArea.setText("Adding...[Add logic here]");
    }
    
    public int doQuery()
    {
    	queryResultArea.setText("");

    	ArrayList<Word> list = null;
    	
    	int length = wordLengthSlider.getValue();
    	int rat = wordRatingSlider.getValue();
    	String key = wordEntry.getText();
    	LengthControl lenCtrl = LengthControl.ALL;
    	PatternControl patCtrl = PatternControl.ALL;
    	RatingControl ratCtrl = RatingControl.ALL;
    	UsedControl useCtrl = UsedControl.ALL;
    	ResearchControl resCtrl = ResearchControl.ALL;
    	
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
    	
    	list = dict.getWords(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl);
		
		if ( list == null || list.isEmpty() )
		{
			queryResultArea.setText(NO_RESULTS_FOUND);
			return 0;
		}
		else
		{
			for ( Word w : list )
				queryResultArea.append(w.getEntry() + " : " + w.getRating() + "\n");
		}
		
		return list.size();
    }
    
    public void doRate()
    {
    	rateResultArea.setText("Rating...[Add logic here]");
    }
    
    public void doLoad()
    {
    	loadResultArea.setText("Loading...[Add logic here]");
    }
    
    public void doExport()
    {
    	exportResultArea.setText("Exporting...[Add logic here]");
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
        
        wordSparkleSlider.setMajorTickSpacing(10);
        wordSparkleSlider.setMinorTickSpacing(2);
        wordSparkleSlider.setPaintTicks(true);
        wordSparkleSlider.setPaintLabels(true);
    	
        wordFacilitySlider.setMajorTickSpacing(10);
        wordFacilitySlider.setMinorTickSpacing(2);
        wordFacilitySlider.setPaintTicks(true);
        wordFacilitySlider.setPaintLabels(true);

        wordCurrencySlider.setMajorTickSpacing(10);
        wordCurrencySlider.setMinorTickSpacing(2);
        wordCurrencySlider.setPaintTicks(true);
        wordCurrencySlider.setPaintLabels(true);

        wordTasteSlider.setMajorTickSpacing(10);
        wordTasteSlider.setMinorTickSpacing(2);
        wordTasteSlider.setPaintTicks(true);
        wordTasteSlider.setPaintLabels(true);
    }

    public void setupListeners()
    {
        wordLengthSlider.addChangeListener(lengthListener);
        wordRatingSlider.addChangeListener(ratingListener);
        wordSparkleSlider.addChangeListener(sparkleListener);
        wordFacilitySlider.addChangeListener(facilityListener);
        wordCurrencySlider.addChangeListener(currencyListener);
        wordTasteSlider.addChangeListener(tasteListener);
        usedNYT.addChangeListener(usedNYTListener);
        usedAny.addChangeListener(usedAnyListener);
    }

    public boolean isAdvancedMode() {
		return advancedMode;
	}

	public void setAdvancedMode(boolean advancedMode) {
		this.advancedMode = advancedMode;
	}

	private Font getCodeFont(int style, int size) 
    {
        String fontName = "Monospaced";
        
        String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String fontName1 : fontNames) {
            if (fontName1.indexOf("Courier") != -1) {
                fontName = fontName1;
            }
        }

        return new Font(fontName, style, size);
    }

	@Override
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
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
