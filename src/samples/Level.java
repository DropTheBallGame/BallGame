/** The following was Written by Jordan Harlow with assistance from Richard Hamm
 *  While the code is inspired from several examples from dyn4j.org's examples it is
 *  entirely original code
 *  
 *  This class will serve as the basic template for our levels
 *  By extending Level, we will have access to all the tools to create levels quickly
 *  simply by overloading the initializeWorld() function
 *  
 *           __                      	
 *         _/  |______    ____  ____  
 *         \   __\__  \ _/ ___\/  _ \ 
 *	    |  |  / __ \\  \__(  <_> )
 *	    |__| (____  /\___  >____/  (:
 *                    \/     \/   
 **/

package samples;



import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.*;

import org.dyn4j.collision.AxisAlignedBounds;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Circle;
//import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;

import org.dyn4j.dynamics.DetectResult;
import org.dyn4j.dynamics.contact.ContactAdapter;
import org.dyn4j.dynamics.contact.ContactConstraintId;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.ContactPointId;
import org.dyn4j.dynamics.contact.PersistedContactPoint;

public class Level extends JFrame 
{
	/*  Generated serialVersionUID  */
	private static final long serialVersionUID = 7489010610517678448L;
	public static final double SCALE = 45.0;
	public static final double NANO_TO_BASE = 1.0e9;
	protected Point point;
	
	//Toolkit
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	
	//Picutres I guess
	public Image r_arrow_image = toolkit.getImage("blue-arrow-png-22.png");
	/*  The picking results  */
	protected List<DetectResult> results = new ArrayList<DetectResult>();
	
	protected Canvas canvas;
	protected World world;
	protected AxisAlignedBounds bounds = null;
	protected boolean stopped;
	protected boolean reset, game_over, restart = false;
	protected boolean showStartMenu = true;
	protected long last;
	/*  Only changed by the setAsPermanent Function */
	protected static int permanent_count;
	
	/*  A mapping of contact id to UUID  */
	protected Map<ContactPointId, UUID> contact_ids = new HashMap<ContactPointId, UUID>();
	
	protected static List<Body> game_bodies = new ArrayList<Body>();
	protected static List<UUID> boost_blocks = new ArrayList<UUID>();
	protected Body player = null;
	protected Body goal = null;
	
	protected static int scheme;
	
	public static int randoNum(int s, int l)
	{
		Random randgen = new Random();
		return randgen.nextInt(l - s + 1) + s;
	}
	
	public static class GameObject extends Body 
	{
		protected Color color;
		/*  Permanents are not deletable  */
		protected boolean permanent;
		protected boolean booster;
		protected Image image;
		
		public GameObject() 
		{
			permanent = false;
			booster = false;
			image = null;
			
			/*  Set this bitch up to generate random shades of brown 
			 *  I picked absolutely random numbers but it worked out  */
			color = SchemeColor();
		}
		
		public void setColor(Color c)
		{
			color = c;
		}
		
		protected void setAsPermanent(Level lev)
		{
			permanent = true;
			permanent_count++;
		}
		
		public boolean isPermanent()
		{
			return permanent;
		}
		
		public void render(Graphics2D fixtureGraphics) 
		{
			AffineTransform original_transformation = fixtureGraphics.getTransform();
			
			AffineTransform new_transformation = new AffineTransform();
			new_transformation.translate(transform.getTranslationX() * SCALE, transform.getTranslationY() * SCALE);
			new_transformation.rotate(transform.getRotation());
			
			fixtureGraphics.transform(new_transformation);

			for(BodyFixture fixture : fixtures) 
			{
				Convex convex = fixture.getShape();
				/*  Uses Graphics2DRenderer file from dyn4j example source  */
				Graphics2DRenderer.render(fixtureGraphics, convex, SCALE, color);
			}
			fixtureGraphics.setTransform(original_transformation);
		}
	}
	
	public static class BoosterBlock extends GameObject
	{
		protected char direction;
		
		public BoosterBlock()
		{
			booster = true;
			permanent = true;
			permanent_count++;
			
			direction = 'r';
			boost_blocks.add(getId());
		}
		
		public BoosterBlock(char d)
		{
			booster = true;
			permanent = true;
			permanent_count++;
			direction = d;
			boost_blocks.add(getId());
		}
		
		public void addDirection(char d)
		{
			direction = d;
		}
		
