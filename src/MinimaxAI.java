class MinimaxAI implements AIStrategy {
    private String name;
    private char aiSymbol;
    private char playerSymbol;
    private int maxDepth;
    
    public MinimaxAI(char aiSymbol, char playerSymbol, int maxDepth) {
        this.aiSymbol = aiSymbol;
        this.playerSymbol = playerSymbol;
        this.maxDepth = maxDepth;
        this.name = generateName(maxDepth); 
    }
    
    // maxDepth 값을 받아 난이도 이름을 설정합니다.
    private String generateName(int depth) {
        if (depth == TicTacToeGame.MEDIUM_DEPTH) {
            return "중간 AI";
        } else {
            return "어려움 AI";
        }
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    // AI 턴: 미니맥스를 호출하여 최선의 수를 탐색합니다.
    // 계산된 값은 결국 min 함수를 거치므로, AI는 반드시 최고의 점수를 얻는 것이 아니라, 
    // 플레이어가 방해할 때 발생할 수 있는 최악의 상황을 고려하여
    // ‘최대한 리스크를 피하면서 가장 나쁘지 않은 선택’을 하도록 움직입니다.
    @Override
    public int makeMove(Board board) {
        int bestMove = -1;
        int bestScore = Integer.MIN_VALUE; 
        
        for (int i = 0; i < 9; i++) {
            if (board.getCell(i) == Board.EMPTY) {
                Board newBoard = board.clone();
                newBoard.makeMove(i, aiSymbol);
                
                int score = minimax(newBoard, 0, false);
                
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = i;
                }
            }
        }
        
        return bestMove;
    }
    
    // 미니맥스 알고리즘을 사용하여 AI가 최적의 수를 탐색합니다.
    // max 함수는 AI가 가장 유리한 점수를 얻도록, min 함수는 플레이어가 가장 유리한 점수를 얻도록 시뮬레이션합니다.
    // depth를 활용해 탐색 깊이를 제한함으로써 난이도를 조절할 수 있으며,
    // 이 함수는 AI가 선택 가능한 모든 수와 그 결과 점수를 계산하며, 단순히 이기는 수뿐 아니라, 지더라도 가능한 한 늦게 지는 최적의 방향으로 게임을 진행합니다.
    private int minimax(Board board, int depth, boolean isMaximizing) {
        char winner = board.checkWinner();
        
        // 1. 종료 조건 (depth는 실제 진행된 수의 횟수)
        if (winner == aiSymbol) return 10 - depth; // 승리: 빨리 이길수록 높은 점수
        if (winner == playerSymbol) return depth - 10; // 패배: 늦게 질수록 높은 점수(덜 나쁜 수)
        
        // 2. 탐색 깊이 제한: maxDepth에 도달하면 탐색 중단
        if (board.isFull() || depth >= maxDepth) return 0; 
        
        if (isMaximizing) {
            // 최대화 (AI 차례)
            int maxScore = Integer.MIN_VALUE;
            
            for (int i = 0; i < 9; i++) {
                if (board.getCell(i) == Board.EMPTY) {
                    Board newBoard = board.clone();
                    newBoard.makeMove(i, aiSymbol);
                    
                    // 재귀 호출 시 깊이를 1 증가시켜 전달
                    int score = minimax(newBoard, depth + 1, false);
                    maxScore = Math.max(maxScore, score);
                }
            }
            return maxScore;
        } else {
            // 최소화 (플레이어 차례)
            int minScore = Integer.MAX_VALUE;
            
            for (int i = 0; i < 9; i++) {
                if (board.getCell(i) == Board.EMPTY) {
                    Board newBoard = board.clone();
                    newBoard.makeMove(i, playerSymbol);
                    
                    // 재귀 호출 시 깊이를 1 증가시켜 전달
                    int score = minimax(newBoard, depth + 1, true);
                    minScore = Math.min(minScore, score);
                }
            }
            return minScore;
        }
    }
}