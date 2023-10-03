package code;

class Main{

	public static void main(String[] args)throws Exception {
		
		Board puzzle = new Board();
		puzzle.loadPuzzle("hard");
		puzzle.display();
		puzzle.logicCycles();
		puzzle.display();
		System.out.println("Error: " + puzzle.errorFound());
		System.out.println("Solved: " + puzzle.isSolved());
	}

}
