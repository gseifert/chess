/*
 * Knight.java
 *
 * Created on February 24, 2004, 12:07 PM
 */

package com.seifert.chess;

/**
 *
 * @author  ZZ3JPZ
 */
import java.awt.*;
public class Knight extends ChessPiece {
    
    /** Creates a new instance of Knight */
    public Knight(Color color) {
        super(color, ChessPiece.KNIGHT);
        this.setIconStrings("images/wknight.gif", "images/wknight_captured.gif", "Knight");                     
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
        
        // a knight must change its row by 2 and its column by 1 or 
        // its column by 2 and its row by 1
        int iRowChange = Math.abs(newRow - currentRow);
        int iColChange = Math.abs(newCol - currentCol);
        if ((iRowChange == 2 && iColChange == 1) || (iRowChange == 1 && iColChange == 2)) {            
            // if needed, check if this move would put the player into check
            if (check) {
                if (isCheck(newSq)) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }        
    }
}
