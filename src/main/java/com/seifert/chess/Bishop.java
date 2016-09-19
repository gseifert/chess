/*
 * Bishop.java
 *
 * Created on February 24, 2004, 12:07 PM
 */

package com.seifert.chess;

/**
 *
 * @author  ZZ3JPZ
 */
import java.awt.*;
public class Bishop extends ChessPiece {
    
    /** Creates a new instance of Bishop */
    public Bishop(Color color) {
        super(color, ChessPiece.BISHOP);
        this.setIconStrings("images/wbishop.gif", "images/wbishop_captured.gif", "Bishop");                
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
        
        // a bishop can only move diagonally, therefore the row change must be
        // equal to the column change
        int iRowChange = Math.abs(newRow - currentRow);
        int iColChange = Math.abs(newCol - currentCol);
        if (iRowChange != iColChange) {
            return false;
        }
        // a bishop cannot move over any pieces        
        int iDirectionRow, iDirectionCol;
        if (newRow < currentRow) {
            iDirectionRow = -1;
        }
        else {
            iDirectionRow = 1;
        }        
        if (newCol < currentCol) {
            iDirectionCol = -1;
        }
        else {
            iDirectionCol = 1;
        }        
        
        int j = currentCol + (1 * iDirectionCol);
        for (int i = currentRow + (1 * iDirectionRow); i * iDirectionRow < newRow * iDirectionRow; i += (1 * iDirectionRow)) {
            if (board.getSquare(i, j).getPiece().getType() != ChessPiece.EMPTY) {
                return false;
            }
            j += 1 * iDirectionCol;
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
