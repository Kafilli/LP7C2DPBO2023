/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package synchronization;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.util.Random;

/**
 *
 * @author Satria Ramadhani
 */
public class Game extends Canvas implements Runnable
{
    /**
     * 
     * Attribute declaration.
     */
    
    /* View-related attributes. */
    private Random rand;   
    public static final int width = 640;
    public static final int height = 480;
    private Display display;
    
    /* Process-related attributes. */
    private boolean running;
    private Handler handler;
    private Thread thread;
    private GameObject temp1;
    private GameObject temp2;
    
    /* Animation-related attributes. */
    private boolean startCounting = false;
    private int score = 0;
    private int point = 0;
    private int counter = 0;
    private int stateCounter = 0;
    private int direction = 0;
    
    
    // Default constructor.
    public Game()
    {
        this.rand = new Random();
        try
        {
            // Initialize display.
            display = new Display(width, height, "Synchronization Tutorial");
            display.open(this); 
            
            // Initialize game handler.
            handler = new Handler();
            
            // Initialize controller (keyboard input).
            this.setFocusable(true);
            this.requestFocus();
            this.addKeyListener(new Controller(this, handler));
            
            // Initialize all object.
            running = true;
            if(running)
            {
                handler.add(new Player(320, 160));
                handler.add(new Kotak(100, 100));
            }
        } catch(Exception e)
        {
            System.err.println("Failed to instance data.");
        }
        
    }
    
    /**
     * 
     * Getter and Setter.
     */
    
    /* Game running status. */
    
    public boolean isRunning()
    {
        return running;
    }

    public void setRunning(boolean running)
    {
        this.running = running;
    }
    
    /* Game score. */
    
    public int getScore()
    {
        return score;
    }

    public void setScore(int score)
    {
        this.score += score;
    }
    public int getPoint()
    {
        return point;
    }

    public void setPoint(int score)
    {
        this.point += score;
    }
    
    /**
     * 
     * Public methods.
     */
    
    // Clamp, so player won't get offset the display bound.
    public static int clamp(int var, int min, int max)
    {
        if(var >= max)
        {
            return var = max;
        }
        else if(var <= min)
        {
            return var = min;
        }
        
        return var;
    }
    
    // Close display.
    public void close()
    {
        display.close();
    }
    
    /**
     * 
     * Game controller.
     */
    
    // Start threading.
    public synchronized void start()
    {
        thread = new Thread(this);
        thread.start(); running = true;
        
    }
    
    // Stop threading.
    public synchronized void stop()
    {
        try
        {
            thread.join();
            running = false;
        }
        catch(InterruptedException e)
        {
            System.out.println("Thread error : " + e.getMessage());
        }
    }
    
    // Initialize game when it run for the first time.
    public void render()
    {
        // Use buffer strategy.
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null)
        {
            this.createBufferStrategy(SOMEBITS);
            return;
        }
        
        // Initialize graphics.
        Graphics g = bs.getDrawGraphics();
        Image bg = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/assets/game.jpg"));
        g.drawImage(bg, 0, 0, null);
        
        if(running == true)
        {
            // Render handler.
            handler.render(g);
            
            // Render score.
            Font oldFont = g.getFont();
            Font newFont = oldFont.deriveFont(oldFont.getSize() * 1.3f);
            g.setFont(newFont);
            
            g.setColor(Color.blue);
            g.drawString("Score : " + Integer.toString(score), 20, 30);
            g.drawString("Point : " + Integer.toString(point), 20, 50);
            
            
        }
        
        // Loop the process so it seems like "FPS".
        g.dispose();
        bs.show();
    }
    
    // Main loop proccess.
    public void loop()
    {
        GameObject player = null;
        
        handler.loop();
        if(this.running)
        {   
            counter++;
            if(startCounting)
            {
                stateCounter++;
            }
            
            if(stateCounter >= 40)
            {
                stateCounter = 0;
                startCounting = false;
            }
            
            if(counter >= 50)
            {
                direction = (direction == 0) ? 1 : 0;
                counter = 0;
            }
            
            for(int i = 0; i < handler.count(); i++)
            {
                if(handler.get(i).getType().equals("Player"))
                {
                    player = handler.get(i);
                    temp1 = handler.get(0);
                }
                if(handler.get(i).getType().equals("Rectangle"))
                {
                    temp2 = handler.get(i);
                }
            }
            Rectangle r = new Rectangle(temp1.x,temp1.y,30,30);
            Rectangle p = new Rectangle(temp2.x,temp2.y,50,50);

            // Assuming there is an intersect method, otherwise just handcompare the values
            if (r.intersects(p))
            {
                System.out.println("bersentuhan");
                handler.remove(temp2);
                int lebar = rand.nextInt(400);
                int tinggi = rand.nextInt(300);
                handler.add(new Kotak(tinggi, lebar));
                setPoint(1);
            }
        }
    }
    
    /**
     * 
     * Override interface.
     */
    
    @Override
    public void run()
    {
        double fps = 60.0;
        double ns = (1000000000 / fps);
        double delta = 0;
        
        // Timer attributes.
        long time = System.nanoTime();
        long now = 0;
        long timer = System.currentTimeMillis();
        
        int frames = 0;
        while(running)
        {
            now = System.nanoTime();
            delta += ((now - time) / ns);
            time = now;
            
            while(delta > 1)
            {
                loop();
                delta--;
            }
            
            if(running)
            {
                render();
                frames++;
            }
            
            if((System.currentTimeMillis() - timer) > 1000)
            {
                timer += 1000;
                frames = 0;
            }
        }
        
        stop();
    }
}
