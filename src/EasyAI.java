import java.util.List;
import java.util.Random;

class EasyAI implements AIStrategy {
    private String name;
    private Random random;
    
    public EasyAI() {
        this.name = "쉬움 AI";
        this.random = new Random();
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public int makeMove(Board board) {
        List<Integer> availableMoves = board.getAvailableMoves();
        
        if (!availableMoves.isEmpty()) {
            return availableMoves.get(random.nextInt(availableMoves.size()));
        }
        
        return -1;
    }
}