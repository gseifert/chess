/*
 * Board.java
 *
 * Created on February 24, 2004, 9:45 AM
 */

package com.seifert.chess;

/**
 *
 * @author  ZZ3JPZ
 */
import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
public class Board extends JPanel {
    
    protected static final int GAME_OVER = 3;
    protected static final int PLAYER_1 = 1;
    protected static final int PLAYER_2 = 2;    
    protected static final int POSITION_1 = 1;  // player 1 at the bottom
    protected static final int POSITION_2 = 2;  // player 1 at the left
    protected static final int POSITION_3 = 3;  // player 1 at the top
    protected static final int POSITION_4 = 4;  // player 1 at the right
    protected static final int NO_CHECK = 0;
    protected static final int CHECK = 1;
    protected static final int CHECK_MATE = 2;
        
    private String[] playerName = new String[2];    
    private int whoseMove = PLAYER_1;    // flag to indicate whose move it is    
    private Square[][] squares;
    private JPanel panelBoard;    
    private JPanel[] panelPlayer = new JPanel[2];
    private GridLayout gridLayoutHorizontal = new GridLayout(1, 16);
    private GridLayout gridLayoutVertical = new GridLayout(16, 1);    
    private int[] capturedCount = {0, 0};
    private int position = POSITION_1;        
    private boolean allowUndo = true;
    private boolean autoRotate = false;
    private Vector moves = new Vector();    
    private int redoIndex = 0;    
        
    /** Creates a new instance of Board.
     *  @param name1 the name of player 1
     *  @param name2 the name of player 2
     */
    public Board(String name1, String name2) {
        super(new BorderLayout());
        // create the board
        panelBoard = new JPanel(new GridLayout(8, 8));        
        panelBoard.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
        squares = new Square[8][8];        
        for (int i = 0; i <= 7; i++) {
            for (int j = 0; j <= 7; j++) {
                squares[i][j] = new Square(i, j);
                panelBoard.add(squares[i][j]);
            }
        }        
        // create the panels that will hold the captured pieces and load them with blanks        
        panelPlayer[0] = new JPanel(gridLayoutHorizontal);
        panelPlayer[1] = new JPanel(gridLayoutHorizontal);         
        for (int i = 0; i <= 1; i++) {
            for (int j = 0; j <= 15; j++) {
                panelPlayer[i].add(new JLabel(Chess.createImageIcon("images/blank.gif", "")));
            }
        }        
        // add all the panels to the container
        this.add(panelPlayer[0], BorderLayout.SOUTH);                
        this.add(panelPlayer[1], BorderLayout.NORTH);
        this.add(panelBoard, BorderLayout.CENTER);                
        
        // set the players' names
        playerName[0] = name1;
        playerName[1] = name2;        
    }
    
    /** Gets the player's name */
    public String getPlayerName(int player) {
        return playerName[player - 1];
    }
 
    /** Sets the player's name */
    public void setPlayerName(int player, String name) {
        playerName[player - 1] = name;
    }
    
    /** Gets the player whose turn it is. */
    public int getWhoseMove() {
        return whoseMove;
    }
    
    public void setWhoseMove(int player) {
        whoseMove = player;
    }
    
    /** Switches whose turn it is */
    public void switchWhoseMove() {
        if (whoseMove == Board.PLAYER_1) {
            whoseMove = Board.PLAYER_2;
        }
        else if (whoseMove == Board.GAME_OVER + Board.PLAYER_2) {
            whoseMove = Board.PLAYER_2;
        }
        else {
            whoseMove = Board.PLAYER_1;
        }        
        Chess chess = (Chess)this.getTopLevelAncestor();
        if (chess != null) {
            chess.updateStatusBar();        
        }
    }
    
    /** Returns the square at the given row and column of the board */
    public Square getSquare(int row, int col) {
        return squares[row][col];
    }
    
    /** Returns the current position of the board */
    public int getPosition() {
        return position;
    }
    
