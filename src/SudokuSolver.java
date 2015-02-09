/**
 * Place for your code.
 */
public class SudokuSolver {
	private Arc[][] arcs;

	private final int BOARD_SIZE = 9;
	/**
	 * @return names of the authors and their student IDs (1 per line).
	 */
	public String authors() {
		// TODO write it;
		return "Stewart Grant";
	}

	/**
	 * Performs constraint satisfaction on the given Sudoku board using Arc Consistency and Domain Splitting.
	 * 
	 * @param board the 2d int array representing the Sudoku board. Zeros indicate unfilled cells.
	 * @return the solved Sudoku board
	 */
	public int[][] solve(int[][] board) {
		// TODO write it;
		initArcs(board);
		printArcs();
		return board;
	}

	/**
	 * Intalize a set of arcs for each of squares on the sudoku board. The board is intalized with all arcs containing the values [1..9], then if the board contains a vaild set number the arc is trimmed down to the one coreect value. All of the arcs which were effected by the modification are set to dirty
	 * @param board the sudoku board with preset values
	 */
	private void initArcs(int [][]board){
		arcs = new Arc[BOARD_SIZE][BOARD_SIZE];
		for(int i=0;i<BOARD_SIZE;i++){
			for(int j=0;j<BOARD_SIZE;j++){
				//Init all new arcs
				arcs[i][j] = new Arc(BOARD_SIZE);
			}
		}
		for(int i=0;i<BOARD_SIZE;i++){
			for(int j=0;j<BOARD_SIZE;j++){
				//Trim domains preset values
				if(board[i][j]!=0){
					for(int k=0;k<BOARD_SIZE;k++){
						//System.out.println("i = "+i+", j = "+j + "k ="+k);
						if(board[i][j]!=(k+1)){
							arcs[i][j].dom.remove(new Integer(k + 1));
						}
					}
					dirtyEffected(i,j);
				}
			}
		}
	}

	/*
	 * dirtyEffected marks all arcs that have had a dependent arc [i][j] modified.
	 * @param i row of the updated arc
	 * @param j colum of the updated arc
	 */
	private void dirtyEffected(int i, int j){
		for(int k=0;k<BOARD_SIZE;k++){
			if(k!=j){
				arcs[i][k].setDirty(true);
			}
			if(k!=i){
				arcs[k][j].setDirty(true);
			}
		}
		for(int k = (i - i%3); k < (i - i%3 +3); k++){
			for(int l = (j - j%3); l < (j - j%3 +3); l++){
				//System.out.println("k = "+k+" l = "+l);
				if(k!=i && l!=j){
				       arcs[k][l].setDirty(true);
				}
			}
		}
	}		


	/**
	 * Preforms a completion check on the board. The board is complete if all the values are non zero
	 *
	 * @param board 2d int array representing a sudoku board
	 * @return true if the board is solved, false otherwise
	 */
	private boolean complete(int [][]board){
		for(int i=0;i<BOARD_SIZE;i++){
			for(int j=0;j<BOARD_SIZE;j++){
				if(board[i][j] == 0){
					return false;
				}
			}
		}
		return true;
	}
	
	private void printArcs(){
		for(int i=0;i<BOARD_SIZE;i++){
			for(int j=0;j<BOARD_SIZE;j++){
				System.out.println(arcs[i][j].toString());
			}
		}
	}


}
