/*
 * King.java
 *
 * Created on February 24, 2004, 12:08 PM
 */

package com.seifert.chess;

/** Represents the king.
 *
 * @author  ZZ3JPZ
 */
import java.awt.*;

import javax.swing.JOptionPane;
/** Implements the king. */
public class King extends ChessPiece {
    
    protected static final int CASTLE_KINGSIDE = 1;
    protected static final int CASTLE_QUEENSIDE = -1;
    
    /** Creates a new instance of King */
    public King(Color color) {
        super(color, ChessPiece.KING);
        this.setIconStrings("images/wking.gif", "images/wking_captured.gif", "King");               
    }
    
    /** Checks to see if the piece can legally move to the given square of the board
     *  from its present location.
     *  @param newSq the square of the board the piece is trying to move to
     *  @param check true if this method should check if the move would put the player in check
     *  @return true if the piece can move to the given square of the board
     */
    public boolean isValidMove(Square newSq, boolean check) {        
        // get the row and column of the square that the piece wants to move to
        int newRow = newSq.getRow();
        int newCol = newSq.getCol();
        // get the square that the piece is currently on
        Square sq = (Square)this.getParent();
        // get the current row and column of the square that the piece is on
        int currentRow = sq.getRow();
        int currentCol = sq.getCol();        
        
        // a king can only move 1 space        
        int iRowChange = Math.abs(newRow - currentRow);
        int iColChange = Math.abs(newCol - currentCol);
        if (iRowChange > 1 || iColChange > 1) {
            return false;
        }        
        
        // if needed, check if this move would put the player into check
        if (check) {
            if (isCheck(newSq)) {
                return false;
            }
        }
        
        // if we get here, it is a valid move
        return true;
    }   
    
    /** Returns true if a castle move can be made in the given direction
     *  @param iDirection the direction of the castle, either King.CASTLE_KINGSIDE
     *  or King.CASTLE_QUEENSIDE
     *  @return true if the king can castle in the given direction
     */
    public boolean isValidCastle(int iDirection) {
        // make sure the king has not moved
        if (this.getNumMoves() != 0) {
            return false;
        }
        
        Square sq = (Square)this.getParent();
        Board board = (Board)sq.getParent().getParent();
        
        // make sure the player is not currently in check
        if (board.check(board.getWhoseMove(), false) != Board.NO_CHECK) {
        	return false;
        }
        
        int iRowFactor = 0;
        int iColFactor = 0;
        switch (board.getPosition()) {                
            case Board.POSITION_3:
                // flip the direction due to the rotation of the board
                iDirection = iDirection * -1;
                // no break so it falls through to the next case
            case Board.POSITION_1:
                iRowFactor = 0;
                iColFactor = 1;
                break;
            case Board.POSITION_4:
                // flip the direction due to the rotation of the board
                iDirection = iDirection * -1;
                // no break so it falls through to the next case
            case Board.POSITION_2:
                iRowFactor = 1;
                iColFactor = 0;
                break;    
        }                        

        int i = 1;
        int iRow, iCol;                    
        while (i <= 4) {
            iRow = sq.getRow() + i * iRowFactor * iDirection;
            iCol = sq.getCol() + i * iColFactor * iDirection;
            if (iRow > 7 || iRow < 0 || iCol > 7 || iCol < 0) {
                return false;
            }
            Square square = board.getSquare(iRow, iCol);
            ChessPiece castlePiece = square.getPiece();
            if (castlePiece.getType() == ChessPiece.ROOK) {
                // check to make sure the rook has not moved
                if (castlePiece.getNumMoves() != 0) {
                    return false;
                }
                break;                        
            }
            else if (castlePiece.getType() != ChessPiece.EMPTY) {
                return false;
            } else if (i <= 2 && isCheck(square)) {
                return false;
            }
            i++;
        }                                                    
        
        // if we get here it is a valid castle
        return true;
    }
    
