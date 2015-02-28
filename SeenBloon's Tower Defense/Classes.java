import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.awt.image.*; 
import java.awt.geom.*;
import java.io.*; 
import javax.imageio.*; 
/*Classes.java
 *Shiyang Han (Sean)
 *
 *This file contains the source code of all classes needed to
 *run SeenBloon's Tower Defense.
 *
 *General conventions:
 *-if it starts with p, (ie: pSomething), it has to do with a projectile
 *-if it starts with b, bloon, t -> tower, e ->effect*/
 
 
/*An enumeration is used throughout the code as an identifier
 *for sprite image by any object that wants to use a sprite image*/
enum Sprites{//Towers:
			 DARTMONKEY,DARTMONKEY2,DARTMONKEY3,GLUEMONKEY,GLUEMONKEY2,GLUEMONKEY3,
			 CANNON,CANNON2,CANNON3,ICEMONKEY,ICEMONKEY2,ICEMONKEY3,
			 SNIPERMONKEY,SNIPERMONKEY2,SNIPERMONKEY3,SUPERMONKEY,
			 SUPERMONKEY2,SUPERMONKEY3,
			 //Projectiles:
			 PDART,PGLUE,PCORROGLUE,PBOMB,PMISSILE,PMOABMISSILE,
			 PLASER,PPLASMA,
			 //Bloons:
			 REDBLOON,BLUEBLOON,GREENBLOON,YELLOWBLOON,RAINBOWBLOON,
			 //Effects:
			 GLUESPLAT,CORROSPLAT,SMALLBOOM,MEDBOOM,LARGEBOOM,
			 SMALLICE,MEDICE,LARGEICE,BLOONBURST
			 };

/*SpriteSet
 *This class is used as a global sprite-getter to prevent the constant reloading of 
 *sprites each time displaying is required as that would be very costly.*/
class SpriteSet{
	private static ArrayList<Sprite> allSprites = new ArrayList<Sprite>();
	
	/*Method: Constructor
	 *Parameters: none
	 *
	 *Load in all sprite images from all assets*/
	public SpriteSet(){
		try {
			load("sprites\\Towers\\",18);
			load("sprites\\Projectiles\\",8);
			load("sprites\\Bloons\\",5);
			load("sprites\\Effects\\",9);
	    } catch(IOException ex){
	    	System.out.println("There was a problem loading the sprites");
        	System.out.println (ex.toString());
    	}
	}
	
	/*Method: load
	 *Parameters: A path and the number of sprites located in the path
	 *
	 *This method loads in all sprites located at the path and number given
	 *and stores the frames into an arraylist of sprite objects.
	 *
	 *Note: Towers are treated differently as they have extra projectile properties.
	 **/
	private static void load(String path, int numSprites) throws IOException{
		ImageIcon TempIcon = new ImageIcon();
		//parse through each sprite directory in the given path
		for (int i = 0; i < numSprites; i++){
			//Load in info file
			
			//Note: info file contains the number of frames in this animation, 
			//the x and y offsets as well as projectile offsets and frame offset if
			//the sprite is a tower sprite
			BufferedReader in = new BufferedReader(new FileReader(path+i+"\\info.txt"));
			ArrayList<Image> temp = new ArrayList<Image>();
			String type = in.readLine();
			int n = Integer.parseInt(in.readLine());
			int offX = Integer.parseInt(in.readLine());
			int offY = Integer.parseInt(in.readLine());
			
			//parse through each individual frame
			for (int j = 0; j < n; j++){
				TempIcon = new ImageIcon(String.format("%s%d\\%04d.png",path,i,j+1));
				temp.add(TempIcon.getImage());
			}
			
			if (type.equals("Tower")){
				//add additional info if we have a tower sprite
				int pOffX = Integer.parseInt(in.readLine());
				int pOffY = Integer.parseInt(in.readLine());
				int pFrame = Integer.parseInt(in.readLine())-1;
				allSprites.add(new Sprite(temp,offX,offY,pOffX,pOffY,pFrame));
			} else {
				allSprites.add(new Sprite(temp,offX,offY));
			}
		}
	}
	/*Method: getSprite
	 *Parameters: an enumeration representing sprite identification
	 *
	 *This method RETURNS a sprite. All towers use this to prevent constant reloading.*/
	public static Sprite getSprite(Sprites n){
		return allSprites.get(n.ordinal());
		//Side Notes: ordinal() is rather frowned upon. Seems to work fine.
	}
}

/*Sprite
 *This class takes care of all sprite drawing. Any class that uses sprite imaging
 *will have a sprite object returned by the SpriteSet class. This class contains an
 *overloaded constructor for tower sprite use and regular sprite use.*/
class Sprite {
	private ArrayList<Image> frames;//frames this sprite has
	private int offX,offY,pFrame = 0; //general location info + projectile offsets
	double pAngleOffset = 0,pOffset = 0;
	
	/*Method: Constructor
	 *Parameters: array of frame images, general location information, projectile frame
	 *
	 *This method constructs a sprite object using the information passed in.
	 *->for tower sprite use.*/
	public Sprite(ArrayList<Image> frames,int offX,int offY,int pOffX,int pOffY,int pFrame){
		this.frames = frames;
		this.offX = offX;
		this.offY = offY;
		int dx = pOffX-offX;
		int dy = pOffY-offY;
		pOffset = Math.hypot(dx,dy);
		if (pOffset != 0)//prevent undefined slope
			this.pAngleOffset = Math.atan2(dy,dx);
		this.pOffset = pOffset;
		this.pFrame = pFrame;
	}
	/*Method: Overloaded Constructor
	 *Parameters: general location information only
	 *
	 *This method constructs a sprite object using the information passed in.
	 *->For general animation use*/
	public Sprite(ArrayList<Image> frames,int offX,int offY){
		this.frames = frames;
		this.offX = offX;
		this.offY = offY;
	}
	
	/*Method: draw
	 *Parameters: a frame id, location, angle of direction, and graphics and JPanel context
	 *
	 *This method draws onto the graphics context given an image of the frame given
	 *at x,y rotated at the angle specified.*/
	public void draw(int frameId,int x,int y,double direction,Graphics g,JPanel pane){
		if (direction != 0){//only rotate if needed
			//rotate world space
			Graphics2D g2D = (Graphics2D)g;
			AffineTransform saveXform = g2D.getTransform();
			AffineTransform at = new AffineTransform();
			at.rotate(direction,x,y);
			g2D.transform(at);
			g2D.drawImage(frames.get(frameId),x-offX,y-offY,pane);//blit on image
			g2D.setTransform(saveXform);//rotate back
		} else {
			g.drawImage(frames.get(frameId),x-offX,y-offY,pane);
		}
	}
	
	/*Methods that return information:
	 *-pretty self explanatory.*/
	public double getPAngleOffset(){
		return pAngleOffset;
	}
	public double getPOffset(){
		return pOffset;
	}
	public int getPFrame(){
		return pFrame;
	}
	public int size(){
		return frames.size();
	}
}

/*BPoint
 *This class is used as a "packing" tool to contain all points of location.
 *I choose to use double to increase precision when calculating velocity.*/
class BPoint{
	private double x,y;
	/*Method: Constructor
	 *Parameters: a location
	 *
	 *The method simply stores the info*/
	public BPoint(double x, double y){
		this.x = x;
		this.y = y;
	}
	/*Method: setLocation
	 *Parameters: a new location
	 *
	 *The method changes the location of this BPoint to the given coords.*/
	public void setLocation(double x, double y){
		this.x = x;
		this.y = y;
	}
	/*Method: distance
	 *Parameters: another BPoint
	 *
	 *This method calculates and returns the distance between this BPoint
	 *and the given BPoint.*/
	public double distance(BPoint pt){
		return Math.hypot(pt.x-x,pt.y-y);
	}
	
	/*Methods that return field information.*/
	public double getX(){
		return x;
	}
	public double getY(){
		return y;
	}
	public String toString(){//for debugging use
		return String.format("%.2f,%.2f",x,y);
	}
}

