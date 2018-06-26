import sun.hotspot.WhiteBox;
import java.util.concurrent.CyclicBarrier;
import jdk.internal.misc.Unsafe;



class RTM {
  private static final Unsafe UNSAFE = Unsafe.getUnsafe();
  protected final Object monitor = new Object();
  WhiteBox wb;
  Thread thread0;
  Thread thread1; // conflicting thread
  Runnable work0;
  Runnable work1; // conflicting task
  CyclicBarrier barrier;
  private static int sharedVariable = 0;


  void isMonitorInflated() {
    wb = WhiteBox.getWhiteBox();
    System.out.println("Is monitor inflated? " + (wb.isMonitorInflated(this.monitor) ? "Yes" : "No"));
  }

  void inflateMonitor() throws Exception {
   barrier = new CyclicBarrier(2);

   work0 = () -> {
      synchronized (monitor) {
      try {
         barrier.await();
      } catch (Exception e) {
        System.out.println("0");

      }


      try {
        monitor.wait();
      } catch (Exception e) {
        System.out.println("2");
      }

      }

    };


    System.out.println("Creating thread0...");

    thread0 = new Thread(work0);
    thread0.setDaemon(true);
    thread0.start();


  barrier.await();

    System.out.println("Trying to inflate lock...");
    synchronized (monitor) {
       sharedVariable++;
       }
    }



   void causeConflict() throws Exception {
   work1 = () -> {
      try {
          System.out.println("Entering thread to sleep...");
          synchronized (monitor) {
              barrier.await();
              Thread.sleep(1000); // 1s
          }
      } catch (Exception e) {
          System.out.println("fail #100");
      }
  };

  thread1 = new Thread(work1);

  // Grab lock and don't release before 5s
  thread1.start();

  // Try to grab the lock from another thread
  syncAndTest();

  thread1.join();
  }

  public void syncAndTest() {
      try {
          barrier.await();
      } catch (Exception e) {
          System.out.println("fail #101");
      }
      synchronized (monitor) {
          sharedVariable++;
      }
  }


}
class retry  {
  public static void main(String[] args) throws Exception {


   RTM rtm = new RTM();
   rtm.inflateMonitor();
   rtm.isMonitorInflated();
   rtm.causeConflict();

  }

}
