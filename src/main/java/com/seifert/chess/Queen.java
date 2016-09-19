/*
 * Queen.java
 *
 * Created on February 24, 2004, 12:07 PM
 */

package com.seifert.chess;

/**
 *
 * @author  ZZ3JPZ
 */
import java.awt.*;
public class Queen extends ChessPiece {
    
    /** Creates a new instance of Queen */
    public Queen(Color color) {
        super(color, ChessPiece.QUEEN);
        this.setIconStrings("images/wqueen.gif", "images/wqueen_captured.gif", "Queen");                
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
        
        // if a queen changes both its row and column, the change must be equal
        int iRowChange = Math.abs(newRow - currentRow);
        int iColChange = Math.abs(newCol - currentCol);
        if (iRowChange != 0 && iColChange != 0 && iRowChange != iColChange) {
            return false;
        }
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
        // a queen cannot move over any pieces
        if (iDirectionRow == 0) {
            for (int j = currentCol + (1 * iDirectionCol); j * iDirectionCol < newCol * iDirectionCol; j += (1 * iDirectionCol)) {
                if (board.getSquare(newRow, j).getPiece().getType() != ChessPiece.EMPTY) {
                    return false;
                }                
            }
        }
        else {
            int j = currentCol + (1 * iDirectionCol);
            for (int i = currentRow + (1 * iDirectionRow); i * iDirectionRow < newRow * iDirectionRow; i += (1 * iDirectionRow)) {
                if (board.getSquare(i, j).getPiece().getType() != ChessPiece.EMPTY) {
                    return false;
                }
                j += 1 * iDirectionCol;
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