/*~~~~~~~~~~~~~~~~~~~~~~Towers: ~~~~~~~~~~~~~~~~~~~~~*/
/*Tower:
 *This is a general abstract class template that towers are based upon.
 *Towers must implement their own method of getProjectile, getUpgradedTower and getUIMsg.
 *The class includes methods to attack, and interact with the stored BloonMap*/
 
abstract class Tower{
	//Fields:
	protected int lastAttack = 0,attackRate,range,id,price,sellPrice,frame = 0,frameTimer = 3;
	protected Sprite tSprite = new Sprite(new ArrayList<Image>(),-1,-1);
	protected boolean attacking = false;
	protected BPoint pos;
	protected BloonMap map;//gateway to interaction with main game
	protected Bloon target = null;//initialize to null, contains the current bloon target
	protected double direction = 0;
	
	/*Method: Constructor
	 *The constructor of tower takes in all information regarding the tower's position, range
	 *sprites as well as User Interface information such as pricing and creates a new tower.*/
	public Tower(int attackRate,int x,int y,int range,int price,int sellPrice,Sprites spriteID){
		this.attackRate = attackRate;
		this.pos = new BPoint(x,y);
		this.range = range;
		this.price = price;
		this.sellPrice = sellPrice;
		this.tSprite = SpriteSet.getSprite(spriteID);
	}
	
	/*Abstract Methods:*/
	public abstract Projectile getProjectile();//returns the projectile this tower fires
	public abstract Tower getUpgradedTower();//returns the next tier
	public abstract String getUIMsg();//returns upgrade comment
	
	/*Graphical Methods:*/
	/*Method: showRange
	 *Parameters: graphics context
	 *
	 *This method displays a translucent, white circle of "range"
	 *representing the tower's range ability.*/
	public void showRange(Graphics g){
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		g.setColor(new Color(255,255,255,122));
		g.fillOval(x-range,y-range,range*2,range*2);
		g.setColor(new Color(0,0,0));
		g.drawOval(x-range,y-range,range*2,range*2);
	}
	
	/*Method: draw
	 *Parameters: Graphics context, JPanel context
	 *
	 *This is the method used when displaying the tower. It
	 *draws to the given context this tower at it's position
	 *and angle of rotation*/
	public void draw (Graphics g,JPanel pane){
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		tSprite.draw(frame,x,y,direction,g,pane);
	}
	
	/*Methods regarding frame animation:*/
	/*Method: advanceFrame
	 *Parameters: none
	 *
	 *This method handles all frame advancing and as a result,
	 *when an attack is launched. It advances a frame of animation
	 *only when the frame timer hits 0 (ie: every 30 millis or so)
	 *and launches an attack when the frame of attack (provided by the sprite)
	 *is reached.*/
	public void advanceFrame(){
		if (frameTimer == 0){//advance frame if possible
			frame += 1;
			frameTimer = 3;
		}
		frameTimer = Math.max(0,frameTimer-1);
		if (frame == tSprite.getPFrame() && frameTimer == 0){//add attack if possible
			map.addEvent(new BloonMapEvent(BloonMapEvent.ADD_PROJ,id));
		}
		if (frame == tSprite.size()){//reset the frames if we've hit the end
			attacking = false;
			lastAttack = attackRate;
			frame = 0;
		}
	}
	
	/*Method: updateDirection
	 *Parameters: none
	 *
	 *This method updates the direction of the current target.
	 *To avoid a null pointer, it's only called when a target is guaranteed
	 *to exist.*/
	public void updateDirection(){
		double[] bLocation = target.near(pos,9999);//in other words, use it to get dx and dy
		if (bLocation != null)
			direction = Math.atan2(bLocation[1],bLocation[0]);
	}
	/*Method: move
	 *Parameters: none
	 *
	 *This method essentially handles the "AI" of the tower.
	 *If it can attack again and an enemy exists, put it into attack mode
	 *for advanceFrame to handle. Otherwise, keep scanning for a target.*/
	public void move(){
		if (attacking){
			advanceFrame();
			updateDirection();//only called when a target exists
		} else {
			for (Bloon b: map.getBloons()){
				double[] bLocation = b.near(pos,range);
				if (bLocation != null && lastAttack == 0){//if possible to attack, launch an attack
					attacking = true;
					target = b;
					break;
				}
			}
		}
		lastAttack = Math.max(0,lastAttack-1);//lastAttack countdown
	}
	/*Method: collidePoint
	 *Parameters: mx,my, distance
	 *Returns true if the mouse is within this tower's radius (for clicking)
	 *and false if not.*/
	public boolean collidePoint(int mx,int my,int d){
		return ( new BPoint(mx,my).distance(pos) ) < d;
	}
	
	/*Method: setMap
	 *Parameters: BloonMap
	 *
	 *Sets the bloonMap context*/
	public void setMap(BloonMap map){
		this.map = map;
		this.id = map.nextID(BloonMap.TOWER);
	}
	
	/*Methods that return information:*/
	public int getUpgradePrice(){
		return price;
	}
	
	public int getSellPrice(){
		return sellPrice;
	}
	public int getID(){
		return id;
	}
	
	/*Method: getPLocation
	 *Parameters: none
	 *
	 *This method returns the location of where a launched
	 *projectile would start from if this tower launched it.
	 *It is only called by the class's subclasses*/
	protected BPoint getPLocation(){
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		double pRadius = tSprite.getPOffset();
		double angOffset = tSprite.getPAngleOffset();
		double nx = x+Math.cos(direction+angOffset)*pRadius;
		double ny = y+Math.sin(direction+angOffset)*pRadius;
		return new BPoint(nx,ny);
	}
}

/*~~~~~Specific Towers~~~~~~~~~*/
/*All base towers have an overloaded constructor.
 *This allows for more methods to be added later to each base tower
 *and also have them carried over to its upgraded towers while
 *allowing its upgraded towers full control over specific tower stats.
 *
 *The first constructor is for general use (provide x y only)
 *The second allows each subclass to directly provide their own details
 *for stats.
 *
 *I will not be noting this for the towers.*/

/*DartMonkey:
 *Dart Monkey Base Tower. It throws a dart at a relatively slow rate.*/
class DartMonkey extends Tower{
	/*Constructors: Refer to header.*/
	public DartMonkey(int x, int y){
		super(60,x,y,100,250,150,Sprites.DARTMONKEY);
	}
	public DartMonkey(int attackRate,int x,int y,int range,int price,int sellPrice,Sprites spriteID){
		super(attackRate,x,y,range,price,sellPrice,spriteID);
	}
	/*Implementation of getProjectile; return a new PDart with
	 *a velocity and direction of towards the current target.*/
	public Projectile getProjectile(){
		BPoint pPos = getPLocation();
		int x = (int)pPos.getX();
		int y = (int)pPos.getY();
		double[] bLocation = target.near(pPos,9999);//plug in 9999 to get dx and dy
		return new PDart(x,y,bLocation[0],bLocation[1]);
	}
	/*Implementation of getUpgradedTower; return a second-tier
	 *DartMonkey object.*/
	public Tower getUpgradedTower(){
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new DartMonkey2(x,y);
	}
	public String getUIMsg(){
		return "Increased range and Attack Speed on next Upgrade.";
	}
}
/*DartMonkey2:
 *Dart Monkey second tier. Increased range and attack speed.*/
class DartMonkey2 extends DartMonkey{
	//Constructor: Refer to header
	public DartMonkey2(int x, int y){
		super(40,x,y,130,350,350,Sprites.DARTMONKEY2);
	}
	/*Inherets getProjectile as the projectile doesn't change.*/
	
	/*Re-Implementation of getUpgradedTower; return a third-tier
	 *DartMonkey object*/
	public Tower getUpgradedTower(){
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new DartMonkey3(x,y);
	}
}
/*DartMonkey3:
 *Final tier of dartmonkey. Further range, attack speed.*/
class DartMonkey3 extends DartMonkey{
	//Constructor: Refer to header
	public DartMonkey3(int x, int y){
		super(20,x,y,160,99999,450,Sprites.DARTMONKEY3);
	}
	/*Inherets getProjectile*/
	
	/*Re-Implementation of getUpgradedTower -> I use a placeholder for future possibilities*/
	public Tower getUpgradedTower(){//placeholder for future additions
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new DartMonkey(x,y);
	}
	public String getUIMsg(){
		return "No more Upgrades Available.";
	}
}