		public char getDirection()
		{
			return direction;
		}
	}
	
	public static Color SchemeColor()
	{
		Color schemecolor = new Color(
				(float)randoNum(70, 108) / 255.0f,
				(float)randoNum(50, 67) / 255.0f,
				(float)randoNum(15, 35) / 255.0f);
		int seasoncolor = 1;
		
		switch(Level.scheme)
		{
			case 0:  /*  Standard  */
				schemecolor = new Color(
						(float)randoNum(70, 108) / 255.0f,
						(float)randoNum(50, 67) / 255.0f,
						(float)randoNum(15, 35) / 255.0f);
				
				break;
			case 1:  /* Spring  */
				seasoncolor = randoNum(1, 4);
				switch(seasoncolor)
				{
					case 1:
						schemecolor = new Color(
								(float)randoNum(0, 130) / 255.0f,
								(float)randoNum(200, 255) / 255.0f,
								(float)randoNum(0, 130) / 255.0f);
						break;
					case 2:
						int yellowscheme = randoNum(200, 255);
						schemecolor = new Color(
								(float)yellowscheme / 255.0f,
								(float)yellowscheme / 255.0f,
								(float)randoNum(0, 150) / 255.0f);
						break;
					case 3:
						int pinkscheme = randoNum(200, 255);
						schemecolor = new Color(
								(float)pinkscheme / 255.0f,
								(float)randoNum(50, 130) / 255.0f,
								(float)pinkscheme / 255.0f);
						break;
					case 4:
						schemecolor = new Color(
								(float)255 / 255.0f,
								(float)randoNum(150, 225) / 255.0f,
								(float)randoNum(150, 225) / 255.0f);
				}
				break;
			case 2:  /*  Summer  */
				seasoncolor = randoNum(1, 4);
				switch(seasoncolor)
				{
					case 1:
						int goldscheme = randoNum(100, 150);
						schemecolor = new Color(
								(float)goldscheme / 255.0f,
								(float)goldscheme / 255.0f,
								0);
						break;
					case 2:
						schemecolor = new Color(
								(float)randoNum(0, 50) / 255.0f,
								(float)randoNum(50, 100) / 255.0f,
								(float)randoNum(0, 50) / 255.0f);
						break;
					case 3:
						int bluescheme = randoNum(200, 255);
						schemecolor = new Color(
								(float)randoNum(0, 150) / 255.0f,
								(float)bluescheme / 255.0f,
								(float)bluescheme / 255.0f);
						break;
					case 4:
						schemecolor = new Color(
								(float)randoNum(150, 255) / 255.0f,
								0,
								0);
				}
				break;
			case 3:  /*  Autumn  */
				seasoncolor = randoNum(1, 2);
				switch(seasoncolor)
				{
					case 1:
						schemecolor = new Color(
								(float)randoNum(100, 255) / 255.0f,
								(float)randoNum(0, 100) / 255.0f,
								(float)randoNum(0, 60) / 255.0f);		
						break;
					case 2:
						int yellowscheme = randoNum(200, 255);
						schemecolor = new Color(
								(float)yellowscheme / 255.0f,
								(float)yellowscheme / 255.0f,
								(float)randoNum(0, 150) / 255.0f);		
				}
				break;
			case 4:  /*  Winter  */
				seasoncolor = randoNum(1, 4);
				switch(seasoncolor)
				{
					case 1:
						int graycolor = randoNum(0, 255);
						schemecolor = new Color(
								(float)graycolor / 255.0f,
								(float)graycolor / 255.0f,
								(float)graycolor / 255.0f);
						break;
					case 2:
						schemecolor = new Color(
								(float)randoNum(0, 204) / 255.0f,
								(float)randoNum(255, 255) / 255.0f,
								(float)randoNum(204, 255) / 255.0f);
						break;
					case 3:
						schemecolor = new Color(
								0,
								0,
								(float)randoNum(100, 160) / 255.0f);
						break;
					case 4:
						int purplescheme = randoNum(80, 130);
						schemecolor = new Color(
								(float)purplescheme / 255.0f,
								0,
								(float)purplescheme / 255.0f);
				}
		}
		
		return schemecolor;
	}
	
