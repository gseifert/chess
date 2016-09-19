/*
 * Square.java
 *
 * Created on February 24, 2004, 9:47 AM
 */

package com.seifert.chess;

/**
 *
 * @author  ZZ3JPZ
 */
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
public class Square extends JPanel {
    
    protected static Color colorSquare1;
    protected static Color colorSquare2;    
    
    private int row;            // the row of the square on the board
    private int col;            // the column of the square on the board
    private Color color;        // the color of the square
    private ChessPiece piece;   // the piece on the square (blank squares will
                                // still contain a piece, the piece's type will
                                // be ChessPiece.EMPTY)        
    private MoveTransferHandler moveHandler;    // supports dnd
    private DropTarget dt;                      // supports dnd
    
    static {
        colorSquare1 = new Color(220, 200, 175);
        colorSquare2 = new Color(128, 64, 64);
    }
    
    /** Creates a new instance of Square */
    public Square(int row, int col) {
        super(new GridLayout(1, 1));                
        this.row = row;
        this.col = col;
        // if the row and column are both odd or if the row and column are both
        // even, then it is a "white" square, else it is a "black" square         
        if ((isOdd(row) && isOdd(col)) || (!isOdd(row) && !isOdd(col))) {
            color = colorSquare1;
        }
        else {
            color = colorSquare2;
        }
        // set the color of the square
        this.setBackground(color);
        // set the border of the square
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        // get starting piece for this square             
        piece = Square.getStartingPiece(row, col);
        // create transfer handler to enable drag and drop of the piece on 
        // the square
        moveHandler = new MoveTransferHandler("piece");
        this.setTransferHandler(moveHandler);
        // create a drop target to listen for the drop event        
        dt = new DropTarget(this, new MoveDropTargetAdapter());        
        // add mouse listener        
        piece.addMouseListener(new DragMouseHandler());
        // add the piece to the square
        this.add(piece);
    }
    
    /** Returns the starting piece for the given row and column of a square */
    protected static ChessPiece getStartingPiece(int row, int col) {
        switch (row) {
            case 1:                                
                return new Pawn(ChessPiece.colorPlayer2);                
            case 6:
                return new Pawn(ChessPiece.colorPlayer1);                
            case 0:
                switch (col) {
                    case 0:
                    case 7:                    
                        return new Rook(ChessPiece.colorPlayer2);
                    case 1:                        
                    case 6:
                        return new Knight(ChessPiece.colorPlayer2);                        
                    case 2:
                    case 5:
                        return new Bishop(ChessPiece.colorPlayer2);                        
                    case 3:
                        return new Queen(ChessPiece.colorPlayer2);                        
                    case 4:
                        return new King(ChessPiece.colorPlayer2);                        
                }                
            case 7:
                switch (col) {
                    case 0:                                            
                    case 7:                    
                        return new Rook(ChessPiece.colorPlayer1);
                    case 1:                        
                    case 6:
                        return new Knight(ChessPiece.colorPlayer1);                        
                    case 2:
                    case 5:
                        return new Bishop(ChessPiece.colorPlayer1);                        
                    case 3:
                        return new Queen(ChessPiece.colorPlayer1);                        
                    case 4:
                        return new King(ChessPiece.colorPlayer1);                        
                }
            default:
                return new ChessPiece(null, ChessPiece.EMPTY); 
        }
    }
    
    /** Returns true if given number is odd */
    private boolean isOdd(int x) {
        if (x % 2 == 1) {
            return true;
        }
        else {
            return false;
        }
    }

    /** Returns the row of the square */
    public int getRow() {
        return row;
    }
    
    /** Sets the row of the square */
    public void setRow(int newRow) {
        row = newRow;
    }

    /** Returns the column of the square */
    public int getCol() {
        return col;
    }
    
    /** Sets the column of the square */
    public void setCol(int newCol) {
        col = newCol;
    }
    
    /** Returns the color of the square */
    public Color getColor() {
        return color;
    }
    
    /** Returns the chess piece on the square */
    public ChessPiece getPiece() {
        return piece;
    }
    
    /** Sets the chess piece on the square */
    public void setPiece(ChessPiece piece) {
        this.remove(this.piece);        
        this.piece = piece;
        this.add(this.piece);
        this.validate();
    }
    