/*GlueMonkey:
 *Glue monkey base tower. Projectiles splash glue at targets to slow them down.
 *Corrosive glue will pop bloons after 3 seconds.
 *Relative slow starting speed, but wide range.*/
class GlueMonkey extends Tower{
	//Constructors: Refer to Header
	public GlueMonkey(int x, int y){
		super(60,x,y,150,300,200,Sprites.GLUEMONKEY);
	}
	public GlueMonkey(int attackRate,int x,int y,int range,int price,int sellPrice,Sprites spriteID){
		super(attackRate,x,y,range,price,sellPrice,spriteID);
	}
	//Implementation of getProjectile; returns a PGlue object directed towards its target
	public Projectile getProjectile(){
		BPoint pPos = getPLocation();
		int x = (int)pPos.getX();
		int y = (int)pPos.getY();
		double[] bLocation = target.near(pPos,9999);
		return new PGlue(x,y,bLocation[0],bLocation[1]);
	}
	
	/*Implementation of getUpgradedTower; return a second-tier
	 *GlueMonkey*/
	public Tower getUpgradedTower(){
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new GlueMonkey2(x,y);
	}
	public String getUIMsg(){
		return "Increased range and Attack Speed on next Upgrade.";
	}
}

/*GlueMonkey2
 *Increased range and attack rate.*/
class GlueMonkey2 extends GlueMonkey{
	//Constructor: Refer to Header
	public GlueMonkey2(int x, int y){
		super(40,x,y,200,400,400,Sprites.GLUEMONKEY2);
	}
	/*Inherited implmentation from getProjectile (projectile stays the same)*/
	
	/*Re-Implemented version of getUpgradedTower; return a third-tier
	 *GlueMonkey*/
	public Tower getUpgradedTower(){
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new GlueMonkey3(x,y);
	}
	public String getUIMsg(){
		return "Corrosive Glue melts bloons after a period on next Upgrade.";
	}
}

/*GlueMonkey3
 *Increased range and attack rate, spurts corrosive glue.*/
class GlueMonkey3 extends GlueMonkey{
	//Constructor: Refer to Header
	public GlueMonkey3(int x, int y){
		super(30,x,y,250,99999,600,Sprites.GLUEMONKEY3);
	}
	/*Re-Implemented getProjectile to return corroGlue*/
	public Projectile getProjectile(){
		BPoint pPos = getPLocation();
		int x = (int)pPos.getX();
		int y = (int)pPos.getY();
		double[] bLocation = target.near(pPos,9999);
		return new PCorroGlue(x,y,bLocation[0],bLocation[1]);
	}
	/*Re-Implemented getUpgradedTower -> Placeholder for future possibilities*/
	public Tower getUpgradedTower(){//placeholder
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new DartMonkey(x,y);
	}
	public String getUIMsg(){
		return "No more Upgrades Available.";
	}
}

/*Cannon
 *Cannon base class, hurls exploding projectiles that have splash damage.
 *Decent range, slow attack rate.
 *
 *Note: As tiers move up, projectiles have more splash range.*/
class Cannon extends Tower{
	//Constructors: Refer to Header
	public Cannon(int x, int y){
		super(90,x,y,150,300,225,Sprites.CANNON);
	}
	public Cannon(int attackRate,int x,int y,int range,int price,int sellPrice,Sprites spriteID){
		super(attackRate,x,y,range,price,sellPrice,spriteID);
	}
	/*Implementation of getProjectile; returns a PBomb object directed towards its target*/
	public Projectile getProjectile(){
		BPoint pPos = getPLocation();
		int x = (int)pPos.getX();
		int y = (int)pPos.getY();
		double[] bLocation = target.near(pPos,9999);
		return new PBomb(x,y,bLocation[0],bLocation[1]);
	}
	/*Implementation of getUpgradedTower; returns a second-tier
	 *Cannon*/
	public Tower getUpgradedTower(){
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new Cannon2(x,y);
	}
	public String getUIMsg(){
		return "Increased range, Attack Speed and Splash Range on next Upgrade.";
	}
}

/*Cannon2
 *Second-Tier Cannon
 *Increased range, attack range, larger splash damage*/
class Cannon2 extends Cannon{
	//Constructor: Refer to Header
	public Cannon2(int x, int y){
		super(60,x,y,200,500,425,Sprites.CANNON2);
	}
	/*Re-Implementation of getProjectile; returns a PMissile object instead*/
	public Projectile getProjectile(){
		BPoint pPos = getPLocation();
		int x = (int)pPos.getX();
		int y = (int)pPos.getY();
		double[] bLocation = target.near(pPos,9999);
		return new PMissile(x,y,bLocation[0],bLocation[1]);
	}
	
	/*Re-Implementation of getUpgradedTower; returns a Third-Tier
	 *Cannon*/
	public Tower getUpgradedTower(){
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new Cannon3(x,y);
	}
	public String getUIMsg(){
		return "Increased range, Attack Speed and Splash Range on next Upgrade.";
	}
}

/*Cannon3
 *Final Tier of Cannon, larger range, splash range and attack rate*/
class Cannon3 extends Cannon{
	//Constructor: Refer to Header
	public Cannon3(int x, int y){
		super(30,x,y,250,99999,725,Sprites.CANNON3);
	}
	/*Re-implementation of getProjectile; returns a PMoabMissile instead*/
	public Projectile getProjectile(){
		BPoint pPos = getPLocation();
		int x = (int)pPos.getX();
		int y = (int)pPos.getY();
		double[] bLocation = target.near(pPos,9999);
		return new PMoabMissile(x,y,bLocation[0],bLocation[1]);
	}
	/*Re-implementation of getUpgradedTower() -> Placeholder for future possiblities*/
	public Tower getUpgradedTower(){//placeholder
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new DartMonkey(x,y);
	}
	public String getUIMsg(){
		return "No more Upgrades Available.";
	}
}

/*IceMonkey
 *IceMonkey base tower:
 *Freezes bloons to a stop with its area for a short
 *period of time.*/
class IceMonkey extends Tower{
	//Constructor: Refer to header
	public IceMonkey(int x, int y){
		super(70,x,y,70,200,200,Sprites.ICEMONKEY);
	}
	public IceMonkey(int attackRate,int x,int y,int range,int price,int sellPrice,Sprites spriteID){
		super(attackRate,x,y,range,price,sellPrice,spriteID);
	}
	/*Re-implementation of move, as icemonkeys don't turn when they attack*/
	public void move(){
		super.move();
		direction = 0;
	}
	/*Implementation of getProjectile(); returns a
	 *PSmallIce projectile*/
	public Projectile getProjectile(){
		BPoint pPos = getPLocation();
		int x = (int)pPos.getX();
		int y = (int)pPos.getY();
		double[] bLocation = target.near(pPos,9999);
		return new PSmallIce(x,y,range);
	}
	/*Implementation of getUpgradedTower; returns a tier-two
	 *Icemonkey*/
	public Tower getUpgradedTower(){
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new IceMonkey2(x,y);
	}
	public String getUIMsg(){
		return "Increased range, Attack Speed and Freeze Time on next Upgrade.";
	}
}

/*IceMonkey2
 *Second-Tier IceMonkey
 *Increased freeze time, range and attack rate.*/
class IceMonkey2 extends IceMonkey{
	//Constructor: Refer to Header
	public IceMonkey2(int x, int y){
		super(50,x,y,100,400,350,Sprites.ICEMONKEY2);
	}
	/*Re-implementation of getProjectile -> Returns a
	 *PMedIce projectile.*/
	public Projectile getProjectile(){
		BPoint pPos = getPLocation();
		int x = (int)pPos.getX();
		int y = (int)pPos.getY();
		double[] bLocation = target.near(pPos,9999);
		return new PMedIce(x,y,range);
	}
	/*Re-implemenation of getUpgradedTower; returns a 
	 *Third-Tier IceMonkey*/
	public Tower getUpgradedTower(){
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new IceMonkey3(x,y);
	}
	public String getUIMsg(){
		return "Increased range, Attack Speed and Freeze Time on next Upgrade.";
	}
}

/*IceMonkey3
 *Final-Tier of IceMonkey
 *Increased freeze time, range and attack rate*/
