import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.awt.image.*; 
import java.awt.geom.*;
import java.io.*; 
import javax.imageio.*; 

/*BloonInterface.java
 *Shiyang Han (Sean)
 *This file contains the classes than handle the User Interface
 *
 *Notes: I couldn't find a decent drag drop function in swing that
 *suited my needs, so I made this class to handle it. It also gives me
 *more control.*/
 
 /*BloonInterface:
  *This class is the main interface. It takes in events from gamepanel such
  *as mouse clicks and mouse motion and responds accordingly.
  *
  *Also, map is located inside this class, so essentially, this class encapsulates
  *the entire game.*/
  
class BloonInterface {
	private final int MAPX = 833, MAPY = 634;
	private int state = -1;//default state: nothing's going on
	private int mx = 0,my = 0;
	private int id = 0;//this tracks all of the button ids
	private int upgradePrice = -1;//used to check upgrade pricing for selected tower
	private int playerScorePosition = -1;//player's score isn't on the top 10
	private Tower selectedTower = null;//default, nothing's selected
	private String UIMsg = "Click and drag to place a tower.";
	private String[] UIMsgs = new String[]{"Dart Monkey. Decent Attack Speed.","Glue Monkey - Slows bloons in a spash of glue",
											"Cannon - Spash damage, medium range, slow Attack Speed","IceMonkey - Freezes bloons in spot. Will freeze off glue.",
											"Sniper Monkey - Can hit target anywhere on the map.","Super Monkey - Beast Monkey is Beast."};
	private BloonMap map;
	
	//Imaging
	private Image menuPic = new ImageIcon("UI\\TowerMenu.png").getImage();
	private Rectangle2D.Double menuBox = new Rectangle2D.Double(833,0,124,634);
	
	private ArrayList<BloonButton> buttons = new ArrayList<BloonButton>();//buttons
	
	private ArrayList<Integer> highscores = new ArrayList<Integer>();
	
	/*Straight forward constructor. Loads buttons after setting map*/
	public BloonInterface(BloonMap m){
		map = m;
		loadButtons();
	}
	
	/*This method takes in a score and inserts it
	 *if possible into the list of scores. It is assumed that these 
	 *scores come ordered greatest to least*/
	public void loadHighscores(int score){
		try {
			BufferedReader highscoreIn = new BufferedReader(new FileReader("UI\\highscores.txt"));
			for (int i = 0; i < 10; i++){
				highscores.add(Integer.parseInt(highscoreIn.readLine()));
			}
			highscoreIn.close();
			
			//insert score if possible
			for (int i = 0; i < 10; i++){
				if (highscores.get(i) >= score)
					continue;
				highscores.add(i,score);
				playerScorePosition = i;//player is in top 10 :DD
				break;
			}
			
			//output the new scores
			PrintWriter out = new PrintWriter(
							new BufferedWriter(
							new FileWriter("UI\\highscores.txt")));
			
			for (int s: highscores)
				out.write(String.format("%d\n",s));
			out.close();
			
		} catch(IOException ex){
			System.out.println("There was a problem loading the highscores");
			System.out.println(ex.toString());
		}
	}
	
	public void setState(int s){
		state = s;
	}
	
	/*This method loads all buttons that belong to the menu, defined in a
	 *text document found in the buttons folder. The format of the file is as
	 *follows:
	 *Button normal, button hover, button not-available,x,y*/
	public void loadButtons(){
		try{
        	BufferedReader buttIn = new BufferedReader(new FileReader("UI\\Buttons\\info.txt"));
        	int n = Integer.parseInt(buttIn.readLine());
        	for (int i = 0; i < n;i++){
        		ImageIcon normal = new ImageIcon("UI\\Buttons\\"+buttIn.readLine());
				int w = normal.getIconWidth();
				int h = normal.getIconHeight();
				Image picNormal = normal.getImage();
				Image picHover = new ImageIcon("UI\\Buttons\\"+buttIn.readLine()).getImage();
				Image picNotAvail = new ImageIcon("UI\\Buttons\\"+buttIn.readLine()).getImage();
				int x = Integer.parseInt(buttIn.readLine());
				int y = Integer.parseInt(buttIn.readLine());
				addButton(x,y,w,h,new Image[]{picNormal,picHover,picNotAvail});
        	}
		} catch(IOException ex){
			System.out.println("There was a problem loading the UI");
    		System.out.println (ex.toString());
		}
	}
	
	public void addButton(int x,int y,int w,int h,Image[] pics){
		BloonButton b = new BloonButton(x,y,w,h,pics,id++);
		b.setUI(this);
		buttons.add(b);
	}
	
	/*This method handles the motion of the mouse in the UI. It
	 *performs a button.contains on each button, highlighting it if needed*/
	public boolean moveMouse(int mx,int my){
		if (map.getGameState() < 0)
			return false;//make ui irresponsive if game over
		this.mx = mx;
		this.my = my;
		for (BloonButton b: buttons){
			b.contains(mx,my);
		}
		return true;
	}
	
