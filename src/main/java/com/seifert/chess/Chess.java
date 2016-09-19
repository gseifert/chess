/*
 * Chess.java
 *
 * Created on February 24, 2004, 10:11 AM
 */

package com.seifert.chess;

/**
 *
 * @author  ZZ3JPZ
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.applet.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
/** This is the main class that implements the window of the application. */
public class Chess extends JFrame {
    
    private static final String APP_TITLE = "Seifert Chess";
    private static final String APP_VER = "1.0";
    private static final String APP_DESC = "This is a java chess application.";
    private static final String APP_DISC = "Feel free to copy, modify, and/or distribute.";
    private static final String propsFile = "chess.properties";
    protected static Logger logger;
    protected static WaitDialog currentWaitDialog = null;
    
    private Board board;                        // the chess board    
    private JMenuBar menuBar = new JMenuBar();  // the application's menubar
    private JToolBar toolBar = new JToolBar();  // the application's toolbar
    private FileAction newAction, connectAction, hostAction, loadAction, saveAction, closeAction;    
    private EditAction undoAction, redoAction;
    private MoveAction castleKingsideAction, castleQueensideAction;
    private ViewAction rotateAction, messagesAction;            
    private HelpAction aboutAction;             // the actions of the menu items and toolbar buttons
    private Properties props;                   // used to persist user selections
    private JPanel panelBoard;                  // the panel that will contain the board in the frame
    private JLabel filler1, filler2;            // used as filler in the layout of the frame    
    private Box boxSouth;                       // contains all the components at the south position
    private JPanel panelStatus;                 // the status bar
    private JLabel lblWhoseMoveIcon;            // shows whose move it is in the status bar
    private MessageServer ms = null;            // used to allow remote players to send messages to each other
    private JPanel panelMessages;               // contains the message components
    private JTextPane txtAll;    
    private StyledDocument doc;
    private JTextField txtMessage;              
    