class IceMonkey3 extends IceMonkey{
	//Constructor: Refer to Header
	public IceMonkey3(int x, int y){
		super(30,x,y,140,99999,500,Sprites.ICEMONKEY3);
	}
	
	/*Re-implementation of getProjectile ->Returns a 
	 *PLargeIce projectile*/
	public Projectile getProjectile(){
		BPoint pPos = getPLocation();
		int x = (int)pPos.getX();
		int y = (int)pPos.getY();
		double[] bLocation = target.near(pPos,9999);
		return new PLargeIce(x,y,range);
	}
	
	/*Re-implementation of getUpgradedTower; -> Placeholder for future possibilities*/
	public Tower getUpgradedTower(){//placeholder
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new DartMonkey(x,y);
	}
	public String getUIMsg(){
		return "No more Upgrades Available.";
	}
}

/*SniperMonkey
 *SniperMonkey base class.
 *Infinite range, but relative slow attack speed.*/
class SniperMonkey extends Tower{
	//Constructors: Refer to Header
	public SniperMonkey(int x, int y){
		super(80,x,y,2000,350,225,Sprites.SNIPERMONKEY);
	}
	public SniperMonkey(int attackRate,int x,int y,int range,int price,int sellPrice,Sprites spriteID){
		super(attackRate,x,y,range,price,sellPrice,spriteID);
	}
	
	/*Implementation of getProjectile; Returns a PSnipeBullet 
	 *projectile at the place of the target*/
	public Projectile getProjectile(){
		BPoint bPos = map.getPos(target.getPos()/10-1);//-1 to avoid null pointer
		int x = (int)bPos.getX();
		int y = (int)bPos.getY();
		return new PSnipeBullet(x,y);
	}
	/*Implementation of getUpgradedTower; Returns a second-tier
	 *SniperMonkey*/
	public Tower getUpgradedTower(){
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new SniperMonkey2(x,y);
	}
	public String getUIMsg(){
		return "Increased Attack Speed on next Upgrade.";
	}
}

/*SniperMonkey2
 *Second-tier of SniperMonkey
 *Increased attack rate.*/
class SniperMonkey2 extends SniperMonkey{
	//Constructor: Refer to Header
	public SniperMonkey2(int x, int y){
		super(40,x,y,2000,450,450,Sprites.SNIPERMONKEY2);
	}
	/*Re-Implementation of getUpgradedTower; Returns a third-tier
	 *SniperMonkey*/
	public Tower getUpgradedTower(){
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new SniperMonkey3(x,y);
	}
	public String getUIMsg(){
		return "Increased Attack Speed on next Upgrade.";
	}
}

/*SniperMonkey3
 *Final-tier of SniperMonkey
 *Increased attack rate.*/
class SniperMonkey3 extends SniperMonkey{
	//Constructor: Refer to Header
	public SniperMonkey3(int x, int y){
		super(25,x,y,2000,99999,700,Sprites.SNIPERMONKEY3);
	}
	/*Re-Implementation of getUpgradedTower -> Placeholder for future possibilities*/
	public Tower getUpgradedTower(){//placeholder
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new DartMonkey(x,y);
	}
	public String getUIMsg(){
		return "No more Upgrades Available.";
	}
}

/*SuperMonkey
 *Base Class of Supermonkey.
 *Basically ultimate tower, spits out bullets.*/
class SuperMonkey extends Tower{
	//Constructors: Refer to Header
	public SuperMonkey(int x, int y){
		super(4,x,y,200,1200,350,Sprites.SUPERMONKEY);
	}
	public SuperMonkey(int attackRate,int x,int y,int range,int price,int sellPrice,Sprites spriteID){
		super(attackRate,x,y,range,price,sellPrice,spriteID);
	}
	/*Re-implementation of move
	 *Because supermonkeys tend to attack regardless of frame id,
	 *I reimplemented the animation and bullet attack to be simultaneous.*/
	public void move(){
		boolean attacked= false;
		for (Bloon b: map.getBloons()){//scan
			double[] bLocation = b.near(pos,range);
			if (bLocation != null){
				attacked = true;//if we've found one, attack if possible
				target = b;
				if (lastAttack == 0){//limit attack time (a few milliseconds)
					if (frameTimer == 0){
						frame = (frame+1)%tSprite.size();
						if (frame == 0)//Frame 0 of the animation shows the monkey idle. Get rid of it.
							frame = 1;
						frameTimer = 3;//Refresh Frame Timer to 3
					}
					frameTimer = Math.max(0,frameTimer-1);
					
					direction = Math.atan2(bLocation[1],bLocation[0]);
					map.addEvent(new BloonMapEvent(BloonMapEvent.ADD_PROJ,id));
					lastAttack = attackRate;
					break;//Target first bloon you see only.
				}
			}
		}
		if (!attacked){//To smooth the animation out, I set an attacked flag. 
						//If the monkey doesnt continuously attack, break the animation
			frame = 0;
		}
		lastAttack = Math.max(0,lastAttack-1);
	}
	
	/*Implementation of getProjectile; returns a 
	 *PDart projectile directed at the target*/
	public Projectile getProjectile(){
		BPoint pPos = getPLocation();
		int x = (int)pPos.getX();
		int y = (int)pPos.getY();
		double[] bLocation = target.near(pPos,9999);
		return new PDart(x,y,bLocation[0],bLocation[1]);
	}
	
	/*Implementation of getUpgradedTower; returns a
	 *second-tier SuperMonkey*/
	public Tower getUpgradedTower(){
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new SuperMonkey2(x,y);
	}
	
	public String getUIMsg(){
		return "Increased Range and Lasers on next Upgrade.";
	}
}

/*SuperMonkey2
 *Second-Tier Supermonkey
 *Increased Range, cooler bullets xD*/
class SuperMonkey2 extends SuperMonkey{
	//Constructor: Refer to Header
	public SuperMonkey2(int x,int y){
		super(4,x,y,250,3000,700,Sprites.SUPERMONKEY2);
	}
	/*Re-implementation of getProjectile; returns a 
	 *PLaser projectile directed at the target*/
	public Projectile getProjectile(){
		BPoint pPos = getPLocation();
		int x = (int)pPos.getX();
		int y = (int)pPos.getY();
		double[] bLocation = target.near(pPos,9999);
		return new PLaser(x,y,bLocation[0],bLocation[1]);
	}
	/*Implementation of getUpgradedTower; returns a 
	 *Third-Tier SuperMonkey*/
	public Tower getUpgradedTower(){
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new SuperMonkey3(x,y);
	}
	public String getUIMsg(){
		return "Increased Attack Speed,Range, and Plasma on next Upgrade.";
	}
}

/*SuperMonkey3
 *Final Tier SuperMonkey
 *The beast of all beasts lol. Super attack rate (every clock tick)*/
class SuperMonkey3 extends SuperMonkey{
	//Constructor: Refer to Header
	public SuperMonkey3(int x,int y){
		super(2,x,y,300,99999,1600,Sprites.SUPERMONKEY3);
	}
	
	/*Re-implementation of getProjectile; returns a 
	 *PPlasma projectile directed at the target*/
	public Projectile getProjectile(){
		BPoint pPos = getPLocation();
		int x = (int)pPos.getX();
		int y = (int)pPos.getY();
		double[] bLocation = target.near(pPos,9999);
		return new PPlasma(x,y,bLocation[0],bLocation[1]);
	}
	/*Implementation of getProjectile ->Place holder for future Possiblities*/
	 	public Tower getUpgradedTower(){//placeholder
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new DartMonkey(x,y);
	}
	public String getUIMsg(){
		return "No more Upgrades Available.";
	}
}


/*Begin Projectiles ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

/*Projectile
 *Abstract Projectile class template that all projectiles must follow.
 *Each projectile must implement it's own getEffect to return an effect 
 *as a result of collision*/
abstract class Projectile {	
	//Fields:						
	protected Sprite pSprite;
	protected BPoint pos;
	protected int id,dmg,rad,frame = 0,frameTimer = 3;
	protected double dx,dy,direction;
	protected BloonMap map;
	protected boolean dead = false,addEffect = false;
	//Note: addEffect flag used to determine whether an effect occurs
	//ie: bombs, don't explode when they leave the screen.
	
