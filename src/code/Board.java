package code;

import java.io.File;
import java.util.Scanner;
import java.util.Arrays;

public class Board{
	
	private Cell[][] board = new Cell[9][9];
	private String level = "";
    private Cell[][][] boardstate = new Cell[81][9][9];
    private int[][] guess = new int [81][3]; //stores the guess, x and y coordinate and guess value
	
	///TODO: CONSTRUCTOR
	//This must initialize every cell on the board with a generic cell.  It must also assign all of the boxIDs to the cells
	public Board()
	{
		for(int x = 0; x < 9; x++)
        {
			for(int y = 0 ; y < 9; y++)
			{
				board[x][y] = new Cell();
				board[x][y].setBoxID( 3*(x/3) + (y)/3+1);
			}
        }
	}
	
	///TODO: loadPuzzle
	/*This method will take a single String as a parameter.  The String must be either "easy", "medium" or "hard"
	 * If it is none of these, the method will set the String to "easy".  The method will set each of the 9x9 grid
	 * of cells by accessing either "easyPuzzle.txt", "mediumPuzzle.txt" or "hardPuzzle.txt" and setting the Cell.number to 
	 * the number given in the file.  
	 * 
	 * This must also set the "level" variable
	 * TIP: Remember that setting a cell's number affects the other cells on the board.
	 */
	public void loadPuzzle(String level) throws Exception
	{
		this.level = level;
		String fileName = "easyPuzzle.txt";
		if(level.contentEquals("medium"))
			fileName = "mediumPuzzle.txt";
		else if(level.contentEquals("hard"))
			fileName = "hardPuzzle.txt";
		
		Scanner input = new Scanner (new File(fileName));
		
		for(int x = 0; x < 9; x++)
			for(int y = 0; y < 9; y++)
			{
				int number = input.nextInt();
				if(number != 0)
					solve(x, y, number);
			}
						
		input.close();
	}
	
	///TODO: isSolved
	/*This method scans the board and returns TRUE if every cell has been solved.  Otherwise it returns FALSE
	 * 
	 */
	public boolean isSolved()
	{
        for(int x = 0; x < 9; x++)
        {
            for(int y = 0; y < 9; y++)
            {
                if(board[x][y].getNumber() == 0)
                    return false;
            }
        }
        return true;
	}

	///TODO: DISPLAY
	/*This method displays the board neatly to the screen.  It must have dividing lines to show where the box boundaries are
	 * as well as lines indicating the outer border of the puzzle
	 */
	public void display()
	{
		for(int x = 0; x < 9; x++)
        {
            for(int y = 0; y < 9; y++)
            {
                System.out.print(board[x][y].getNumber() + " ");
                if(y == 2 || y == 5)
                {
                    System.out.print("| ");
                }
            }
            System.out.println();
            if(x == 2 || x == 5)
            {
                System.out.println("---------------------");
            }
        }
        System.out.println();
	}
	
	///TODO: solve
	/*This method solves a single cell at x,y for number.  It also must adjust the potentials of the remaining cells in the same row,
	 * column, and box.
	 */
	public void solve(int x, int y, int number)
	{
        int i, j;
        for(i = 0; i< 9; i++)
        {
            if(board[x][i].canBe(number) == true)
            {
                board[x][i].cantBe(number);
            }
            if(board[i][y].canBe(number) == true)
            {
                board[i][y].cantBe(number);
            }
        }
        for(i=0; i< 9; i++)
        {
            for(j=0; j < 9; j++)
            {
                if(board[i][j].getBoxID() == board[x][y].getBoxID())
                {
                    if(board[i][j].canBe(number) == true)
                    {
                        board[i][j].cantBe(number);
                    }
                }    
            }
        }
        board[x][y].setNumber(number);
	}
	
	//logicCycles() continuously cycles through the different logic algorithms until no more changes are being made.
	public void logicCycles()throws Exception
	{
        int changesMade;
        int stateNumber = 0;
        for(int i = 0; i < 81; i++)
        {
            for(int x = 0; x < 9; x++)
            {
                for(int y = 0 ; y < 9; y++)
                {
                    boardstate[i][x][y] = new Cell();
                    boardstate[i][x][y].setBoxID( 3*(x/3) + (y)/3+1);
                }
            }
        }
        do{
            do{
				changesMade = 0;
				changesMade += logic1();
				changesMade += logic2();
				changesMade += logic3();
				changesMade += logic4();
                if(changesMade > 0){
                    display();  
                    System.out.println("Changes made: " + changesMade);
                }
                if(errorFound())
					break;
			}while(changesMade != 0);
            if(errorFound()){
                stateNumber = revert(stateNumber);
            }
            else {
                stateNumber = guessAlgorithm(stateNumber);
            }
        }while(!isSolved());
	}
	
