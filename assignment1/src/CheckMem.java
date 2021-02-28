import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of memory statistics collection at regular interval
 */
public class CheckMem implements Runnable {

    private int sleep_time = 5;
    private long maxmem = 0;
    private long totmem;
    private long freemem;
    private long curmem;
    private boolean runCheckMem = true;
    private boolean printCheckMem = false;

    /**
     * set the sleep time value value
     */
    public CheckMem(int sleep_time) {
        this.sleep_time = sleep_time;
    }

    /**
     * get the maximum memory used value
     */
    public long getMaxmem() {
        return maxmem;
    }

    /**
     * stop the memory statistics collection
     */
    public void stopCheckMem() {
        this.runCheckMem = false;
    }

    /**
     * activate or deactivate the memory statistics printing
     * @param printCheckMem a boolean instructing if we print out the mem stats or not
     */
    public void printCheckMem(boolean printCheckMem) {
        this.printCheckMem = printCheckMem;
    }

    /**
     * print the memory statistics
     */
    public void printMemUsage() {
        String s;
        Date date = Calendar.getInstance().getTime();
        //DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        DateFormat dateFormat = new SimpleDateFormat("HHmmss");
        String strDate = dateFormat.format(date);
        s = "CHECKMEM ==> : Cur:" + curmem + " Max:" + maxmem + " Tot:" + totmem + " Free:" + freemem + " Time " + strDate;
        System.out.println(s);
    }

    /**
     * main thread to collect the memory stats
     */
    public void run() {
        do {
            totmem = Runtime.getRuntime().totalMemory() / 1024 / 1024;
            freemem = Runtime.getRuntime().freeMemory() / 1024 / 1024;
            curmem = totmem - freemem;
            if (curmem > maxmem) {
                maxmem = curmem;
            }
            if (printCheckMem) {
                printMemUsage();
            }
            try {
                TimeUnit.SECONDS.sleep(sleep_time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (runCheckMem);

        if (printCheckMem) {
            System.out.print("Exiting CheckMem thread : ");
            printMemUsage();
        }

    }
}