	/*Method: Constructor
	 *Parameters: general location, velocity sprites,hit radius and damage
	 *
	 *This method constructs a projectile using the given information. It also allows for
	 *the option of no image.*/
	public Projectile(int x,int y,double dx,double dy,int rad,int dmg,Sprites spriteID){
		this.pos = new BPoint(x,y);
		this.dx = dx;
		this.dy = dy;
		this.rad = rad;
		this.dmg = dmg;
		this.direction = Math.atan2(dy,dx);
		//choose whether this projectile is visible or not
		if (spriteID != null)
			this.pSprite = SpriteSet.getSprite(spriteID);
		else
			this.pSprite = null;
	}
	/*Abstract Methods:*/
	/*Method: getEffect
	 *Parameters: None
	 *
	 *Returns: the projectiles collision effect*/
	public abstract Effect getEffect();
	
	/*Method: draw
	 *Parameters: Graphics and JPanel Context
	 *
	 *Draws this projectile onto the given context*/
	public void draw (Graphics g,JPanel pane){
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		if (pSprite != null)//make sure image exists; null referencing can be dangerous...
			pSprite.draw(frame,x,y,direction,g,pane);
	}
	/*Method: move
	 *Parameters: None
	 *
	 *Moves the projectile along it's velocity. If it's exited the track,
	 *kill it with false; no effect is played*/
	public void move(){
		if (pos.getX() < 0 || pos.getX() > 833 ||pos.getY() < 0 || pos.getY() > 634)
			die(false);
		else
			pos.setLocation(pos.getX()+dx*30,pos.getY()+dy*30);
		if (frameTimer == 0){//animate every 30 millis or so
			frameTimer = 3;
			if (pSprite != null)//make sure the sprite exists
				frame = (frame+1)%pSprite.size();
		}
		frameTimer = Math.max(0,frameTimer-1);
	}
	
	/*Method: die
	 *Parameters: is there a collision?
	 *
	 *If there is a collision, flag the addEffect to true so an effect is played.
	 *Sends the map a message to remove this projectile*/
	public void die(boolean collision){
		map.addEvent(new BloonMapEvent(BloonMapEvent.REMOVE_PROJ,id));
		dead = true;//prevent multi-kill after this projectile is dead
		addEffect = collision;//if collision, play effect
	}
	
	/*Bloon-Modifier Effects*/
	
	/*Method: explode
	 *Parameters: range of explosion
	 *
	 *Essentially explodes; killing all bloons within the radius*/
	public void explode(int range){
		int count = 0;
		for (Bloon b: map.getBloons()){
			//if (count > 14)
			//	break;
			if (b.near(pos,range) != null && !b.isDead())//make sure bloon is alive
				b.die(false);
			count++;
		}
	}
	/*Method: freeze
	 *Parameters: range of effect and freeze time
	 *
	 *Essentailly freezes all bloons within the radius for the given time*/
	public void freeze(int range,int time){
		for (Bloon b: map.getBloons()){
			if (b.near(pos,range) != null && !b.isDead() && b.validState(3))//make sure their not already frozen
				b.setState(Bloon.FREEZE,time);
		}
	}
	
	/*Method: glue
	 *Parameters: range of effect
	 *
	 *Glues all bloons within the given radius.
	 *NOTE: Freeze will freeze off glue.*/
	public void glue(int range){
		for (Bloon b: map.getBloons()){
			if (b.near(pos,range) != null && !b.isDead() && b.validState(1))
				b.setState(Bloon.GLUE,-1);
		}
	}
	
	/*Method: corroGlue
	 *Parameters: range of effect, time-till-death
	 *
	 *Glues all bloons with a death-timer*/
	public void corroGlue(int range,int time){
		for (Bloon b: map.getBloons()){
			if (b.near(pos,range) != null && !b.isDead() && b.validState(2))
				b.setState(Bloon.CORROGLUE,time);
		}
	}
	
	/*Method: setMap
	 *Parameters: BloonMap
	 *
	 *Sets the bloonMap context*/
	public void setMap(BloonMap map){
		this.map = map;
		this.id = map.nextID(map.PROJ);
	}
	
	/*Methods that return information*/
	public boolean isDead(){
		return dead;
	}
	public int getID(){
		return id;
	}
	public BPoint getPos(){
		return pos;
	};
	public int getRad(){
		return rad;
	};
}

/*Game Projectiles 
 *Pretty self explanatory
 *
 *I will only be denoting any additions out of the ordinary implementation
 *where it is assumed that an effect is added.*/
class PDart extends Projectile{
	public PDart(int x,int y,double dx,double dy){
		super(x,y,dx,dy,20,1,Sprites.PDART);//damage will perhaps be used in future
	}
	public Effect getEffect(){
		return null;
	}
}


class PGlue extends Projectile{
	public PGlue(int x,int y,double dx,double dy){
		super(x,y,dx,dy,20,1,Sprites.PGLUE);
	}
	public Effect getEffect(){
		if (addEffect){
			glue(40);
			int x = (int)pos.getX();
			int y = (int)pos.getY();
			return new Effect(x,y,Sprites.GLUESPLAT);
		}
		return null;
	}
}

class PCorroGlue extends Projectile{
	public PCorroGlue(int x,int y,double dx,double dy){
		super(x,y,dx,dy,20,1,Sprites.PCORROGLUE);
	}
	public Effect getEffect(){
		if (addEffect){
			corroGlue(80,300);
			int x = (int)pos.getX();
			int y = (int)pos.getY();
			return new Effect(x,y,Sprites.CORROSPLAT);
		}
		return null;
	}
}

class PBomb extends Projectile{
	public PBomb(int x,int y,double dx,double dy){
		super(x,y,dx,dy,20,1,Sprites.PBOMB);
	}
	public Effect getEffect(){
		if (addEffect){
			explode(20);
			int x = (int)pos.getX();
			int y = (int)pos.getY();
			return new Effect(x,y,Sprites.SMALLBOOM);
		}
		return null;
	}
}

class PMissile extends Projectile{
	public PMissile(int x,int y,double dx,double dy){
		super(x,y,dx,dy,20,1,Sprites.PMISSILE);
	}
	public Effect getEffect(){
		if (addEffect){
			explode(30);
			int x = (int)pos.getX();
			int y = (int)pos.getY();
			return new Effect(x,y,Sprites.MEDBOOM);
		}
		return null;
	}	
}

class PMoabMissile extends Projectile{
	public PMoabMissile(int x,int y,double dx,double dy){
		super(x,y,dx,dy,20,1,Sprites.PMOABMISSILE);
	}
	public Effect getEffect(){
		if (addEffect){
			explode(50);
			int x = (int)pos.getX();
			int y = (int)pos.getY();
			return new Effect(x,y,Sprites.LARGEBOOM);
		}
		return null;
	}
}

class PLaser extends Projectile{
	public PLaser(int x,int y,double dx,double dy){
		super(x,y,dx,dy,20,1,Sprites.PLASER);
	}
	public Effect getEffect(){
		return null;
	}
}

class PPlasma extends Projectile{
	public PPlasma(int x,int y,double dx,double dy){
		super(x,y,dx,dy,20,1,Sprites.PPLASMA);
	}
	public Effect getEffect(){
		return null;
	}
}
//projectiles with no image
/*The sniperBullet places a bullet right at 
 *the enemy for instant hit and thus has no image.*/
class PSnipeBullet extends Projectile{
	public PSnipeBullet(int x,int y){
		super(x,y,0,0,20,1,null);
	}
	public Effect getEffect(){
		return null;
	}
}

//projectiles that invoke an area of effect.
/*These projectiles die on the spot to invoke an effect right away (freeze)*/
abstract class PAreaEffect extends Projectile{
	public PAreaEffect(int x,int y,int range){
		super(x,y,0,0,range,1,null);
	}
	public void move(){
		//instant death to invoke effect on-the-spot
		die(true);
	}
}

class PSmallIce extends PAreaEffect {
	public PSmallIce(int x,int y,int range){
		super(x,y,range);
	}
	public Effect getEffect(){
		freeze(rad,50);
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new Effect(x,y,Sprites.SMALLICE);
	}
}