    /** Creates a new instance of Chess */
    public Chess() {
        super("Seifert Chess 1.0");
        this.addWindowListener(new WindowHandler());
        this.setJMenuBar(menuBar);
        
        // Create the file menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        // Create the action items
        newAction = new FileAction("New...", Chess.createImageIcon("images/new.gif", "New Board"), KeyStroke.getKeyStroke('N',Event.CTRL_MASK), new Integer(KeyEvent.VK_N), "New Board");
        connectAction = new FileAction("Connect...", Chess.createImageIcon("images/connect.gif", "Connect To Remote Board"), KeyStroke.getKeyStroke('O',Event.CTRL_MASK), new Integer(KeyEvent.VK_O), "Connect To Remote Board");
        hostAction = new FileAction("Host...", Chess.createImageIcon("images/host.gif", "Host Board"), KeyStroke.getKeyStroke('H',Event.CTRL_MASK), new Integer(KeyEvent.VK_H), "Host Remote Board");
        loadAction = new FileAction("Load...", Chess.createImageIcon("images/load.gif", "Load Board"), KeyStroke.getKeyStroke('L',Event.CTRL_MASK), new Integer(KeyEvent.VK_L), "Load Board");
        saveAction = new FileAction("Save...", Chess.createImageIcon("images/save.gif", "Save Board"), KeyStroke.getKeyStroke('S',Event.CTRL_MASK), new Integer(KeyEvent.VK_S), "Save Board");        
        closeAction = new FileAction("Close", null, KeyStroke.getKeyStroke('C',Event.CTRL_MASK), new Integer(KeyEvent.VK_C), "Close");        
        // Add the actions to the menu
        addMenuItem(fileMenu, newAction, false);
        addMenuItem(fileMenu, connectAction, false);
        addMenuItem(fileMenu, hostAction, false);
        fileMenu.addSeparator();
        addMenuItem(fileMenu, loadAction, false);
        addMenuItem(fileMenu, saveAction, false);        
        fileMenu.addSeparator();
        addMenuItem(fileMenu, closeAction, false);
        // Add the menu to the menubar
        menuBar.add(fileMenu);
        
        // Create the edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        // Create the action items
        undoAction = new EditAction("Undo Move", Chess.createImageIcon("images/undo.gif", "Undo Move"), KeyStroke.getKeyStroke('Z', Event.CTRL_MASK), new Integer(KeyEvent.VK_U), "Undo Move");        
        undoAction.setEnabled(false);
        redoAction = new EditAction("Redo Move", Chess.createImageIcon("images/redo.gif", "Redo Move"), KeyStroke.getKeyStroke('Y', Event.CTRL_MASK), new Integer(KeyEvent.VK_R), "Redo Move");        
        redoAction.setEnabled(false);
        // Add the actions to the menu
        addMenuItem(editMenu, undoAction, false);
        addMenuItem(editMenu, redoAction, false);
        // Add the menu to the menubar
        menuBar.add(editMenu);

        // Create the move menu
        JMenu moveMenu = new JMenu("Move");
        moveMenu.setMnemonic(KeyEvent.VK_M);        
        moveMenu.addMenuListener(new MenuHandler());        
        // Create the action items
        castleKingsideAction = new MoveAction("Castle Kingside", null, null, new Integer(KeyEvent.VK_K), "Castle Kingside");        
        castleQueensideAction = new MoveAction("Castle Queenside", null, null, new Integer(KeyEvent.VK_Q), "Castle Queenside");
        // Add the actions to the menu
        addMenuItem(moveMenu, castleKingsideAction, false);
        addMenuItem(moveMenu, castleQueensideAction, false);
        // Add the menu to the menubar
        menuBar.add(moveMenu);
        
        // Create the view menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        // Create the action items
        rotateAction = new ViewAction("Rotate", Chess.createImageIcon("images/rotate.gif", "Rotate Board"), KeyStroke.getKeyStroke('R', Event.CTRL_MASK), new Integer(KeyEvent.VK_R), "Rotate Board");
        messagesAction = new ViewAction("Messages", null, KeyStroke.getKeyStroke('M', Event.CTRL_MASK), new Integer(KeyEvent.VK_M), "Messages");
        messagesAction.setEnabled(false);
        // Add the actions to the menu
        addMenuItem(viewMenu, rotateAction, false);
        viewMenu.addSeparator();
        addMenuItem(viewMenu, messagesAction, true);
        // Add the menu to the menubar
        menuBar.add(viewMenu);        
        
        // Create the help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        // Create the action items
        aboutAction = new HelpAction("About", null, KeyStroke.getKeyStroke('A', Event.CTRL_MASK), new Integer(KeyEvent.VK_A), "About");
        // Add the actions to the menu
        addMenuItem(helpMenu, aboutAction, false);
        // Add the menu to the menubar
        menuBar.add(helpMenu); 
        
        // Add the appropriate actions to the toolbar
        toolBar.add(newAction);
        toolBar.add(connectAction);
        toolBar.add(hostAction);
        toolBar.addSeparator();
        toolBar.add(loadAction);
        toolBar.add(saveAction);
        toolBar.addSeparator();
        toolBar.add(undoAction);
        toolBar.add(redoAction);
        toolBar.addSeparator();
        toolBar.add(rotateAction);
        
        // do not allow the toolbar to float
        toolBar.setFloatable(false);
        // retrieve the JFrame's content pane
        Container contentPane = getContentPane(); 
        // Add the toolbar to the app
        contentPane.add(toolBar, BorderLayout.NORTH);        
         
        // create the default properties
        Properties propsDefaults = new Properties();
        propsDefaults.setProperty("NamePlayer1", "Player 1");
        propsDefaults.setProperty("NamePlayer2", "Player 2"); 
        propsDefaults.setProperty("ColorPlayer1", String.valueOf(Color.WHITE.getRGB()));
        propsDefaults.setProperty("ColorPlayer2", String.valueOf((new Color(89, 132, 189)).getRGB()));
        propsDefaults.setProperty("HostPort", String.valueOf(HostBoard.DEFAULT_HOST_PORT));
        propsDefaults.setProperty("RemoteIP", RemoteBoard.DEFAULT_REMOTE_IP);
        propsDefaults.setProperty("RemotePort", String.valueOf(RemoteBoard.DEFAULT_REMOTE_PORT));
        props = new Properties(propsDefaults);
        // try to load the properties from the properties file
        try {
            props.load(new FileInputStream(Chess.propsFile));
        }
        catch (FileNotFoundException fnfe) {
            logger.warning("Couldn't find properties file: " + Chess.propsFile);
        }
        catch (IOException ioe) {
            logger.warning(ioe.toString() + ": " + "Couldn't load properties file: " + Chess.propsFile);
        }
        
        // set the chess piece colors
        ChessPiece.colorPlayer1 = new Color(Integer.parseInt(props.getProperty("ColorPlayer1")));
        ChessPiece.colorPlayer2 = new Color(Integer.parseInt(props.getProperty("ColorPlayer2")));
        
        // Create a new board
        board = new Board(props.getProperty("NamePlayer1"), props.getProperty("NamePlayer2"));
        
        // create the panel that will contain the board
        panelBoard = new JPanel(new BorderLayout());
        panelBoard.add(board, BorderLayout.CENTER);
        // add the filler
        filler1 = new JLabel(Chess.createImageIcon("images/blank.gif", ""));
        filler2 = new JLabel(Chess.createImageIcon("images/blank.gif", ""));
        setFiller(board.getPosition());
        
        // Add the board panel to the app
        contentPane.add(panelBoard, BorderLayout.CENTER);         
        
        // create the status bar
        panelStatus = new JPanel(new BorderLayout());        
        panelStatus.setBorder(BorderFactory.createEtchedBorder());
        ImageIcon whoseMoveImg = Chess.createImageIcon("images/whoseMove.gif", "Whose Move");        
        if (whoseMoveImg != null) {
            JPanel whoseMovePanel = new JPanel();
            JLabel lblWhoseMove = new JLabel("Next Move: ");
            lblWhoseMoveIcon = new JLabel(whoseMoveImg);            
            whoseMovePanel.add(lblWhoseMove);
            whoseMovePanel.add(lblWhoseMoveIcon);
            panelStatus.add(whoseMovePanel, BorderLayout.EAST);
            // update the status bar
            updateStatusBar();            
        }        
        // create the south panel
        boxSouth = new Box(BoxLayout.Y_AXIS);        
        boxSouth.add(panelStatus);
        contentPane.add(boxSouth, BorderLayout.SOUTH);
    }
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {        
        try {            
            // set up logging for the app
            Handler fh = new FileHandler("chess.log");
            fh.setFormatter(new SimpleFormatter());
            logger = Logger.getLogger("com.seifert.chess");
            logger.addHandler(fh);
            logger.setLevel(Level.INFO);
            // set the look and feel for the app
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // create the chess window
            Chess chess = new Chess();            
            chess.pack();
            chess.setLocationRelativeTo(null);
            ImageIcon appImg = createImageIcon("images/chess.gif", APP_TITLE);
            if (appImg != null) {
                chess.setIconImage(appImg.getImage());
            }
            chess.setVisible(true);            
        }
        catch (Exception e) {
            logger.severe(e.toString());
            e.printStackTrace();                        
            System.exit(0);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });                
    }    
    
    /** Returns the port the Message Server is listening on */
    public int getMessagePort() {
        return ms.getPort();
    }
    
    /** Posts a new message */
    public void insertMessage(String msg) {
        try {
            if (msg.indexOf(":") != -1) {
                String style;
                NetBoard nb = (NetBoard)board;            
                if (nb.getPlayerName(nb.getLocalPlayer()).equals(msg.substring(0, msg.indexOf(":")))) {            
                    style = "local";                
                }
                else {
                    style = "remote";
                }
                doc.insertString(doc.getLength(), msg + "\n", doc.getStyle(style));
                txtAll.setCaretPosition(doc.getLength());
            }
        }
        catch (Exception e) {
            logger.warning("Couldn't insert text into text pane: " + e);                                    
        }
    }
     
    /** Gets the extension of a file. 
     *  @param f the file to get the extension of
     *  @return the extension of the given file (without the period)
     */  
    private static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /** Creates an ImageIcon object of the given image file and returns the object
     *  @param path the relative path of the image file
     *  @param description the description of the image
     *  @return an ImageIcon object of the given image file, or null if the path
     *  was invalid
     */
    protected static ImageIcon createImageIcon(String path, String description) {
        URL imgURL = Chess.class.getClassLoader().getResource(path);        
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);            
        }
        else {     
            String msg = "Couldn't find image file for " + description + ": " + path;
            // create dummy frame so message shows on top of other windows            
            JFrame dummy = new JFrame();
            JOptionPane.showMessageDialog(dummy, msg, "File not found", JOptionPane.ERROR_MESSAGE);
            dummy = null;
            logger.warning(msg);
            return null;
        }
    }    
    
    /** Plays the given sound file
     *  @param soundFile the relative path of the audio file to play
     */
    protected static void playSound(String soundFile) {        
        try {
            final String file = soundFile;
            // play the sound in the event dispatching thread
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    AudioClip clip = Applet.newAudioClip(Chess.class.getClassLoader().getResource(file));
                    clip.play();
                }
            });                    
        }
        catch (Exception e) {
            logger.warning(e.toString() + ": " + "Couldn't play sound file: " + soundFile);
        }
    }
    
    /** Updates the status bar to indicate whose move it is     
     */
    protected void updateStatusBar() {        
        ImageIcon imgIcon = Chess.createImageIcon("images/whoseMove.gif", "Whose Move");
        Image img = imgIcon.getImage();        
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        //build icm and buffered image
        final int NUM_BITS_PER_PIXEL = 8;
        final int NUM_COLORS = 256;        
        final int TRANSPARENT_COLOR = 16;
        byte[] r = new byte[NUM_COLORS];
        byte[] g = new byte[NUM_COLORS];
        byte[] b = new byte[NUM_COLORS];                                
        r[0] = (byte)TRANSPARENT_COLOR;
        g[0] = (byte)TRANSPARENT_COLOR;
        b[0] = (byte)TRANSPARENT_COLOR;
        r[1] = (byte)0;
        g[1] = (byte)0;
        b[1] = (byte)0;
        if (board.getWhoseMove() == Board.PLAYER_1) {        
            r[2] = (byte)ChessPiece.colorPlayer1.getRed();
            g[2] = (byte)ChessPiece.colorPlayer1.getGreen();
            b[2] = (byte)ChessPiece.colorPlayer1.getBlue();                
        }
        else {
            r[2] = (byte)ChessPiece.colorPlayer2.getRed();
            g[2] = (byte)ChessPiece.colorPlayer2.getGreen();
            b[2] = (byte)ChessPiece.colorPlayer2.getBlue();            
        }   
        for (int i = 3; i < NUM_COLORS; i++) {
            r[i] = (byte)0;
            g[i] = (byte)0;
            b[i] = (byte)0; 
        }   
        // create the indexcolormodel with the transparent color set to index 0
        IndexColorModel icm = new IndexColorModel(NUM_BITS_PER_PIXEL, NUM_COLORS, r, g, b, 0);
        // create a blank buffered image with the indexcolormodel
        BufferedImage newImg = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED, icm);
        // draw the piece's image onto the buffered image
        newImg.createGraphics().drawImage(img, 0, 0, null);
        // set the icon to the new image
        imgIcon.setImage(newImg);                
        lblWhoseMoveIcon.setIcon(imgIcon); 
        
        // refresh the status bar
        lblWhoseMoveIcon.validate();        
    }
    
    /** Adds the given action to the given menu.
     *  @param menu the JMenu to add the action to
     *  @param action the Action to add to the menu
     *  @return the JMenuItem that is created and added to the menu
     */
    private JMenuItem addMenuItem(JMenu menu, Action action, boolean checkbox) {                
        // Add the action to the given menu
        JMenuItem item;
        if (!checkbox) {
            item = menu.add(action);        
        }
        else {
            item = new JCheckBoxMenuItem(action);
            menu.add(item);
        }
        // we don't want the icons in the menus, just on the toolbar
        item.setIcon(null);
        // return the item if needed
        return item;
    }

    /** Sets whether or not the undo command is enabled if undos are allowed
     *  @param enabled true to enable the undo command, false to disable it
     */
    public void enableUndo(boolean enabled) {
        if (board.getAllowUndo()) {
            this.undoAction.setEnabled(enabled);
        }
        else {
            this.undoAction.setEnabled(false);
        }
    }
    
    /** Sets whether or not the redo command is enabled if undos are allowed
     *  @param enabled true to enable the redo command, false to disable it
     */
    public void enableRedo(boolean enabled) {
        if (board.getAllowUndo()) {
            this.redoAction.setEnabled(enabled);
        }
        else {
            this.redoAction.setEnabled(false);
        }
    }
    
    /** Removes the current board and adds the new one.
     *  @param newBoard the new board to load
     */
    private void refreshBoard(Board newBoard) {
        // close the connection on the current board if needed
        if (board instanceof RemoteBoard) {
            ((RemoteBoard)board).closeConnection(false);
        }
        else if (board instanceof HostBoard) {
            ((HostBoard)board).closeConnection(false);
        }        
        Container c = board.getParent();
        c.remove(board);                    
        board = newBoard;
        c.add(board, BorderLayout.CENTER);
        setFiller(board.getPosition());
        // set the undo and redo actions appropriately
        if (board.canUndo()) {
            this.enableUndo(true);
            if (board.canRedo()) {
                this.enableRedo(true);
            }
            else {
                this.enableRedo(false);
            }            
        }
        else {
            this.enableUndo(false);
            this.enableRedo(false);
        }
        // update the status bar
        this.updateStatusBar();
        
        // enable/disable messages appropriately
        if (board instanceof NetBoard) {
            messagesAction.setEnabled(true);
        }
        else {
            messagesAction.setEnabled(false);
        }
        
        // refresh the screen
        c.validate();
        c.repaint();
    }
    
    /** Implements the file filter when opening or loading a board.
     *  It only displays files with the specific extension that is provided in its
     *  constructor.
     */
    class BoardFileFilter extends javax.swing.filechooser.FileFilter {
        private String description;
        private String extension;
        
        BoardFileFilter(String extension, String description) {            
            this.extension = extension;
            this.description = description;
        }
        
        public boolean accept(File f) {
            // always accept directories so the user can peruse the file system
            if (f.isDirectory()) {
                return true;
            }
            // check the extension of the file with the filtered extension            
            if (this.extension.equalsIgnoreCase(Chess.getExtension(f))) {
                return true;
            }            
            return false;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getExtension() {
            return extension;
        }
    }
    
    /** Implements the actions of the file menu and toolbar buttons. */
    class FileAction extends AbstractAction {        
        FileAction(String name, ImageIcon icon, KeyStroke keystroke, Integer i, String tooltip) {
            super(name);
            if (icon != null) {
                putValue(SMALL_ICON, icon);
            }
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }            
            if (i != null) {
                putValue(MNEMONIC_KEY, i);
            }
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }
        }
        
        public void actionPerformed(ActionEvent e) {            
            String name = (String)this.getValue(AbstractAction.NAME);
            if (name.startsWith("New")) {                
                NewDialog newDialog = new NewDialog();
                newDialog.pack();
                newDialog.setLocationRelativeTo(board);
                newDialog.setVisible(true);                
            }
            else if (name.startsWith("Connect")) {                
                ConnectDialog connectDialog = new ConnectDialog();
                connectDialog.pack();
                connectDialog.setLocationRelativeTo(board);
                connectDialog.setVisible(true);                
            }
            else if (name.startsWith("Host")) {                
                HostDialog hostDialog = new HostDialog();
                hostDialog.pack();
                hostDialog.setLocationRelativeTo(board);
                hostDialog.setVisible(true);                
            }
            else if (name.startsWith("Load")) {
                // Create a file chooser which will open to the current directory
                JFileChooser chooser = new JFileChooser(".");
                // Create a file filter that will accept only board files
                BoardFileFilter filter = new BoardFileFilter("brd", "Board files");
                chooser.setFileFilter(filter);                
                int retVal = chooser.showOpenDialog(board.getTopLevelAncestor());
                if (retVal != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                ObjectInputStream objectIn = null;
                try {
                    File f = chooser.getSelectedFile();
                    objectIn = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
                    Object obj = objectIn.readObject();        
                    objectIn.close();
                    // remove board and then load the saved one
                    refreshBoard((Board)obj);                    
                    logger.info("Board loaded from " + f);
                }
                catch (ClassNotFoundException ce) {
                    String msg = "Error loading board.";
                    JOptionPane.showMessageDialog(board, msg, "ClassNotFoundException", JOptionPane.ERROR_MESSAGE);
                    logger.warning(ce.toString() + ": " + msg);                    
                }
                catch (IOException ioe) {
                    String msg = "Error loading board.";
                    JOptionPane.showMessageDialog(board, msg, "IOException", JOptionPane.ERROR_MESSAGE);
                    logger.warning(ioe.toString() + ": " + msg);
                }
            }
            else if (name.startsWith("Save")) {                
                JFileChooser chooser = new JFileChooser(".");
                BoardFileFilter filter = new BoardFileFilter("brd", "Board files");                
                chooser.setFileFilter(filter);
                int retVal = chooser.showSaveDialog(board.getTopLevelAncestor());
                if (retVal != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                ObjectOutputStream objectOut = null;
                try {                    
                    String file = chooser.getSelectedFile().getPath();
                    // add the extension if none was given
                    if (Chess.getExtension(chooser.getSelectedFile()) == null) {
                        file += "." + filter.getExtension();
                    }
                    // Create the object output stream
                    objectOut = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
                    
                    // if this is a NetBoard save to Board object                    
                    if (board instanceof NetBoard) {
                        Board tempBoard = new Board(board.getPlayerName(Board.PLAYER_1), board.getPlayerName(Board.PLAYER_2));                        
                        while (tempBoard.getPosition() != board.getPosition()) {
                            tempBoard.rotate();
                        }
                        if (tempBoard.getWhoseMove() != board.getWhoseMove()) {
                            tempBoard.switchWhoseMove();
                        }
                        for (int i = 0; i <= 7; i++) {
                            for (int j = 0; j<=7; j++) {                        
                                tempBoard.getSquare(i, j).setPiece(board.getSquare(i, j).getPiece());
                            }
                        }  
                        tempBoard.setAllowUndo(false);
                        
                        // Write the board object to the file
                        objectOut.writeObject(tempBoard);
                        
                        // restore the current board if needed
                        if (board instanceof NetBoard) {                        
                            for (int i = 0; i <= 7; i++) {
                                for (int j = 0; j<=7; j++) {                        
                                    board.getSquare(i, j).setPiece(tempBoard.getSquare(i, j).getPiece());
                                }                            
                            }
                        }
                    }       
                    else {
                        // Write the board object to the file
                        objectOut.writeObject(board);
                    }
                    // flush the output stream
                    objectOut.flush();
                    // Close the stream
                    objectOut.close();
                    logger.info("Board saved to " + file);
                }
                catch (IOException ioe) {
                    String msg = "Error saving board.";
                    JOptionPane.showMessageDialog(board, msg, "IOException", JOptionPane.ERROR_MESSAGE);
                    logger.warning(ioe.toString() + ": " + msg);                    
                }
            }
            else if (name.startsWith("Close")) {
                // close the chess application 
                // (Board->JPanel->Content Pane->Layered Pane->Root Pane->Chess)
                Chess chess = (Chess)board.getTopLevelAncestor();                                
                chess.dispatchEvent(new WindowEvent(chess, WindowEvent.WINDOW_CLOSING));
            }             
        }
    }
    
    /** Implements the actions of the edit menu and toolbar buttons. */
    class EditAction extends AbstractAction {        
        EditAction(String name, ImageIcon icon, KeyStroke keystroke, Integer i, String tooltip) {
            super(name);
            if (icon != null) {
                putValue(SMALL_ICON, icon);
            }
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }            
            if (i != null) {
                putValue(MNEMONIC_KEY, i);
            }
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }            
        }
        
        public void actionPerformed(ActionEvent e) {            
            String name = (String)this.getValue(AbstractAction.NAME);
            if (name.startsWith("Undo")) {
                // undo the latest move
                board.undoMove();
            }
            else if (name.startsWith("Redo")) {
                // redo the move that was undone
                board.redoMove();
            }
        }
    }  
    
    /** Implements the actions of the move menu and toolbar buttons. */
    class MoveAction extends AbstractAction {        
        MoveAction(String name, ImageIcon icon, KeyStroke keystroke, Integer i, String tooltip) {
            super(name);
            if (icon != null) {
                putValue(SMALL_ICON, icon);
            }
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }            
            if (i != null) {
                putValue(MNEMONIC_KEY, i);
            }
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }            
        }
        
        public void actionPerformed(ActionEvent e) {            
            String name = (String)this.getValue(AbstractAction.NAME);
            Square sqKing = null;
            Color matchColor;            
            if (board.getWhoseMove() == Board.PLAYER_1) {
                matchColor = ChessPiece.colorPlayer1;                        
            }
            else {
                matchColor = ChessPiece.colorPlayer2;                        
            }
            // ********** inefficient code ***************
            outer1:
            for (int i = 0; i <= 7; i++) {
                for (int j = 0; j <= 7; j++) {
                    sqKing = board.getSquare(i, j);                    
                    if (sqKing.getPiece().getType() == ChessPiece.KING && sqKing.getPiece().getColor().equals(matchColor)) {
                        break outer1;
                    }
                }
            }        
            
            if (name.startsWith("Castle Kingside")) {
                // castle kingside
                if (sqKing != null) {                                    
                    King king = (King)sqKing.getPiece();
                    if (king.isValidCastle(King.CASTLE_KINGSIDE)) {
                        king.castle(King.CASTLE_KINGSIDE, false);
                        // send the move to the remote player if needed                            
                        if (board instanceof NetBoard) {                                           
                            NetBoard netBoard = (NetBoard)board;
                            netBoard.getOut().println(netBoard.getChessProtocol().createMessage(netBoard));
                            // listen for remote player's next move
                            netBoard.listenForMove();
                        }
                    }
                }
            }
            else if (name.startsWith("Castle Queenside")) {
                // caslte queenside
                if (sqKing != null) {                                    
                    King king = (King)sqKing.getPiece();
                    if (king.isValidCastle(King.CASTLE_QUEENSIDE)) {
                        king.castle(King.CASTLE_QUEENSIDE, false);
                        // send the move to the remote player if needed                            
                        if (board instanceof NetBoard) {                                           
                            NetBoard netBoard = (NetBoard)board;
                            netBoard.getOut().println(netBoard.getChessProtocol().createMessage(netBoard));
                            // listen for remote player's next move
                            netBoard.listenForMove();
                        }
                    }
                }
            }
        }
    }
    
    /** Implements the actions of the view menu and toolbar buttons. */
    class ViewAction extends AbstractAction {        
        ViewAction(String name, ImageIcon icon, KeyStroke keystroke, Integer i, String tooltip) {
            super(name);
            if (icon != null) {
                putValue(SMALL_ICON, icon);
            }
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }            
            if (i != null) {
                putValue(MNEMONIC_KEY, i);
            }
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }            
        }
        
        public void actionPerformed(ActionEvent e) {            
            String name = (String)this.getValue(AbstractAction.NAME);
            if (name.startsWith("Rotate")) {
                // rotate the board
                board.rotate();
                setFiller(board.getPosition());
            }
            else if (name.startsWith("Messages")) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();                
                if (item.getState() == true) {
                    // create the components if needed
                    if (txtAll == null) {                        
                        txtAll = new JTextPane();
                        txtAll.setBorder(BorderFactory.createEtchedBorder());
                        doc = txtAll.getStyledDocument();
                        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
                        Style local = doc.addStyle("local", def);
                        Style remote = doc.addStyle("remote", local);
                        StyleConstants.setBold(remote, true);
                        txtMessage = new JTextField();
                        txtMessage.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {                                
                                NetBoard netBoard = (NetBoard)board;
                                String msg = netBoard.getPlayerName(netBoard.getLocalPlayer()) + ": " + txtMessage.getText() + "\n";
                                // send the message to the Message Server                                    
                                netBoard.getMessageOut().println(msg);
                                // clear the message field
                                txtMessage.setText("");                                    
                            }
                        });
                        txtMessage.setBorder(BorderFactory.createEtchedBorder());
                        JScrollPane scrollPane = new JScrollPane(txtAll, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        Chess chess = (Chess)board.getTopLevelAncestor();
                        scrollPane.setPreferredSize(new Dimension(0, 50));                        
                        panelMessages = new JPanel(new GridLayout(1, 1));
                        Box boxMessages = new Box(BoxLayout.Y_AXIS);
                        boxMessages.add(scrollPane);
                        boxMessages.add(txtMessage);                    
                        panelMessages.add(boxMessages);                        
                    }                                                                        
                    boxSouth.add(panelMessages);
                }
                else {
                    boxSouth.remove(panelMessages);                                        
                }
                Chess chess = (Chess)board.getTopLevelAncestor();                
                chess.pack();
                chess.validate();                
            }
        }
    }  
    
    /** Implements the actions of the help menu and toolbar buttons. */
    class HelpAction extends AbstractAction {        
        HelpAction(String name, ImageIcon icon, KeyStroke keystroke, Integer i, String tooltip) {
            super(name);
            if (icon != null) {
                putValue(SMALL_ICON, icon);
            }
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }            
            if (i != null) {
                putValue(MNEMONIC_KEY, i);
            }
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }            
        }
        
        public void actionPerformed(ActionEvent e) {            
            String name = (String)this.getValue(AbstractAction.NAME);
            if (name.startsWith("About")) {
                // show the about dialog
                AboutDialog aboutDialog = new AboutDialog();
                aboutDialog.pack();
                aboutDialog.setLocationRelativeTo(board.getTopLevelAncestor());
                aboutDialog.setVisible(true);
            }
        }
    }
    
    /** Implements a waiting dialog box with a message and a cancel button */
    class WaitDialog extends JDialog implements ActionListener {        
        Thread t;                
        
        /** Creates an instance of WaitDialog
         *  @param d the owner dialog
         *  @param title the title of the dialog
         *  @param message the message to display on the dialog
         *  @param t the thread to execute while showing the dialog
         */
        WaitDialog(JDialog d, String title, String message, Thread t) {            
            super(d, title, true);            
            this.t = t;
            this.addWindowListener(new WaitWindowHandler());
            // force the user to dismiss the dialog with the cancel button
            this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            Container contentPane = getContentPane();
            contentPane.setLayout(new BorderLayout());
            
            // create the box that will contain the message
            Box boxMsg = new Box(BoxLayout.X_AXIS);
            JLabel lblMsg = new JLabel(message);
            JPanel panelMsg = new JPanel();
            panelMsg.add(lblMsg);
            boxMsg.add(Box.createHorizontalStrut(10));
            boxMsg.add(panelMsg);
            boxMsg.add(Box.createHorizontalStrut(10));
            
            // create the overall box
            Box boxDialog = new Box(BoxLayout.Y_AXIS);
            boxDialog.add(Box.createVerticalStrut(10));
            boxDialog.add(boxMsg);
            boxDialog.add(Box.createVerticalStrut(10));
            
            // create the cancel button and its container
            JButton btnCancel = new JButton("Cancel");
            btnCancel.setMnemonic(KeyEvent.VK_C);
            btnCancel.setActionCommand("Cancel");
            btnCancel.addActionListener(this);                        
            JPanel panelButton = new JPanel();
            panelButton.add(btnCancel);
            
            // add everything to the dialog
            contentPane.add(boxDialog, BorderLayout.CENTER);
            contentPane.add(panelButton, BorderLayout.SOUTH);
        }
        
        public void actionPerformed(ActionEvent e) {
            if ("Cancel".equals(e.getActionCommand())) {
                if (t instanceof HostThread) {
                    // cancel the accept on the server socket
                    try {
                        ((HostThread)t).getHostBoard().getServerSocket().close();
                    }
                    catch (NullPointerException npe) {
                        // in case the user quickly clicks cancel before the server socket
                        // has had a chance to call accept()
                    }
                    catch (IOException ioe) {
                        JOptionPane.showMessageDialog(((JComponent)e.getSource()).getTopLevelAncestor(), "Unable to cancel the host.  If you want to cancel the host, you must end the application.", "Unable to cancel", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
        
        class WaitWindowHandler extends WindowAdapter {
            public void windowOpened(WindowEvent e) {
                Chess.currentWaitDialog = (WaitDialog)e.getSource();
                t.start();
            }
        }        
    }
    
    /** Implements the dialog box for the user to select the settings for a new board */
    class NewDialog extends JDialog implements ActionListener {        
        JTextField txtName1, txtName2;
        JComboBox cboColor1, cboColor2;
        JCheckBox chkAllowUndo;
        // stores the default background color of a button
        Color colorButton;
        // stores the previously selected index for both color ComboBoxes
        int[] prevColorIndex = {0, 1};
        
        NewDialog() {
            super((Chess)board.getTopLevelAncestor(), "New Board", true);
            this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            Container contentPane = getContentPane();
            contentPane.setLayout(new BorderLayout());

            // create the objects used for the color selection combo boxes
            ColorRenderer cr = new ColorRenderer();
            Vector colors = new Vector();
            // add the two standard colors
            Color stdColor1 = Color.WHITE;
            Color stdColor2 = new Color(89, 132, 189);
            colors.add(stdColor1);
            colors.add(stdColor2);
            // add the current chess piece colors if needed
            if (!ChessPiece.colorPlayer1.equals(stdColor1) && !ChessPiece.colorPlayer1.equals(stdColor2)) {
                colors.add(ChessPiece.colorPlayer1);
            }
            if (!ChessPiece.colorPlayer2.equals(stdColor1) && !ChessPiece.colorPlayer2.equals(stdColor2) && !ChessPiece.colorPlayer2.equals(ChessPiece.colorPlayer1)) {
                colors.add(ChessPiece.colorPlayer2);                
            }
            DefaultComboBoxModel model1 = new DefaultComboBoxModel();
            DefaultComboBoxModel model2 = new DefaultComboBoxModel();
            for (int i = 0; i < colors.size(); i++) {            
                model1.addElement(colors.get(i));
                model2.addElement(colors.get(i));
            }
            JButton btnOther1 = new JButton("Other...");
            btnOther1.setPreferredSize(new Dimension(0, 20));
            model1.addElement(btnOther1);
            JButton btnOther2 = new JButton("Other...");
            btnOther2.setPreferredSize(new Dimension(0, 20));
            model2.addElement(btnOther2);
            // store the background color of the buttons
            colorButton = btnOther1.getBackground();

            // create the components for entering the settings                        
            JLabel lblName1 = new JLabel("Name:");
            JLabel lblName2 = new JLabel("Name:");
            txtName1 = new JTextField(board.getPlayerName(Board.PLAYER_1), 15);            
            txtName2 = new JTextField(board.getPlayerName(Board.PLAYER_2), 15);             
            FocusHandler fh = new FocusHandler();            
            txtName1.addFocusListener(fh);
            txtName2.addFocusListener(fh);
            
            JLabel lblColor1 = new JLabel("Color:");
            cboColor1 = new JComboBox();            
            cboColor1.setModel(model1);
            cboColor1.setRenderer(cr);
            cboColor1.setEditable(false); 
            cboColor1.setSelectedIndex(0);
            cboColor1.setActionCommand("Color1");
            cboColor1.addActionListener(this);
            cboColor1.setSelectedItem(ChessPiece.colorPlayer1);
            JLabel lblColor2 = new JLabel("Color:");
            cboColor2 = new JComboBox(); 
            cboColor2.setModel(model2);
            cboColor2.setRenderer(cr);
            cboColor2.setEditable(false);
            cboColor2.setSelectedIndex(1);
            cboColor2.setActionCommand("Color2");
            cboColor2.addActionListener(this);
            cboColor2.setSelectedItem(ChessPiece.colorPlayer2);
            
            chkAllowUndo = new JCheckBox("Allow Undo", true);
            
            // create the components containers            
            JPanel panelName1 = new JPanel(new GridLayout(2, 1));            
            JPanel panelName2 = new JPanel(new GridLayout(2, 1));            
            JPanel panelColor1 = new JPanel(new GridLayout(2, 1));                        
            JPanel panelColor2 = new JPanel(new GridLayout(2, 1));            
            JPanel panelAllowUndo = new JPanel(new GridLayout(1, 1));            
            
            // add the components to their containers
            panelName1.add(lblName1);
            panelName1.add(txtName1);                                    
            panelColor1.add(lblColor1);
            panelColor1.add(cboColor1);            
            panelName2.add(lblName2);
            panelName2.add(txtName2);              
            panelColor2.add(lblColor2);
            panelColor2.add(cboColor2);
            panelAllowUndo.add(chkAllowUndo);            
                        
            // create the boxes to contain the player settings
            Box boxPlayer1 = new Box(BoxLayout.Y_AXIS);            
            boxPlayer1.add(panelName1);            
            boxPlayer1.add(Box.createVerticalStrut(10));
            boxPlayer1.add(panelColor1);            
            boxPlayer1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Player 1 Settings"), BorderFactory.createEmptyBorder(5,5,5,5)));
            
            Box boxPlayer2 = new Box(BoxLayout.Y_AXIS);            
            boxPlayer2.add(panelName2);
            boxPlayer2.add(Box.createVerticalStrut(10));
            boxPlayer2.add(panelColor2);
            boxPlayer2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Player 2 Settings"), BorderFactory.createEmptyBorder(5,5,5,5)));
            
            // create the box to contain both player settings
            Box boxSettings = new Box(BoxLayout.X_AXIS);
            boxSettings.add(boxPlayer1);
            boxSettings.add(Box.createHorizontalStrut(15));
            boxSettings.add(boxPlayer2);
            
            // create the overall box
            Box boxDialog = new Box(BoxLayout.Y_AXIS);
            boxDialog.add(Box.createVerticalStrut(10));
            boxDialog.add(boxSettings);
            boxDialog.add(Box.createVerticalStrut(10));
            boxDialog.add(panelAllowUndo);
            
            // create the button panel
            JPanel panelButtons = new JPanel();
            JButton btnOK = new JButton("OK");
            getRootPane().setDefaultButton(btnOK);
            btnOK.setMnemonic(KeyEvent.VK_O);
            btnOK.setActionCommand("OK");
            btnOK.addActionListener(this);            
            JButton btnCancel = new JButton("Cancel");
            btnCancel.setMnemonic(KeyEvent.VK_C);
            btnCancel.setActionCommand("Cancel");
            btnCancel.addActionListener(this);
            panelButtons.add(btnOK);            
            panelButtons.add(btnCancel);
            
            // add everything to the dialog window
            contentPane.add(boxDialog, BorderLayout.CENTER);            
            contentPane.add(panelButtons, BorderLayout.SOUTH);
        }
        
        /** This class is used to render the objects in the color combo boxes */
        private class ColorRenderer extends JPanel implements ListCellRenderer {
            JPanel panel = new JPanel();

            public ColorRenderer() {
                super();
                setLayout(new BorderLayout());
                add(panel, BorderLayout.CENTER);
                panel.setOpaque(true);
                setPreferredSize(new Dimension(0, 20));                
            }

            /** This method is the only method in the ListCellRenderer interface */
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {                
                // if the item is a color, set the background of the panel to the color
                if (value instanceof Color) {
                    panel.setOpaque(true);
                    panel.setBackground((Color)value); 
                    return this;
                }
                else {                    
                    // if the item is not a color, it is the "other" button                    
                    panel.setOpaque(false);
                    JButton btn = (JButton)value;
                    // reset its background because it gets changed somehow
                    btn.setBackground(colorButton);
                    return btn;
                }                
            }
        }
                
        /** Displays the color chooser for the user to select a color
         *  @param cboColor the JComboBox that the user selected an item from
         *  @param player the player whose settings are being changed
         */
        private void processItem(JComboBox cboColor, int player) {                
            if (cboColor.getSelectedItem() instanceof JButton) {
                Color color = JColorChooser.showDialog(this, "Select Color", (Color)cboColor.getItemAt(prevColorIndex[player - 1]));
                if (color != null) {
                    // check if color is already in list
                    int index = ((DefaultComboBoxModel)cboColor.getModel()).getIndexOf(color);
                    if (index == -1) {
                        cboColor.insertItemAt(color, cboColor.getItemCount() - 1);                    
                        cboColor.setSelectedIndex(cboColor.getItemCount() - 2);   
                    }
                    else {
                        cboColor.setSelectedIndex(index);
                    }
                }
                else {
                    cboColor.setSelectedIndex(prevColorIndex[player - 1]);
                }                    
            }
            prevColorIndex[player - 1] = cboColor.getSelectedIndex();
        }
        
        public void actionPerformed(ActionEvent e) {            
            if ("Color1".equals(e.getActionCommand())) {                 
                // handle the selection from the ComboBox
                this.processItem((JComboBox)e.getSource(), 1);
            }
            else if ("Color2".equals(e.getActionCommand())) {
                // handle the selection from the ComboBox
                this.processItem((JComboBox)e.getSource(), 2);                
            }
            else if ("OK".equals(e.getActionCommand())) {                                
                // validate the data
                if (txtName1.getText().equals("")) {
                    JOptionPane.showMessageDialog(((JComponent)e.getSource()).getTopLevelAncestor(), "The player's name cannot be blank.", "Invalid name", JOptionPane.INFORMATION_MESSAGE);
                    txtName1.requestFocusInWindow();
                    return;
                }
                if (txtName2.getText().equals("")) {
                    JOptionPane.showMessageDialog(((JComponent)e.getSource()).getTopLevelAncestor(), "The player's name cannot be blank.", "Invalid name", JOptionPane.INFORMATION_MESSAGE);
                    txtName2.requestFocusInWindow();
                    return;
                }
                // set the colors
                ChessPiece.colorPlayer1 = (Color)cboColor1.getSelectedItem();
                ChessPiece.colorPlayer2 = (Color)cboColor2.getSelectedItem();
                // remove board and add the new one
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));                                                 
                refreshBoard(new Board(txtName1.getText(), txtName2.getText()));
                this.setCursor(Cursor.getDefaultCursor());
                // set the allow undo flag accordingly
                board.setAllowUndo(chkAllowUndo.isSelected());
                logger.info("New board created.");                
                this.dispose();
            }
            else if ("Cancel".equals(e.getActionCommand())) {
                this.dispose();
            }
        }
    }
    
    /** Implements the dialog box for the user to select the settings to connect
     *  to a remote board
     */
    class ConnectDialog extends JDialog implements ActionListener {        
        JTextField txtName, txtRemoteIPAddress, txtRemotePort;        
        
        ConnectDialog() {
            super((Chess)board.getTopLevelAncestor(), "Connect To Remote Board", true);            
            this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            Container contentPane = getContentPane();
            contentPane.setLayout(new BorderLayout());            
            
            // create the focus handler for the text boxes
            FocusHandler fh = new FocusHandler();
            
            // retrieve the properties to get the default values
            Chess chess = (Chess)board.getTopLevelAncestor();            
            String remoteIP = chess.props.getProperty("RemoteIP");
            String remotePort = chess.props.getProperty("RemotePort");
            
            // create the components for entering the settings
            JLabel lblName = new JLabel("Your name:");            
            txtName = new JTextField(board.getPlayerName(Board.PLAYER_2), 15);                        
            txtName.addFocusListener(fh);            
            JLabel lblRemoteIPAddress = new JLabel("Remote IP Address:");            
            txtRemoteIPAddress = new JTextField(remoteIP, 15);
            txtRemoteIPAddress.addFocusListener(fh);
            JLabel lblRemotePort = new JLabel("Remote Port:");
            txtRemotePort = new JTextField(remotePort, 5);
            txtRemotePort.addFocusListener(fh);
            
            JPanel panelName = new JPanel(new GridLayout(2, 1));            
            panelName.add(lblName);                        
            panelName.add(txtName);
            JPanel panelRemoteIP = new JPanel(new GridLayout(2, 1));
            panelRemoteIP.add(lblRemoteIPAddress);
            panelRemoteIP.add(txtRemoteIPAddress);
            JPanel panelRemotePort = new JPanel(new GridLayout(2, 1));
            panelRemotePort.add(lblRemotePort);
            panelRemotePort.add(txtRemotePort);
            
            // create the box that will contain the settings
            Box boxSettings = new Box(BoxLayout.Y_AXIS);
            boxSettings.add(panelName);
            boxSettings.add(Box.createVerticalStrut(15));
            boxSettings.add(panelRemoteIP);
            boxSettings.add(Box.createVerticalStrut(15));
            boxSettings.add(panelRemotePort);
            boxSettings.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Settings"), BorderFactory.createEmptyBorder(5,5,5,5)));
                                    
            // create the overall box
            Box boxDialog = new Box(BoxLayout.Y_AXIS);
            boxDialog.add(Box.createVerticalStrut(10));
            boxDialog.add(boxSettings);
            boxDialog.add(Box.createVerticalStrut(10));
            
            // create the button panel
            JPanel panelButtons = new JPanel();
            JButton btnOK = new JButton("OK");
            getRootPane().setDefaultButton(btnOK);
            btnOK.setMnemonic(KeyEvent.VK_O);
            btnOK.setActionCommand("OK");
            btnOK.addActionListener(this);            
            JButton btnCancel = new JButton("Cancel");
            btnCancel.setMnemonic(KeyEvent.VK_C);
            btnCancel.setActionCommand("Cancel");
            btnCancel.addActionListener(this);
            panelButtons.add(btnOK);            
            panelButtons.add(btnCancel);
            
            // add everything to the dialog window
            contentPane.add(boxDialog, BorderLayout.CENTER);            
            contentPane.add(panelButtons, BorderLayout.SOUTH);            
        }
        
        public void actionPerformed(ActionEvent e) {
            if ("OK".equals(e.getActionCommand())) {                
                // validate the data
                if (txtName.getText().equals("")) {
                    JOptionPane.showMessageDialog(((JComponent)e.getSource()).getTopLevelAncestor(), "Your name cannot be blank.", "Invalid name", JOptionPane.ERROR_MESSAGE);
                    txtName.requestFocusInWindow();
                    return;
                }
                String remoteIPAddress = txtRemoteIPAddress.getText();
                if (remoteIPAddress.equals("")) {
                    JOptionPane.showMessageDialog(((JComponent)e.getSource()).getTopLevelAncestor(), "The IP Address of the remote computer cannot be blank.", "Invalid IP Address", JOptionPane.ERROR_MESSAGE);
                    txtRemoteIPAddress.requestFocusInWindow();
                    return;
                }                
                if (!Chess.isValidPort(txtRemotePort)) {
                    return;
                }                
                
                // try to connect
                URL remoteURL;
                try {
                    remoteURL = new URL("http", txtRemoteIPAddress.getText(), Integer.parseInt(txtRemotePort.getText()), "/Chess");
                }
                catch (MalformedURLException mue) {            
                    JOptionPane.showMessageDialog(((JComponent)e.getSource()).getTopLevelAncestor(), "Could not create URL with the given IP address and port number.", "Invalid URL", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                RemoteBoard remoteBoard;
                try {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    remoteBoard = new RemoteBoard(txtName.getText(), remoteURL);
                    this.setCursor(Cursor.getDefaultCursor());
                }
                catch (IOException ioe) {                    
                    JOptionPane.showMessageDialog(((JComponent)e.getSource()).getTopLevelAncestor(), ioe.getMessage(), "Unable to connect to remote board", JOptionPane.ERROR_MESSAGE);
                    this.setCursor(Cursor.getDefaultCursor());
                    return;
                }
                
                // also connect to the Message Server
                try {
                    Socket messageSocket = new Socket(InetAddress.getByName(remoteBoard.getRemoteURL().getHost()), MessageServer.DEFAULT_MESSAGE_PORT);
                    remoteBoard.setMessageSocket(messageSocket);
                    PrintWriter messageOut = new PrintWriter(messageSocket.getOutputStream(), true);
                    remoteBoard.setMessageOut(messageOut);    
                    BufferedReader messageIn = new BufferedReader(new InputStreamReader(messageSocket.getInputStream()));
                    remoteBoard.setMessageIn(messageIn);
                    remoteBoard.listenForMessage();
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(((JComponent)e.getSource()).getTopLevelAncestor(), ex, "Unable to connect to message server", JOptionPane.ERROR_MESSAGE);
                    logger.warning("Error connecting to the Message Server: " + ex);
                }
                
                // remove board and add the new one
                refreshBoard(remoteBoard);
                // set the allow undo flag accordingly
                board.setAllowUndo(false);
                logger.info("Connected to remote board at " + remoteURL);
                this.dispose();
            }
            else if ("Cancel".equals(e.getActionCommand())) {
                this.dispose();
            }
        }        
    }
    
    /** Implements the dialog box for the user to select the settings to host
     *  a board for a remote player to connect to
     */
    class HostDialog extends JDialog implements ActionListener {        
        JTextField txtName, txtHostPort;        
        JCheckBox chkLayout;
        
        HostDialog() {
            super((Chess)board.getTopLevelAncestor(), "Host Board", true);
            this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);            
            Container contentPane = getContentPane();
            contentPane.setLayout(new BorderLayout());            
            
            // create the focus handler for the text boxes
            FocusHandler fh = new FocusHandler();
            
            // retrieve the properties to get the default values
            Chess chess = (Chess)board.getTopLevelAncestor();
            String hostPort = chess.props.getProperty("HostPort");
            
            // create the components for entering the settings
            JLabel lblName = new JLabel("Your name:");            
            txtName = new JTextField(board.getPlayerName(Board.PLAYER_1), 15);                        
            txtName.addFocusListener(fh);            
            JLabel lblHostPort = new JLabel("Host Port:");
            txtHostPort = new JTextField(hostPort, 5);
            txtHostPort.addFocusListener(fh);            
            chkLayout = new JCheckBox("Use current board");
            JPanel panelName = new JPanel(new GridLayout(2, 1));            
            panelName.add(lblName);                        
            panelName.add(txtName);       
            JPanel panelHostPort = new JPanel(new GridLayout(2, 1));
            panelHostPort.add(lblHostPort);
            panelHostPort.add(txtHostPort);            
            Box boxLayout = new Box(BoxLayout.X_AXIS);
            boxLayout.add(chkLayout);            
            
            // create the box that will contain the settings
            Box boxSettings = new Box(BoxLayout.Y_AXIS);
            boxSettings.add(panelName);
            boxSettings.add(Box.createVerticalStrut(15));
            boxSettings.add(panelHostPort);
            boxSettings.add(Box.createVerticalStrut(15));
            boxSettings.add(boxLayout);
            boxSettings.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Settings"), BorderFactory.createEmptyBorder(5,5,5,5)));
                        
            // create the overall box
            Box boxDialog = new Box(BoxLayout.Y_AXIS);
            boxDialog.add(Box.createVerticalStrut(10));
            boxDialog.add(boxSettings);
            boxDialog.add(Box.createVerticalStrut(10));
            
            // create the button panel
            JPanel panelButtons = new JPanel();
            JButton btnOK = new JButton("OK");
            getRootPane().setDefaultButton(btnOK);
            btnOK.setMnemonic(KeyEvent.VK_O);
            btnOK.setActionCommand("OK");
            btnOK.addActionListener(this);            
            JButton btnCancel = new JButton("Cancel");
            btnCancel.setMnemonic(KeyEvent.VK_C);
            btnCancel.setActionCommand("Cancel");
            btnCancel.addActionListener(this);
            panelButtons.add(btnOK);            
            panelButtons.add(btnCancel);
            
            // add everything to the dialog window
            contentPane.add(boxDialog, BorderLayout.CENTER);            
            contentPane.add(panelButtons, BorderLayout.SOUTH);            
        }
        
        public void actionPerformed(ActionEvent e) {
            if ("OK".equals(e.getActionCommand())) {                                
                // validate the data
                if (txtName.getText().equals("")) {
                    JOptionPane.showMessageDialog(((JComponent)e.getSource()).getTopLevelAncestor(), "Your name cannot be blank.", "Invalid name", JOptionPane.INFORMATION_MESSAGE);
                    txtName.requestFocusInWindow();
                    return;
                }
                if (!Chess.isValidPort(txtHostPort)) {
                    return;
                }
                
                // create the Message Server
                try {
                    ms = new MessageServer(MessageServer.DEFAULT_MESSAGE_PORT);
                    Runnable r = new Runnable() {
                        public void run() {
                            try {
                                ms.getClients();
                            }
                            catch (Exception ioe) {
                                Chess.logger.warning("Error with message server: " + ioe);
                                ioe.printStackTrace();
                            }
                        }
                    };
                    Thread t = new Thread(r, "MessageServer");
                    t.start();
                }
                catch (IOException ioe) {
                    Chess.logger.warning("Error with message server: " + ioe);
                    ioe.printStackTrace();
                }
                 
                // try to host
                URL hostURL;
                try {
                    hostURL = new URL("http", InetAddress.getLocalHost().getHostAddress(), Integer.parseInt(txtHostPort.getText()), "/Chess");
                }
                catch (UnknownHostException uhe) {
                    JOptionPane.showMessageDialog(((JComponent)e.getSource()).getTopLevelAncestor(), "Could not detect localhost.", "Invalid localhost", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                catch (MalformedURLException mue) {            
                    JOptionPane.showMessageDialog(((JComponent)e.getSource()).getTopLevelAncestor(), "Could not create host URL.", "Invalid URL", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                HostThread hostThread;
                if (chkLayout.isSelected()) {
                    hostThread = new HostThread(txtName.getText(), hostURL, board);
                }
                else {
                    hostThread = new HostThread(txtName.getText(), hostURL, null);
                }
                WaitDialog waitDialog = new WaitDialog((JDialog)((JComponent)e.getSource()).getTopLevelAncestor(), "Wait For Connection", "Waiting for a player to join...", hostThread);
                waitDialog.pack();
                waitDialog.setLocationRelativeTo(board);                
                waitDialog.setVisible(true);                
                
                if (!hostThread.wasHostSuccessful()) {
                    JOptionPane.showMessageDialog(((JComponent)e.getSource()).getTopLevelAncestor(), hostThread.getHostMessage() , "Unable to host board", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // remove board and add the new one
                refreshBoard(hostThread.getHostBoard());
                // set the allow undo flag accordingly
                board.setAllowUndo(false);
                logger.info("New board hosted at " + hostURL);
                this.dispose();
            }
            else if ("Cancel".equals(e.getActionCommand())) {
                this.dispose();
            }
        }        
    }
    
    /** Implements the dialog box to display information about the program */
    class AboutDialog extends JDialog implements ActionListener {        
        AboutDialog() {
            super((Chess)board.getTopLevelAncestor(), "About", true);
            this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            Container contentPane = getContentPane();
            contentPane.setLayout(new BorderLayout());

            // create the objects to display the information
            JLabel lblTitle = new JLabel(APP_TITLE);
            lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
            JLabel lblVersion = new JLabel("Version: " + APP_VER);
            lblVersion.setFont(new Font("SansSerif", Font.PLAIN, 12));
            JLabel lblDescription = new JLabel(APP_DESC);
            lblDescription.setFont(new Font("SansSerif", Font.PLAIN, 12));
            JLabel lblDisclaimer = new JLabel(APP_DISC);
            lblDisclaimer.setFont(new Font("SansSerif", Font.PLAIN, 10));
            
            Box boxInfo = new Box(BoxLayout.Y_AXIS);
            boxInfo.add(Box.createVerticalStrut(10));
            boxInfo.add(lblTitle);            
            boxInfo.add(lblVersion);
            boxInfo.add(Box.createVerticalStrut(15));
            boxInfo.add(lblDescription);
            boxInfo.add(Box.createVerticalStrut(15));
            
            Box boxTop = new Box(BoxLayout.X_AXIS);
            boxTop.add(Box.createHorizontalStrut(10));
            boxTop.add(new JLabel(createImageIcon("images/about.gif", APP_TITLE)));
            boxTop.add(Box.createHorizontalStrut(15));
            boxTop.add(boxInfo);
            
            // create the button panel
            JPanel panelButtons = new JPanel();
            JButton btnOK = new JButton("OK");
            getRootPane().setDefaultButton(btnOK);
            btnOK.setMnemonic(KeyEvent.VK_O);
            btnOK.setActionCommand("OK");
            btnOK.addActionListener(this);            
            panelButtons.add(btnOK);                                    
            
            // create the bottom panel
            JPanel panelBottom = new JPanel(new BorderLayout());                    
            panelBottom.add(new JSeparator(), BorderLayout.NORTH);
            JPanel panelBottom2 = new JPanel();            
            panelBottom2.add(lblDisclaimer);
            panelBottom2.add(panelButtons);
            panelBottom.add(panelBottom2, BorderLayout.CENTER);
            
            // add everything to the dialog window
            contentPane.add(boxTop, BorderLayout.CENTER);            
            contentPane.add(panelBottom, BorderLayout.SOUTH);
        }
        
        public void actionPerformed(ActionEvent e) {            
            if ("OK".equals(e.getActionCommand())) {                                
                // close the dialog
                this.dispose();
            }            
        }
    }
    
    /** Handles window events */
    class WindowHandler extends WindowAdapter {
        public void windowClosing(WindowEvent e) {            
            try {
                // save the properties           
                String name = board.getPlayerName(Board.PLAYER_1);
                props.setProperty("NamePlayer1", name != null ? name : "");
                name = board.getPlayerName(Board.PLAYER_2);
                props.setProperty("NamePlayer2", name != null ? name : "");                
                props.setProperty("ColorPlayer1", String.valueOf(ChessPiece.colorPlayer1.getRGB()));
                props.setProperty("ColorPlayer2", String.valueOf(ChessPiece.colorPlayer2.getRGB()));
                if (board instanceof RemoteBoard) {
                    RemoteBoard rb = (RemoteBoard)board;
                    // close the connection if needed
                    rb.closeConnection(false);                    
                    rb.closeMessageConnection();
                    props.setProperty("RemoteIP", rb.getRemoteURL().getHost());
                    props.setProperty("RemotePort", String.valueOf(rb.getRemoteURL().getPort()));
                }
                else if (board instanceof HostBoard) {
                    HostBoard hb = (HostBoard)board;
                    // close the connection if needed
                    hb.closeConnection(false);
                    hb.closeMessageConnection();
                    // close the message server if needed
                    ms.closeConnection();
                    props.setProperty("HostPort", String.valueOf(hb.getHostURL().getPort()));
                }
                props.store(new FileOutputStream(Chess.propsFile), "Chess Properties");
            }            
            catch (IOException ioe) {
                logger.warning(ioe.toString() + ": " + "Couldn't write properties file: " + Chess.propsFile);
            }
            catch (Exception ex) {
                logger.warning("Error closing application: " + ex);
            }
            // dispose of the chess application            
            ((Chess)e.getSource()).dispose();                
            System.exit(0);
        }
    }
    
    /** Handles focus events */
    class FocusHandler extends FocusAdapter {
        public void focusGained(FocusEvent e) {
            // select all the text in the JTextField when it receives the focus
            ((JTextField)e.getSource()).selectAll();
        }
    }
    
    /** Handles menu events */
    class MenuHandler implements MenuListener {
        public void menuSelected(MenuEvent e) {                        
            // check to see if castle moves are valid
            // first, retrieve the king for the player whose move it is
            Square sqKing = null;
            Color matchColor;            
            if (board.getWhoseMove() == Board.PLAYER_1) {
                matchColor = ChessPiece.colorPlayer1;                        
            }
            else {
                matchColor = ChessPiece.colorPlayer2;                        
            }
            // ********** inefficient code ***************
            outer1:
            for (int i = 0; i <= 7; i++) {
                for (int j = 0; j <= 7; j++) {
                    sqKing = board.getSquare(i, j);                    
                    if (sqKing.getPiece().getType() == ChessPiece.KING && sqKing.getPiece().getColor().equals(matchColor)) {
                        break outer1;
                    }
                }
            }                    

            if (sqKing != null) {                                                        
                King king = (King)sqKing.getPiece();
                // check castle kingside
                if (king.isValidCastle(King.CASTLE_KINGSIDE)) {
                    castleKingsideAction.setEnabled(true);
                }
                else {
                    castleKingsideAction.setEnabled(false);
                }
                // check castle queenside
                if (king.isValidCastle(King.CASTLE_QUEENSIDE)) {
                    castleQueensideAction.setEnabled(true);
                }
                else {
                    castleQueensideAction.setEnabled(false);
                }                    
            }                        
        }

        public void menuCanceled(MenuEvent e) {
        }
        public void menuDeselected(MenuEvent e) {
        }
    }
    
    /** Checks to see whether or not the given port is a valid port number
     *  @param txtPort the text box that contains the port number to check
     *  @return true if the port is valid, false if it is not
     */
    private static boolean isValidPort(JTextField txtPort) {
        try {
            int port = Integer.parseInt(txtPort.getText());
            if (port < 0 || port > 99999) {
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(txtPort.getTopLevelAncestor(), "The port must be an integer between 0 and 99999.", "Invalid Port", JOptionPane.INFORMATION_MESSAGE);
            txtPort.requestFocusInWindow();
            return false;
        }
        return true;
    }
    
    /** Adjusts the filler components based on the position of the board
     *  @param position the current position of the board
     */
    public void setFiller(int position) {                
        // remove current fillers                
        panelBoard.remove(filler1);
        panelBoard.remove(filler2);        
        switch (position) {
            case Board.POSITION_1:                
            case Board.POSITION_3:
                // add filler to the east and west                                
                panelBoard.add(filler1, BorderLayout.WEST);
                panelBoard.add(filler2, BorderLayout.EAST);
                break;
            case Board.POSITION_2:
            case Board.POSITION_4:
                // add filler to the north and south
                panelBoard.add(filler1, BorderLayout.NORTH);
                panelBoard.add(filler2, BorderLayout.SOUTH);                
                break;            
        }
        panelBoard.validate();
    }
}
