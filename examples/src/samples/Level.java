package samples;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy; 
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.dynamics.DetectResult;

public class Level extends JFrame {
	/**Generated serialVersionUID**/
	private static final long serialVersionUID = 7489010610517678448L;
	public static final double SCALE = 45.0;
	public static final double NANO_TO_BASE = 1.0e9;
	private Point point;
	
	/** The picking results */
	private List<DetectResult> results = new ArrayList<DetectResult>();
	
	public static class GameObject extends Body 
	{
		protected Color color;
		
		public GameObject() {
			this.color = new Color(
					(float)Math.random() * 0.5f + 0.5f,
					(float)Math.random() * 0.5f + 0.5f,
					(float)Math.random() * 0.5f + 0.5f);
		}
		
		public void render(Graphics2D g) 
		{
			//Capture original transformation
			AffineTransform ot = g.getTransform();
			
			// transform the coordinate system from world coordinates to local coordinates
			AffineTransform lt = new AffineTransform();
			lt.translate(this.transform.getTranslationX() * SCALE, this.transform.getTranslationY() * SCALE);
			lt.rotate(this.transform.getRotation());
			
			// apply the transform
			g.transform(lt);

			for (BodyFixture fixture : this.fixtures) 
			{

				Convex convex = fixture.getShape();
				Graphics2DRenderer.render(g, convex, SCALE, color);
			}
			
			//Set new transformation
			g.setTransform(ot);
		}
	}
	
	protected Canvas canvas;
	protected World world;
	protected boolean stopped;
	protected long last;
	