	///TODO: logic1
	/*This method searches each row of the puzzle and looks for cells that only have one potential.  If it finds a cell like this, it solves the cell 
	 * for that number. This also tracks the number of cells that it solved as it traversed the board and returns that number.
	 */
	public int logic1()
	{
		int changesMade = 0;
        int i, j;
        for(i = 0; i < 9; i++)
        {
            for(j = 0; j < 9; j++)
            {
                if(board[i][j].getNumber() == 0)
                {
                    if(board[i][j].numberOfPotentials() == 1)
                    {
                        solve(i,j,board[i][j].getFirstPotential());
                        changesMade++;
                    }
                }
                if(board[j][i].getNumber() == 0)
                {
                    if(board[i][j].numberOfPotentials() == 1)
                    {
                        solve(j,i,board[j][i].getFirstPotential());
                        changesMade++;
                    }
                }
            }
        }
		return changesMade;
	}
	
	///TODO: logic2
	/*This method searches each row for a cell that is the only cell that has the potential to be a given number.  If it finds such a cell and it
	 * is not already solved, it solves the cell.  It then does the same thing for the columns.This also tracks the number of cells that 
	 * it solved as it traversed the board and returns that number.
	 */
	
	public int logic2()
	{
        int count;
		int changesMade = 0;
		int i, j, k;
        int position = 0;
        for(i=0; i<9; i++)
        {
            for(j = 1; j < 10; j++)
            {
                count = 0; 
                for(k = 0; k < 9; k++)
                {
                    if(board[i][k].getNumber() == j)
                    {
                        break;
                    }
                    else if(board[i][k].getPotential()[j])
                    {
                        count++;
                        position = k;
                    }
                }
                if(count == 1)
                {
                    solve(i,position, j);
                }
                count = 0;
                for(k=0; k < 9; k++)
                {
                    if(board[k][i].getNumber() == j)
                    {
                        break;
                    }
                    else if(board[k][i].getPotential()[j])
                    {
                        count++;
                        position = k;
                    }
                }
                if(count == 1)
                {
                    solve(position, i, j);
                    changesMade++;
                }
            }
        }
		return changesMade;
	}
	
	///TODO: logic3
	/*This method searches each box for a cell that is the only cell that has the potential to be a given number.  If it finds such a cell and it
	 * is not already solved, it solves the cell. This also tracks the number of cells that it solved as it traversed the board and returns that number.
	 */
	public int logic3()
	{
		int changesMade = 0;
        int count, positionX = 0, positionY = 0;
        int i, j, k, l;
        for(i=1; i < 10; i++)
        {
            for(j=1; j < 10; j++)
            {
                count = 0;
                for(k = 0; k < 9; k++)
                {
                    for(l=0; l < 9; l++)
                    {
                        if(board[k][l].getBoxID() == i)
                        {
                            if(board[k][l].getPotential()[j])
                            {
                                count++;
                                positionX = k;
                                positionY = l;
                            }
                        }
                    }
                }
                if(count == 1)
                {
                    solve(positionX, positionY, j);
                    changesMade++;
                }
            }
        }
		return changesMade;
	}
	