class PMedIce extends PAreaEffect {
	public PMedIce(int x,int y,int range){
		super(x,y,range);
	}
	public Effect getEffect(){
		freeze(rad,70);
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new Effect(x,y,Sprites.MEDICE);
	}
}

class PLargeIce extends PAreaEffect {
	public PLargeIce(int x,int y,int range){
		super(x,y,range);
	}
	public Effect getEffect(){
		freeze(rad,120);
		int x = (int)pos.getX();
		int y = (int)pos.getY();
		return new Effect(x,y,Sprites.LARGEICE);
	}
}

/*Begin Bloons~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
/*Basic Bloon Class in the game. Has 5 colours, more to be added
 *in the future if need be;*/
class Bloon{
	//Fields:
	public static final int RED = 0,BLUE = 1,GREEN = 2,YELLOW = 3,RAINBOW = 4;//types
	public static final int GLUE = 1,CORROGLUE = 2,FREEZE = 3;//states, 0 = normal
	protected int pos,id,value,oriSpeed,speed,state = 0,stateTimer = 0;
	protected BloonMap map;
	protected Sprite bSprite;
	protected boolean dead = false,popped = false;
	
	/*Method Constructor:
	 *Self-explanatory - creates a bloon with the specified properties and sprite*/
	public Bloon(int pos,int speed,int value,Sprites spriteID){//note: value = how much money earned once defeated
		this.pos = pos;
		this.value = value;
		this.speed = speed;
		this.oriSpeed = speed;
		this.bSprite = SpriteSet.getSprite(spriteID);
	}
	
	/*Method: draw
	 *Takes in a given graphics and JPanel context
	 *and draws this bloon at it's position*/
	public void draw(Graphics g, JPanel pane){
		BPoint p = map.getPos(pos/10);
		int x = (int)p.getX();
		int y = (int)p.getY();
		bSprite.draw(0,x,y,0,g,pane);
		if (state > 0)//overlay a state sprite if the bloon isn't normal
			bSprite.draw(state,x,y,0,g,pane);
	}
	
	/*Method: near
	 *Parameters: BPoint and Range
	 *returns null if the point isn't in the range, otherwise
	 *returns a vx and vy.
	 *Used for tower-sight collisions and obtaining vx an vy*/
	public double[] near(BPoint p, int range){
		BPoint position = map.getPos(pos/10);
		double dx = position.getX()-p.getX();
		double dy = position.getY()-p.getY();
		double dist = Math.hypot(dx,dy);
		if (dist < 10+range)
			return new double[]{dx/dist,dy/dist};
		return null;
	}
	
	/*Method: die
	 *Parameters: offMap
	 *if the bloon went off map, subtract life instead of adding money
	 *and display "popped" effect*/
	public void die(boolean offMap){ // if the bloon leaves the map, subtract life
		map.addEvent(new BloonMapEvent(BloonMapEvent.REMOVE_BLOON,id));
		if (offMap){
			map.addLife(-1);
		} else {
			map.addMoney(value);
			popped = true;//display effect
		}
		dead = true;
	}
	
	//Method: validState:
	//outputs true if the current state is less than the given limit
	public boolean validState(int limit){
		if (state < limit)
			return true;
		return false;
	}
	
	//Method: setState:
	//Sets the bloons state with a given time
	//the method also applies the states' effects
	public void setState(int newState,int time){
		state = newState;
		speed = oriSpeed;//prevent stacking of effects
		if (state == FREEZE)
			speed = 0;
		else if (state == GLUE)
			speed = (int)(speed*0.6);
		else if (state == CORROGLUE)
			speed = (int)(speed*0.4);
		stateTimer = time;
	}
	//Method: handleState()
	//handles the bloons state. If the times up, perform the proper effect
	private void handleState(){
		if (stateTimer > 0)
			stateTimer -=1;
		if (stateTimer == 0){
			if (state == FREEZE){
				setState(0,0);//return to normal state
			} else if (state == CORROGLUE){
				die(false);//kill bloon instead xD
			}
		}
	}
	
	//Method: move()
	//Moves bloon further down the track. 
	//If it's exited the map, kill it with true to take life
	public void move(){
		if (map.getPos((pos+speed)/10)==null)
			die(true);
		else
			pos+=speed;
		handleState();//refresh state
	}
	
	//Method: getEffect
	//All bloons will return a popped effect if properly popped
	public Effect getEffect(){
		if (popped){
			Random dice = new Random();//pick a random location in the bloons box to place "pop"
			int offX = dice.nextInt(10)-5;
			int offY = dice.nextInt(10)-5;
			BPoint position = map.getPos(pos/10);
			int x = (int)position.getX();
			int y = (int)position.getY();
			return new Effect(x+offX,y+offY,Sprites.BLOONBURST);
		} else {
			return null;//if it's exited the map, don't display anything
		}
	}
	
	/*Method: setMap
	 *Parameters: BloonMap
	 *
	 *Sets the bloonMap context*/
	public void setMap(BloonMap map){
		this.map = map;
		this.id = map.nextID(map.BLOON);
	}
	
	/*Methods Returning Information*/
	public boolean isDead(){
		return dead;
	}
	public int getID(){
		return id;
	}
	public int getPos(){
		return pos;
	};
}
/*Game Bloons: Pretty self explanatory.
 *Bloons each add to the die method to spawn their 
 *respective spawnlings.*/
 
class RedBloon extends Bloon{
	public RedBloon(int pos){
		super(pos,5,1,Sprites.REDBLOON);
	}
}

class BlueBloon extends Bloon{
	public BlueBloon(int pos){
		super(pos,10,2,Sprites.BLUEBLOON);
	}
	public void die(boolean offMap){
		super.die(offMap);
		map.addEvent(new BloonMapEvent(
						BloonMapEvent.ADD_BLOONS,
						new int[]{Bloon.RED},pos));
	}
}

class GreenBloon extends Bloon{
	public GreenBloon(int pos){
		super(pos,20,3,Sprites.GREENBLOON);
	}
	public void die(boolean offMap){
		super.die(offMap);
		map.addEvent(new BloonMapEvent(
						BloonMapEvent.ADD_BLOONS,
						new int[]{Bloon.BLUE},pos));
	}
}

class YellowBloon extends Bloon{
	public YellowBloon(int pos){
		super(pos,25,7,Sprites.YELLOWBLOON);
	}
	public void die(boolean offMap){
		super.die(offMap);
		map.addEvent(new BloonMapEvent(
						BloonMapEvent.ADD_BLOONS,
						new int[]{Bloon.GREEN,Bloon.GREEN},pos));
	}
}

class RainbowBloon extends Bloon{
	public RainbowBloon(int pos){
		super(pos,30,14,Sprites.RAINBOWBLOON);
	}
	public void die(boolean offMap){
		super.die(offMap);
		map.addEvent(new BloonMapEvent(
						BloonMapEvent.ADD_BLOONS,
						new int[]{Bloon.YELLOW,Bloon.YELLOW},pos));
	}
}

/*Begin Effect Handler~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
/*Effect
 *This class handles all effects that go on in game.
 *Effects play for one loop and then disappear.*/
class Effect {
	//Fields:
	private Sprite eSprite;
	private int x,y,id,frameTimer = 2,frame = 0;
	private BloonMap map;
	//nicer timed effects improve visuals
	
	/*Method: Constructor
	 *Makes an effect using a location and spriteid*/
	public Effect(int x,int y,Sprites spriteID){
		this.x = x;
		this.y = y;
		eSprite = SpriteSet.getSprite(spriteID);
	}
	
	/*Method: move
	 *Doesn't actually move the object. 
	 *Advances frame and exits the map if it's finished playing*/
	public void move(){
		if (frameTimer == 0){
			frame +=1;
			frameTimer = 2;
		}
		//once animation loops once, remove this effect
		if (frame == eSprite.size()){
			map.addEvent(new BloonMapEvent(BloonMapEvent.REMOVE_EFFECT,id));
		}
		frameTimer = Math.max(0,frameTimer-1);
	}
	
	/*Method: draw
	 *Draws this effect onto the
	 *given graphics and Jpanel context*/
	public void draw(Graphics g,JPanel pane){
		eSprite.draw(frame,x,y,0,g,pane);
	}
	