	private final class CustomMouseAdapter extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			//store the mouse click position for use later
			point = new Point(e.getX(), e.getY());
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			point = null;
		}
	}
 
	public Level() 
	{
		super("Level");
		// setup the JFrame
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Window Listener for funzies
		this.addWindowListener(new WindowAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosing(WindowEvent e) {
				// before we stop the JVM stop the example
				stop();
				super.windowClosing(e);
			}
		});
		
		// create the size of the window
		Dimension size = new Dimension(800, 600);
		
		// create a canvas to paint to 
		this.canvas = new Canvas();
		this.canvas.setPreferredSize(size);
		this.canvas.setMinimumSize(size);
		this.canvas.setMaximumSize(size);
		
		//Mouse listener for the best of times
		MouseAdapter ml = new CustomMouseAdapter();
		this.canvas.addMouseMotionListener(ml);
		this.canvas.addMouseWheelListener(ml);
		this.canvas.addMouseListener(ml);
		
		// add the canvas to the JFrame
		this.add(this.canvas);
		
		// make the JFrame not resizable
		this.setResizable(false);
		
		// size everything
		this.pack();
		
		// make sure we are not stopped
		this.stopped = false;
		
		// setup the world
		this.initializeWorld();
	}

	protected void initializeWorld() 
	{
		this.world = new World();
		
		// create the goal floor
		Rectangle floorRect = new Rectangle(15.0, 1.0);
		GameObject goal_floor = new GameObject();
		goal_floor.addFixture(new BodyFixture(floorRect));
		goal_floor.setMass(MassType.INFINITE);
		goal_floor.translate(0.0, -4.0);
		this.world.addBody(goal_floor);
		
		// Create the player, a circle
		Circle cirShape = new Circle(0.6);
		GameObject player = new GameObject();
		player.addFixture(cirShape);
		player.setMass(MassType.NORMAL);
		player.translate(2.0, 20.0);
		// set some linear damping to simulate rolling friction
		player.setLinearDamping(0.05);
		this.world.addBody(player);
		
		//Try some squares dude
		for(int i = 0; i < 4; i++)
		{
			Rectangle rectShape = new Rectangle(2.5, 2.5);
			GameObject rectangle = new GameObject();
			rectangle.addFixture(rectShape);
			rectangle.setMass(MassType.NORMAL);
			rectangle.translate(2.0, i*2.0);
			this.world.addBody(rectangle);	
		}
	}
	
	public void start() {
		// initialize the last update time
		this.last = System.nanoTime();
		// don't allow AWT to paint the canvas since we are
		this.canvas.setIgnoreRepaint(true);
		// enable double buffering (the JFrame has to be
		// visible before this can be done)
		this.canvas.createBufferStrategy(2);
		// run a separate thread to do active rendering
		// because we don't want to do it on the EDT
		Thread thread = new Thread() 
		{
			public void run() 
			{
				// perform an infinite loop stopped
				// render as fast as possible
				while (!isStopped()) 
				{
					gameLoop();
	
				}
			}
		};
		// set the game loop thread to a daemon thread so that
		// it cannot stop the JVM from exiting
		thread.setDaemon(true);
		// start the game loop
		thread.start();
	}
	
	protected void gameLoop() {
		// get the graphics object to render to
		Graphics2D g = (Graphics2D)this.canvas.getBufferStrategy().getDrawGraphics();
		
		// before we render everything im going to flip the y axis and move the
		// origin to the center (instead of it being (0, 0) in the top left corner)
		AffineTransform yFlip = AffineTransform.getScaleInstance(1, -1);
		AffineTransform move = AffineTransform.getTranslateInstance(400, -300);
		g.transform(yFlip);
		g.transform(move);
		
		// now (0, 0) is in the center of the screen with the positive x axis
		// pointing right and the positive y axis pointing up
		
		// render anything about the Example (will render the World objects)
		this.render(g);
		
		// dispose of the graphics object
		g.dispose();
		
		// blit/flip the buffer
		BufferStrategy strategy = this.canvas.getBufferStrategy();
		if (!strategy.contentsLost()) {
			strategy.show();
		}
		
		// Sync the display on some systems.
        // (on Linux, this fixes event queue problems)
        Toolkit.getDefaultToolkit().sync();
        
        // update the World
		this.results.clear();
		
		// we are going to use a circle to do our picking
		//Convex convex = Geometry.createCircle(Level.PICKING_RADIUS);
		//Transform transform = new Transform();
		double x = 0;
		double y = 0;
        
     // see if the user clicked
     		if (this.point != null) {
     			// convert from screen space to world space coordinates
     			x =  (this.point.getX() - this.canvas.getWidth() / 2.0) / SCALE;
     			y = -(this.point.getY() - this.canvas.getHeight() / 2.0) / SCALE;
     			
     			for (int i = 0; i < this.world.getBodyCount(); i++) 
     			{
    				Body b = this.world.getBody(i);
    				if (b.contains(new Vector2(x, y))) 
    				{
    					this.world.removeBody(b);
;    				}
    			}
     			
     			// clear the point
     			this.point = null;
     		}
        
        // get the current time
        long time = System.nanoTime();
        // get the elapsed time from the last iteration
        long diff = time - this.last;
        // set the last time
        this.last = time;
    	// convert from nanoseconds to seconds
    	double elapsedTime = diff / NANO_TO_BASE;
        // update the world with the elapsed time
        this.world.update(elapsedTime);
	}

	protected void render(Graphics2D g) {
		// lets draw over everything with a white background
		g.setColor(Color.WHITE);
		g.fillRect(-400, -300, 800, 600);
		
		// lets move the view up some
		//g.translate(0.0, -1.0 * SCALE);
		
		// draw all the objects in the world
		for (int i = 0; i < this.world.getBodyCount(); i++) {
			// get the object
			GameObject go = (GameObject) this.world.getBody(i);
			// draw the object
			go.render(g);
		}
	}

	public synchronized void stop() {
		this.stopped = true;
	}
	
	public synchronized boolean isStopped() {
		return this.stopped;
	}
	
	public static void main(String[] args) {
		
		Level window = new Level();
		
		// show it
		window.setVisible(true);
		
		// start it
		window.start();
	}
}