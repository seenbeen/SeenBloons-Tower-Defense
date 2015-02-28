import java.awt.*;
import java.util.ArrayList;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*; 
import java.io.*; 
import javax.imageio.*; 
/*Game.java
 *Shiyang Han(Sean)
 *This is the file that runs the main game.*/
public class Game extends JFrame implements ActionListener{
	Timer myTimer;   
	GamePanel myGame;
		
    public Game(){
		super("SeenBloon's Tower Def");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(963,662);

		myTimer = new Timer(10, this);	 // trigger every 10 ms
		myTimer.start();

		myGame = new GamePanel();
		add(myGame);

		setResizable(false);
		setVisible(true);
    }

	public void actionPerformed(ActionEvent evt){
		if(myGame!= null && myGame.ready){
			myGame.clockTick();//tick time
			myGame.repaint(); //show current point in time
		}
	}

    public static void main(String[] arguments) throws IOException{
		Game frame = new Game();		
    }
}

class GamePanel extends JPanel implements MouseMotionListener, MouseListener{
	public boolean ready=false;
	private BloonInterface GameUI;
	
	public void clockTick(){
		GameUI.clockTick();
	}
	
	public GamePanel(){ //initialize game variables, assign map to UI
		addMouseMotionListener(this);
		addMouseListener(this);
		BloonMap gameMap = new BloonMap("monkeyStream");
		GameUI= new BloonInterface(gameMap);
		new SpriteSet();
		setSize(957,934);
	}
	
    public void addNotify() {
        super.addNotify();
        requestFocus();
        ready = true;
    }
    
    public void paintComponent(Graphics g){//display the UI
		GameUI.display(g,this);
    }
    
    // ------------ MouseListener ------------------------------------------
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    
    public void mouseReleased(MouseEvent e) {
    	if (e.getButton() == e.BUTTON1){
    		GameUI.mouseClick(false); // transfer mouse actions to game UI
    		GameUI.moveMouse(e.getX(),e.getY());
    	}
    }    
    public void mouseClicked(MouseEvent e){}  
    	 
    public void mousePressed(MouseEvent e){
    	if (e.getButton() == e.BUTTON1)
    		GameUI.mouseClick(true);
	}
    	
    // ---------- MouseMotionListener ------------------------------------------
    public void mouseDragged(MouseEvent e){
    	GameUI.moveMouse(e.getX(),e.getY());
    }
    
    public void mouseMoved(MouseEvent e){
    	GameUI.moveMouse(e.getX(),e.getY());
    }
}