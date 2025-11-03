import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

public class TicTacToeGame extends JFrame {
    // JFrame 상속으로 Serializable이 자동 상속되어 생긴 경고로, serialVersionUID 선언으로 해결
    private static final long serialVersionUID = 1L;
    private Board board;
    private JButton[] buttons;
    private JLabel statusLabel;
    private AIStrategy aiPlayer;
    private char humanSymbol = 'X'; // 기본값 X
    private char aiSymbol = 'O';    // 기본값 O
    private boolean gameActive; // AI 차례 여부
    private String currentDifficulty; // 현재 난이도 저장
    private int humanScore = 0;
    private int aiScore = 0;
    private JLabel scoreLabel;
    private boolean[] isClicked = new boolean[9]; // 각 버튼의 클릭 상태를 저장하는 플래그
    
    // 난이도 설정: 
    // 중간 난이도는 미니맥스 탐색 깊이를 3으로 제한하여 탐색을 줄이고,
    // 어려움 난이도는 탐색 깊이를 10으로 설정해 거의 완전 탐색을 수행, 최적의 수를 반환합니다.
    public static final int MEDIUM_DEPTH = 3;
    private static final int HARD_DEPTH = 10;
    
    // 기본 설정 : 윈도우 창을 닫을 때 프로그램이 종료 된다.
    public TicTacToeGame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public void start() {
        showDifficultySelection();
    }
    
    
    private void showDifficultySelection() {
        JDialog difficultyDialog = new JDialog(this, "난이도 선택", true);	// 1. 다이얼로그(팝업창) 생성
        difficultyDialog.setLayout(new GridLayout(4, 1, 10, 10));	// 2. 레이아웃 설정: 4행 1열 그리드
        difficultyDialog.setSize(350, 250); // 3. 크기와 위치
        difficultyDialog.setLocationRelativeTo(null);  // 화면 중앙
        JLabel titleLabel = new JLabel("틱택토 게임", SwingConstants.CENTER);	// 4. 제목 라벨
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        difficultyDialog.add(titleLabel);
        JButton easyButton = createDifficultyButton("쉬움", difficultyDialog, "EASY"); // 5. 난이도 버튼 3개 생성 및 추가
        JButton mediumButton = createDifficultyButton("중간", difficultyDialog, "MEDIUM");
        JButton hardButton = createDifficultyButton("어려움", difficultyDialog, "HARD");
        
        difficultyDialog.add(easyButton);
        difficultyDialog.add(mediumButton);
        difficultyDialog.add(hardButton);
       
        difficultyDialog.setVisible(true); // 6. 다이얼로그 표시 (modal: 이 창이 닫힐 때까지 다른 작업 불가)
    }
    
    private JButton createDifficultyButton(String text, JDialog dialog, String difficulty) {
        JButton button = new JButton(text);
        button.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        button.addActionListener(e -> {		// 버튼 클릭 시 실행될 이벤트 리스너
            initializeGame(difficulty);  	// 해당 난이도로 게임 설정 및 초기화
            dialog.dispose();           	// 모달 다이얼로그(JDialog)를 닫기
            setVisible(true);            	// 메인 게임 창(JFrame)을 화면에 표시
        });
        return button;
    }
    
