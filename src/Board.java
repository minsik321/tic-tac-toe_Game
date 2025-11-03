import java.util.ArrayList;
import java.util.List;

class Board {
    private char[][] board;
    private static final int SIZE = 3;
    public static final char EMPTY = ' ';
    
    public Board() {
        board = new char[SIZE][SIZE];
        reset();
    }
    
    public void reset() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = EMPTY;
            }
        }
    }
    
    public boolean makeMove(int position, char player) {
        int row = position / SIZE;
        int col = position % SIZE;
        
        if (row >= 0 && row < SIZE && col >= 0 && col < SIZE && board[row][col] == EMPTY) {
            board[row][col] = player;
            return true;
        }
        return false;
    }
    
    public char getCell(int position) {
        int row = position / SIZE;
        int col = position % SIZE;
        return board[row][col];
    }
    
    public char checkWinner() {
        // 가로 체크
        for (int i = 0; i < SIZE; i++) {
            if (board[i][0] != EMPTY && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return board[i][0];
            }
        }
         
        // 세로 체크
        for (int i = 0; i < SIZE; i++) {
            if (board[0][i] != EMPTY && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return board[0][i];
            }
        }
        
        // 대각선 체크
        if (board[0][0] != EMPTY && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return board[0][0];
        }
        
        if (board[0][2] != EMPTY && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return board[0][2];
        }
        
        return EMPTY;
    }
    
    public boolean isFull() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public Board clone() {
        Board newBoard = new Board();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                newBoard.board[i][j] = this.board[i][j];
            }
        }
        return newBoard;
    }
    
    // 해당 메서드는 호출 시점에 빈 칸의 개수를 미리 알 수 없고, 반복문을 돌면서 빈 칸을 발견할 때마다 요소를 추가해야 합니다.
    // 따라서 컬렉션 프레임워크 중에서 동적 크기를 지원하고 요소 추가가 효율적인 ArrayList가 가장 적합하여 사용했습니다.
    public List<Integer> getAvailableMoves() {
        List<Integer> moves = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (getCell(i) == EMPTY) {
                moves.add(i);
            }
        }
        return moves;
    }
}