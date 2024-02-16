import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class Orbit2 extends Plot implements Runnable{
    static double rMax = 5; // max range of plot
    double tolerance;
    double x1, x2;
    double y1, y2;
    double v1x, v2x;
    double v1y, v2y;
    double a1x, a2x;
    double a1y, a2y;
    double totalEnergy;
    Image savedOffscreenImage;
    boolean running;
    double m1, m2;
    Orbit2() {
        super("Orbits",-rMax, rMax, 1, -rMax, rMax, 1);
        // initialize variables and start simulation thread
        this.tolerance = 0.02;
        this.x1 = 1;
        this.y1 = 0;
        this.v1x =0;
        this.v1y = 2*Math.PI/Math.sqrt(1);
        this.x2 = 1.5;
        this.y2 = 0;
        this.v2x = 0;
        this.v2y = 2*Math.PI/Math.sqrt(1.5);
        this.m1 = 0.02;
        this.m2 = 0.02;
        this.totalEnergy = this.totalE();
        System.out.print("Energy: ");
        System.out.println(this.totalEnergy);
        a1x = this.ax(1);
        a1y = this.ay(1);
        a2x = this.ax(2);
        a2y = this.ay(2);
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

    private double ax(int n) {
        double G = 4 * Math.PI * Math.PI;
        double x, y, m;
        int sgn = 1;
        if (n == 1) {
            x = this.x1;
            y = this.y1;
            m = this.m1;
        } else {
            x = this.x2;
            y = this.y2;
            m = this.m2;
            sgn = -1;
        }
        return -G * x * Math.pow((x * x + y * y), -1.5) + sgn / m * this.interaction() * (this.x2 - this.x1);
    }

    private double ay(int n) {
        double G = 4 * Math.PI * Math.PI;
        double x, y, m;
        int sgn = 1;
        if (n == 1) {
            x = this.x1;
            y = this.y1;
            m = this.m1;
        } else {
            x = this.x2;
            y = this.y2;
            m = this.m2;
            sgn = -1;
        }
        return -G * y * Math.pow((x * x + y * y), -1.5) + sgn / m * this.interaction() * (this.y2 - this.y1);
    }

    private double totalE() {
        double G = 4 * Math.PI * Math.PI;
        double kinE1 = 0.5 * this.m1 * (this.v1x * this.v1x + this.v1y * this.v1y);
        double kinE2 = 0.5 * this.m2 * (this.v2x * this.v2x + this.v2y * this.v2y);
        double potE1 = - G * this.m1 * Math.pow((this.x1 * this.x1 + this.y1 * this.y1), -0.5);
        double potE2 = - G * this.m2 * Math.pow((this.x2 * this.x2 + this.y2 * this.y2), -0.5);
        double potE12 = - G * this.m1 * this.m2 * Math.pow(((this.x2 - this.x1)*(this.x2 - this.x1) + (this.y2 - this.y1)*(this.y2 -this.y1)), -0.5);
        return kinE1 + potE1 + kinE2 + potE2 + potE12;
    }

    synchronized void doStep() {
        // clean images of planets
        offScreenGraphics.drawImage(savedOffscreenImage, 0, 0, this);
        // execute one step of simulation
        double dt1 = this.tolerance * Math.pow((this.a1x*this.a1x + this.a1y*this.a1y), -0.5);
        double dt2 = this.tolerance * Math.pow((this.a2x*this.a2x + this.a2y*this.a2y), -0.5);
        double dt = Math.min(dt1, dt2);
        this.x1 += this.v1x * dt + 0.5 * this.a1x * dt * dt;
        this.y1 += this.v1y * dt + 0.5 * this.a1y * dt * dt;
        this.x2 += this.v2x * dt + 0.5 * this.a2x * dt * dt;
        this.y2 += this.v2y * dt + 0.5 * this.a2y * dt * dt;
        this.v1x += 0.5 * this.a1x * dt;
        this.v1y += 0.5 * this.a1y * dt;
        this.v2x += 0.5 * this.a2x * dt;
        this.v2y += 0.5 * this.a2y * dt;
        this.a1x = this.ax(1);
        this.a1y = this.ay(1);
        this.a2x = this.ax(2);
        this.a2y = this.ay(2);
        this.v1x += 0.5 * this.a1x * dt;
        this.v1y += 0.5 * this.a1y * dt;
        this.v2x += 0.5 * this.a2x * dt;
        this.v2y += 0.5 * this.a2y * dt;
        this.setColor(Color.red);
        this.addPoint(this.x1, this.y1);
        this.setColor(Color.green);
        this.addPoint(this.x2, this.y2);
        this.totalEnergy = this.totalE();
        // save image without the planets
        savedOffscreenImage.getGraphics().drawImage(offScreenImage, 0, 0, this);
        this.showEnergy();
        this.showPlanets();
    }

    private void showPlanets() {
        // draw planets at current position
        int size = 7;
        int pixelx = (int) Math.round(plotWidth * (x1-xMin) / xRange);	// convert x1 to a screen coordinate
        int pixely = (int) Math.round(plotHeight * (yMax-y1) / yRange);
        int offset = (int) (size/2.0);
        offScreenGraphics.setColor(Color.blue);
        offScreenGraphics.drawOval(pixelx - offset, pixely - offset, size - 1, size - 1);
        pixelx = (int) Math.round(plotWidth * (x2-xMin) / xRange);	// convert x2 to a screen coordinate
        pixely = (int) Math.round(plotHeight * (yMax-y2) / yRange);
        offScreenGraphics.drawOval(pixelx - offset, pixely - offset, size - 1, size - 1);
        offScreenGraphics.setColor(this.pointColor);
        repaint();
    }

    private void showEnergy() {
        offScreenGraphics.setColor(Color.black);
        offScreenGraphics.drawString("Energy = " + this.totalEnergy,0, 380);
        offScreenGraphics.setColor(this.pointColor);
    }
    public synchronized void clearThePlot() {
       super.clearThePlot();
       this.setColor(Color.yellow);
       this.setPointSize(15);
       super.addPoint(0, 0);
       this.setPointSize(1);
       this.setColor(Color.red);
       if (savedOffscreenImage != null) {
           this.savedOffscreenImage.getGraphics().drawImage(this.offScreenImage, 0, 0, this);
       }
    }

    private double interaction() {
        double G = 4 * Math.PI * Math.PI;
        return G * this.m1 * this.m2 * Math.pow(((this.x2 - this.x1)*(this.x2 - this.x1) + (this.y2 - this.y1)*(this.y2 - this.y1)), -1.5);
    }
    public static void main(String[] args) {
        new Orbit2();
    }
}