    private void initializeGame(String difficulty) {
        
    	// difficulty는 지역 변수로 스택에 저장되어 함수 종료 시 사라집니다.
    	// 이벤트 리스너는 나중에 실행되므로, 이전 난이도 정보를 유지하려면 
    	// 힙에 저장되는 인스턴스 변수 currentDifficulty에 할당해야 합니다.
        currentDifficulty = difficulty;
        
        // AI 플레이어 생성
        switch (difficulty) {
            case "EASY":
                aiPlayer = new EasyAI();
                break;
            case "MEDIUM":
                aiPlayer = new MinimaxAI(aiSymbol, humanSymbol, MEDIUM_DEPTH);
                break;
            case "HARD":
                aiPlayer = new MinimaxAI(aiSymbol, humanSymbol, HARD_DEPTH);
                break;
        }
        
        // 게임 보드 초기화
        board = new Board();
        gameActive = (humanSymbol == 'X'); // X가 먼저 시작
        
        // 기존 컴포넌트 제거
        getContentPane().removeAll();
        
        // GUI 설정
        setTitle("틱택토 - " + aiPlayer.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // X 버튼 클릭 시 프로그램 종료
        setLayout(new BorderLayout(10, 10));
        setSize(400, 500);
        setLocationRelativeTo(null); // 화면 중앙에 위치
        
        // 상태 레이블
        String initialMessage = gameActive ? 
            "당신의 차례입니다 (" + humanSymbol + ")" : 
            "AI의 차례입니다 (" + aiSymbol + ")";
        statusLabel = new JLabel(initialMessage, SwingConstants.CENTER);
        statusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        add(statusLabel, BorderLayout.NORTH);
        
        // 점수 레이블 초기화
        scoreLabel = new JLabel(formatScoreText(), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        
        // 상태 레이블과 점수 레이블을 담을 상단 패널 생성 및 배치
        JPanel northPanel = new JPanel(new GridLayout(2, 1)); // 2행 1열

        
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        northPanel.add(statusLabel);	// 1행: 게임 상태 메시지
        northPanel.add(Box.createVerticalStrut(5)); // 상태와 점수 사이 간격
        northPanel.add(scoreLabel);		// 2행: 점수 표시	
        add(northPanel, BorderLayout.NORTH);
        
        // 게임 보드 패널
        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 8, 8));       
        boardPanel.setBackground(Color.BLACK);
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttons = new JButton[9];
        isClicked = new boolean[9];
        
        for (int i = 0; i < 9; i++) {
            final int position = i;
            buttons[i] = new JButton("");
            buttons[i].setFont(new Font("맑은 고딕", Font.BOLD, 60));
            buttons[i].setFocusPainted(false);
            buttons[i].setBackground(Color.WHITE);
            // 마우스 리스너 추가: 커서 모양 제어
            buttons[i].addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (isClicked[position]) { // 이미 클릭된 칸이면 기본 커서 유지
                        buttons[position].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    } else { // 클릭 가능하면 손가락 모양 커서 (클릭 가능하다는 시각적 피드백 제공)
                        buttons[position].setCursor(new Cursor(Cursor.HAND_CURSOR));
                    }
                }
            });
            buttons[i].addActionListener(e -> handlePlayerMove(position));
            boardPanel.add(buttons[i]);
        }
        
        add(boardPanel, BorderLayout.CENTER);
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton resetButton = new JButton("다시 시작");
        resetButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        resetButton.addActionListener(e -> resetGame()); // 버튼을 누르면 기존의 게임 지우고 새로 시작
        
        JButton changeSymbolButton = new JButton("심볼 변경 (" + (humanSymbol == 'X' ? "O" : "X") + "로)");
        changeSymbolButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        changeSymbolButton.addActionListener(e -> {
            // 심볼 변경
            if (humanSymbol == 'X') {
                humanSymbol = 'O';
                aiSymbol = 'X';
            } else {
                humanSymbol = 'X';
                aiSymbol = 'O';
            }
            // 현재 난이도로 게임 재시작
            initializeGame(currentDifficulty);
        });
        
        JButton changeDifficultyButton = new JButton("난이도 변경");
        changeDifficultyButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        changeDifficultyButton.addActionListener(e -> { // 버튼을 누르면 기존의 화면을 전부 지우고 새로 난이도 선택 및 시작
            showDifficultySelection();
        });
        
        buttonPanel.add(resetButton);
        buttonPanel.add(changeSymbolButton);
        buttonPanel.add(changeDifficultyButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 컴포넌트를 추가/제거하거나 상태를 바꾼 뒤 화면을 완전히 갱신할 때 사용
        revalidate();
        repaint();
        
        // O를 선택했다면 AI가 먼저 시작
        if (!gameActive) {
        	startAIMoveTimer();
        }
    }
    
    // 게임 보드 칸에 버튼이 눌리면 해당 함수가 동작함. AI의 차례이면 플레이어 입력 무시, 이미 X나 O가 있는 칸이면 무시
    private void handlePlayerMove(int position) {
        if (!gameActive ) return;
        // 이미 채워진 칸을 선택하면 예외 발생
        try {
        	if (board.getCell(position) != Board.EMPTY) {
                // isClicked 플래그가 제대로 작동하지 않았을 때 예외를 발생시킵니다.
                throw new InvalidMoveException("이미 심볼이 놓인 칸(" + position + ")을 선택했습니다.");
            }
        
        // 플레이어 이동
        board.makeMove(position, humanSymbol);
        buttons[position].setText(String.valueOf(humanSymbol));
        buttons[position].setForeground(Color.BLUE);      
        buttons[position].setBorder(null);  // L&F의 Hover 테두리 효과를 제거합니다.
        buttons[position].setContentAreaFilled(false); // L&F의 Hover 배경 효과를 막기 위해 ContentAreaFilled를 끕니다.
        buttons[position].setOpaque(true); // 배경색(WHITE)이 보이도록 설정
        isClicked[position] = true;
        buttons[position].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
     
        // 게임이 끝났는지 확인
        if (checkGameEnd()) {
            return;
        }
        
        // AI 차례로 전환
        gameActive = false;
        statusLabel.setText("AI가 생각 중...");
        
        startAIMoveTimer();
        } catch (InvalidMoveException e) {
        	System.err.println("로직 오류: " + e.getMessage());
            //팝업으로 경고
            javax.swing.JOptionPane.showMessageDialog(
                null,                   // 부모 컴포넌트 (null이면 화면 중앙)
                "이미 선택한 칸입니다!",   	// 메시지
                "잘못된 선택",           	// 타이틀
                javax.swing.JOptionPane.WARNING_MESSAGE // 경고 아이콘
            );
        }
    }
 
    private String formatScoreText() {
        return String.format("플레이어 (%c): %d | AI (%c): %d", 
                             humanSymbol, humanScore, aiSymbol, aiScore);
    }
    
    private void updateScore(char winnerSymbol) {
        if (winnerSymbol == humanSymbol) {
            humanScore++;
        } else if (winnerSymbol == aiSymbol) {
            aiScore++;
        }

        if (scoreLabel != null) {
            scoreLabel.setText(formatScoreText());
        }
    }

    // AI 수 두기 로직
    private void makeAIMove() {
    	try {
    		
        int aiMove = aiPlayer.makeMove(board);
        if (aiMove == -1) { // aiMove가 -1일 경우 예외 발생
            throw new InvalidMoveException("AI가 둘 수 있는 유효한 수를 찾지 못했습니다. (makeMove가 -1 반환)");
        }
            board.makeMove(aiMove, aiSymbol);
            buttons[aiMove].setText(String.valueOf(aiSymbol));
            buttons[aiMove].setForeground(Color.RED);
            buttons[aiMove].setBorder(null);
            buttons[aiMove].setContentAreaFilled(false);
            buttons[aiMove].setOpaque(true);
            isClicked[aiMove] = true;
            buttons[aiMove].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            
        // 게임이 끝나지 않았다면 플레이어에게 차례를 안내하고 플레이어의 입력을 받을 수 있도록 gameActive를 true로 변경
        if (!checkGameEnd()) {
            statusLabel.setText("당신의 차례입니다 (" + humanSymbol + ")");
            gameActive = true;
        }
    	} catch (InvalidMoveException e) {
    		System.err.println("로직 오류 발생 (AI): " + e.getMessage()); 
    		statusLabel.setText("게임 오류 발생. (재시작 필요)");
    		gameActive = false; 
    	}
    }
    private void endGame(String message, Color color, char winner) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
        gameActive = false;
        if (winner != ' ') { // ' '는 무승부/빈칸 등의 의미로 사용 (Board.EMPTY를 사용해도 됨)
            updateScore(winner); 
        }
        disableAllButtons();
    }
    
    private boolean checkGameEnd() {
        char winner = board.checkWinner();
        if (winner == humanSymbol) {
            endGame("축하합니다! 당신이 이겼습니다!", new Color(0, 150, 0), humanSymbol);
            return true;
        } else if (winner == aiSymbol) {
            endGame("AI가 이겼습니다!", new Color(200, 0, 0),aiSymbol);
            return true;
        } else if (board.isFull()) {
            endGame("무승부입니다!", new Color(100, 100, 100),' ');
            return true;
        }
            return false; // 게임 계속
    }
    
    // 게임 보드의 버튼을 비활성화
    private void disableAllButtons() {
        for (JButton button : buttons) {
            button.setEnabled(false);
        }
    }
    
    private void resetGame() {
        board.reset(); 
        isClicked = new boolean[9];
        gameActive = (humanSymbol == 'X'); // X가 먼저 시작
        String initialMessage = gameActive ? 
            "당신의 차례입니다 (" + humanSymbol + ")" : 
            "AI의 차례입니다 (" + aiSymbol + ")";
        statusLabel.setText(initialMessage);
        statusLabel.setForeground(Color.BLACK);
        
        for (int i = 0; i < 9; i++) {
            buttons[i].setText("");
            buttons[i].setBackground(Color.WHITE);
            buttons[i].updateUI();
            buttons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            buttons[i].setEnabled(true);
        }
        
        // O를 선택했다면 AI가 먼저 시작
        if (!gameActive) {
        	startAIMoveTimer();
        }
    }
    // 500ms 지연 후 makeAIMove()를 실행하는 타이머 - 자연스러운 움직임을 위함
    private void startAIMoveTimer() {
        Timer timer = new Timer(500, e -> makeAIMove()); 
        timer.setRepeats(false); // 한 번만 실행되도록 설정
        timer.start();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TicTacToeGame game = new TicTacToeGame();
            game.start();
        });
    }
}