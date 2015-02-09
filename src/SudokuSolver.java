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
		//printArcs();
		while(!complete(board) && !clean()){
			arcConsistancy(board);
		}
		return board;
	}


	private void arcConsistancy(int[][]board){
		for(int i=0;i<BOARD_SIZE;i++){
			for(int j=0;j<BOARD_SIZE;j++){
				if(arcs[i][j].getDirty() && arcs[i][j].dom.size() > 1){
					rowCondition(i,j);
					columnCondition(i,j);
					sectorCondition(i,j);
					//if the size of the domain is now 1 set the actual board
					if(arcs[i][j].dom.size() == 1){
						//System.out.println("setting ["+i+"]["+j+"]");
						board[i][j] = arcs[i][j].dom.get(0);
					}
				}
				arcs[i][j].setDirty(false);
			}
		}

	}

	private void sectorCondition(int i, int j){
		boolean modified = false;
		for(int k = (i - i%3); k < (i - i%3 +3); k++){
			for(int l = (j - j%3); l < (j - j%3 +3); l++){
				if(i == k && j == l){
					continue;
				} else {
					//System.out.println("cons ["+i+"]["+j+"]->["+k+"]["+l+"]");
					modified = (modified || notEqualConsistancy(arcs[i][j],arcs[k][l]));
				}
			}
		}
		if(modified){
			dirtyEffected(i,j);
		}
	}

	private void columnCondition(int i, int j){
		boolean modified = false;
		for(int r=0;r<BOARD_SIZE;r++){
			if(i == r){
				continue;
			} else {
				//System.out.println("cons ["+i+"]["+j+"]->["+r+"]["+j+"]");
				modified = (modified || notEqualConsistancy(arcs[i][j],arcs[r][j]));
			}
		}
		if(modified){
			dirtyEffected(i,j);
		}
	}

	private void rowCondition(int i, int j){
		boolean modified = false;
		for(int r=0;r<BOARD_SIZE;r++){
			if(j == r){
				continue;
			} else {
				//System.out.println("cons ["+i+"]["+j+"]->["+i+"]["+r+"]");
				modified = (modified || notEqualConsistancy(arcs[i][j],arcs[i][r]));
			}
		}
		if(modified){
			dirtyEffected(i,j);
		}
	}

	/**
	 *notEqualConsistancy makes two squares not equal consistant, the arcs are modifed so that arc a is trimmed to be consistant with arc b and not the other way around.
	 @param Arc a, the arc being made consistant
	 @param Arc b, the arc used as a comparitor
	 */
	private boolean notEqualConsistancy(Arc a, Arc b){
		boolean modified = false;
		for(int k=0;k<a.dom.size();k++){
			boolean consistant = false;
			for(int l=0;l<b.dom.size();l++){
				if(a.dom.get(k).intValue() != b.dom.get(l).intValue()){
					consistant = true;
				}
			}
			if(!consistant){
				//System.out.println("removing");
				a.dom.remove(new Integer(a.dom.get(k).intValue()));
				modified = true;
			}
		}
		return modified;

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
	
	/**
	 * Preforms a completion check on the board. The board is clean if all the arcs cannot be reduced any more
	 *
	 * @param board 2d int array representing a sudoku board
	 * @return true if the board is solved, false otherwise
	 */
	private boolean clean(){
		for(int i=0;i<BOARD_SIZE;i++){
			for(int j=0;j<BOARD_SIZE;j++){
				if(arcs[i][j].getDirty()){
					return false;
				}
			}
		}
		return true;
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
	
	private void printArcs(){
		for(int i=0;i<BOARD_SIZE;i++){
			for(int j=0;j<BOARD_SIZE;j++){
				System.out.println(arcs[i][j].toString());
			}
		}
	}


}
