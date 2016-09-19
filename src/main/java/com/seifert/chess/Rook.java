/*
 * Rook.java
 *
 * Created on February 24, 2004, 12:07 PM
 */

package com.seifert.chess;

/**
 *
 * @author  ZZ3JPZ
 */
import java.awt.*;
public class Rook extends ChessPiece {
    
    /** Creates a new instance of Rook */
    public Rook(Color color) {
        super(color, ChessPiece.ROOK);
        this.setIconStrings("images/wrook.gif", "images/wrook_captured.gif", "Rook");                
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
        // get the instance of the board
        Board board = (Board)sq.getParent().getParent();        
        
        // a rook cannot change both row and column in the same move
        if (newRow != currentRow && newCol != currentCol) {
            return false;
        }
        // a rook cannot move over any pieces
        int iDirection;
        if (newRow == currentRow) {            
            if (newCol < currentCol) {
                iDirection = -1;
            }
            else {
                iDirection = 1;
            }
            for (int j = currentCol + (1 * iDirection); j * iDirection < newCol * iDirection; j += (1 * iDirection)) {
                if (board.getSquare(newRow, j).getPiece().getType() != ChessPiece.EMPTY) {
                    return false;
                }
            }
        }
        else {            
            if (newRow < currentRow) {
                iDirection = -1;
            }
            else {
                iDirection = 1;
            }
            for (int i = currentRow + (1 * iDirection); i * iDirection < newRow * iDirection; i += (1 * iDirection)) {
                if (board.getSquare(i, newCol).getPiece().getType() != ChessPiece.EMPTY) {
                    return false;
                }
            }
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
}