	protected class WinDetection extends ContactAdapter 
	{ 
		@Override
		public boolean begin(ContactPoint point) 
		{
			/*  ContactPointID contains the ContactConstraintID  */
			ContactPointId id = point.getId();
			/*  ContactConstraintID contains the information we need such as BodyIDs  */
			ContactConstraintId thegoodstuff = id.getContactConstraintId();
			
			UUID uuid = UUID.randomUUID();
			contact_ids.put(id, uuid);
			
			/*  If the two bodies that contact are the player and goal  */
			if(goal != null && player != null && ((thegoodstuff.getBody1Id() == goal.getId() && thegoodstuff.getBody2Id() == player.getId()) || 
					(thegoodstuff.getBody1Id() == player.getId() && thegoodstuff.getBody2Id() == goal.getId())))
			{
				//System.out.println("The ball touched the thing!" + permanent_count + " -> " + game_bodies.size());
				
				/*  Player just lost the game, reset world  */
				if(game_bodies.size() > permanent_count)
				{
					//stop();
					reset = true;
					JOptionPane.showMessageDialog(null, "You failed the level!\nPress 'OK' to try again", "You Lose!", JOptionPane.INFORMATION_MESSAGE);
					//System.out.println("Bruh you made contact before all the shit was gone!");
					//reset = true;
					//start();
				}
				/*  Player wins the game  */
				else
				{	
					JOptionPane.showMessageDialog(null, "You completed the level!\nPress 'OK' to move on to the next level", "You Win!", JOptionPane.INFORMATION_MESSAGE);
					// System.out.println("You fuckin' Won!!!!");
					game_over = true;	
				}
			}
			//If player ball touches a boost block, we're gonna fling them in the blocks direction
			else if(goal != null && player != null && ((thegoodstuff.getBody1Id() == player.getId() && boost_blocks.contains(thegoodstuff.getBody2Id()))
				|| (thegoodstuff.getBody2Id() == player.getId() && boost_blocks.contains(thegoodstuff.getBody1Id()))))
			{
				for(Body b : game_bodies)
				{
					if(((GameObject) b).booster == true && 
						(b.getId() == thegoodstuff.getBody1Id() || b.getId() == thegoodstuff.getBody2Id()))
					{
						//System.out.println("BOOST ME SCOTTY! -> " + player.getForce());
						
						switch(((BoosterBlock) b).getDirection())
						{
							case 'r':	
								player.clearAccumulatedForce();
								player.applyForce(new Vector2(300.0, 0.0));
								break;
							case 'l':
								player.clearAccumulatedForce();
								player.applyForce(new Vector2(-300.0, 0.0));
								break;
							case 'u':
								player.clearAccumulatedForce();
								player.applyForce(new Vector2(0.0, 300.0));
								break;
							case 'd':
								player.clearAccumulatedForce();
								player.applyForce(new Vector2(0.0, -300.0));
								break;
							case 'n':
								player.clearAccumulatedForce();
								player.applyForce(new Vector2(300.0, 300.0));
								break;
							case 's':
								player.clearAccumulatedForce();
								player.applyForce(new Vector2(0.0, 3500.0));
								break;	
							case 'w':
								player.clearAccumulatedForce();
								player.applyForce(new Vector2(-1000.0, 3500.0));
								break;	
						}
						return false;
					}
				
				}
			}
			
			return true;
		}
		
		@Override
		public void end(ContactPoint point) 
		{
			ContactPointId id = point.getId();
			contact_ids.remove(id);
		}
		
		@Override
		public boolean persist(PersistedContactPoint point) 
		{
			ContactPointId id = point.getId();
			contact_ids.get(id);
			return true;
		}
	}
	
	private final class GameObjectDeleter extends MouseAdapter 
	{
		@Override
		public void mousePressed(MouseEvent e) 
		{
			/*  Store the mouse click position  */
			point = new Point(e.getX(), e.getY());
		}
		
		@Override
		public void mouseReleased(MouseEvent e) 
		{
			point = null;
		}
	}
 
	public Level() 
	{
		this.initialize(true, 0);
	}