    /** Performs the castle move on this king in the given direction.
     *  @param iDirection the direction of the castle, either King.CASTLE_KINGSIDE or
     *  King.CASTLE_QUEENSIDE
     *  @param isRedo true if this castle is a redo
     */
    public void castle(int iDirection, boolean isRedo) {
        int i = 0;
        String strDirection = "";
        switch (iDirection) {
            case CASTLE_KINGSIDE:
                i = 3;
                strDirection = "kingside";
                break;
            case CASTLE_QUEENSIDE:
                i = 4;
                strDirection = "queenside";
                break;
            default:
                return;
        }
        
        Square startKingSquare = (Square)this.getParent();
        Board board = (Board)startKingSquare.getParent().getParent();
        int iRowFactor = 0;
        int iColFactor = 0;
        switch (board.getPosition()) {                
            case Board.POSITION_3:
                // flip the direction due to the rotation of the board
                iDirection = iDirection * -1;
                // no break so it falls through to the next case
            case Board.POSITION_1:
                iRowFactor = 0;
                iColFactor = 1;
                break;
            case Board.POSITION_4:
                // flip the direction due to the rotation of the board
                iDirection = iDirection * -1;
                // no break so it falls through to the next case
            case Board.POSITION_2:
                iRowFactor = 1;
                iColFactor = 0;
                break;    
        }        
        
        int iRow, iCol;        
        iRow = startKingSquare.getRow();
        iCol = startKingSquare.getCol();
        // move the king two spaces
        Square endKingSquare = board.getSquare(iRow + 2 * iRowFactor * iDirection, iCol + 2 * iColFactor * iDirection);
        endKingSquare.setPiece(this);
        // put a blank on the king's start square
        startKingSquare.setPiece(new ChessPiece(null, ChessPiece.EMPTY));
        // move the rook one square next to the king towards the center
        Square startRookSquare = board.getSquare(iRow + i * iRowFactor * iDirection, iCol + i * iColFactor * iDirection);
        Square endRookSquare = board.getSquare(iRow + 1 * iRowFactor * iDirection, iCol + 1 * iColFactor * iDirection);
        ChessPiece rook = startRookSquare.getPiece();
        endRookSquare.setPiece(rook);
        // put a blank on the rook's start square
        startRookSquare.setPiece(new ChessPiece(null, ChessPiece.EMPTY));
        
        // increment the number of moves the king and rook have made
        this.incrementNumMoves();
        rook.incrementNumMoves();
        
        // update whose move it is
        int player = board.getWhoseMove();
        board.switchWhoseMove();
        
        // play the appropriate sound                            
        Chess.playSound("sounds/castle.wav");                            
        
        // store the move if needed
        if (!isRedo) {
            board.addMove(new CastleMove(startKingSquare, endKingSquare, startRookSquare, endRookSquare, iDirection));
        }
        
        // log the castle
        Chess.logger.info(board.getPlayerName(player) + " castled " + strDirection);
        
        // check to see if player is in check or check mate
        int chk = board.check(board.getWhoseMove(), true);
        if (chk == Board.CHECK) {
        	Chess.playSound("sounds/check.wav");
            // alert the user
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "Check.", "Check", JOptionPane.INFORMATION_MESSAGE);
            // log the check
            Chess.logger.info(board.getPlayerName(board.getWhoseMove()) + " is in check.");
        }
        else if (chk == Board.CHECK_MATE) {
        	Chess.playSound("sounds/check_mate.wav");
            // indicate that the game is over                    
            String msg = "Check Mate.  " + board.getPlayerName(player) + " wins.";
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), msg, "Check Mate", JOptionPane.INFORMATION_MESSAGE);
            board.setWhoseMove(Board.GAME_OVER + player);
            // log the check mate
            Chess.logger.info(msg);
        }
        
        if (board.isAutoRotate() && !(board instanceof NetBoard)) {
        	board.rotate();
			Chess chess = (Chess) board.getTopLevelAncestor();
            chess.setFiller(board.getPosition());
            board.rotate();
        	chess.setFiller(board.getPosition());
        }
        
        // refresh the screen
        board.validate();
        board.repaint();
    }
}