	///TODO: logic4
		/*This method searches each row for the following conditions:
		 * 1. There are two unsolved cells that only have two potential numbers that they can be
		 * 2. These two cells have the same two potentials (They can't be anything else)
		 * 
		 * Once this occurs, all of the other cells in the row cannot have these two potentials.  Write an algorithm to set these two potentials to be false
		 * for all other cells in the row.
		 * 
		 * Repeat this process for columns and rows.
		 * 
		 * This also tracks the number of cells that it solved as it traversed the board and returns that number.
		 */
	public int logic4()
	{
		int changesMade = 0;
        //rows and columns
        int x, y, i, j;
        for(x=0; x < 9; x++){
            for(y=0; y< 9; y++){
                if(board[x][y].numberOfPotentials() == 2){
                    for(i = (y+1); i < 9;i++){
                        if(board[x][i].numberOfPotentials() == 2){
                            if(board[x][y].getFirstPotential() == board[x][i].getFirstPotential() && board[x][y].getSecondPotential() == board[x][i].getSecondPotential()){
                                for(j=0; j<9; j++){
                                    if(j != y && j!= i){
                                        if(board[x][j].canBe(board[x][y].getFirstPotential())){
                                            board[x][j].cantBe(board[x][y].getFirstPotential());
                                            changesMade++;
                                        }
                                        if(board[x][j].canBe(board[x][y].getSecondPotential())){
                                            board[x][j].cantBe(board[x][y].getSecondPotential());
                                            changesMade++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for(x=0; x < 9; x++)
        {
            for(y=0; y< 9; y++)
            {
                if(board[y][x].numberOfPotentials() == 2)
                {
                    for(i = (y+1); i < 9;i++)
                    {
                        if(board[i][x].numberOfPotentials() == 2)
                        {
                            if(board[y][x].getFirstPotential() == board[i][x].getFirstPotential() && board[y][x].getSecondPotential() == board[i][x].getSecondPotential())
                            {
                                for(j=0; j<9; j++)
                                {
                                    if(j != y && j!= i)
                                    {
                                        if(board[j][x].canBe(board[y][x].getFirstPotential()))
                                        {
                                            board[j][x].cantBe(board[y][x].getFirstPotential());
                                            changesMade++;
                                        }
                                        if(board[j][x].canBe(board[y][x].getSecondPotential()))
                                        {
                                            board[j][x].cantBe(board[y][x].getSecondPotential());
                                            changesMade++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //box
        for(x = 0; x < 9; x++)
        {
            for(y=0; y < 9; y++)
            {
                if(board[x][y].numberOfPotentials() == 2)
                {
                    for(i=0; i < 9; i++)
                    {
                        for(j=0; j< 9; j++)
                        {
                            if(board[i][j].numberOfPotentials() == 2 && board[i][j].getBoxID() == board[x][y].getBoxID() && i != x && j != y){
                                if(board[i][j].getFirstPotential() == board[x][y].getFirstPotential() && board[i][j].getSecondPotential() == board[x][y].getSecondPotential())
                                {
                                    for(int k = 0; k < 9; k++)
                                    {
                                        for(int l = 0; l < 9; l++)
                                        {
                                            if(board[x][y].getBoxID() == board[k][l].getBoxID()){
                                                if(k != x && k != i && l != y && l != j){
                                                    if(board[k][l].canBe(board[x][y].getFirstPotential()))
                                                    {
                                                        board[k][l].cantBe(board[x][y].getFirstPotential());
                                                        changesMade++;
                                                    }
                                                    if(board[k][l].canBe(board[x][y].getSecondPotential()))
                                                    {
                                                        board[k][l].cantBe(board[x][y].getSecondPotential());
                                                        changesMade++;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
		return changesMade;
	}
    //to do: fix revert, and change it so that the potential is also transfered
    public int revert(int stateNumber)
    {
        int x, y;
        stateNumber--;
        for(x = 0; x < 9; x++)
        {
            for(y=0; y < 9; y++)
            {
                board[x][y].setNumber(boardstate[stateNumber][x][y].getNumber());
                board[x][y].setPotential(boardstate[stateNumber][x][y].getPotential());
            }
        }
        board[guess[stateNumber][0]][guess[stateNumber][1]].cantBe(guess[stateNumber][2]);
        return stateNumber;
    }
    
    public int guessAlgorithm(int stateNumber)
    {
        int x, y;
        for(x = 0; x < 9; x++)
        {
            for(y=0; y < 9; y++)
            {
                if(board[x][y].getNumber() == 0)
                {
                    if(board[x][y].numberOfPotentials() > 0)
                    {
                        saveBoardState(stateNumber);
                        guess[stateNumber][0] = x;
                        guess[stateNumber][1] = y;
                        guess[stateNumber][2] = board[x][y].getFirstPotential();
                        stateNumber++;
                        solve(x,y, board[x][y].getFirstPotential());
                    }
                    return stateNumber;
                }
            }
        }
        return stateNumber;
    }
    
    public void saveBoardState(int stateNumber)
    {
        int x, y;
        for(x = 0; x < 9; x++)
        {
            for(y=0; y < 9; y++)
            {
                boardstate[stateNumber][x][y].setNumber(board[x][y].getNumber());
                boardstate[stateNumber][x][y].setPotential(board[x][y].getPotential());
            }
        }
    }
	
	///TODO: errorFound
	/*This method scans the board to see if any logical errors have been made.  It can detect this by looking for a cell that no longer has the potential to be 
	 * any number.
	 */
	public boolean errorFound()
	{
        for(int i = 0; i< 9; i++)
        {
            for(int j = 0; j < 9; j++)
            {
                if(board[i][j].getNumber() == 0 && board[i][j].numberOfPotentials() == 0)
                {
                    return true;
                }
            }
        }
		return false;
	}
}