	protected void initialize(boolean showMenu,int scheme)
	{
		/*  Set color scheme to Standard by default  */
		Level.scheme = scheme;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		/*  Window Listener for funzies  */
		addWindowListener(new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent e) 
			{
				stop();
				super.windowClosing(e);
			}
		});
		
		Dimension size = new Dimension(800, 600);
		
		/* Create a canvas to paint to  */
		canvas = new Canvas();
		canvas.setPreferredSize(size);
		canvas.setMinimumSize(size);
		canvas.setMaximumSize(size);
		
		/*  Mouse listener for the best of times  */
		MouseAdapter ml = new GameObjectDeleter();
		canvas.addMouseMotionListener(ml);
		canvas.addMouseWheelListener(ml);
		canvas.addMouseListener(ml);

		/*  Add the canvas to the JFrame  */
		this.setJMenuBar(this.buildMenu());
		
		JButton buttonStart = new JButton("Start");
		buttonStart.setPreferredSize(new Dimension(100,40));
		buttonStart.setLocation(400, 200);
		buttonStart.addActionListener((ActionEvent event)-> {
			this.getGlassPane().setVisible(false);
			this.showStartMenu = false;
			game_over = true;
        	
        });
		
		JButton buttonRules= new JButton("The Rules");
		buttonRules.addActionListener((ActionEvent event)-> {
			this.showTheRules();
        });
		
		
		buttonRules.setPreferredSize(new Dimension(100,40));
		
		JPanel glass = (JPanel) this.getGlassPane();
		Color colorA = new Color(200,200,200,200);
		glass.setLayout(new FlowLayout());
		glass.setOpaque(true);
		glass.setBackground(colorA);
		
		glass.add(buttonStart);
		glass.add(buttonRules);
		
		glass.setVisible(showMenu);		
		
		this.getContentPane().add(canvas);
		
		/*  Make it unresizable and pack it  */
		setResizable(false);
		pack();
		stopped = false;
		
		/*  Create World  */
		initializeWorld();		
		
	}
	
	public Level(boolean showMenu, int scheme)
	{
		this.showStartMenu = showMenu;
		Level.scheme = scheme;
		
		this.initialize(showMenu, scheme);
	}
	
	private void showTheRules() {
		JOptionPane.showMessageDialog(null, 
				"These are the rules. \n 1. Don't let the ball touch the goal (its orange) before deletable things are gone. \n\n2. That's the rules.", 
				"THE RULES", JOptionPane.INFORMATION_MESSAGE);
		
	}

	protected JMenuBar buildMenu()
	{
		JMenuBar menu = new JMenuBar();
		menu.setOpaque(true); //makes it visible
        menu.setPreferredSize(new Dimension(300, 30)); //size of the menu bar
        
        JMenu gameMenu = new JMenu("Game");
        JMenuItem restartItem = new JMenuItem("Restart Game");
        JMenuItem restartLevel = new JMenuItem("Restart Level");
        
        JMenuItem exitItem = new JMenuItem("Exit");
        
        JMenu colorMenu = new JMenu("Schemes");
        ButtonGroup colorGroup = new ButtonGroup();
        
        JRadioButtonMenuItem standard = new JRadioButtonMenuItem ("Standard");
        standard.setSelected(true);
        
        standard.addActionListener((ActionEvent event) -> { 
        	this.setScheme(0);
        	reset = true;
        	});        
        
        JRadioButtonMenuItem spring = new JRadioButtonMenuItem ("Spring");
        
        spring.addActionListener((ActionEvent event) -> { 
        	this.setScheme(1);
        	reset = true;
        	});
        
        JRadioButtonMenuItem summer = new JRadioButtonMenuItem ("Summer");
        summer.addActionListener((ActionEvent event) -> { 
        	this.setScheme(2);
        	reset = true;
        	});
        
        JRadioButtonMenuItem autumn = new JRadioButtonMenuItem ("Autumn");
        autumn.addActionListener((ActionEvent event) -> { 
        	this.setScheme(3);
        	reset = true;
        	});
        
        JRadioButtonMenuItem winter = new JRadioButtonMenuItem ("Winter");
        winter.addActionListener((ActionEvent event) -> { 
        	this.setScheme(4);
        	reset = true;
        	});
 
        colorMenu.add(standard);
        colorGroup.add(standard);
        
        colorMenu.add(spring);
        colorGroup.add(spring);
        
        colorMenu.add(summer);
        colorGroup.add(summer);
        
        colorMenu.add(autumn);
        colorGroup.add(autumn);
        
        colorMenu.add(winter);
        colorGroup.add(winter);
        
        // Events
        restartItem.addActionListener((ActionEvent event)-> {
        	this.restartGame(event);
        });
        
        restartLevel.addActionListener((ActionEvent event) -> {
        	this.restartLevel(event);
        });
        
        exitItem.addActionListener((ActionEvent event) -> {
            System.exit(0);
        });

        gameMenu.add(restartItem);
        gameMenu.add(restartLevel);
        gameMenu.add(exitItem);

        menu.add(gameMenu);
        menu.add(colorMenu);
        
        return menu;
	}
	
	protected void restartGame(ActionEvent event)
	{
		restart = true;
		this.showStartMenu = false;
	}
	
	protected void restartLevel(ActionEvent event)
	{
		reset = true;
	}
	
	public void setScheme(int schemeNumber)
	{
		scheme = schemeNumber;
	}
		
	/*  Pretty Much the only function you need to overload  */
	protected void initializeWorld() 
	{
		//bounds = new AxisAlignedBounds(25, 30);
		//bounds.translate(0, 0);
		world = new World();
		world.setGravity(new Vector2(0.00, 0.00));
		//System.out.print("World bounds -> " + world.getBounds());
		permanent_count = 0;
		
		for(int i = 0; i < 4; i++)
		{
			Rectangle rectShape = new Rectangle(1.0, 1.0);
			GameObject rectangle = new GameObject();
			rectangle.addFixture(rectShape);
			rectangle.setMass(MassType.NORMAL);
			rectangle.setAsPermanent(this);
			rectangle.translate(-6.5, 4.5 - i);
			world.addBody(rectangle);
		}
		
		Rectangle rect1 = new Rectangle(1.0, 1.0);
		GameObject r1 = new GameObject();
		r1.addFixture(rect1);
		r1.setMass(MassType.NORMAL);
		r1.setAsPermanent(this);
		r1.translate(-5.5, 4.5);
		world.addBody(r1);
		
		GameObject r2 = new GameObject();
		r2.addFixture(rect1);
		r2.setAsPermanent(this);
		r2.setMass(MassType.NORMAL);
		r2.translate(-5.5, 1.5);
		world.addBody(r2);
		
		GameObject r3 = new GameObject();
		r3.addFixture(rect1);
		r3.setAsPermanent(this);
		r3.setMass(MassType.NORMAL);
		r3.translate(-4.5, 2.5);
		world.addBody(r3);
		
		GameObject r4 = new GameObject();
		r4.addFixture(rect1);
		r4.setAsPermanent(this);
		r4.setMass(MassType.NORMAL);
		r4.translate(-4.5, 3.5);
		world.addBody(r4);
		
		//cooooollll
		GameObject r5 = new GameObject();
		r5.addFixture(rect1);
		r5.setAsPermanent(this);
		r5.setMass(MassType.NORMAL);
		r5.translate(-100.5, -45.5);
		r5.applyForce(new Vector2(600.00, 300.00));
		world.addBody(r5);
		
		GameObject r6 = new GameObject();
		r6.addFixture(rect1);
		r6.setAsPermanent(this);
		r6.setMass(MassType.NORMAL);
		r6.translate(-2.5, 3.5);
		world.addBody(r6);
		
		GameObject r7 = new GameObject();
		r7.addFixture(rect1);
		r7.setAsPermanent(this);
		r7.setMass(MassType.NORMAL);
		r7.translate(-2.5, 2.5);
		world.addBody(r7);
		
		GameObject r8 = new GameObject();
		r8.addFixture(rect1);
		r8.setAsPermanent(this);
		r8.setMass(MassType.NORMAL);
		r8.translate(-2.5, 1.5);
		world.addBody(r8);
		
		GameObject r9 = new GameObject();
		r9.addFixture(rect1);
		r9.setAsPermanent(this);
		r9.setMass(MassType.NORMAL);
		r9.translate(-1.5, 3.5);
		world.addBody(r9);
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				if(i != 1 || j != 1)
				{
					GameObject r10 = new GameObject();
					r10.addFixture(rect1);
					r10.setAsPermanent(this);
					r10.setMass(MassType.NORMAL);
					r10.translate(0.5 + i, 3.5 - j);
					world.addBody(r10);
				}
			}
		}
		
		for( int i = 0; i < 5; i++)
		{
			GameObject r11 = new GameObject();
			r11.addFixture(rect1);
			r11.setAsPermanent(this);
			r11.setMass(MassType.NORMAL);
			r11.translate(4.5, 4.5 - i);
			world.addBody(r11);
		}
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 2; j++)
			{
				if(!(i == 1 && j == 0))
				{
					GameObject r12 = new GameObject();
					r12.addFixture(rect1);
					r12.setAsPermanent(this);
					r12.setMass(MassType.NORMAL);
					r12.translate(5.5 + j, 4.5 - i);
					world.addBody(r12);
				}
			}
		}
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				if(!(i == 0 && j == 1) && !(i == 1 && j == 1))
				{
					GameObject r13 = new GameObject();
					r13.addFixture(rect1);
					r13.setAsPermanent(this);
					r13.setMass(MassType.NORMAL);
					r13.translate(-7.5 + j, -0.5 - i);
					world.addBody(r13);
				}
			}
		}
		
		for(int i = 0; i < 3; i++)
		{
			GameObject r14 = new GameObject();
			r14.addFixture(rect1);
			r14.setAsPermanent(this);
			r14.setMass(MassType.NORMAL);
			r14.translate(-3.5, -0.5 - i);
			world.addBody(r14);
		}
		
		GameObject r15 = new GameObject();
		r15.addFixture(rect1);
		r15.setAsPermanent(this);
		r15.setMass(MassType.NORMAL);
		r15.translate(-2.5, -0.5);
		world.addBody(r15);
		
		for(int i = 0; i < 5; i++)
		{
			GameObject r16 = new GameObject();
			r16.addFixture(rect1);
			r16.setAsPermanent(this);
			r16.setMass(MassType.NORMAL);
			r16.translate(-1.5, -2.5 - i);
			world.addBody(r16);
		}
		
		for(int i = 0; i < 5; i ++)
		{
			if(i % 2 == 0)
			{
				GameObject r17 = new GameObject();
				r17.addFixture(rect1);
				r17.setAsPermanent(this);
				r17.setMass(MassType.NORMAL);
				r17.translate(-0.5, -2.5 - i);
				world.addBody(r17);
			}
		}
		
		for(int i = 0; i < 3; i++)
		{
			if(i != 1) 
			{
				GameObject r18 = new GameObject();
				r18.addFixture(rect1);
				r18.setAsPermanent(this);
				r18.setMass(MassType.NORMAL);
				r18.translate(0.5, -3.5 - i);
				world.addBody(r18);
			}
		}
		
		for(int i = 0; i < 4; i++)
		{
			GameObject r19 = new GameObject();
			r19.addFixture(rect1);
			r19.setAsPermanent(this);
			r19.setMass(MassType.NORMAL);
			r19.translate(2.5, -3.5 - i);
			world.addBody(r19);
		}
		
		for(int i = 0; i < 4; i++)
		{
			GameObject r20 = new GameObject();
			r20.addFixture(rect1);
			r20.setAsPermanent(this);
			r20.setMass(MassType.NORMAL);
			r20.translate(4.5, -3.5 - i);
			world.addBody(r20);
		}
		
		for(int i = 0; i < 3; i++)
		{
			if(i != 1) 
			{
				GameObject r21 = new GameObject();
				r21.addFixture(rect1);
				r21.setAsPermanent(this);
				r21.setMass(MassType.NORMAL);
				r21.translate(3.5, -3.5 - i);
				world.addBody(r21);
			}
		}
		
		for(int i = 0; i < 5; i++)
		{
			GameObject r22 = new GameObject();
			r22.addFixture(rect1);
			r22.setAsPermanent(this);
			r22.setMass(MassType.NORMAL);
			r22.translate(6.5, -2.5 - i);
			world.addBody(r22);
		}
		
		for(int i = 0; i < 5; i++)
		{
			GameObject r23 = new GameObject();
			r23.addFixture(rect1);
			r23.setAsPermanent(this);
			r23.setMass(MassType.NORMAL);
			r23.translate(8.5, -2.5 - i);
			world.addBody(r23);
		}
		
		Circle cirShape = new Circle(4.0);
		GameObject ball = new GameObject();
		ball.addFixture(cirShape);
		ball.setMass(MassType.NORMAL);
		ball.translate(-4.0, 50.0);
		ball.applyForce(new Vector2(0.00, -10000.00));
		ball.setAsPermanent(this);
		world.addBody(ball);
		
		/*  attach the contact listener  */
		world.addListener(new WinDetection());
	}
	
	public void start() 
	{
		this.setVisible(true);
		
		
		last = System.nanoTime();
		canvas.setIgnoreRepaint(true);
		canvas.createBufferStrategy(2);
		
		/*  run a separate thread to do active rendering
		 *  because we don't want to do it on the EDT  */
		Thread thread = new Thread() 
		{
			public void run() 
			{
				/*  perform an infinite loop stopped
				 *  render as fast as possible  */
				while (!isStopped()) 
				{
					gameLoop();
	
				}
			}
		};
		
		thread.setDaemon(true);
		thread.start();
	}
	
	protected void gameLoop() 
	{	
		game_bodies = world.getBodies();
		Graphics2D graphics = (Graphics2D)canvas.getBufferStrategy().getDrawGraphics();
		
		AffineTransform yFlip = AffineTransform.getScaleInstance(1, -1);
		AffineTransform move = AffineTransform.getTranslateInstance(400, -300);
		graphics.transform(yFlip);
		graphics.transform(move);
		
		render(graphics);
		
		graphics.dispose();
		
		BufferStrategy strategy = canvas.getBufferStrategy();
		
		if(!strategy.contentsLost()) 
		{
			strategy.show();
		}
		
        Toolkit.getDefaultToolkit().sync();
        
		results.clear();
        
     	if (point != null) 
     	{
     		/*  convert from screen space to world space coordinates  */
     		double x =  (point.getX() - canvas.getWidth() / 2.0) / SCALE;
     		double y = -(point.getY() - canvas.getHeight() / 2.0) / SCALE;
     			
     		/*  iterates through all bodies in the world and checks
     		 *  if they are within the mouse click  */
     		for(int i = 0; i < world.getBodyCount(); i++) 
     		{
    				Body b = world.getBody(i);
    				if(b.contains(new Vector2(x, y)) && !((GameObject) b).isPermanent()) 
    				{
    					world.removeBody(b);
						/*	World.setUpdateRequired(true) basically has to get called any time
						 *	you change the physics or world space, such as removing Bodies  */
    					world.setUpdateRequired(true);    				
    				}
     		}
     			/*	Reset point so we don't repeatedly call the same mouse point  */
     			point = null;
     	}
     		
     	for(int i = 0; i < world.getBodyCount(); i++)
 		{
 			Body b = world.getBody(i);
 			if(bounds != null && (bounds.isOutside(b) == true && !((GameObject)b).isPermanent()))
 			{	
 				//System.out.print("Shit's gone!");
 				world.removeBody(b);
 				world.setUpdateRequired(true);
 			}
 			else if(player != null && (bounds.isOutside(b) == true && ((GameObject) b).getId() == player.getId()))
			{
					reset = true;
			}
 		}
     		
     	if(reset == true)
     	{
     		initializeWorld();
     		reset = false;
     	}
     	
     	if(game_over == true)
		{
     		this.goNextLevel();
		}
     	
     	if(restart == true)
     	{
     		stop();
     		setVisible(false);
     		start();
     	}
       
        long time = System.nanoTime();
        long diff = time - last;
        last = time;
        double elapsedTime = diff / NANO_TO_BASE;
        world.update(elapsedTime);
	}
	
	protected void goNextLevel()
	{
		stop();
 		setVisible(false);
 		
 		Level next = new LevelOne(this.showStartMenu, Level.scheme);
 		next.start();
		// LevelOne.main(new String[] {((Boolean)this.showStartMenu).toString(), ((Integer)Level.scheme).toString()});
	}

	protected void render(Graphics2D level_graphics) 
	{
		/*  Fill world with white color to draw objects over  */
		level_graphics.setColor(Color.WHITE);
		level_graphics.fillRect(-400, -300, 800, 600);
		
		/*  Draw all the objects in the world  */
		for(int i = 0; i < world.getBodyCount(); i++) 
		{
			GameObject go = (GameObject)world.getBody(i);
			
			//Attempted way to have screen centered on player, didn't work QQ
			go.render(level_graphics);
		}
	}

	public synchronized void stop() 
	{
		stopped = true;
	}
	
	public synchronized boolean isStopped() 
	{
		return stopped;
	}
	
	public static void main(String[] args) 
	{
		Level level0 = new Level();
		level0.start();		
	}
}