    /** Moves the piece on the given start square to the given end square 
     *  @param startSquare the square that holds the piece being moved
     *  @param endSquare the square that the piece is being moved to
     *  @param isRedo true if this move is a redo
     */
    public void movePiece(Square startSquare, Square endSquare, boolean isRedo) {
        // get the piece that is being moved
        ChessPiece movingPiece = startSquare.getPiece();
        // get the piece on the square that the piece is trying to move to
        // or the target pawn if en passant
    	ChessPiece targetPiece = endSquare.getPiece();
    	Square sqEnPassant = null;
		if (targetPiece.getType() == ChessPiece.EMPTY && movingPiece.getType() == ChessPiece.PAWN) {
	    	// get the row and column of the square that the piece wants to move to
	        int newRow = endSquare.getRow();
	        int newCol = endSquare.getCol();
	        // get the current row and column of the square that the piece is on
	        int currentRow = startSquare.getRow();
	        int currentCol = startSquare.getCol();        
	        // get the instance of the board
	        Board board = (Board)startSquare.getParent().getParent();                
	        
	        // get the direction of the move
	        int iDirectionRow, iDirectionCol;
	        if (newRow > currentRow) {
	            iDirectionRow = 1;
	        }
	        else if (newRow < currentRow) {
	            iDirectionRow = -1;            
	        }        
	        else {
	            iDirectionRow = 0;
	        }        
	        if (newCol > currentCol) {
	            iDirectionCol = 1;
	        }
	        else if (newCol < currentCol) {
	            iDirectionCol = -1;            
	        }
	        else {
	            iDirectionCol = 0;
	        }
	        
	        if (((Pawn) movingPiece).isEnPassant(board, newRow, newCol, iDirectionRow, iDirectionCol, isRedo)) {
		        int sqRow = newRow;
				int sqCol = newCol;
				switch (board.getPosition()) {
				case Board.POSITION_1:
				case Board.POSITION_3:
					sqRow = newRow - (1 * iDirectionRow);
					break;
		        case Board.POSITION_2:
		        case Board.POSITION_4:
		        	sqCol = newCol - (1 * iDirectionCol);
		        	break;                
				}
				sqEnPassant = board.getSquare(sqRow, sqCol);
				targetPiece = sqEnPassant.getPiece();
				sqEnPassant.setPiece(new ChessPiece(null, ChessPiece.EMPTY));
	        }
        }
        // store the move if needed
    	Move move = null;
        if (!isRedo) {
        	move = new Move(startSquare, endSquare, targetPiece);
        	move.setEnPassant(sqEnPassant);
            addMove(move);
        }
        // log the move            
        Chess.logger.info((isRedo ? "REDO: " : "") + playerName[whoseMove - 1] + " moved " + movingPiece.getTypeDesc() + " from " + startSquare.getRow() + "," + startSquare.getCol() + " to " + endSquare.getRow() + "," + endSquare.getCol());        
        // set the moving piece on the ending square
        endSquare.setPiece(movingPiece);
        // set a blank piece on the starting square
        startSquare.setPiece(new ChessPiece(null, ChessPiece.EMPTY));
        // increment the number of moves the moving piece has made
        movingPiece.incrementNumMoves();
        
        // update whose move it is                  
        int player = whoseMove;
        switchWhoseMove();
        
        if (targetPiece.getType() == ChessPiece.EMPTY) {
            Chess.playSound("sounds/move.wav");            
        }
        else {                                                
            Chess.playSound("sounds/capture.wav");
            // move piece off board           
            addCapturedPiece(player, targetPiece);
            // log the capture
            Chess.logger.info(targetPiece.getTypeDesc() + " captured.");
        }        
        
        // check for pawn promotion
        if (movingPiece instanceof Pawn && ((Pawn) movingPiece).canPromote(endSquare, this.getPosition())) {
        	if (move != null) {
        		move.setPromotion(true);
        	}
        	PawnPromotionDialog pawnPromotionDialog = new PawnPromotionDialog(this, endSquare, (Pawn) movingPiece);
        	pawnPromotionDialog.pack();
        	pawnPromotionDialog.setLocationRelativeTo(this);
        	pawnPromotionDialog.setVisible(true);
        }
        
        // check to see if player is in check or check mate
        int chk = this.check(whoseMove, true);
        if (chk == Board.CHECK) {
        	Chess.playSound("sounds/check.wav");
            // alert the user
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "Check.", "Check", JOptionPane.INFORMATION_MESSAGE);
            // log the check
            Chess.logger.info(playerName[whoseMove - 1] + " is in check.");
        }
        else if (chk == Board.CHECK_MATE) {
        	Chess.playSound("sounds/check_mate.wav");
            // indicate that the game is over                    
            String msg = "Check Mate.  " + playerName[player - 1] + " wins.";
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), msg, "Check Mate", JOptionPane.INFORMATION_MESSAGE);
            whoseMove = GAME_OVER + player;
            // log the check mate
            Chess.logger.info(msg);
        }
        
        if (this.isAutoRotate() && !(this instanceof NetBoard)) {
    		this.rotate();
			Chess chess = (Chess) this.getTopLevelAncestor();
            chess.setFiller(this.getPosition());
            this.rotate();
        	chess.setFiller(this.getPosition());
        }
        
        // refresh the screen
        this.validate();
        this.repaint();
    }

	/** Returns whether or not this board allows undos */
    public boolean getAllowUndo() {
        return allowUndo;
    }
    
    /** Sets whether or not this board allows undos */
    public void setAllowUndo(boolean allowUndo) {
        this.allowUndo = allowUndo;
    }
    
    public boolean isAutoRotate() {
		return autoRotate;
	}

	public void setAutoRotate(boolean autoRotate) {
		this.autoRotate = autoRotate;
	}

	/** Gets the last move */
    public Object getLastMove() {
        return moves.lastElement();
    }
    
    /** Returns whether or not an undo can be currently made */
    public boolean canUndo() {
        return moves.size() == 0 ? false : true;
    }
    
    /** Returns whether or not a redo can be currently made */
    public boolean canRedo() {
        return redoIndex == moves.size() ? false : true;
    }
    
    /** Adds the given move to the collection */
    public void addMove(Object move) {
        moves.add(redoIndex, move);
        // remove all moves after redoIndex
        int i = redoIndex + 1;
        while (i <= moves.size() - 1) {
            moves.remove(i);
        }           
        // update the index
        redoIndex = moves.size();
        // enable the undo action and disable the redo action
        Chess chess = (Chess)this.getTopLevelAncestor();
        chess.enableUndo(true);
        chess.enableRedo(false);
    }
    
    /** Undo the last move */
    public void undoMove() {        
        Object move = moves.get(redoIndex - 1);
        if (move instanceof CastleMove) {
            CastleMove cm = (CastleMove)move;
            Square endKingSq = cm.getEndKingSquare();
            ChessPiece king = endKingSq.getPiece();
            king.decrementNumMoves();
            cm.getStartKingSquare().setPiece(king);        
            endKingSq.setPiece(new ChessPiece(null, ChessPiece.EMPTY));
            Square startRookSq = cm.getEndRookSquare();
            ChessPiece rook = startRookSq.getPiece();
            rook.decrementNumMoves();
            cm.getStartRookSquare().setPiece(rook);
            startRookSq.setPiece(new ChessPiece(null, ChessPiece.EMPTY));            
            
            // update whose move it is                  
            switchWhoseMove();
            
            // play the appropriate sound            
            Chess.playSound("sounds/undo_castle.wav");
        }
        else {
            Move m = (Move)move;
            Square endSq = m.getEndSquare();
            ChessPiece movedPiece = endSq.getPiece();
            if (m.isPromotion()) {
            	movedPiece = new Pawn(movedPiece.getColor());
            	movedPiece.addMouseListener(m.getStartSquare().new DragMouseHandler());
            } else {
            	movedPiece.decrementNumMoves();
            }
            m.getStartSquare().setPiece(movedPiece);
            ChessPiece capturedPiece = m.getCapturedPiece();
            if (m.getEnPassant() != null) {
            	m.getEnPassant().setPiece(capturedPiece);
            	endSq.setPiece(new ChessPiece(null, ChessPiece.EMPTY));
            } else {
            	endSq.setPiece(capturedPiece);
            }
         
            // update whose move it is                  
            switchWhoseMove();
            
            // play the appropriate sound
            if (capturedPiece.getType() == ChessPiece.EMPTY) {
                Chess.playSound("sounds/undo_move.wav");
            }
            else {                                                
                Chess.playSound("sounds/undo_capture.wav");
                // remove captured piece
                removeCapturedPiece(whoseMove, capturedPiece);
            }        
        }
        
        // update the redo index
        redoIndex--;        
        // check to see if there are any more moves
        Chess chess = (Chess)this.getTopLevelAncestor();
        if (redoIndex == 0) {
            // there is nothing more to undo
            chess.enableUndo(false);                    
        }
        // enable a redo
        chess.enableRedo(true);
        
        // log the undo
        Chess.logger.info("Undo last move.");
        
        // refresh the screen
        this.validate();
        this.repaint();
    }    
    
    /** Redo the last move */
    public void redoMove() {        
        Object move = moves.get(redoIndex);
        if (move instanceof CastleMove) {
            CastleMove cm = (CastleMove)move;            
            King king = (King)cm.getStartKingSquare().getPiece();
            king.castle(cm.getDirection(), true);
        }
        else {            
            Move m = (Move)move;            
            this.movePiece(m.getStartSquare(), m.getEndSquare(), true);
        }
        
        // update the index
        redoIndex++;        
        // check to see if there are any more moves
        Chess chess = (Chess)this.getTopLevelAncestor();
        if (redoIndex == moves.size()) {
            // there is nothing more to redo
            chess.enableRedo(false);
        }
        // enable an undo
        chess.enableUndo(true);
        
        // refresh the screen
        this.validate();
        this.repaint();
    }
    
    /** Adds the given piece to the captured pieces panel for the given player
     *  @param player the player who captured the piece
     *  @param piece the piece that was captured
     */
    public void addCapturedPiece(int player, ChessPiece piece) {
        piece.setCaptured(true);
        capturedCount[player - 1]++;
        // remove the last blank and add the piece to the beginning
        panelPlayer[player - 1].remove(15);
        panelPlayer[player - 1].add(piece, 0);
    }
    
    /** Removes the given piece from the captured pieces panel for the given player
     *  @param player the player who captured the piece
     *  @param piece the piece that was captured
     */
    public void removeCapturedPiece(int player, ChessPiece piece) {
        piece.setCaptured(false);
        capturedCount[player - 1]--;
        // The piece has already been removed from the panel because when you
        // add a component to a different container it is removed from its
        // current container        
        // just add a blank to the end        
        panelPlayer[player - 1].add(new JLabel(Chess.createImageIcon("images/blank.gif", "")), -1);
    }

    /** Checks to see whether or not the given player is in check or check mate 
     *  @param player the player to check
     *  @param blnCheckMate true to check for check mate
     */
    public int check(int player, boolean blnCheckMate) {
        Square sqKing = null;
        Square sqOpp = null;
        Color matchColor = null;
        Color oppColor = null;
        
        if (player == Board.PLAYER_1) {
            matchColor = ChessPiece.colorPlayer1;
            oppColor = ChessPiece.colorPlayer2;
        }
        else {
            matchColor = ChessPiece.colorPlayer2;
            oppColor = ChessPiece.colorPlayer1;
        }
        // ********** inefficient code ***************
        outer1:
        for (int i = 0; i <= 7; i++) {
            for (int j = 0; j <= 7; j++) {
                sqKing = getSquare(i, j);
                if (sqKing.getPiece().getType() == ChessPiece.KING && sqKing.getPiece().getColor().equals(matchColor)) {
                    break outer1;
                }
            }
        }
        
        if (sqKing != null) {
            boolean blnCheck = false;
            // ********** inefficient code ***************
            outer2:
            for (int i = 0; i <= 7; i++) {
                for (int j = 0; j <= 7; j++) {
                    ChessPiece cpOpp = getSquare(i, j).getPiece();
                    if (cpOpp.getType() != ChessPiece.EMPTY && cpOpp.getColor().equals(oppColor)) {
                        if (cpOpp.isValidMove(sqKing, false)) {
                            blnCheck = true;
                            break outer2;
                        }
                    }
                }
            }

            if (blnCheck) {            
                if (blnCheckMate) {
                    // check for check mate
                    // ********** inefficient code ***************
                    outer3:
                    for (int i = 0; i <= 7; i++) {
                        for (int j = 0; j <= 7; j++) {
                            ChessPiece cp = getSquare(i, j).getPiece();
                            if (cp.getType() != ChessPiece.EMPTY && cp.getColor().equals(matchColor)) {
                                for (int k = 0; k <= 7; k++) {
                                    for (int l = 0; l <= 7; l++) {
                                        if (!cp.getColor().equals(squares[k][l].getPiece().getColor()) && cp.isValidMove(squares[k][l], true)) {
                                            blnCheckMate = false;
                                            break outer3;
                                        }
                                    }
                                }
                            }
                        }
                    }                                
                    if (blnCheckMate) {
                        return Board.CHECK_MATE;                    
                    }
                    else {
                        return Board.CHECK;                    
                    }
                }
                else {
                    return Board.CHECK;                    
                }
            }
        }
        
        // if we get here, the player is not in check
        return NO_CHECK;
    }
    
    /** Rotates the board 90 degrees */
    public void rotate() {
        // a temporary object to use during the rotation
        Object tempSquares[][] = new Object[8][8];
        // save the current board
        for (int i = 0; i <= 7; i++) {
            for (int j = 0; j <= 7; j++) {
                tempSquares[i][j] = squares[i][j];
            }
        }        
        // loop through the board, moving the squares so that the board rotates 90 degrees        
        for (int i = 0; i <= 7; i++) {
            // initialize the offset
            int iOffset = 7;
            for (int j = 0; j<= 7; j++) {                
                squares[i][j] = (Square)tempSquares[j + iOffset][i];
                squares[i][j].setRow(i);
                squares[i][j].setCol(j);
                // adjust the offset
                iOffset = iOffset - 2;
            }            
        }
        // we are finished with the temporary object
        tempSquares = null;
        
        // now physically alter the board        
        panelBoard.removeAll();        
        for (int i = 0; i <= 7; i++) {
            for (int j = 0; j<= 7; j++) {
                panelBoard.add(squares[i][j]);                
            }            
        }
        this.remove(panelPlayer[0]);
        this.remove(panelPlayer[1]);
        // adjust the position
        switch (position) {
            case POSITION_1:
                position = POSITION_2;
                panelPlayer[0].setLayout(gridLayoutVertical);
                panelPlayer[1].setLayout(gridLayoutVertical);
                this.add(panelPlayer[0], BorderLayout.WEST);                
                this.add(panelPlayer[1], BorderLayout.EAST);                
                break;
            case POSITION_2:
                position = POSITION_3;
                panelPlayer[0].setLayout(gridLayoutHorizontal);
                panelPlayer[1].setLayout(gridLayoutHorizontal);
                this.add(panelPlayer[0], BorderLayout.NORTH);
                this.add(panelPlayer[1], BorderLayout.SOUTH);
                break;
            case POSITION_3:
                position = POSITION_4;
                panelPlayer[0].setLayout(gridLayoutVertical);
                panelPlayer[1].setLayout(gridLayoutVertical);
                this.add(panelPlayer[0], BorderLayout.EAST);
                this.add(panelPlayer[1], BorderLayout.WEST);
                break;                
            case POSITION_4:
                position = POSITION_1;
                panelPlayer[0].setLayout(gridLayoutHorizontal);
                panelPlayer[1].setLayout(gridLayoutHorizontal);
                this.add(panelPlayer[0], BorderLayout.SOUTH);
                this.add(panelPlayer[1], BorderLayout.NORTH);
                break;    
        }

        this.validate();
        Chess.playSound("sounds/moves.wav");
        Chess.logger.info("Board rotated to position " + position);
    }
    
    /** Saves its serializable fields by calling defaultWriteObject and then explicitly
     *  saves the static fields
     * 
     *  @serialData Store own serializable fields by calling defaultWriteObject
     *              and save static fields as optional data.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {        
    	// Take care of this class's serializable fields first by calling defaultWriteObject
    	out.defaultWriteObject();
	        
        // Square's static fields
        out.writeObject(Square.colorSquare1);
        out.writeObject(Square.colorSquare2);
        // ChessPiece's static fields
        out.writeObject(ChessPiece.colorPlayer1);
        out.writeObject(ChessPiece.colorPlayer2);
    }

    /** Restores its serializable fields by calling defaultReadObject and then explicitly
     *  restores the static fields. 
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {    
        // Take care of this class's serializable fields first by calling defaultReadObject	    
        in.defaultReadObject();

        // Square's static fields
        Square.colorSquare1 = (Color)in.readObject();
        Square.colorSquare2 = (Color)in.readObject();        
        // ChessPiece's static fields
        ChessPiece.colorPlayer1 = (Color)in.readObject();        
        ChessPiece.colorPlayer2 = (Color)in.readObject();            
    }
    
    /** Implements the dialog box for the user to select the piece to promote his pawn to */
    class PawnPromotionDialog extends JDialog implements ActionListener {
    	Board board;
    	Square endSquare;
    	Queen queen;
    	Rook rook;
    	Bishop bishop;
    	Knight knight;
        PawnPromotionDialog(Board board, Square endSquare, Pawn pawn) {
        	super((Chess)board.getTopLevelAncestor(), "Pawn Promotion", true);
        	this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        	this.board = board;
        	this.endSquare = endSquare;
            Container contentPane = getContentPane();
            contentPane.setLayout(new BorderLayout());

            JLabel lblMsg = new JLabel("Please select a piece to promote this pawn to: (yay for you)");
            JPanel panelMsg = new JPanel(new GridLayout(1, 1));
            panelMsg.add(lblMsg);
            
            // create the button panel
            Color color = pawn.getColor();
            this.queen = new Queen(color);
            this.rook = new Rook(color);
            this.bishop = new Bishop(color);
            this.knight = new Knight(color);
            JPanel panelButtons = new JPanel(new GridLayout(1, 4));
            JButton btnQueen = new JButton(this.queen.getIcon());
            getRootPane().setDefaultButton(btnQueen);
            btnQueen.setActionCommand("Queen");
            btnQueen.addActionListener(this);
            JButton btnRook = new JButton(this.rook.getIcon());
            btnRook.setActionCommand("Rook");
            btnRook.addActionListener(this);
            JButton btnBishop = new JButton(this.bishop.getIcon());
            btnBishop.setActionCommand("Bishop");
            btnBishop.addActionListener(this);
            JButton btnKnight = new JButton(this.knight.getIcon());
            btnKnight.setActionCommand("Knight");
            btnKnight.addActionListener(this);
            panelButtons.add(btnQueen);            
            panelButtons.add(btnRook);
            panelButtons.add(btnBishop);
            panelButtons.add(btnKnight);
            
            // add everything to the dialog window
            contentPane.add(panelMsg, BorderLayout.NORTH);
            contentPane.add(panelButtons, BorderLayout.CENTER);
        }
        
        public void actionPerformed(ActionEvent e) {
        	ChessPiece newPiece;
            if ("Queen".equals(e.getActionCommand())) {
            	newPiece = this.queen;
            }
            else if ("Rook".equals(e.getActionCommand())) {
            	newPiece = this.rook;
            }
            else if ("Bishop".equals(e.getActionCommand())) {
            	newPiece = this.bishop;
            }
            else { //if ("Knight".equals(e.getActionCommand())) {
            	newPiece = this.knight;                
            }
            newPiece.addMouseListener(endSquare.new DragMouseHandler());
            endSquare.setPiece(newPiece);
            newPiece.setPromoted(true);
            this.dispose();
        }
    }
}