	/*Method: setMap
	 *Parameters: BloonMap
	 *
	 *Sets the bloonMap context*/
	public void setMap(BloonMap map){
		this.map = map;
		this.id = map.nextID(map.EFFECT);
	}
	
	//Methods that return information:
	public int getID(){
		return id;
	}
}

/*Begin Map Handlers~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
class BloonMap{
	public static final int BLOON = 0, TOWER = 1, PROJ = 2,EFFECT = 3,MAPX = 833,MAPY = 634;
	private ArrayList<Bloon> mapBloons = new ArrayList<Bloon>();
	private ArrayList<Bloon> priorityBloons = new ArrayList<Bloon>();
	//used to get towers to always strike at farthest bloon
	private PriorityQueue<Bloon> bloonQueue = new PriorityQueue<Bloon>(4000, new BloonComparator());
	
	private ArrayList<Tower> mapTowers = new ArrayList<Tower>();
	private ArrayList<Projectile> mapProjectiles = new ArrayList<Projectile>();
	private ArrayList<Effect> mapEffects = new ArrayList<Effect>();
	
	//event box
	private ArrayList<BloonMapEvent> events = new ArrayList<BloonMapEvent>();
	
	//id's represent the number of each type of maplife
	//constructed ie: bloons,towers,projectiles,effects
	private int[] ids = new int[]{0,0,0,0};
	
	//mapping each bloon position to an x and y coord
	private BPoint[] BloonPositions;
	
	//Spawn-handling Variable Declarations
	private BufferedReader spawnIn;
	private int nextSpawn =-1, spawnTime =-1,gameTime = 0,gameLevel = 0,gameState = 1;
	
	//regarding UI.
	private int lives = 100,money = 0,selectedTower = -1;
	private Image mapPic;
	private BufferedImage cantBuild;
	
	/*Method: Constructor
	 *Loads the map with the given name in.*/
	public BloonMap(String name){
		try {
			BufferedReader in = new BufferedReader(new FileReader("maps\\"+name+"\\bloonPos.txt"));
			//load in images
			mapPic = new ImageIcon("maps\\"+name+"\\back.png").getImage();
			//use buffered image for future "get pixel" use
  			cantBuild = ImageIO.read(new File("maps\\"+name+"\\cantBuild.png"));
  			
			//load in bloonPositions
			int n = Integer.parseInt(in.readLine());
			int c = 0;//counter used as an incrementor
			String[] rawPoint;
			BloonPositions = new BPoint[n];
			for (int i = 0; i < n; i++){
				rawPoint = in.readLine().split(",");
				BloonPositions[c++] = new BPoint(Integer.parseInt(rawPoint[0]),Integer.parseInt(rawPoint[1]));
			}
			
			in.close();
			//Load in level 0;
			nextLevel();
			
		} catch(IOException ex){
			System.out.println("There was a problem loading the map");
        	System.out.println (ex.toString());
    	}
	}
	
	//Methods Regarding UI
	
	//Field modifiers (add or subtract)
	public void addLife(int n){
		lives = Math.max(0,lives+n);
	}
	public void addMoney(int n){
		money = Math.min(99998,money+n);
		//humour: lol you're not buying that 99999 final "impossible"
		//upgrade you know XDD
	}
	
	//method: getUIInfo
	//returns money, lives and gamelevel (which is 1 more than current level
	//because I use an incrementor variable to track level)
	public int[] getUIInfo(){
		return new int[]{money,lives,gameLevel-1};
	}
	
	/*method: selectTower
	 *Parameters: mouse x and y
	 *selects and returns the selected tower if
	 *the mouse has clicked on a tower*/
	public int selectTower(int mx,int my){
		for (Tower t: mapTowers){
			if (t.collidePoint(mx,my,20)){
				selectedTower = t.getID();
				return selectedTower;
			}
		}
		selectedTower = -1;
		return selectedTower;
	}
	
	/*method: focusRecentTower
	 *This method sets the tower of focus to the newest tower placed*/
	public void focusRecentTower(){
		selectedTower = ids[TOWER]-1;
	}
	
	/*method: getSelectedTower
	 *This method turns the selected tower t*/
	public Tower getSelectedTower(){
		for (Tower t: mapTowers)
			if (t.getID() == selectedTower)
				return t;
		return null;
	}
	
	/*method: sellSelectedTower
	 *This method sells the selected tower*/
	public void sellSelectedTower(){
		Tower t = getSelectedTower();
		money+= t.getSellPrice();//add money
		removeTower(selectedTower);//remove tower
		selectedTower = -1;//set selected to none
	}
	
	/*method: upgradeSelectedTower
	 *This method upgrades the selected tower*/
	public void upgradeSelectedTower(){
		Tower t = getSelectedTower();
		money-= t.getUpgradePrice();//subtract money
		removeTower(selectedTower);//remove tower
		addTower(t.getUpgradedTower());//place upgradedtower
		focusRecentTower();//place focus on the new tower
	}
	
	/*method: getAlpha
	 *returns the alpha value of the pixel at x and y on cantBuild*/
	private int getAlpha(int x,int y){
		int clr =  cantBuild.getRGB(x,y); 
		return (clr>>24) & 0xFF;
	}
	/*method: withinBounds
	 *returns true if the x and y are within the map bounds*/
	private boolean withinBounds(int x,int y){
		return (x >= 0 && x < MAPX && y >= 0 && y < MAPY);
	}
	
	/*method: canPlace
	 *returns true if a tower can be placed at the position specified*/
	public boolean canPlace(int mx,int my){
		BPoint pt = new BPoint(mx,my);
		BPoint pt2;
		//make sure that the tower doesn't touch any coloured pixels on cantBuild
		for (int i = mx-20;i < mx+20; i++){
			for (int j = my-20;j < my+20; j++){
				pt2 = new BPoint(i,j);
				if (withinBounds(i,j) && pt.distance(pt2) < 20 && getAlpha(i,j) != 0)
					return false;
			}
		}
		//make sure the space isn't occupied
		for (Tower t: mapTowers){
			if (t.collidePoint(mx,my,40))
				return false;
		}
		return true;
	}
	
	/*MapLevel Handlers*/
	/*method: nextLevelReady
	 *returns true if the level is ready (no enemies and no more to spawn)*/
	public boolean nextLevelReady(){
		if (mapBloons.size() == 0 && spawnTime == -1 && nextSpawn == -1)
			return true;
		return false;
	}
	
	/*method: nextLevel
	 *Advances the level, restarts the timer.*/
	public void nextLevel(){
		try {
			spawnIn = new BufferedReader(new FileReader("levels\\"+(gameLevel++)+".txt"));//load next level
			money += Integer.parseInt(spawnIn.readLine());
			String[] temp = spawnIn.readLine().split(" ");
			spawnTime = Integer.parseInt(temp[0]);
			nextSpawn = Integer.parseInt(temp[1]);
			gameTime = 0;//restart timer, start pumping bloons again
		} catch(IOException ex){
			System.out.println("There was a problem loading the level");
        	System.out.println (ex.toString());
    	}
	}
	
	/*The following 3 methods return UI information. (self explanatory)*/
	public int getGameState(){
		return gameState;
	}
	
	public void setGameState(int s){
		gameState = s;
	}
	
	public int getScore(){
		return (gameLevel-1)*1000;
	}
	
	//method: clockTick
	//this method increases game time, and drives the spawnPump
	public void clockTick(){
		gameTime+=1;
		spawnBloon();
	}
	
	/*method: nextSpawn
	 *
	 *Essentially the spawnPump of the game. Returns the next available spawn
	 *when it's ready.*/
	public int nextSpawn(){ 
		//returns -1 if there's no spawn ready
		//else returns a valid spawn
		//Note: After all spawns are done, spawnTime will remain at -1
		if (nextSpawn == -1 || spawnTime == -1)
			return -1;
			
		if (gameTime == spawnTime){//if spawn is ready, return it.
			try{
        		String[] temp = spawnIn.readLine().split(" ");
				int return_Spawn = nextSpawn;
				spawnTime = Integer.parseInt(temp[0]);
				nextSpawn = Integer.parseInt(temp[1]);
				return return_Spawn;
    		} catch(IOException ex){
    			System.out.println("There's something wrong with spawn pump.");
        		System.out.println (ex.toString());
    		}
		}
		return -1;
	}
	
	/*method: spawnBloon
	 *This method makes spawns a bloon if possible*/
	public void spawnBloon(){
		int spawn = nextSpawn();
		if (spawn != -1){
			addEvent(new BloonMapEvent(
					BloonMapEvent.ADD_BLOONS,
					new int[]{spawn},0));
		}
	}
	
	/*method: checkCollision
	 *This method checks for all collisions between projectiles and bloons
	 *and handles them accordingly*/
	public void checkCollision(){
		for (Projectile p: mapProjectiles){
			for (Bloon b: mapBloons){
				if (b.near(p.getPos(),p.getRad()) != null && !b.isDead() && !p.isDead()){
					p.die(true);//projectile collided;invoke effect
					b.die(false);//bloon was killed so increase score instead
				}
			}
		}
	}
	
	/*method: refreshBloons
	 *This method refreshes and rids the priority queue of dead bloons*/
	public void refreshBloons(){
		bloonQueue.clear();
		priorityBloons.clear();
		for (Bloon b: mapBloons){
			bloonQueue.add(b);
		}
		while (bloonQueue.peek() != null)
			priorityBloons.add(bloonQueue.poll());
	}
	
	public ArrayList<Bloon> getBloons(){
		return priorityBloons;
	}
	
	/*method: getPos
	 *This method returns the position of the bloon if it's on map.
	 *Otherwise it returns null*/
	public BPoint getPos(int pos){
		if (pos < BloonPositions.length)
			return BloonPositions[pos];
		else
			return null;
	}
	
	//self explanatory add and remove methods
	public void addTower(Tower t){
		t.setMap(this);
		mapTowers.add(t);
	}
	
	public void removeTower(int id){
		for (int i = 0; i < mapTowers.size();i++){
			if (mapTowers.get(i).getID() == id){
				mapTowers.remove(i);
				break;
			}
		}
	}
	
	public void addProjectile(Projectile p){
		p.setMap(this);
		mapProjectiles.add(p);
	}
	public void addTowerProj(int towerID){
		Projectile p = new PDart(0,0,0,0);//temp placeholder
		for (Tower t: mapTowers){
			if (t.getID()==towerID)
				p = t.getProjectile();
		}
		addProjectile(p);
	}
	
	public void addBloon(Bloon b){
		b.setMap(this);
		mapBloons.add(b);
	}
	
	public void addEffect(Effect e){
		if (e != null){//some objects may return null for no effect
			e.setMap(this);
			mapEffects.add(e);
		}
	}
	
	/*method: display
	 *This method draws the map (including all objects of the map)
	 *onto the screen.*/
	public void display(Graphics g,JPanel pane){
		g.drawImage(mapPic,0,0,pane);
		
		for (Tower t: mapTowers){
			if (t.getID() == selectedTower)
				t.showRange(g);//if the tower is selected, show it's range
			t.draw(g,pane);
		}
		for (Projectile p: mapProjectiles){
			p.draw(g,pane);
		}
		for (Bloon b: mapBloons){
			b.draw(g,pane);
		}
		for (Effect e: mapEffects){
			e.draw(g,pane);
		}
	}
	
	/*method: move
	 *this method moves time by 1 increment for all objects in the map and performs a collision check*/
	public void move(){
		for (Bloon b: mapBloons){
			b.move();
		}
		for (Tower t: mapTowers){
			t.move();
		}
		for (Projectile p: mapProjectiles){
			p.move();
		}
		for (Effect e: mapEffects){
			e.move();
		}
		checkCollision();
	}
	
	/*Each object has it's individual id for it's type
	 *This is denoted at the top. ie: TOWER,BLOON,EFFECT,PROJ
	 *
	 *Using this id tracking strategy, we can properly add and remove objects*/
	public int nextID(int type){
		return ids[type]++;
	}
	
	public void addEvent(BloonMapEvent evt){
		events.add(evt);
	}
	
	/*method: removeItem
	 *This method takes in an id and type
	 *and removes the id of the corresponding type.
	 *
	 *It is called solely by the event pump to handle "remove" events*/
	public void removeItem(int id, int type){
		if (type == BloonMapEvent.REMOVE_TOWER){
			for (int i = 0; i < mapTowers.size();i++){//search and scan until we find
				if (mapTowers.get(i).getID() == id){
					mapTowers.remove(i);//remove and break
					break;
				}
			}
		}else if (type == BloonMapEvent.REMOVE_PROJ){
			for (int i = 0; i < mapProjectiles.size();i++){
				if (mapProjectiles.get(i).getID() == id){
					addEffect(mapProjectiles.get(i).getEffect());
					mapProjectiles.remove(i);
					break;
				}
			}
		} else if (type == BloonMapEvent.REMOVE_BLOON){
			for (int i = 0; i < mapBloons.size();i++){
				if (mapBloons.get(i).getID() == id){
					addEffect(mapBloons.get(i).getEffect());
					mapBloons.remove(i);
					break;
				}
			}
		} else if (type == BloonMapEvent.REMOVE_EFFECT){
			for (int i = 0; i < mapEffects.size();i++){
				if (mapEffects.get(i).getID() == id){
					mapEffects.remove(i);
					break;
				}
			}
		}
	}
	
	/*method: resolveEvents
	 *At the end of each "period" in time, this method
	 *is called as an event-pump cleaner. It resolves
	 *all events that may have occured during the current point in time*/
	public void resolveEvents(){
		BloonMapEvent evt;
		while (events.size()> 0){
			evt = events.get(0);
			if (evt.type == BloonMapEvent.REMOVE_BLOON){
				removeItem(evt.id,evt.type);
			}
			if (evt.type == BloonMapEvent.REMOVE_PROJ){
				removeItem(evt.id,evt.type);
			}
			if (evt.type == BloonMapEvent.ADD_PROJ){
				addTowerProj(evt.id);
			}
			if (evt.type == BloonMapEvent.ADD_BLOONS){
				for (int t: evt.spawnInfo){//spawn respective bloons
					if (t == Bloon.RED)
						addBloon(new RedBloon(evt.spawnPos));
					if (t == Bloon.BLUE)
						addBloon(new BlueBloon(evt.spawnPos));
					if (t == Bloon.GREEN)
						addBloon(new GreenBloon(evt.spawnPos));
					if (t == Bloon.YELLOW)
						addBloon(new YellowBloon(evt.spawnPos));
					if (t == Bloon.RAINBOW)
						addBloon(new RainbowBloon(evt.spawnPos));
				}
			}
			if (evt.type == BloonMapEvent.REMOVE_EFFECT){
				removeItem(evt.id,evt.type);
			}
			events.remove(0);
		}
	}
}

