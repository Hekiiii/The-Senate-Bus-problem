/*
 * The program contains a solution for Senate bus problem usng semaphores.
 * 120323L  120279F
 */
package lab_03;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Lab_03 {

    static int waiting = 0;                     //A variable to store the number of riders waiting in the boarding area
    static Semaphore mutex = new Semaphore(1);  //To protect waiting_riders
    static Semaphore bus = new Semaphore(0);    //Sgnals when the bus has arrived
    static Semaphore boarded = new Semaphore(0);//Signals that a rider has boarded

    /*
     * An inner class to represent an instance of a bus
     */
    class Bus implements Runnable {

        private int riders_to_board;   //A variable to hold the number of riders available for the bus
        private int bus_id;       
        private int rider_count = 0;   //A variable to hold the number of riders boarded into the bus

        /*
        Each bus is assigned a unique identifier so that it is easy to demonstrate the procedure when there are more than one bus. 
        */
        Bus(int index) {
            this.bus_id = index;
        }

        @Override
        public void run() {
            try {
                mutex.acquire();                             //bus locks the mutex
                System.out.println("Bus " + bus_id + " locked the bus stop !!");//The number of riders available for the bus is 50 out of all the passengers in the boarding area. If the waiting passengers is less than 50, then all can get in.
                
                riders_to_board = Math.min(waiting, 50);   
                System.out.println("waiting  : " + waiting + " To board : "+riders_to_board);   
                
                for (int i = 0; i < riders_to_board; i++) {     //A loop to get all the available riders  on board 
                    System.out.println("Bus " + bus_id + " released for "+i+"th rider");
                    bus.release();                              //bus signals that it has arrived and can take a passenger on board
                    boarded.acquire();                          //Allows one rider to get on board
                    System.out.println("Bus " + bus_id + " acquired boarded !!!!! !!");
                }
                
                /*
                If the number of riders waiting earlier was greater than 50, then the remainig riders is waiting_riders - 50
                Else if the number of riders who were waiting earlier was less than 50, then all of them got on board. therefore the number of remaining riders is 0.
                */
                waiting = Math.max((waiting - 50), 0);    
                mutex.release();                             //bus unlocks the mutex   
                
            } catch (InterruptedException ex) {              //Exception if the above procedure got interupted in the middle
                Logger.getLogger(Lab_03.class.getName()).log(Level.SEVERE, "Bus " + bus_id + "'s thread got interrupted !!", ex);
            }
            
            System.out.println("Bus " + bus_id + " departed with " + riders_to_board + " riders on board!");                                    
        }
    }

    /*
     * Rider Java inner class to represent an instance of a rider 
     */
    class Rider implements Runnable {

        private int rider_id;

        /*
        Eachrider is assigned a unique identifier so that it is easy to demonstrate the procedure when there are more than one bus. 
        */
        Rider(int index) {
            this.rider_id = index;
        }

        @Override
        public void run() {
            try {
                
                /*
                *   First the rider increments the waiting rider count to get into the waiting rider set.
                *   Since the waiting_riders is a shared variable, the rider sgould acquire the mutex in order to access this variable.
                */
                mutex.acquire();                //rider locks the mutex
                System.out.println("Rider " + rider_id + " is waiting !!");
                waiting += 1;                   //increment the number of waiting riders by one
                mutex.release();                //release the mutex

                bus.acquire();                  //acquire the bus semaphore to get on board
                System.out.println("Rider " + rider_id + "got onboard.");            //acquire the bus semaphore to get on board
                boarded.release();              //once boarded, release the boarded semaphore

            } catch (InterruptedException ex) { //Exception if the above procedure got interupted in the middle
                Logger.getLogger(Lab_03.class.getName()).log(Level.SEVERE, "Rider" + rider_id + "'s thread got interrupted !!", ex);

            }
        }
    }

    /**
     * Main method of the class
     *
     * @param args input parameters for the main method
     */
    public static void main(String[] args) {
        Lab_03 lab = new Lab_03();  
        int bus_id=0, rider_id=0;                           //Ids are assigned for buses and riders for the easy demonstration of the program
        long diff_bus=0,diff_rider=0,time_curr=0,time_prev_bus=System.currentTimeMillis(),time_prev_rider=System.currentTimeMillis();
        double mean_rider=30000,mean_bus=1200000;           //Declaring the mean of the exponential distributions of inter-arrival times of riders and buses
        double rand_bus  = 0.0,rand_rider=0,wait_time_rider=0,wait_time_bus=0;
        
        rand_bus = new Random().nextDouble();   
        wait_time_rider = Math.round(Math.log10(rand_bus)*-1*mean_rider);  //Calculating the time before the next bus arrives
        
        rand_rider = new Random().nextDouble();   
        wait_time_bus = Math.round(Math.log10(rand_rider)*-1*mean_bus);     //Calculating the time before the next bus arrives
        //System.out.println("rand - "+Math.round(rand*100.0)/100.0+ " wait_time_rider -  "+wait_time_rider+" wait_time_bus - "+wait_time_bus);
        
        while(Boolean.TRUE) {     
           
           time_curr=System.currentTimeMillis();
           diff_rider=time_curr-time_prev_rider;                      //Calculating the time passed after the previous rider
           diff_bus=time_curr-time_prev_bus;                          //Calculating the time passed after the previous bus
           
           
           if(diff_rider==wait_time_rider){                           //Checking if it is the time for the next rider to come
               //System.out.println("rand - "+Math.round(rand*100.0)/100.0+ " wait_time_rider -  "+wait_time_rider);
               Rider new_rider = lab.new Rider(rider_id++);
               new Thread(new_rider).start();
               time_prev_rider=time_curr;
                  
               rand_rider  = new Random().nextDouble();   
               wait_time_rider = Math.round(Math.log10(rand_rider)*-1*mean_rider); //Calculating the inter arrival time before the next rider arrives
           }
           if(diff_bus==wait_time_bus){                           //Checking if it is the time for the next bus to come
               //System.out.println("rand - "+Math.round(rand*100.0)/100.0+ " wait_time_bus - "+wait_time_bus);
               Bus new_bus = lab.new Bus(bus_id++);
               new Thread(new_bus).start();
               time_prev_bus=time_curr;
              
               rand_bus  = new Random().nextDouble();
               wait_time_bus = Math.round(Math.log10(rand_bus)*-1*mean_bus);//Calculating the inter arrival time before the next bus arrives
           }
       }
       
    }
}