	/*This method handles the clicking of the mouse in the UI
	 *click is a "upward click" if click is false, and a downward click
	 *if click is true. The UI then responds accordingly.*/
	public boolean mouseClick(boolean click){
		if (map.getGameState() < 0)
			return false;//make ui irresponsive if game over
		if (click){//if downclick
			if (!menuBox.contains(mx,my)){
				map.selectTower(mx,my);//attempt to select a tower at position mx,my
				selectedTower = map.getSelectedTower();
				if (selectedTower != null){
					upgradePrice = selectedTower.getUpgradePrice();
				} else {
					upgradePrice = -1;
				}
			}
		}
		if (!click){//if upclick
			if (!menuBox.contains(mx,my)){//if its on the map, attempt to place tower
				int[] needed = new int[]{250,300,350,300,350,1500};
				if (map.canPlace(mx,my)){
					if (state == 0){ // states are set if they clicked on a tower button
						map.addTower(new DartMonkey(mx,my));
					} else if (state == 1){
						map.addTower(new GlueMonkey(mx,my));
					} else if (state == 2){
						map.addTower(new Cannon(mx,my));
					} else if (state == 3){
						map.addTower(new IceMonkey(mx,my));
					} else if (state == 4){
						map.addTower(new SniperMonkey(mx,my));
					} else if (state == 5){
						map.addTower(new SuperMonkey(mx,my));
					}
					
					if (state > -1 && state < 6){
						map.focusRecentTower();
						selectedTower = map.getSelectedTower();
						map.addMoney(-needed[state]);
					}
					state = -1;
				}
			} else {//states 6 7 and 8 are upgrade, sell and nextLevel
				if (state == 6){
					selectedTower = null;
					map.sellSelectedTower();
				} else if (state == 7){
					map.upgradeSelectedTower();
					selectedTower = map.getSelectedTower();
				} 
				if (state == 8){
					map.nextLevel();
				}
			}
		}
		for (BloonButton b: buttons){//set state of buttons
			b.onClick(click);
		}
		return true;
	}
	
	/*This method is what runs the time in the game. If the
	 *game is still active, time flows, otherwise, load highscores
	 *once and never bother with anything else ever again.*/
	public void clockTick(){
		if (map.getUIInfo()[1] == 0 && map.getGameState() > 0){
			map.setGameState(-1);
			loadHighscores(map.getScore());
		} else if (map.getGameState() > 0){
			map.clockTick();
			map.refreshBloons();
			map.move();
			map.resolveEvents();
		} 
	}
	
	/*This method returns true if the buttonid can be pressed.*/
	public boolean canPress(int id){
		int[] UIInfo = map.getUIInfo();
		int userMoney = UIInfo[0];
		int[] needed = new int[]{250,300,350,300,350,1500};
		if (id < 6){//make sure they hav enough money
			if (userMoney >= needed[id])
				return true;
			else 
				return false;
		} else {
			if (selectedTower != null){
				if (id == 7){//make sure they have enough for an upgrade
					return (userMoney >= selectedTower.getUpgradePrice());
				} else if (id == 6){//you can always sell a tower if selected
					return true;
				}
			}
			if (id == 8){//check if they've cleared the level
				return map.nextLevelReady();
			}
		}
		
		return false;
	}
	
	/*If they're placing a tower, we display the "outline" of where
	 *that tower would go, and also if they can place the tower or not.*/
	private void displaySelectedTower(Graphics g,JPanel pane){
		Sprites towerID = Sprites.DARTMONKEY;//default placeholder values
		int range = 100;
		
		if (state == 0){
			towerID = Sprites.DARTMONKEY;
			range = 100;
		} else if (state == 1){
			towerID = Sprites.GLUEMONKEY;
			range = 150;
		} else if (state == 2){
			towerID = Sprites.CANNON;
			range = 150;
		} else if (state == 3){
			towerID = Sprites.ICEMONKEY;
			range = 70;
		} else if (state == 4){
			towerID = Sprites.SNIPERMONKEY;
			range = 2000;
		} else if (state == 5){
			towerID = Sprites.SUPERMONKEY;
			range = 200;
		}
		if (state > -1 && state < 6 && !menuBox.contains(mx,my)){
			//show "outline" of tower at mouse position
			Sprite towerSprite = SpriteSet.getSprite(towerID);
			if (map.canPlace(mx,my))
				g.setColor(new Color(255,255,255,122));
			else//red if they can't place the tower
				g.setColor(new Color(255,0,0,122));
			g.fillOval(mx-range,my-range,range*2,range*2);
			g.setColor(new Color(0,0,0));
			g.drawOval(mx-range,my-range,range*2,range*2);
			towerSprite.draw(0,mx,my,0,g,pane);
		}
	}
	