    // Handler class for mouse events
    class DragMouseHandler extends MouseAdapter implements Serializable {
        public void mousePressed(MouseEvent e) {            
            // check for popup button on certain platforms
            if (e.isPopupTrigger()) {
                maybeShowPopup(e);
            }
            // check for right mouse click
            if (e.getButton() == e.BUTTON3) {
                return;
            }
            // make sure the piece is on a square on the board            
            if (!(((JLabel)e.getSource()).getParent() instanceof Square)) {
                return;
            }
            Square startSquare = (Square)((JLabel)e.getSource()).getParent();
            // make sure there is a piece on this square to move
            if (startSquare.getPiece().getType() == ChessPiece.EMPTY) {                
                return;
            }            
            
            // make sure the game is not over
            Board board = (Board)startSquare.getParent().getParent();
            if ((board.getWhoseMove() == Board.GAME_OVER + Board.PLAYER_1) || (board.getWhoseMove() == Board.GAME_OVER + Board.PLAYER_2)) {
                JOptionPane.showMessageDialog(board.getTopLevelAncestor(), "The game is over.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // make sure it is this player's turn
            // and it is not a remote player's piece
            String strName = "";
            boolean blnValidMove = true;
            if (startSquare.getPiece().getColor() == ChessPiece.colorPlayer1) {
                if (board instanceof RemoteBoard) {
                    return;
                }
                if (board.getWhoseMove() != Board.PLAYER_1) {
                    strName = board.getPlayerName(Board.PLAYER_2);
                    blnValidMove = false;
                }
            }
            else if (board instanceof HostBoard) {
                return;
            }
            else if (board.getWhoseMove() != Board.PLAYER_2) {
                strName = board.getPlayerName(Board.PLAYER_1);
                blnValidMove = false;
            }
            if (blnValidMove) {
                TransferHandler handler = startSquare.getTransferHandler();
                // initiate the drag
                handler.exportAsDrag(startSquare, e, TransferHandler.COPY);                    
            }
            else {
                JOptionPane.showMessageDialog(board.getTopLevelAncestor(), "It is not your move.  It is " + strName + "'s move.", "Invalid move", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        
        public void mouseReleased(MouseEvent e) {
            // check if it is a popup mouse click
            if (e.isPopupTrigger()) {
                maybeShowPopup(e);                
            }            
        }
        
        private void maybeShowPopup(MouseEvent e) {
            // bring up castle popup menu if appropriate
            ChessPiece cp = (ChessPiece)e.getSource();            
            // make sure it is this player's turn
            // and it is not a remote player's piece                        
            final Board board = (Board)cp.getParent().getParent().getParent();
            if (cp.getColor() == ChessPiece.colorPlayer1) {
                if (board instanceof RemoteBoard) {
                    return;
                }
                if (board.getWhoseMove() != Board.PLAYER_1) {                    
                    return;
                }
            }
            else if (board instanceof HostBoard) {
                return;
            }
            else if (board.getWhoseMove() != Board.PLAYER_2) {
                return;
            }            
            // check if the piece is the king
            if (cp.getType() == ChessPiece.KING) {                    
                // create the popup menu for castling
                JPopupMenu popup = new JPopupMenu();                    
                final King king = (King)cp;
                if (king.isValidCastle(King.CASTLE_KINGSIDE)) {
                    // add castle kingside to popup menu                        
                    JMenuItem kingside = new JMenuItem("Castle Kingside");
                    kingside.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            king.castle(King.CASTLE_KINGSIDE, false);
                            // send the move to the remote player if needed                            
                            if (board instanceof NetBoard) {                                           
                                NetBoard netBoard = (NetBoard)board;
                                netBoard.getOut().println(netBoard.getChessProtocol().createMessage(netBoard));
                                // listen for remote player's next move
                                netBoard.listenForMove();
                            }
                        }
                    });
                    popup.add(kingside);
                }
                if (king.isValidCastle(King.CASTLE_QUEENSIDE)) {
                    // add castle queenside to popup menu
                    JMenuItem queenside = new JMenuItem("Castle Queenside");
                    queenside.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            king.castle(King.CASTLE_QUEENSIDE, false);
                            // send the move to the remote player if needed                            
                            if (board instanceof NetBoard) {                                           
                                NetBoard netBoard = (NetBoard)board;
                                netBoard.getOut().println(netBoard.getChessProtocol().createMessage(netBoard));
                                // listen for remote player's next move
                                netBoard.listenForMove();
                            }
                        }
                    });
                    popup.add(queenside);
                }
                // show popup menu if appropriate
                if (popup.getComponentCount() > 0) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }            
        }
    }
    
    class MoveTransferHandler extends TransferHandler {
        public MoveTransferHandler(String property) {
            super(property);
        }
        
        /** Overridden to show the piece while it is being moved.
         *  Sadly, this method never gets called by the TransferHandler
         *  mechanism.  This is a reported bug with Sun.
         */        
        public Icon getVisualRepresentation(Transferable t) {
            try {
                return ((ChessPiece)t.getTransferData(t.getTransferDataFlavors()[0])).getIcon();
            }
            catch (Exception e) {                  
            }
            return null;
        }
                
        /** Overridden to remove the piece from its original position if move was successful */        
//        protected void exportDone(JComponent c, Transferable data, int action) {            
//            if (action != TransferHandler.NONE) { 
//                Square sq = (Square)c;
//                ChessPiece cp = new ChessPiece(null, ChessPiece.EMPTY);
//                sq.setPiece(cp);                
//            }
//        }             
    }
    
    class MoveDropTargetAdapter extends DropTargetAdapter implements Serializable {   
        public void dragEnter(DropTargetDragEvent dtde) {
            // get the square that the piece is being dragged over
            Square sq = (Square)dtde.getDropTargetContext().getComponent();
            // enhance its border
            sq.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));            
        }
        
        public void dragExit(DropTargetEvent dtd) {
            // get the square that the piece is being dragged over
            Square sq = (Square)dtd.getDropTargetContext().getComponent();
            // reset its border
            sq.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        }
        
        public void drop(DropTargetDropEvent dtde) {            
            // get the square that the piece is trying to move to
            Square endSquare = (Square)dtde.getDropTargetContext().getComponent();            
            // get the board object
            Board board = (Board)endSquare.getParent().getParent();            
            // reset the drop square's border
            endSquare.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            try {                                
                // get the piece that is being moved
                ChessPiece movingPiece = (ChessPiece)dtde.getTransferable().getTransferData(dtde.getCurrentDataFlavors()[0]);                
                // check to see if the move is valid for this piece
                // (a piece cannot move to a square where there is already a piece of the same color)
                if (!movingPiece.getColor().equals(endSquare.getPiece().getColor()) && movingPiece.isValidMove(endSquare, true)) {
                    // indicate to the dnd mechanism that the drop is accepted
                    dtde.acceptDrop(dtde.getDropAction());                                        
                    //moveHandler.importData(endSquare, dtde.getTransferable());
                    try {
                        // move the piece                                                            
                        board.movePiece((Square)movingPiece.getParent(), endSquare, false);
                        // send the move to the remote player if needed                        
                        if (board instanceof NetBoard) {                                           
                            NetBoard netBoard = (NetBoard)board;
                            netBoard.getOut().println(netBoard.getChessProtocol().createMessage(netBoard));
                            // listen for remote player's next move
                            netBoard.listenForMove();
                        }
                    }
                    catch (Exception e) {                        
                        String msg = "Unknown error: " + e;
                        JOptionPane.showMessageDialog(board, msg, "Exception", JOptionPane.ERROR_MESSAGE);
                        Chess.logger.warning(msg);
                    }                                     
                }
                else {
                    // indicate to the dnd mechanism that the drop is rejected
                    dtde.rejectDrop();
                    JOptionPane.showMessageDialog(board, "This is not a valid move.", "Invalid move", JOptionPane.INFORMATION_MESSAGE, movingPiece.getIcon());                    
                    Chess.logger.info("Invalid move.");
                }
            }
            catch (UnsupportedFlavorException ufe) {                
                dtde.rejectDrop();
                String msg = "The requested data flavor is not supported.";
                JOptionPane.showMessageDialog(board, msg, "UnsupportedFlavorException", JOptionPane.ERROR_MESSAGE);
                Chess.logger.warning(ufe.toString() + ": " + msg);
            }
            catch (IOException ioe) {
                dtde.rejectDrop();
                String msg = "The data is no longer available in the requested flavor.";
                JOptionPane.showMessageDialog(board, msg, "IOException", JOptionPane.ERROR_MESSAGE);
                Chess.logger.warning(ioe.toString() + ": " + msg);
            }
            catch (Exception e) {
                dtde.rejectDrop();
                String msg = "Unknown error: " + e;
                JOptionPane.showMessageDialog(board, msg, "Exception", JOptionPane.ERROR_MESSAGE);
                Chess.logger.warning(msg);
            }
            // indicate to the dnd mechanism that the drop is complete
            dtde.dropComplete(true);
        }        
    }
}