/*BloonComparator
 *Used with the bloon priority queue. Organizes
 *bloons by distance from the end so towers can target
 *the farthest bloon in their range.*/
class BloonComparator implements Comparator<Bloon>{
    @Override
    public int compare(Bloon x, Bloon y){
		if (x.getPos() < y.getPos())
    		return 1;
    	if (x.getPos() > y.getPos())
    		return -1;
    	return 0;
    }
}

/*BloonMapEvent
 *This is the event class for the BloonMap. It acts like a package,
 *carrying the necessary information with it to be handled by the pump.
 *It has two constructors, one for spawning bloons and the other for adding
 *and removing objects*/
class BloonMapEvent{
	public static final int ADD_BLOONS = 0,REMOVE_BLOON = 1,ADD_PROJ = 2,REMOVE_PROJ = 3,
							REMOVE_EFFECT = 4,REMOVE_TOWER = 5;
	public int id = 0;
	public int type, spawnPos = 0;
	public int[] spawnInfo;

	/*Constructor for add/remove*/
	public BloonMapEvent(int type, int id){
		this.type = type;
		this.id = id;
	}
	/*constructor for spawning bloons*/
	public BloonMapEvent(int type, int[] spawnInfo,int pos){
		this.type = type;
		this.spawnInfo = spawnInfo;
		this.spawnPos = pos;
	}
}