	public void setUIMsg(int id){
		if (id >= 0 && id <6){
			UIMsg = UIMsgs[id];
		} else if (id == 6){
			UIMsg = "Sell this tower.";
		} else if (id == 7){
			if (selectedTower != null)
				UIMsg = selectedTower.getUIMsg();
		} else if (id == 8){
			UIMsg = "Next Level.";
		} else {
			UIMsg = "Click and drag to place a tower.";
		}

	}
	/*This method draws text onto the given context with colour c,
	 *a font size and a location.*/
	private void drawText(Graphics g,Color c,int fontSize,String text,int x,int y){
		g.setFont(new Font("Segoe Print",Font.BOLD,fontSize));
		//drop shadow effect
		g.setColor(new Color(0,0,0,122));
		g.drawString(text,x,y);
		//draw with colour
		g.setColor(c);
		g.drawString(text,x-2,y-1);
	}
	
	/*This method displays UI info on the screen*/
	private void displayUIText(Graphics g){
		int[] UIInfo = map.getUIInfo();
		//display money and lives
		drawText(g,new Color(30,255,0,200),22,Integer.toString(UIInfo[0]),MAPX+51,81);
		drawText(g,new Color(255,0,0,200),22,Integer.toString(UIInfo[1]),MAPX+72,108);
		if (selectedTower != null){//if they've selected a tower, display sell and buy text
			drawText(g,new Color(255,255,255),16,"Sell:",845,505);
			drawText(g,new Color(255,255,255),16,Integer.toString(selectedTower.getSellPrice()),885,505);
			if (canPress(7)){
				drawText(g,new Color(255,255,255),16,"Upgrade:",820,535);
				drawText(g,new Color(255,255,255),16,Integer.toString(selectedTower.getUpgradePrice()),900,535);
			} else {
				drawText(g,new Color(255,255,255),16,"Need:",845,535);
				drawText(g,new Color(255,255,255),16,Integer.toString(selectedTower.getUpgradePrice()),890,535);
			}
		}
		//display level
		drawText(g,new Color(255,255,255),40,String.format("Level: %d",UIInfo[2]),50,40);
		//display UI text.
		drawText(g,new Color(255,255,255),20,UIMsg,40,610);
	}
	
	/*This method displays the highscores. It overlays the current screen
	 *and shows the scores*/
	public void displayHighscores(Graphics g){
		g.setColor(new Color(0,0,0,122));
		g.fillRect(0,0,957,934);//fill whole screen with translucent black
		drawText(g,new Color(255,0,0),72,"Game Over",255,110);
		drawText(g,new Color(255,255,255),50,"Highscores:",340,160);
		//display scores.
		for (int i = 0; i < 10; i++){
			drawText(g,new Color(255,255,255),45,String.format("%2d. %6d",i+1,highscores.get(i)),345,210+40*i);
		}
		if (playerScorePosition >= 0)//if they're in the highscores
			drawText(g,new Color(255,0,255),45,"<- You",650,210+40*playerScorePosition);
		drawText(g,new Color(0,255,255),35,"Press the x to exit :DD.",290,610);
	}
	
	/*This method is called whenever the game needs to be displayed
	 *displays the entire game, map and all.*/
	public void display(Graphics g,JPanel pane){
		map.display(g,pane);
		displaySelectedTower(g,pane);
		g.drawImage(menuPic,MAPX,0,pane);
		for (BloonButton b: buttons){
			b.draw(g,pane);
		}
		displayUIText(g);
		if (map.getGameState()<0)//if game over, show highscores
			displayHighscores(g);
	}
}

/*BloonButton
 *This is the custom button class of the game. It extends 
 *Rect2D.Double and uses it's contains method.*/
class BloonButton extends Rectangle2D.Double {
	private int id;
	private Image[] pics;//state images
	private boolean onFocus = false;
	private boolean onClick = false;
	private BloonInterface bInterface;
	
	public BloonButton(double x, double y, double w, double h,Image[] pics,int id){
		super(x,y,w,h);
		this.id = id;
		this.pics = pics;
	}
	public void setUI(BloonInterface bInterface){
		this.bInterface = bInterface;
	}
	
	/*This method takes in a boolean representing whether the mouse is
	 *being clicked or released and changes its onClick field
	 *accordingly*/
	public void onClick (boolean click){
		if (click){
			if (onFocus){
				onClick = true;
				if (bInterface.canPress(id))
					bInterface.setState(id);
			}
		} else {
			if (onFocus){
				onClick = false;
			}else{
				onClick = false;
			}
			if (!click){
				bInterface.setState(-1);
			}
				
		}
	}
	
	public void contains(int mx,int my){
		if (super.contains(mx,my)){
			onFocus = true;
			if (bInterface.canPress(id))
				bInterface.setUIMsg(id);
		} else {
			onFocus = false;
			bInterface.setUIMsg(-1);
		}
	}
	
	/*This method draws this button using
	 *an image that represents it's corresponding state.*/
	public void draw(Graphics g,JPanel pane){
		int nx = (int)x;
		int ny = (int)y;
		if (bInterface.canPress(id)){
			if (onFocus && !onClick){
				g.drawImage(pics[1],nx,ny,pane);
				bInterface.setUIMsg(id);
			} else if (onClick){
				g.drawImage(pics[1],nx,ny,pane);
			} else {
				g.drawImage(pics[0],nx,ny,pane);
			}
		} else {//can't click this button
			g.drawImage(pics[2],nx,ny,pane);
		}
	}
}