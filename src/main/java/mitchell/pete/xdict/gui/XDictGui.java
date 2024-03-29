package mitchell.pete.xdict.gui;

import mitchell.pete.xdict.LoadAndExportUtilities;
import mitchell.pete.xdict.Word;
import mitchell.pete.xdict.XDictConfig;
import mitchell.pete.xdict.XDictConfig.RATINGS;
import mitchell.pete.xdict.db.XDictDB_Interface;
import mitchell.pete.xdict.db.XDictDB_MySQL;
import mitchell.pete.xdict.db.XDictSQLException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

public class XDictGui extends JFrame implements WindowListener 
{
    private static final String VERSION = "2.1";
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
    private static int ADD_RATING_DEFAULT = XDictConfig.OK;
    private static int QUERY_RATING_DEFAULT = 1;
    private static int LOAD_RATING_DEFAULT = XDictConfig.OK;

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
    private JComponent        entryMatch;
    private JComponent        entryLength;
    private JComponent        entryRating;
    
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
    private JRadioButton queryMethodRanked = new JRadioButton("Rank-loaded");
    private JRadioButton queryMethodAuto = new JRadioButton("Auto-loaded");

    private JTextField wordEntry            = new JTextField(30);
    private JTextField wordComment          = new JTextField(100);
    private JLabel wordToRate           	= new JLabel();
    private JSlider wordLengthSlider        = new JSlider(3,25,LENGTH_DEFAULT);
    private JLabel wordLengthLabel          = new JLabel(String.valueOf(wordLengthSlider.getValue()));
    private JSlider wordRatingSlider        = new JSlider(0,100,QUERY_RATING_DEFAULT);
    private JLabel wordRatingLabel          = new JLabel(String.valueOf(wordRatingSlider.getValue()));
    private JSlider manualRatingSlider		= new JSlider(0,100,ADD_RATING_DEFAULT);
    private JLabel manualRatingLabel        = new JLabel(String.valueOf(manualRatingSlider.getValue()));
    private JSlider manualRatingSlider2		= new JSlider(0,100,ADD_RATING_DEFAULT);
    private JLabel manualRatingLabel2       = new JLabel(String.valueOf(manualRatingSlider2.getValue()));
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
    private JButton killButton		        = new JButton(new RateAction(this, XDictConfig.RATINGS.NOPE));
    private JButton poorButton		        = new JButton(new RateAction(this, XDictConfig.RATINGS.POOR));
    private JButton lameButton		        = new JButton(new RateAction(this, XDictConfig.RATINGS.LAME));
    private JButton mehButton               = new JButton(new RateAction(this, XDictConfig.RATINGS.MEH));
    private JButton okButton   		        = new JButton(new RateAction(this, XDictConfig.RATINGS.OK));
    private JButton goodButton		        = new JButton(new RateAction(this, XDictConfig.RATINGS.GOOD));
    private JButton excellentButton		    = new JButton(new RateAction(this, XDictConfig.RATINGS.EXCELLENT));
    private JButton poorButton2		        = new JButton(new SetRatingAction(this, XDictConfig.RATINGS.POOR));
    private JButton lameButton2		        = new JButton(new SetRatingAction(this, XDictConfig.RATINGS.LAME));
    private JButton mehButton2 =            new JButton(new SetRatingAction(this, XDictConfig.RATINGS.MEH));
    private JButton okButton2   		    = new JButton(new SetRatingAction(this, XDictConfig.RATINGS.OK));
    private JButton goodButton2		        = new JButton(new SetRatingAction(this, XDictConfig.RATINGS.GOOD));
    private JButton excellentButton2		= new JButton(new SetRatingAction(this, XDictConfig.RATINGS.EXCELLENT));
    private JButton researchButton		    = new JButton(new RateAction(this, XDictConfig.RATINGS.RESEARCH));
    private JButton skipButton		    	= new JButton(new RateAction(this, XDictConfig.RATINGS.SKIP));
    private JButton manualButton		    = new JButton(new RateAction(this, XDictConfig.RATINGS.MANUAL));
    private JTabbedPane resultPaneTabs 		= new JTabbedPane();
    
    private JTextField loadFile             = new JTextField(50);
    private JButton browseLoadButton        = new JButton(new BrowseAction(this, false));
    private JButton browseRestoreButton     = new JButton(new BrowseAction(this, true));
    private JTextField exportFile           = new JTextField(50);

//    private JFileChooser fileChooser1       = new JFileChooser();
//    private JFileChooser fileChooser2       = new JFileChooser();
//    private FileNameExtensionFilter filter  = new FileNameExtensionFilter("Text files", "txt", "dict");

    private DocumentListener currentWordEntryListener;


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
        entryMatch = buildGenericCombo2("Entry", b1, wordEntry);
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
        entryLength = buildComboSlider("Length", buildLengthRadioButton(), wordLengthLabel, wordLengthSlider);
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
        JComponent b3 = buildRadioButton4(queryMethodAll, queryMethodManual, queryMethodRanked, queryMethodAuto, 1);
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
        entryRating = buildComboSlider("Rating", b2, wordRatingLabel, wordRatingSlider);
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

        // Browse Button
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 5;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        controlPanel.add(browseLoadButton);
        controlPanel.add(browseRestoreButton);
        gbl.setConstraints(browseLoadButton, c);
        gbl.setConstraints(browseRestoreButton, c);
        browseLoadButton.setEnabled(false);	// init to disabled
        browseLoadButton.setVisible(false);
        browseRestoreButton.setEnabled(false);
        browseRestoreButton.setVisible(false);

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
        result.add(poorButton);
        result.add(lameButton);
        result.add(mehButton);
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
        result.add(poorButton2);
        result.add(lameButton2);
        result.add(mehButton2);
        result.add(okButton2);
        result.add(goodButton2);
        result.add(excellentButton2);
//        result.add(manualButton2);
        JComponent entryRating = buildGenericCombo2("Manual Rating", manualRatingLabel2, manualRatingSlider2);
        result.add(entryRating);

