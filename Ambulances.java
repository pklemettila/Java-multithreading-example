package ProducerConsumerAmbulanssi;

import java.util.*;

import static java.lang.Thread.currentThread;


class Incident {          

    private int incNo;
    private int incLocX, incLocY;
    long thisMoment;
    public Incident(int incNo_p, int incLocX_p, int incLocY_p ) {  // Constructor for incident objects, also records current time for later calculation purposes
        Random rndgen = new Random();
        incNo = incNo_p;
        incLocX = rndgen.nextInt(incLocX_p+1);
        incLocY = rndgen.nextInt(incLocY_p+1);
        thisMoment = System.currentTimeMillis();
    }


    public int getIncLocX() {
        return incLocX;
    }

    public void setIncLocX(int incLocX) {
        this.incLocX = incLocX;
    }

    public int getIncLocY() {
        return incLocY;
    }

    public void setIncLocY(int incLocY) {
        this.incLocY = incLocY;
    }

    public String toString(){
        return "Incident number " + incNo;
    }



}

class Ambulance extends Consumer implements Runnable {

    private String name = "Ambulance";
    private int speed;
    private int hospitalX, hospitalY;
    private static int secondsPassed = 0;
    public static int incidentsCompleted = 0;
    private static double averageTimeComplete;
    private static double averageWaitTime;
    private static double totalWaitTime;
    private static double elapsedTime;
    private String state;


    public Ambulance(CircularBuffer cb, int givenSpeed, int id, int hosplocX, int hosplocY) { // constructor for Ambulance
        super(cb);
        name = name+ " " + Integer.toString(id);
        speed = givenSpeed;
        hospitalX=hosplocX;
        hospitalY=hosplocY;
    }

    public String getState() {         // returns current state
        return this.state;
    }

    protected void consume(Object obj) {    // Consumes the Incident from CB and simulates patient pickup
        Incident c = (Incident) obj;
        System.out.println(name + " starts heading to the location of " + c + " \n");
        this.state = "Heading to site";
        float secondsNeeded = Math.abs((c.getIncLocX()-hospitalX)+Math.abs(c.getIncLocY()-hospitalY))/speed;

        try {
            Thread.sleep(1000 * (long)secondsNeeded);
        } catch (InterruptedException ie) {
            currentThread().interrupt();
        }
        totalWaitTime = totalWaitTime+(System.currentTimeMillis()-(c.thisMoment))/1000;
        System.out.println(name + " is on site of " + c + "\n");
        this.state = "On site";
        System.out.println(name + " is moving back to hospital \n");
        this.state = "Moving to hospital";

        try {
            Thread.sleep(1000 * (int)secondsNeeded);
        } catch (InterruptedException ie) {
            currentThread().interrupt();
        }
        System.out.println(name + " is transferring the patient from the ambulance to the hospital \n");
        this.state = "Transferring patient";

        try {
            Thread.sleep(10000);
        } catch (InterruptedException ie) {
            currentThread().interrupt();
        }
        elapsedTime = (System.currentTimeMillis()-(c.thisMoment))/1000;
        System.out.println(name + " completed " + c + " successfully. " + elapsedTime + " seconds since incident. \n");
        secondsPassed = secondsPassed + (int)elapsedTime;
        incidentsCompleted++;

        if(incidentsCompleted>=20) {   // Printing recorded results
            System.out.println("Total seconds spent in operations: " + secondsPassed);
            averageTimeComplete = secondsPassed/incidentsCompleted;
            System.out.println("Average seconds spent in operations: " + averageTimeComplete);
            System.out.println("Total wait time: " + totalWaitTime);
            averageWaitTime = totalWaitTime/incidentsCompleted;
            System.out.println("Average wait time: " + averageWaitTime);

        }


    }

    public void run() {   // Runs the thread and launches the Consume method
        Object obj;



        while (incidentsCompleted < 20) {


            try {

                if (getNumStored() == 0)
                    System.out.println(name + " is idle.\n");
                this.state = "Idle";

                obj = takeObject();

                consume(obj);

            } catch (InterruptedException ie) {
                break;
            }

        }

        System.out.println("Incidents completed " + incidentsCompleted + "\n");
        System.exit(-1);
    }

}

class IncidentHappening extends Producer implements Runnable {   // constructor for an Incident event
    private int maxAppearTime = 6;
    public static int incidentNum = 0;
    private Random rndGen;
    private int inclocX, inclocY;
    public static boolean ShouldWeRunIt;

    public IncidentHappening(CircularBuffer cb, int inclocx, int inclocy) {
        super(cb);
        inclocX = inclocx;
        inclocY = inclocy;
        rndGen = new Random();
        ShouldWeRunIt = true;
        incidentNum = 0;


    }

    protected Object produce() {           // Produces an incident object to the CB

        Object obj;

            try {
                Thread.sleep(1000 + Math.abs(rndGen.nextInt(1000 * maxAppearTime)));
            } catch (InterruptedException ie) {
                ;
            }

            incidentNum++;

            obj = new Incident(incidentNum, inclocX, inclocY);
            return obj;
    }



    public void run() {    // Runs the thread of incident producing

        Incident c;

        while (incidentNum < 20) {


            try {
                c = (Incident) produce();
                putObject(c);

                System.out.println("" + c + " appeared at (" + c.getIncLocX() + "," + c.getIncLocY() + ") \n");

            } catch (InterruptedException ie) {
                break;
            }
        }


    }
}


public class Ambulances {  // Main class
    static IncidentHappening aQueue = null;
    static CircularBuffer lances = null;


    public static void main(String[] argv) throws InterruptedException { // Main method

        int aCount = 5, aSpeed = 4, MaxLocX = 10, MaxLocY = 15, hospitalX = 5, hospitalY = 5; // Assigns meaningless values to parameters


            Random random = new Random();          // Parameters are randomly generated within arbitrary bonds
            aCount = random.nextInt(10) + 1;
            System.out.println("Amount of ambulances is " + aCount);
            aSpeed = random.nextInt(5) + 1;
            System.out.println("Ambulance speed is " + aSpeed);
            MaxLocX = random.nextInt(20) + 1;
            System.out.println("Max range of incidents X is " + MaxLocX);
            MaxLocY = random.nextInt(20) + 1;
            System.out.println("Max range of incidents Y is " + MaxLocY);
            hospitalX = random.nextInt(20) + 1;
            System.out.println("Location of hospital X is " + hospitalX);
            hospitalY = random.nextInt(20) + 1;
            System.out.println("Location of hospital Y is " + hospitalY);

            try {
                lances = new CircularBuffer(8); // Creating a new CB

            } catch (IllegalArgumentException iae) {
                System.out.println("Invalid construction of circular buffer ");
                iae.printStackTrace();

                System.exit(-1);
            }

            Thread threads[] = new Thread[aCount];
            aQueue = new IncidentHappening(lances, MaxLocX, MaxLocY);
            Thread incidentThread = new Thread(aQueue);
            incidentThread.start();   // Starting the incident occurrence thread


            for (int i = 0; i < threads.length; i++) {      // Creating the ambulances with knowledge of hospital location
                threads[i] = new Thread(new Ambulance(lances, aSpeed, i + 1, hospitalX, hospitalY));
            }

            for (int i = 0; i < threads.length; i++) {
                threads[i].start();

            }


            incidentThread.join();


            }





    }


