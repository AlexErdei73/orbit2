import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class Orbit2 extends Plot implements Runnable{
    static double rMax = 2; // max range of plot
    double tolerance;
    double x1;
    double y1;
    double v1x;
    double v1y;
    double a1x;
    double a1y;
    double totalEnergy;
    Image savedOffscreenImage;
    boolean running;
    Orbit2() {
        super("Orbits",-rMax, rMax, 1, -rMax, rMax, 1);
        // initialize variables and start simulation thread
        this.tolerance = 0.001;
        this.x1 = 1;
        this.y1 = 0;
        this.v1x = 0;
        this.v1y = 0.75*2*Math.PI;
        this.totalEnergy = this.totalE();
        System.out.print("Energy: ");
        System.out.println(this.totalEnergy);
        a1x = this.ax();
        a1y = this.ay();
        this.setPointSize(1);
        this.savedOffscreenImage = createImage(this.plotHeight + 1, this.plotWidth + 1);
        this.savedOffscreenImage.getGraphics().drawImage(this.offScreenImage, 0, 0, this);
        this.running = false;
        Button startStopButton = new Button("Start");
        startStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!running) {
                    running = true;
                    startStopButton.setLabel("Stop");
                } else {
                    running = false;
                    startStopButton.setLabel("Start");
                }
            }
        });
        this.controlPanel.add(startStopButton);
        this.plotFrame.pack();
        Thread simulationThread = new Thread(this);
        simulationThread.start();
    }

    public void run() {
        while (true) {
            if (this.running) doStep();
            try {
                Thread.sleep(10);
            } catch(InterruptedException e) {}
        }
    }

    private double ax() {
        double G = 4 * Math.PI * Math.PI;
        return -G * this.x1 * Math.pow((this.x1 * this.x1 + this.y1 * this.y1), -1.5);
    }

    private double ay() {
        double G = 4 * Math.PI * Math.PI;
        return -G * this.y1 * Math.pow((this.x1 * this.x1 + this.y1 * this.y1), -1.5);
    }

    private double totalE() {
        double G = 4 * Math.PI * Math.PI;
        double kinE = 0.5 * (this.v1x * this.v1x + this.v1y * this.v1y);
        double potE = - G * Math.pow((this.x1 * this.x1 + this.y1 * this.y1), -0.5);
        return kinE + potE;
    }

    synchronized void doStep() {
        // execute one step of simulation
        double dt = this.tolerance * (this.x1*this.x1 + this.y1*this.y1);
        this.x1 += this.v1x * dt + 0.5 * this.a1x * dt * dt;
        this.y1 += this.v1y * dt + 0.5 * this.a1y * dt * dt;
        this.v1x += 0.5 * this.a1x * dt;
        this.v1y += 0.5 * this.a1y * dt;
        this.a1x = this.ax();
        this.a1y = this.ay();
        this.v1x += 0.5 * this.a1x * dt;
        this.v1y += 0.5 * this.a1y * dt;
        this.addPoint(this.x1, this.y1);
    }

    public void addPoint(double x, double y) {
        offScreenGraphics.drawImage(savedOffscreenImage, 0, 0, this);
        super.addPoint(x, y);
        savedOffscreenImage.getGraphics().drawImage(offScreenImage, 0, 0, this);
        // draw planets at current position
        int size = 7;
        int pixelx = (int) Math.round(plotWidth * (this.x1-xMin) / xRange);	// convert x1 to a screen coordinate
        int pixely = (int) Math.round(plotHeight * (yMax-this.y1) / yRange);
        int offset = (int) (size/2.0);
        offScreenGraphics.setColor(Color.blue);
        offScreenGraphics.drawOval(pixelx - offset, pixely - offset, size - 1, size - 1);
        offScreenGraphics.setColor(this.pointColor);
        repaint();
    }

    public synchronized void clearThePlot() {
       super.clearThePlot();
       if (savedOffscreenImage != null) this.savedOffscreenImage.getGraphics().drawImage(this.offScreenImage, 0, 0, this);
    }

    public static void main(String[] args) {
        new Orbit2();
    }
}