        setAddRatingButtons(true);
        return result;
    }

    private void setRatingButtons(boolean state) {
        if (listToRate == null || listToRate.isEmpty()) {
            ((RateAction)poorButton.getAction()).resetRatingDesc(-1);
            ((RateAction)lameButton.getAction()).resetRatingDesc(-1);
            ((RateAction)mehButton.getAction()).resetRatingDesc(-1);
            ((RateAction)okButton.getAction()).resetRatingDesc(-1);
            ((RateAction)goodButton.getAction()).resetRatingDesc(-1);
            ((RateAction)excellentButton.getAction()).resetRatingDesc(-1);
        } else {
            ((RateAction)killButton.getAction()).resetRatingDesc(listToRate.get(0).length());
            ((RateAction)poorButton.getAction()).resetRatingDesc(listToRate.get(0).length());
            ((RateAction)lameButton.getAction()).resetRatingDesc(listToRate.get(0).length());
            ((RateAction)mehButton.getAction()).resetRatingDesc(listToRate.get(0).length());
            ((RateAction)okButton.getAction()).resetRatingDesc(listToRate.get(0).length());
            ((RateAction)goodButton.getAction()).resetRatingDesc(listToRate.get(0).length());
            ((RateAction)excellentButton.getAction()).resetRatingDesc(listToRate.get(0).length());
        }

        killButton.setEnabled(state);
    	poorButton.setEnabled(state);
    	lameButton.setEnabled(state);
        mehButton.setEnabled(state);
    	okButton.setEnabled(state);
    	goodButton.setEnabled(state);
    	excellentButton.setEnabled(state);
    	manualButton.setEnabled(state);
    	researchButton.setEnabled(state);
    	skipButton.setEnabled(state);
    }

    private void setAddRatingButtons(boolean state) {
        poorButton2.setEnabled(state);
        lameButton2.setEnabled(state);
        mehButton2.setEnabled(state);
        okButton2.setEnabled(state);
        goodButton2.setEnabled(state);
        excellentButton2.setEnabled(state);
//        manualButton2.setEnabled(state);
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
        exportFile.setText(XDictConfig.EXPORT_FILE_DEFAULT_DIR);
        
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

    // Radio button with 4 options
    private JComponent buildRadioButton4(JRadioButton b1, JRadioButton b2, JRadioButton b3, JRadioButton b4, int defaultButton)
    {
        JPanel result = new JPanel();
        result.setLayout(new GridLayout(1, 4));

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
            case 4:
                b4.setSelected(true);
            default:
                break;
        }
        group.add(b1);
        group.add(b2);
        group.add(b3);
        group.add(b4);

        b1.setVerticalAlignment(SwingConstants.TOP);
        b2.setVerticalAlignment(SwingConstants.TOP);
        b3.setVerticalAlignment(SwingConstants.TOP);
        b4.setVerticalAlignment(SwingConstants.TOP);

        result.add(b1);
        result.add(b2);
        result.add(b3);
        result.add(b4);

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

    public boolean isQueryEnabled() { return resultPaneTabs.getSelectedIndex() == 0; }
    public boolean isAddEnabled() { return resultPaneTabs.getSelectedIndex() == 1; }
    public boolean isRatingEnabled() { return resultPaneTabs.getSelectedIndex() == 2; }
    public boolean isLoadEnabled() { return resultPaneTabs.getSelectedIndex() == 3; }
    public boolean isExportEnabled() { return resultPaneTabs.getSelectedIndex() == 4; }

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

        manualRatingSlider2.setMajorTickSpacing(10);
        manualRatingSlider2.setMinorTickSpacing(2);
        manualRatingSlider2.setPaintTicks(true);
        manualRatingSlider2.setPaintLabels(true);
    }

    /*
     ************  GUI CONTROL CHANGE LISTENERS ************
     */
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
        queryMethodRanked.addChangeListener(queryChangedListener);
        queryMethodAuto.addChangeListener(queryChangedListener);
        resultPaneTabs.addChangeListener(tabListener);
        manualRatingSlider.addChangeListener(manualRatingListener);
        manualRatingSlider2.addChangeListener(manualRatingListener2);
        wordEntry.getDocument().addDocumentListener(wordEntryListener);
        currentWordEntryListener = wordEntryListener;
        loadFile.getDocument().addDocumentListener(loadFileListener);
    }

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

    ChangeListener manualRatingListener2 = new ChangeListener()
    {
        public void stateChanged(ChangeEvent e)
        {
            JSlider source = (JSlider)e.getSource();
            manualRatingLabel2.setText(PAD + String.valueOf(source.getValue()) + PAD);
        }
    };

    ChangeListener tabListener = new ChangeListener()
    {
        public void stateChanged(ChangeEvent e)
        {
            JTabbedPane source = (JTabbedPane)e.getSource();
            if (source.getSelectedIndex() == USE_MODE.QUERY.ordinal()) {
                setChangeListenersToQueryMode();
                resetQuery(false);
            } else if (source.getSelectedIndex() == USE_MODE.ADD.ordinal()) {
                setChangeListenersToAddMode();
                resetAdd();
            } else if (source.getSelectedIndex() == USE_MODE.LOAD.ordinal()) {
                setChangeListenersToLoadMode();
                resetLoad();
            } else if (source.getSelectedIndex() == USE_MODE.EXPORT.ordinal()) {
                setChangeListenersToQueryMode();
                resetExport();
            } else if (source.getSelectedIndex() == USE_MODE.RATE.ordinal()) {
                setChangeListenersToQueryMode();
                resetQuery(true);
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
            if (!source.isSelected()) {
                usedNYT.setSelected(false);             // If not selected, then NYT cannot be selected
                notUsed.setSelected(true);              // If not any, then must be "Not Used"
            } else {
                notUsed.setSelected(false);             // If selected, Not Used cannot be selected
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
            if (source.isSelected()) {
                usedAny.setSelected(true);              // If selected, then "Any" must also be selected
                notUsed.setSelected(false);             // If selected, "Not Used" cannot be selected
            }

            nextButton.setEnabled(false);
            if (queryButton.isEnabled())
                getRootPane().setDefaultButton(queryButton);
            else if (rateQueryButton.isEnabled())
                getRootPane().setDefaultButton(rateQueryButton);
        }
    };

    DocumentListener wordEntryListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            nextButton.setEnabled(false);
            if (queryButton.isEnabled())
                getRootPane().setDefaultButton(queryButton);
            else if (rateQueryButton.isEnabled())
                getRootPane().setDefaultButton(rateQueryButton);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            nextButton.setEnabled(false);
            if (queryButton.isEnabled())
                getRootPane().setDefaultButton(queryButton);
            else if (rateQueryButton.isEnabled())
                getRootPane().setDefaultButton(rateQueryButton);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            // do nothing
        }
    };


    DocumentListener wordEntryListenerAdd = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            resetAdd();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            resetAdd();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            // do nothing
        }
    };

    DocumentListener loadFileListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            File f = new File(loadFile.getText());
            if (f.exists() && f.isFile() && !f.getAbsolutePath().contains(XDictConfig.BACKUP_FILE_NAMECHECK)) {
                loadButton.setEnabled(true);
                getRootPane().setDefaultButton(loadButton);
            } else {
                loadButton.setEnabled(false);
                if (f.getAbsolutePath().contains(XDictConfig.BACKUP_FILE_DIR)) {
                    browseLoadButton.setEnabled(false);
                    browseLoadButton.setVisible(false);
                    browseRestoreButton.setEnabled(true);
                    browseRestoreButton.setVisible(true);
                    getRootPane().setDefaultButton(browseRestoreButton);
                }
                else {
                    browseRestoreButton.setEnabled(false);
                    browseRestoreButton.setVisible(false);
                    browseLoadButton.setEnabled(true);
                    browseLoadButton.setVisible(true);
                    getRootPane().setDefaultButton(browseLoadButton);
                }
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            File f = new File(loadFile.getText());
            if (f.exists() && f.isFile() && !f.getAbsolutePath().contains(XDictConfig.BACKUP_FILE_NAMECHECK)) {
                loadButton.setEnabled(true);
                getRootPane().setDefaultButton(loadButton);
            } else {
                loadButton.setEnabled(false);
                if (f.getAbsolutePath().contains(XDictConfig.BACKUP_FILE_DIR)) {
                    browseLoadButton.setEnabled(false);
                    browseLoadButton.setVisible(false);
                    browseRestoreButton.setEnabled(true);
                    browseRestoreButton.setVisible(true);
                    getRootPane().setDefaultButton(browseRestoreButton);
                }
                else {
                    browseRestoreButton.setEnabled(false);
                    browseRestoreButton.setVisible(false);
                    browseLoadButton.setEnabled(true);
                    browseLoadButton.setVisible(true);
                    getRootPane().setDefaultButton(browseLoadButton);
                }
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            // do nothing
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

    ChangeListener notUsedListenerAddorLoad = new ChangeListener()
    {
        public void stateChanged(ChangeEvent e)
        {
            JCheckBox source = (JCheckBox)e.getSource();
            if (source.isSelected()) {
                usedAny.setSelected(false);             // If selected, then "Used Any" and "Used NYT" cannot be
                usedNYT.setSelected(false);
            } else {
                usedAny.setSelected(true);              // If delselected, assume "Used Any"
            }

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


    public void resetQuery(boolean rating) {

        // Enable and show appropriate actions buttons
        if (rating) {
            queryButton.setEnabled(false);
            queryButton.setVisible(false);
            rateQueryButton.setEnabled(true);
            rateQueryButton.setVisible(true);
            getRootPane().setDefaultButton(rateQueryButton);
        } else {
            queryButton.setEnabled(true);
            queryButton.setVisible(true);
            rateQueryButton.setEnabled(false);
            rateQueryButton.setVisible(false);
            getRootPane().setDefaultButton(queryButton);
        }
        nextButton.setEnabled(false);
        nextButton.setVisible(true);
        addButton.setEnabled(false);
        addButton.setVisible(false);
        loadButton.setEnabled(false);
        loadButton.setVisible(false);
        browseLoadButton.setEnabled(false);
        browseLoadButton.setVisible(false);
        browseRestoreButton.setEnabled(false);
        browseRestoreButton.setVisible(false);
        exportButton.setEnabled(false);
        exportButton.setVisible(false);

        // Enable appropriate parameter controls
        wordEntry.setEnabled(true);
        queryEntryEquals.setEnabled(true);
        queryEntryContains.setEnabled(true);
        queryEntryStarts.setEnabled(true);
        queryMethodAuto.setEnabled(true);
        queryMethodAll.setEnabled(true);
        queryMethodRanked.setEnabled(true);
        queryMethodManual.setEnabled(true);
        queryLengthEquals.setEnabled(true);
        queryLengthAtLeast.setEnabled(true);
        queryLengthAtMost.setEnabled(true);
        wordLengthSlider.setEnabled(true);
        queryRatingAtMost.setEnabled(true);
        queryRatingEquals.setEnabled(true);
        queryRatingAtLeast.setEnabled(true);
        wordRatingSlider.setEnabled(true);
        if (rating) {
            wordComment.setEnabled(true);
        } else {
            wordComment.setEnabled(false);
        }

        // Set default values for parameters
        queryEntryEquals.setSelected(true);
        queryLengthAtLeast.setSelected(true);
        wordLengthSlider.setValue(LENGTH_DEFAULT);
        queryRatingAtLeast.setSelected(true);
        wordRatingSlider.setValue(QUERY_RATING_DEFAULT);
        usedAny.setSelected(true);
        usedNYT.setSelected(true);
        notUsed.setSelected(true);
        research.setSelected(false);
        if (rating) {
            queryMethodAuto.setSelected(true);
            manualRatingSlider.setValue(QUERY_RATING_DEFAULT);
        } else {
            queryMethodAll.setSelected(true);
        }

        // Initialize result area and status line
//        queryResultArea.setText("");
        queryResultArea.setEnabled(false);
        queryResultArea.setDisabledTextColor(Color.BLACK);
        statusLine.setText("Ready.");

    }

    public void resetExport() {

        // Enable and show appropriate actions buttons
        queryButton.setEnabled(false);
        queryButton.setVisible(false);
        nextButton.setEnabled(false);
        nextButton.setVisible(false);
        addButton.setEnabled(false);
        addButton.setVisible(false);
        loadButton.setEnabled(false);
        loadButton.setVisible(false);
        browseLoadButton.setEnabled(false);
        browseLoadButton.setVisible(false);
        browseRestoreButton.setEnabled(false);
        browseRestoreButton.setVisible(false);
        exportButton.setEnabled(true);
        exportButton.setVisible(true);
        rateQueryButton.setEnabled(false);
        rateQueryButton.setVisible(false);
        getRootPane().setDefaultButton(exportButton);


        // Enable appropriate parameter controls
        wordEntry.setEnabled(true);
        wordComment.setEnabled(false);
        queryEntryEquals.setEnabled(true);
        queryEntryContains.setEnabled(true);
        queryEntryStarts.setEnabled(true);
        queryMethodAuto.setEnabled(true);
        queryMethodAll.setEnabled(true);
        queryMethodManual.setEnabled(true);
        queryMethodRanked.setEnabled(true);
        queryLengthEquals.setEnabled(true);
        queryLengthAtLeast.setEnabled(true);
        queryLengthAtMost.setEnabled(true);
        wordLengthSlider.setEnabled(true);
        queryRatingAtMost.setEnabled(true);
        queryRatingEquals.setEnabled(true);
        queryRatingAtLeast.setEnabled(true);
        wordRatingSlider.setEnabled(true);

        // Set default values for parameters
        queryEntryEquals.setSelected(true);
        wordEntry.setText("");
        queryLengthAtLeast.setSelected(true);
        wordLengthSlider.setValue(LENGTH_DEFAULT);
        queryRatingAtLeast.setSelected(true);
        wordRatingSlider.setValue(XDictConfig.EXPORT_DEFAULT_MINIMUM_RATING);
        usedAny.setSelected(true);
        usedNYT.setSelected(true);
        notUsed.setSelected(true);
        research.setSelected(false);
        queryMethodAll.setSelected(true);
        wordComment.setText("");
        exportFile.setText(XDictConfig.EXPORT_FILE_DEFAULT_DIR);

        // Initialize result area and status line
//        exportResultArea.setText("");
        exportResultArea.setEnabled(false);
        exportResultArea.setDisabledTextColor(Color.BLACK);
        statusLine.setText("Ready.");
    }

    public void resetLoad() {

        // Enable and show appropriate actions buttons
        queryButton.setEnabled(false);
        queryButton.setVisible(false);
        nextButton.setEnabled(false);
        nextButton.setVisible(false);
        addButton.setEnabled(false);
        addButton.setVisible(false);
        loadButton.setEnabled(true);
        loadButton.setVisible(true);
        browseLoadButton.setEnabled(true);
        browseLoadButton.setVisible(true);
        browseRestoreButton.setEnabled(false);
        browseRestoreButton.setVisible(false);
        exportButton.setEnabled(false);
        exportButton.setVisible(false);
        rateQueryButton.setEnabled(false);
        rateQueryButton.setVisible(false);
        getRootPane().setDefaultButton(loadButton);

        // Enable appropriate parameter controls
        wordEntry.setEnabled(false);
        wordComment.setEnabled(false);
        queryEntryEquals.setEnabled(false);
        queryEntryContains.setEnabled(false);
        queryEntryStarts.setEnabled(false);
        queryMethodAuto.setEnabled(true);
        queryMethodAll.setEnabled(false);
        queryMethodManual.setEnabled(true);
        queryMethodRanked.setEnabled(false);
        queryLengthEquals.setEnabled(false);
        queryLengthAtLeast.setEnabled(false);
        queryLengthAtMost.setEnabled(false);
        wordLengthSlider.setEnabled(false);
        queryRatingAtMost.setEnabled(false);
        queryRatingEquals.setEnabled(true);
        queryRatingAtLeast.setEnabled(false);
        wordRatingSlider.setEnabled(true);

        // Set default values for parameters
        queryEntryEquals.setSelected(true);
        wordEntry.setText("");
        queryLengthAtLeast.setSelected(true);
        wordLengthSlider.setValue(LENGTH_DEFAULT);
        queryRatingEquals.setSelected(true);
        wordRatingSlider.setValue(LOAD_RATING_DEFAULT);
        usedAny.setSelected(false);
        usedNYT.setSelected(false);
        notUsed.setSelected(true);
        research.setSelected(false);
        queryMethodAuto.setSelected(true);
        wordComment.setText("");
        loadFile.setText(XDictConfig.LOAD_FILE_DEFAULT_DIR);


        // Initialize result area and status line
        loadResultArea.setText("");
        loadResultArea.setEnabled(false);
        loadResultArea.setDisabledTextColor(Color.BLACK);
        statusLine.setText("Ready.");
    }

    public void resetAdd() {

        // Enable and show appropriate actions buttons
        queryButton.setEnabled(false);
        queryButton.setVisible(false);
        nextButton.setEnabled(false);
        nextButton.setVisible(false);
        addButton.setEnabled(true);
        addButton.setVisible(true);
        loadButton.setEnabled(false);
        loadButton.setVisible(false);
        browseLoadButton.setEnabled(false);
        browseLoadButton.setVisible(false);
        browseRestoreButton.setEnabled(false);
        browseRestoreButton.setVisible(false);
        exportButton.setEnabled(false);
        exportButton.setVisible(false);
        rateQueryButton.setEnabled(false);
        rateQueryButton.setVisible(false);
        getRootPane().setDefaultButton(addButton);

        // Enable appropriate parameter controls
        wordEntry.setEnabled(true);
        wordComment.setEnabled(true);
        queryEntryEquals.setEnabled(true);
        queryEntryContains.setEnabled(false);
        queryEntryStarts.setEnabled(false);
        queryMethodManual.setEnabled(true);
        queryMethodAuto.setEnabled(false);
        queryMethodAll.setEnabled(false);
        queryMethodRanked.setEnabled(false);
        queryLengthEquals.setEnabled(false);
        queryLengthAtLeast.setEnabled(false);
        queryLengthAtMost.setEnabled(false);
        wordLengthSlider.setEnabled(false);
        queryRatingAtMost.setEnabled(false);
        queryRatingEquals.setEnabled(false);
        queryRatingAtLeast.setEnabled(false);
        wordRatingSlider.setEnabled(false);

        // Set default values for parameters
        queryEntryEquals.setSelected(true);
        if (!wordEntry.getText().isEmpty()) {

            ((SetRatingAction)poorButton2.getAction()).resetRatingDesc(wordEntry.getText().length());
            ((SetRatingAction)lameButton2.getAction()).resetRatingDesc(wordEntry.getText().length());
            ((SetRatingAction)mehButton2.getAction()).resetRatingDesc(wordEntry.getText().length());
            ((SetRatingAction)okButton2.getAction()).resetRatingDesc(wordEntry.getText().length());
            ((SetRatingAction)goodButton2.getAction()).resetRatingDesc(wordEntry.getText().length());
            ((SetRatingAction)excellentButton2.getAction()).resetRatingDesc(wordEntry.getText().length());

            try {
                Word w1 = dict.getWord(wordEntry.getText());
                if (w1 != null) {
                    manualRatingSlider2.setValue(w1.getRating());
                    usedAny.setSelected(w1.isUsedAny());
                    usedNYT.setSelected(w1.isUsedNYT());
                    notUsed.setSelected(!(w1.isUsedAny() || w1.isUsedNYT()));
                    wordComment.setText(w1.getComment());
                    addButton.setText("Modify");
                    // These are disabled, so just set them to display current values
                    queryLengthEquals.setSelected(true);
                    wordLengthSlider.setValue(w1.length());
                    queryRatingEquals.setSelected(true);
                    wordRatingSlider.setValue(w1.getRating());
                    research.setSelected(w1.needsResearch());
                } else {
                    manualRatingSlider2.setValue(ADD_RATING_DEFAULT);
                    usedAny.setSelected(false);
                    usedNYT.setSelected(false);
                    notUsed.setSelected(true);
                    wordComment.setText("");
                    addButton.setText("Add");
                    // These are disabled, so just set them to default vals
                    queryLengthAtLeast.setSelected(true);
                    wordLengthSlider.setValue(Word.format(wordEntry.getText(), false).length());
                    queryRatingAtLeast.setSelected(true);
                    wordRatingSlider.setValue(QUERY_RATING_DEFAULT);
                    research.setSelected(false);
                }
            } catch (XDictSQLException e) {
             // ignore error here?
            }

        } else {
            manualRatingSlider2.setValue(ADD_RATING_DEFAULT);
            usedAny.setSelected(false);
            usedNYT.setSelected(false);
            notUsed.setSelected(true);
            wordComment.setText("");
            addButton.setText("Add");
            // These are disabled, so just set them to default vals
            queryLengthAtLeast.setSelected(true);
            wordLengthSlider.setValue(LENGTH_DEFAULT);
            queryRatingAtLeast.setSelected(true);
            wordRatingSlider.setValue(QUERY_RATING_DEFAULT);
            research.setSelected(false);
            ((SetRatingAction)poorButton2.getAction()).resetRatingDesc(-1);
            ((SetRatingAction)lameButton2.getAction()).resetRatingDesc(-1);
            ((SetRatingAction)mehButton2.getAction()).resetRatingDesc(-1);
            ((SetRatingAction)okButton2.getAction()).resetRatingDesc(-1);
            ((SetRatingAction)goodButton2.getAction()).resetRatingDesc(-1);
            ((SetRatingAction)excellentButton2.getAction()).resetRatingDesc(-1);
        }
        queryMethodManual.setSelected(true);

        // Initialize result area and status line
//        addResultArea.setText("");
        statusLine.setText("Ready.");
    }

    private void setChangeListenersToQueryMode()
    {
        // First remove existing listener, so don't double-set...
        usedNYT.removeChangeListener(usedNYT.getChangeListeners()[0]);
        usedAny.removeChangeListener(usedAny.getChangeListeners()[0]);
        notUsed.removeChangeListener(notUsed.getChangeListeners()[0]);
        wordEntry.getDocument().removeDocumentListener(currentWordEntryListener);

        // Now set listeners as desired
        usedNYT.addChangeListener(usedNYTListener);
        usedAny.addChangeListener(usedAnyListener);
        notUsed.addChangeListener(notUsedListener);
        wordEntry.getDocument().addDocumentListener(wordEntryListener);
        currentWordEntryListener = wordEntryListener;
    }

    private void setChangeListenersToAddMode()
    {
        // First remove existing listener, so don't double-set...
        usedNYT.removeChangeListener(usedNYT.getChangeListeners()[0]);
        usedAny.removeChangeListener(usedAny.getChangeListeners()[0]);
        notUsed.removeChangeListener(notUsed.getChangeListeners()[0]);
        wordEntry.getDocument().removeDocumentListener(currentWordEntryListener);

        // Now set listeners as desired
        usedNYT.addChangeListener(usedNYTListenerAddOrLoad);
        usedAny.addChangeListener(usedAnyListenerAddOrLoad);
        notUsed.addChangeListener(notUsedListenerAddorLoad);
        wordEntry.getDocument().addDocumentListener(wordEntryListenerAdd);
        currentWordEntryListener = wordEntryListenerAdd;
    }

    private void setChangeListenersToLoadMode()
    {
        // First remove existing listener, so don't double-set...
        usedNYT.removeChangeListener(usedNYT.getChangeListeners()[0]);
        usedAny.removeChangeListener(usedAny.getChangeListeners()[0]);
        notUsed.removeChangeListener(notUsed.getChangeListeners()[0]);
        wordEntry.getDocument().removeDocumentListener(currentWordEntryListener);

        // Now set listeners as desired
        usedNYT.addChangeListener(usedNYTListenerAddOrLoad);
        usedAny.addChangeListener(usedAnyListenerAddOrLoad);
        notUsed.addChangeListener(notUsedListenerAddorLoad);
        wordEntry.getDocument().addDocumentListener(wordEntryListener);
        currentWordEntryListener = wordEntryListener;
    }

    /*
     ************  ACTIONS ************
     */

    public XDictDB_Interface.WORD_STATUS doAdd()
    {
    	addResultArea.setText("");
    	String key = wordEntry.getText();
    	int rat = manualRatingSlider2.getValue();
    	XDictDB_Interface.UsedControl useCtrl = XDictDB_Interface.UsedControl.NOT_USED;
    	XDictDB_Interface.ResearchControl resCtrl = XDictDB_Interface.ResearchControl.NO_RESEARCH;
    	XDictDB_Interface.WORD_STATUS status;

    	if ( usedNYT.isSelected() )
    		useCtrl = XDictDB_Interface.UsedControl.NYT;
    	else if ( usedAny.isSelected() )
        	useCtrl = XDictDB_Interface.UsedControl.ANY;
    	
    	if ( research.isSelected() )
    		resCtrl = XDictDB_Interface.ResearchControl.NEEDS_RESEARCH;
    	
    	Word w = new Word.Builder(key).rating((byte)rat).usedAny(useCtrl == XDictDB_Interface.UsedControl.ANY).usedNYT(useCtrl == XDictDB_Interface.UsedControl.NYT).needsResearch(resCtrl == XDictDB_Interface.ResearchControl.NEEDS_RESEARCH).manuallyRated(true).build();
    	w.setComment(wordComment.getText());    // Need to do this regardless, else cannot delete a comment!

    	if (w.length() < 3) {
    		status = XDictDB_Interface.WORD_STATUS.ERROR;
    		addResultArea.setText("Error: " + w.getEntry() + " is less than 3 characters.");
    	} else if (w.length() > 25) {
    		status = XDictDB_Interface.WORD_STATUS.ERROR;
    		addResultArea.setText("Error: " + w.getEntry() + " is more than 25 characters.");
    	} else {
            try {
                status = dict.putWord(w);
                Word w1 = dict.getWord(w.getEntry());
                addResultArea.append(w1.getEntry() + " : " + w1.getRating() + "\n");
            } catch (XDictSQLException e) {
                addResultArea.append(e.toString());
                status = XDictDB_Interface.WORD_STATUS.ERROR;
            }
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
    	XDictDB_Interface.LengthControl lenCtrl = XDictDB_Interface.LengthControl.ALL;
    	XDictDB_Interface.PatternControl patCtrl = XDictDB_Interface.PatternControl.ALL;
    	XDictDB_Interface.RatingControl ratCtrl = XDictDB_Interface.RatingControl.ALL;
    	XDictDB_Interface.UsedControl useCtrl = XDictDB_Interface.UsedControl.ALL;
    	XDictDB_Interface.ResearchControl resCtrl = XDictDB_Interface.ResearchControl.ALL;
    	XDictDB_Interface.MethodControl methCtrl = XDictDB_Interface.MethodControl.ALL;
    	
    	if ( queryRatingAtMost.isSelected() )
    		ratCtrl = XDictDB_Interface.RatingControl.ATMOST;
    	else if ( queryRatingAtLeast.isSelected() )
    		ratCtrl = XDictDB_Interface.RatingControl.ATLEAST;
        else if ( queryRatingEquals.isSelected() )
            ratCtrl = XDictDB_Interface.RatingControl.EQUALS;

    	if ( key.length() == 0 )	// no pattern selected
    		patCtrl = XDictDB_Interface.PatternControl.ALL;
    	else if ( queryEntryEquals.isSelected() )
    		patCtrl = XDictDB_Interface.PatternControl.EQUALS;
    	else if ( queryEntryStarts.isSelected() )
    		patCtrl = XDictDB_Interface.PatternControl.STARTSWITH;
    	else if ( queryEntryContains.isSelected() )
    		patCtrl = XDictDB_Interface.PatternControl.CONTAINS;

    	if ( queryLengthEquals.isSelected() )
    		lenCtrl = XDictDB_Interface.LengthControl.EQUALS;
    	else if ( queryLengthAtMost.isSelected() )
    		lenCtrl = XDictDB_Interface.LengthControl.ATMOST;
    	else if ( queryLengthAtLeast.isSelected() )
    		lenCtrl = XDictDB_Interface.LengthControl.ATLEAST;

        if ( usedNYT.isSelected() && usedAny.isSelected() && notUsed.isSelected())
            useCtrl = XDictDB_Interface.UsedControl.ALL;
        else if ( usedNYT.isSelected() && usedAny.isSelected() && !notUsed.isSelected())
            useCtrl = XDictDB_Interface.UsedControl.ANY;
        else if ( usedNYT.isSelected() && !usedAny.isSelected() && notUsed.isSelected())
            useCtrl = XDictDB_Interface.UsedControl.NOT_OTHER;
        else if ( !usedNYT.isSelected() && usedAny.isSelected() && notUsed.isSelected())
            useCtrl = XDictDB_Interface.UsedControl.NOT_NYT;
        else if ( usedNYT.isSelected() && !usedAny.isSelected() && !notUsed.isSelected())
            useCtrl = XDictDB_Interface.UsedControl.NYT;
        else if ( !usedNYT.isSelected() && usedAny.isSelected() && !notUsed.isSelected())
            useCtrl = XDictDB_Interface.UsedControl.OTHER;
        else if ( !usedNYT.isSelected() && !usedAny.isSelected() && notUsed.isSelected())
            useCtrl = XDictDB_Interface.UsedControl.NOT_USED;
        else
            useCtrl = XDictDB_Interface.UsedControl.NONE;

    	if ( research.isSelected() )
    		resCtrl = XDictDB_Interface.ResearchControl.NEEDS_RESEARCH;
    	
    	if (queryMethodManual.isSelected()) {
    		methCtrl = XDictDB_Interface.MethodControl.MANUAL;
    	} else if (queryMethodAuto.isSelected()) {
    		methCtrl = XDictDB_Interface.MethodControl.AUTOMATIC;
    	} else if (queryMethodRanked.isSelected()) {
            methCtrl = XDictDB_Interface.MethodControl.RANKED;
        }

        try {
            resultSetSize = dict.getCount(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl, methCtrl, false);
            if (next) {
                queryStart += QUERY_LIMIT;
                queryStart = (queryStart > resultSetSize ? resultSetSize : queryStart);
            } else {
                queryStart = 0;
            }
            list = dict.getWords(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl, methCtrl, queryStart, QUERY_LIMIT, false);
        } catch (XDictSQLException e) {
            queryResultArea.setText(e.toString());
            return e.getTitle();
        }
		
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
    	XDictDB_Interface.LengthControl lenCtrl = XDictDB_Interface.LengthControl.ALL;
    	XDictDB_Interface.PatternControl patCtrl = XDictDB_Interface.PatternControl.ALL;
    	XDictDB_Interface.RatingControl ratCtrl = XDictDB_Interface.RatingControl.ALL;
    	XDictDB_Interface.UsedControl useCtrl = XDictDB_Interface.UsedControl.ALL;
    	XDictDB_Interface.ResearchControl resCtrl = XDictDB_Interface.ResearchControl.ALL;
    	XDictDB_Interface.MethodControl methCtrl = XDictDB_Interface.MethodControl.ALL;
    	
    	if ( queryRatingAtMost.isSelected() )
    		ratCtrl = XDictDB_Interface.RatingControl.ATMOST;
    	else if ( queryRatingAtLeast.isSelected() )
    		ratCtrl = XDictDB_Interface.RatingControl.ATLEAST;
        else if ( queryRatingEquals.isSelected() )
            ratCtrl = XDictDB_Interface.RatingControl.EQUALS;

    	if ( key.length() == 0 )	// no pattern selected
    		patCtrl = XDictDB_Interface.PatternControl.ALL;
    	else if ( queryEntryEquals.isSelected() )
    		patCtrl = XDictDB_Interface.PatternControl.EQUALS;
    	else if ( queryEntryStarts.isSelected() )
    		patCtrl = XDictDB_Interface.PatternControl.STARTSWITH;
    	else if ( queryEntryContains.isSelected() )
    		patCtrl = XDictDB_Interface.PatternControl.CONTAINS;

    	if ( queryLengthEquals.isSelected() )
    		lenCtrl = XDictDB_Interface.LengthControl.EQUALS;
    	else if ( queryLengthAtMost.isSelected() )
    		lenCtrl = XDictDB_Interface.LengthControl.ATMOST;
    	else if ( queryLengthAtLeast.isSelected() )
    		lenCtrl = XDictDB_Interface.LengthControl.ATLEAST;

        if ( usedNYT.isSelected() && usedAny.isSelected() && notUsed.isSelected())
            useCtrl = XDictDB_Interface.UsedControl.ALL;
        else if ( usedNYT.isSelected() && usedAny.isSelected() && !notUsed.isSelected())
            useCtrl = XDictDB_Interface.UsedControl.ANY;
        else if ( usedNYT.isSelected() && !usedAny.isSelected() && notUsed.isSelected())
            useCtrl = XDictDB_Interface.UsedControl.NOT_OTHER;
        else if ( !usedNYT.isSelected() && usedAny.isSelected() && notUsed.isSelected())
            useCtrl = XDictDB_Interface.UsedControl.NOT_NYT;
        else if ( usedNYT.isSelected() && !usedAny.isSelected() && !notUsed.isSelected())
            useCtrl = XDictDB_Interface.UsedControl.NYT;
        else if ( !usedNYT.isSelected() && usedAny.isSelected() && !notUsed.isSelected())
            useCtrl = XDictDB_Interface.UsedControl.OTHER;
        else if ( !usedNYT.isSelected() && !usedAny.isSelected() && notUsed.isSelected())
            useCtrl = XDictDB_Interface.UsedControl.NOT_USED;
        else
            useCtrl = XDictDB_Interface.UsedControl.NONE;

        if ( research.isSelected() )
    		resCtrl = XDictDB_Interface.ResearchControl.NEEDS_RESEARCH;
    	else
    		resCtrl = XDictDB_Interface.ResearchControl.NO_RESEARCH;
    	
    	if (queryMethodManual.isSelected()) {
    		methCtrl = XDictDB_Interface.MethodControl.MANUAL;
    	} else if (queryMethodAuto.isSelected()) {
    		methCtrl = XDictDB_Interface.MethodControl.AUTOMATIC;
    	} else if (queryMethodRanked.isSelected()) {
            methCtrl = XDictDB_Interface.MethodControl.RANKED;
        }


        try {
            resultSetSize = dict.getCount(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl, methCtrl, true);
            if (next) {
                queryStart += listToRate.size();    // skip over any that are left in the list on a "next" action
                queryStart = (queryStart > resultSetSize ? resultSetSize : queryStart);
            } else {
                queryStart = 0;
            }

            if (listToRate != null) {
                listToRate.clear();        // clear the list
            }

            listToRate = dict.getWords(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl, methCtrl, queryStart, RATING_QUERY_LIMIT, true);
        } catch (XDictSQLException e) {
            rateResultArea.setText(e.toString());
            return e.getTitle();
        }
		
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

        try {
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
            } else if (r == XDictConfig.RATINGS.NOPE) {
                rat = XDictConfig.NOPE;
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
        } catch (XDictSQLException e) {
            rateResultArea.setText(e.toString());
            return e.getTitle();
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

        this.getComponent(0).requestFocus();    // request focus back to control panel

        return status;
    }

    public String doSetAddRate(RATINGS r)
    {
    	String status = "";
        String w = wordEntry.getText();

        int rat = XDictConfig.getRating(r, w.length());

        manualRatingSlider2.setValue(rat);
        research.setSelected(false);        // clear any "research" flag when selecting a rating

    	return status;
    }
    
    public void doLoad()
    {
    	String filename = loadFile.getText();
    	loadResultArea.setText("Loading from file: " + filename + "\n");
    	BufferedReader br;
    	int newCount = 0;
    	int existCount = 0;
    	int dupCount = 0;
    	int skipCount = 0;
        boolean isError = false;
    	
		try {
			br = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			loadResultArea.append("File not found.\n");
			loadResultArea.append(e.toString());
			return;
		}
    	String line;
    	int count = 0;
    	XDictDB_Interface.WORD_STATUS status;
		byte rating = 0;

        loadResultArea.append("PLEASE WAIT...\n");
        this.setEnabled(false);     // disable gui during load process
        Date startTime = new Date();

        try {
			while ((line = br.readLine()) != null) {
				if (line.length() < 3) {
					continue;
				}
				Word wTmp = LoadAndExportUtilities.parseWordAndRating(line, XDictConfig.LOAD_FILE_DELIMITERS, (byte) wordRatingSlider.getValue());

				Word w = new Word.Builder(wTmp.getEntry()).rating(wTmp.getRating()).rankedList(wTmp.isRankedList()).usedAny(usedAny.isSelected()).usedNYT(usedNYT.isSelected()).manuallyRated(queryMethodManual.isSelected()).build();

		    	if (w.length() < 3) {
		    		status = XDictDB_Interface.WORD_STATUS.ERROR;
		    		loadResultArea.append(w.getEntry() + " is less than 3 characters. Skipped.\n");
					skipCount++;
		    	} else if (w.length() > 25) {
		    		status = XDictDB_Interface.WORD_STATUS.ERROR;
		    		loadResultArea.append(w.getEntry() + " is more than 25 characters. Skipped.\n");
					skipCount++;
		    	} else {
					status = dict.putWord(w);
					if (status == XDictDB_Interface.WORD_STATUS.NEW) {
//                        loadResultArea.append("New Entry: " + w.getEntry() + ": " + w.getRating() + "\n");
						newCount++;
					} else if (status == XDictDB_Interface.WORD_STATUS.EXISTS) {
						existCount++;
					} else {
						dupCount++;
					}
				   count++;
				   if (count % 1000 == 0) {
					   getStatusLine().showInfo("Processing load..." + count + " records processed.");

				   }
		    	}
			}
		} catch (IOException e) {
			loadResultArea.append("Error reading file.\n");
			loadResultArea.append(e.toString());
            getStatusLine().showInfo("Error.");
            isError = true;
		} catch (XDictSQLException e) {
            loadResultArea.append(e.toString());
            getStatusLine().showInfo(e.getTitle());
            isError = true;
        }
    	try {
			br.close();
		} catch (IOException e) {
			loadResultArea.append("Error closing file.\n");
			loadResultArea.append(e.toString());
		}

        if (!isError) {
            loadResultArea.append("Loading complete.\n");
            Date stopTime = new Date();

            loadResultArea.append(count + " words processed. \nNew: " + newCount + "\nModified: " + existCount + "\nDuplicate: " + dupCount + "\nSkipped: " + skipCount + "\n");
            getStatusLine().showInfo("Load complete (" + ((stopTime.getTime() - startTime.getTime()) / (double) 1000) + " secs).");
        }
        this.setEnabled(true);

    	return;
    }

    public void doRestore()
    {
        boolean isError = false;

        resultPaneTabs.setSelectedIndex(3);     // set to load result pane to display results
        File f = new File(loadFile.getText());
        if (f.exists() && f.getAbsolutePath().contains(XDictConfig.BACKUP_FILE_NAMECHECK)) {
            loadFile.setText(f.getAbsolutePath());
        } else {
            File f1 = new File(XDictConfig.BACKUP_FILE_DIR);
            loadFile.setText(f1.getAbsolutePath());
            loadResultArea.setText("Backup files are restored from the \"" + XDictConfig.BACKUP_FILE_DIR + "\" directory and " +
                    "are in the form \"bkup_TIMESTAMP\", where TIMESTAMP is the time the backup file was created.\n");
            loadResultArea.append("Enter the backup file name in the File to Load field and then retry the Restore action (or Browse and select).\n");
            loadResultArea.append("\nTO ENSURE A CLEAN RESTORE, IT IS HIGHLY RECOMMENDED THAT YOU FIRST DO A DATABASE CLEAR TABLES!\n\n");
            fileSelectDialog(true);
            return;
        }

        browseLoadButton.setEnabled(false);
        browseLoadButton.setVisible(false);
        browseRestoreButton.setEnabled(true);
        browseRestoreButton.setVisible(true);
        loadButton.setEnabled(false);

        String filename = loadFile.getText();
        if ( !filename.contains(XDictConfig.BACKUP_FILE_NAMECHECK)) {
            loadResultArea.setText("Backup files are restored from the \"" + XDictConfig.BACKUP_FILE_DIR + "\" directory and " +
                    "are in the form \"bkup_TIMESTAMP\", where TIMESTAMP is the time the backup file was created.\n");
            loadResultArea.append("Enter the backup file name in the File to Load field and then retry the Restore action (or Browse and select).\n");
            loadResultArea.append("TO ENSURE A CLEAN RESTORE, IT IS HIGHLY RECOMMENDED THAT YOU FIRST DO A DATABASE CLEAR TABLES!\n");
            return;
        }
        if (filename.contains(XDictConfig.TEST_MODE_SUFFIX) && !XDictConfig.testMode) {
            loadResultArea.setText("WARNING: You are trying to restore a TEST MODE database but are NOT in TEST MODE!!!\n");
            loadResultArea.append("If you really mean to do this, you must rename the backup file to remove the " + XDictConfig.TEST_MODE_SUFFIX + " from the name.\n");
            loadResultArea.append("This is for your own safety.\n");
            return;
        }
        loadResultArea.setText("Restoring from file: " + filename + "\n");
        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            loadResultArea.append("File not found.\n");
            loadResultArea.append(e.toString());
            return;
        }
        String line;
        int count = 0;
        XDictDB_Interface.WORD_STATUS status;

        loadResultArea.append("PLEASE WAIT...\n");
        this.setEnabled(false);     // disable GUI during processing
        Date startTime = new Date();

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
            getStatusLine().showInfo("Error.");
            isError = true;
        } catch (XDictSQLException e) {
            loadResultArea.append(e.toString());
            getStatusLine().showInfo(e.getTitle());
            isError = true;
        }
        try {
            br.close();
        } catch (IOException e) {
            loadResultArea.append("Error closing file.\n");
            loadResultArea.append(e.toString());
        }

        if (!isError) {
            loadResultArea.append("Restore complete: " + count + " words processed.\n");

            Date stopTime = new Date();
            getStatusLine().showInfo("Restore complete (" + ((stopTime.getTime() - startTime.getTime()) / (double) 1000) + " secs).");
        }
        this.setEnabled(true);      // re-enable GUI
        return;
    }

    public void doExport(boolean isBackup)
    {
        boolean isError = false;
        resultPaneTabs.setSelectedIndex(4);     // set to query result pane to display results

    	String filename = exportFile.getText();
    	if (isBackup) {
    		Timestamp t = new Timestamp(new Date().getTime());
            String s = t.toString().replaceAll(":", "-").replaceAll(" ", "-");
    		filename = XDictConfig.BACKUP_FILE_NAMECHECK + XDictConfig.DB_MODE_SUFFIX + "_" + s + ".txt";
            exportFile.setText(filename);
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
			return;
		}

        exportResultArea.setText((isBackup ? "Backing up to file: " : "Exporting to file: ") + filename + "\n");


        ArrayList<Word> list = null;
    	int resultSetSize = 0;
    	
    	String key = (isBackup ? "" : wordEntry.getText());
    	int length = (isBackup ? 3 : wordLengthSlider.getValue());
    	int rat = (isBackup ? 0 : wordRatingSlider.getValue());
    	XDictDB_Interface.LengthControl lenCtrl = XDictDB_Interface.LengthControl.ALL;
    	XDictDB_Interface.PatternControl patCtrl = XDictDB_Interface.PatternControl.ALL;
    	XDictDB_Interface.RatingControl ratCtrl = XDictDB_Interface.RatingControl.ALL;
    	XDictDB_Interface.UsedControl useCtrl = XDictDB_Interface.UsedControl.ALL;
    	XDictDB_Interface.ResearchControl resCtrl = XDictDB_Interface.ResearchControl.ALL;
    	XDictDB_Interface.MethodControl methCtrl = XDictDB_Interface.MethodControl.ALL;
    	
    	// For backup, we take everything regardless of selections...
    	if (!isBackup) {
	    	if ( queryRatingAtMost.isSelected() )
	    		ratCtrl = XDictDB_Interface.RatingControl.ATMOST;
	    	else if ( queryRatingAtLeast.isSelected() )
	    		ratCtrl = XDictDB_Interface.RatingControl.ATLEAST;
            else if ( queryRatingEquals.isSelected() )
                ratCtrl = XDictDB_Interface.RatingControl.EQUALS;

	    	if ( key.length() == 0 )	// no pattern selected
	    		patCtrl = XDictDB_Interface.PatternControl.ALL;
	    	else if ( queryEntryEquals.isSelected() )
	    		patCtrl = XDictDB_Interface.PatternControl.EQUALS;
	    	else if ( queryEntryStarts.isSelected() )
	    		patCtrl = XDictDB_Interface.PatternControl.STARTSWITH;
	    	else if ( queryEntryContains.isSelected() )
	    		patCtrl = XDictDB_Interface.PatternControl.CONTAINS;
	
	    	if ( queryLengthEquals.isSelected() )
	    		lenCtrl = XDictDB_Interface.LengthControl.EQUALS;
	    	else if ( queryLengthAtMost.isSelected() )
	    		lenCtrl = XDictDB_Interface.LengthControl.ATMOST;
	    	else if ( queryLengthAtLeast.isSelected() )
	    		lenCtrl = XDictDB_Interface.LengthControl.ATLEAST;
	
            if ( usedNYT.isSelected() ) {
                if (notUsed.isSelected())
                    useCtrl = XDictDB_Interface.UsedControl.ALL;
                else
                    useCtrl = XDictDB_Interface.UsedControl.NYT;
            }
            else if ( usedAny.isSelected() )
                useCtrl = XDictDB_Interface.UsedControl.ANY;
            else if ( notUsed.isSelected())
                useCtrl = XDictDB_Interface.UsedControl.NOT_USED;

	    	if ( research.isSelected() )
	    		resCtrl = XDictDB_Interface.ResearchControl.NEEDS_RESEARCH;
	    	
	    	if (queryMethodManual.isSelected()) {
	    		methCtrl = XDictDB_Interface.MethodControl.MANUAL;
	    	} else if (queryMethodAuto.isSelected()) {
	    		methCtrl = XDictDB_Interface.MethodControl.AUTOMATIC;
	    	} else if (queryMethodRanked.isSelected()) {
                methCtrl = XDictDB_Interface.MethodControl.RANKED;
            }

        }

        try {
            resultSetSize = dict.getCount(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl, methCtrl, false);
        }  catch (XDictSQLException e) {
            exportResultArea.append(e.toString());
            getStatusLine().showInfo(e.getTitle());
            return;
        }
        exportResultArea.append("PLEASE WAIT...\n");
        this.setEnabled(false);
        Date startTime = new Date();

        for (int start = 0; start < resultSetSize; start += QUERY_LIMIT) {
			getStatusLine().showInfo("Processing " + (isBackup ? "backup..." : "export...") + start + " records processed.");

            try {
                list = dict.getWords(lenCtrl, length, patCtrl, key, ratCtrl, rat, useCtrl, resCtrl, methCtrl, start, QUERY_LIMIT, false);
            }  catch (XDictSQLException e) {
                exportResultArea.append(e.toString());
                getStatusLine().showInfo(e.getTitle());
                return;
            }
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
					return;
				}
			}
    	}
    	try {
			fw.close();
		} catch (IOException e) {
			exportResultArea.append("Error closing file: " + filename + ".\n");
			exportResultArea.append(e.toString());
			return;
		}
        exportResultArea.append((isBackup ? "Backed up " : "Exported ") + resultSetSize + (resultSetSize == 1 ? " entry" : " entries.\n"));
        Date stopTime = new Date();
        getStatusLine().showInfo((isBackup ? "Backup " : "Export ") + " complete (" + ((stopTime.getTime() - startTime.getTime()) / (double) 1000) + " secs).");

        this.setEnabled(true);

        exportFile.setText(XDictConfig.EXPORT_FILE_DEFAULT_DIR);

        return;
    }

    public void getDatabaseInfo() {
        resultPaneTabs.setSelectedIndex(0);     // set to query result pane to display results
        queryResultArea.setText("Database Info:" + "\n\n");

        try {
            ArrayList<String> tables = dict.showAllTables();

            if (XDictConfig.testMode) {
                queryResultArea.append("Test Mode uses temporary tables only!\n");
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
            }
        } catch (XDictSQLException e) {
            queryResultArea.append(e.toString());
            getStatusLine().showInfo(e.getTitle());
        }
        return;
    }

    public String doClear() {
        String validation = "YES I REALLY MEAN TO DO THIS";
        resultPaneTabs.setSelectedIndex(0);     // set to query result pane to display results
        if (!wordComment.getText().contains(validation)) {
            queryResultArea.setText("You are requesting to clear all tables in your " + (XDictConfig.testMode ? "TEST MODE " : "") + "database!\n");
            queryResultArea.append("If you really mean to do this, you must enter \"YES I REALLY MEAN TO DO THIS\" in the Comment field and then retry.");
            wordComment.setEnabled(true);
            queryResultArea.setEnabled(true);   // enable text area to allow cut-and-paste
            return "Are you sure?";
        }

        try {
            dict.clear_YesIReallyMeanToDoThis();
        } catch (XDictSQLException e) {
            queryResultArea.append(e.toString());
            wordComment.setText("");
            queryResultArea.setEnabled(false);   // re-disable text area
            return e.getTitle();
        }

        queryResultArea.setText("Tables cleared.");
        wordComment.setText("");        // clear the validation field
        queryResultArea.setEnabled(false);   // re-disable text area

        return "Tables cleared.";
    }

    public void getRatingProgress() {
        resultPaneTabs.setSelectedIndex(0);     // set to query result pane to display results

        this.setEnabled(false);     // disable UI during processing

        int totalRated = 0;
        int totalRanked = 0;
        int totalUnrated = 0;

        String key = "";
        XDictDB_Interface.PatternControl patCtrl = XDictDB_Interface.PatternControl.ALL;

        if ( key.length() == 0 )	// no pattern selected
            patCtrl = XDictDB_Interface.PatternControl.ALL;
        else if ( queryEntryEquals.isSelected() )
            patCtrl = XDictDB_Interface.PatternControl.EQUALS;
        else if ( queryEntryStarts.isSelected() )
            patCtrl = XDictDB_Interface.PatternControl.STARTSWITH;
        else if ( queryEntryContains.isSelected() )
            patCtrl = XDictDB_Interface.PatternControl.CONTAINS;

        queryResultArea.setText("Rating Progress:" + "\n");

        try {
            for (int length = 3; length < 26; length++) {

                int ratedCount = dict.getCount(XDictDB_Interface.LengthControl.EQUALS, length, patCtrl, key, XDictDB_Interface.RatingControl.ALL, 0, XDictDB_Interface.UsedControl.ALL, XDictDB_Interface.ResearchControl.ALL, XDictDB_Interface.MethodControl.MANUAL, false);
                int rankedCount = dict.getCount(XDictDB_Interface.LengthControl.EQUALS, length, patCtrl, key, XDictDB_Interface.RatingControl.ALL, 0, XDictDB_Interface.UsedControl.ALL, XDictDB_Interface.ResearchControl.ALL, XDictDB_Interface.MethodControl.RANKED, false);
                int unratedCount = dict.getCount(XDictDB_Interface.LengthControl.EQUALS, length, patCtrl, key, XDictDB_Interface.RatingControl.ALL, 0, XDictDB_Interface.UsedControl.ALL, XDictDB_Interface.ResearchControl.ALL, XDictDB_Interface.MethodControl.AUTOMATIC, false);

                if (ratedCount + rankedCount + unratedCount == 0)
                    continue;

                double ratedPercent = (double) ratedCount / ((double) ratedCount + (double)rankedCount + (double) unratedCount);
                double rankedPercent = (double) rankedCount / ((double) ratedCount + (double)rankedCount + (double) unratedCount);
                double unratedPercent = (double) unratedCount / ((double) ratedCount + (double)rankedCount + (double) unratedCount);
                DecimalFormat df = new DecimalFormat("##.#%");
                String formattedRatedPercent = df.format(ratedPercent);
                String formattedRankedPercent = df.format(rankedPercent);
                String formattedUnratedPercent = df.format(unratedPercent);
                totalRated += ratedCount;
                totalUnrated += unratedCount;
                totalRanked += rankedCount;

                queryResultArea.append("Length: " + length + "  Total: " + (ratedCount + rankedCount + unratedCount) + "  Rated: " + ratedCount + "  Ranked: " + rankedCount + "  Unrated: " + unratedCount + "  Percents: " + formattedRatedPercent + "|" + formattedRankedPercent + "|" + formattedUnratedPercent +  "\n");

            }
        } catch (XDictSQLException e) {
            queryResultArea.append(e.toString());
            getStatusLine().showInfo(e.getTitle());
            this.setEnabled(true);
            return;
        }

        double ratedPercent = (double)totalRated / ( (double)totalRated + (double)totalRanked + (double)totalUnrated);
        double rankedPercent = (double)totalRanked / ( (double)totalRated + (double)totalRanked + (double)totalUnrated);
        double unratedPercent = (double)totalUnrated / ( (double)totalRated + (double)totalRanked + (double)totalUnrated);
        DecimalFormat df = new DecimalFormat("##.#%");
        String formattedRatedPercent = df.format(ratedPercent);
        String formattedRankedPercent = df.format(rankedPercent);
        String formattedUnratedPercent = df.format(unratedPercent);
        queryResultArea.append("TOTAL:   Total: " + (totalRated + totalRanked + totalUnrated) + "  Rated: " + totalRated + "  Ranked: " + totalRanked + "  Unrated: " + totalUnrated + "  Percents: " + formattedRatedPercent + "|" + formattedRankedPercent + "|" + formattedUnratedPercent + "\n");

        this.setEnabled(true);      // re-enable UI

        return;
    }

    public void getRatingBreakdown() {
        resultPaneTabs.setSelectedIndex(0);     // set to query result pane to display results

        this.setEnabled(false);     // disable UI during processing


        int totalHorrible = 0;   // 0 to 25
        int totalBad = 0;        // 26 to 50
        int totalMedium = 0;     // 51 to 60
        int totalGood = 0;       // 61 to 80
        int totalGreat = 0;      // 81 to 100

        String key = "";
        XDictDB_Interface.PatternControl patCtrl = XDictDB_Interface.PatternControl.ALL;

        if ( key.length() == 0 )	// no pattern selected
            patCtrl = XDictDB_Interface.PatternControl.ALL;
        else if ( queryEntryEquals.isSelected() )
            patCtrl = XDictDB_Interface.PatternControl.EQUALS;
        else if ( queryEntryStarts.isSelected() )
            patCtrl = XDictDB_Interface.PatternControl.STARTSWITH;
        else if ( queryEntryContains.isSelected() )
            patCtrl = XDictDB_Interface.PatternControl.CONTAINS;

        queryResultArea.setText("Rating Breakdown:" + "\n");

        try {
            for (int length = 3; length < 26; length++) {

                int horribleCount = dict.getCount(XDictDB_Interface.LengthControl.EQUALS, length, patCtrl, key, 0, 20, XDictDB_Interface.UsedControl.ALL, XDictDB_Interface.ResearchControl.ALL, XDictDB_Interface.MethodControl.ALL);
                int badCount = dict.getCount(XDictDB_Interface.LengthControl.EQUALS, length, patCtrl, key, 21, 40, XDictDB_Interface.UsedControl.ALL, XDictDB_Interface.ResearchControl.ALL, XDictDB_Interface.MethodControl.ALL);
                int mediumCount = dict.getCount(XDictDB_Interface.LengthControl.EQUALS, length, patCtrl, key, 41, 60, XDictDB_Interface.UsedControl.ALL, XDictDB_Interface.ResearchControl.ALL, XDictDB_Interface.MethodControl.ALL);
                int goodCount = dict.getCount(XDictDB_Interface.LengthControl.EQUALS, length, patCtrl, key, 61, 80, XDictDB_Interface.UsedControl.ALL, XDictDB_Interface.ResearchControl.ALL, XDictDB_Interface.MethodControl.ALL);
                int greatCount = dict.getCount(XDictDB_Interface.LengthControl.EQUALS, length, patCtrl, key, 81, 100, XDictDB_Interface.UsedControl.ALL, XDictDB_Interface.ResearchControl.ALL, XDictDB_Interface.MethodControl.ALL);

                int combinedCount = horribleCount + badCount + mediumCount + goodCount + greatCount;
                if (combinedCount == 0)
                    continue;

                double horriblePercent = (double) horribleCount / (double) combinedCount;
                double badPercent = (double) badCount / (double) combinedCount;
                double mediumPercent = (double) mediumCount / (double) combinedCount;
                double goodPercent = (double) goodCount / (double) combinedCount;
                double greatPercent = (double) greatCount / (double) combinedCount;
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
        } catch (XDictSQLException e) {
            queryResultArea.append(e.toString());
            getStatusLine().showInfo(e.getTitle());
            this.setEnabled(true);
            return;
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

        this.setEnabled(true);      // re-enable UI
        return;
    }

    public void doHelp() {
        try {
            Desktop.getDesktop().open(XDictConfig.HELP_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doAbout() {
        Label copyrightL = new Label("\u00a9");
        String aboutMessage = "This is XDict, a Crossword Dictionary Maintenance Program by Pete Mitchell\n\n" +
                "Version: " + VERSION + "  " + copyrightL.getText() + "2020";
        JOptionPane.showMessageDialog(this, aboutMessage,
                "About XDict",
                JOptionPane.PLAIN_MESSAGE);
    }

    public void fileSelectDialog(boolean isRestore) {

        // If there's text in the File to Load field, use that, otherwise use the default.
        File dir = new File(loadFile.getText());
        if (!dir.exists()) {
            loadResultArea.append(dir.getAbsolutePath() + " not found. \n");
            dir = (isRestore ? new File(XDictConfig.BACKUP_FILE_DIR) : new File(XDictConfig.LOAD_FILE_DEFAULT_DIR));
            loadResultArea.append("Trying default directory (" + dir.getAbsolutePath() + ")\n");
        }
        if (!dir.exists()) {
            loadResultArea.append(dir.getAbsolutePath() + " not found. \n");
            if (isRestore)
                loadResultArea.append("\nIF YOU HAVE NEVER CREATED A BACKUP, THERE MAY NOT BE A \"" + XDictConfig.BACKUP_FILE_DIR + "\" DIRECTORY!\n\n");
            else
                loadResultArea.append("Make sure LOAD_FILE_DEFAULT_DIR is set appropriately in your config.txt file.\n");
            dir = new File(System.getProperty("user.dir"));
            loadResultArea.append("Trying base directory (" + dir.getAbsolutePath() + ")\n");
        }

        System.setProperty("apple.awt.fileDialogForDirectories", "false");
        FileDialog fd = new FileDialog(this, "Choose a " + (isRestore ? "backup file to restore" : "word list to load"), FileDialog.LOAD);
        fd.setDirectory(dir.getAbsolutePath());
        fd.setVisible(true);
        String filename = (fd.getFile() == null ? null : fd.getDirectory() + fd.getFile());
        if (filename != null) {         // if null, then cancelled dialog
            loadFile.setText(filename);
            if (isRestore) {
                doRestore();
            } else {
                doLoad();
            }
        }

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
            System.setProperty( "line.separator", "\n" );
            Properties p = new Properties(System.getProperties());
            System.setProperties(p);
            System.getProperties().list(System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // Ignore
        }

        if ( !XDictConfig.processConfigFile())
            return;

		if ( !dict.connect() )
            return;
        
        XDictGui gui = new XDictGui();
        if (XDictConfig.testMode) {
            gui.setTitle("***** TEST MODE ***** TEST MODE ***** TEST MODE ***** TEST MODE ***** TEST MODE ***** TEST MODE ***** TEST MODE *****");
            gui.queryResultArea.setText("YOU ARE IN TEST MODE!" + "\n\n");
            gui.queryResultArea.append("All actions while in TEST MODE will apply to TEST tables." + "\n");
            gui.queryResultArea.append("Test tables ONLY EXIST FOR THE DURATION OF YOUR SESSION!!." + "\n");
            gui.queryResultArea.append("This allows you to play with the functionality of the system without concern for the actual data." + "\n");
            gui.queryResultArea.append("To switch out of TEST MODE, comment out the TEST_MODE line in config.txt." + "\n");
            try {
                dict.createTablesIfNotExists(true);     // create TEMPORARY Tables
            } catch (XDictSQLException e) {
                return;
            }
        } else {
            try {
                dict.createTablesIfNotExists(false);
            } catch (XDictSQLException e) {
                return;
            }
            gui.setTitle("XDict - A Crossword Dictionary Maintenance Tool by Pete Mitchell");
        }

        // Load icon
        try {
            File f = new File(XDictConfig.ICON_FILE.toString());
            System.out.println("Trying to load icon file: " + f.getAbsolutePath());
            if (f.isFile()) {
                System.out.println("(file exists)");
            } else {
                System.out.println("(file does not exist)");
            }
            FileInputStream fis = new FileInputStream(f);
            Image i = ImageIO.read(fis);
            if (i == null) {
                System.out.println("Cannot load icon file.");
            } else {
                System.out.println("icon: " + i.toString());
                gui.setIconImage(i);
            }
        } catch (IOException e) {
            System.out.println("Cannot load icon file.");
            System.out.println(e.toString());
        }

        gui.setVisible(true);

    }
}
