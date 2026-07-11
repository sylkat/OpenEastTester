package lcr.business;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Consumer thread runnable that continuously pulls measurement data from a
 * blocking queue and records it into a CSV file. Includes a drainage phase on shutdown.
 * * @author sylkat
 */
public class CSVRecordingRunnable implements Runnable {

    private final File targetFile;
    private final BlockingQueue<String[]> dataQueue;
    private volatile boolean running = true;

    /**
     * Constructs a CSV recording worker.
     * * @param targetFile the destination file where CSV data will be saved
     * @param dataQueue  the thread-safe queue containing measurement data frames
     */
    public CSVRecordingRunnable(File targetFile, BlockingQueue<String[]> dataQueue) {
        this.targetFile = targetFile;
        this.dataQueue = dataQueue;
    }

    /**
     * Signals the recording loop to stop gracefully.
     */
    public void stopRecording() {
        this.running = false;
    }

    /**
     * Executes the main consumer loop, processing streaming data packets,
     * followed by flushing out remaining elements upon shutdown.
     */
    @Override
    public void run() {
        try (FileWriter fw = new FileWriter(targetFile, false);
             BufferedWriter writer = new BufferedWriter(fw)) {

            writer.write("Timestamp,Primary_Value,Secondary_Value");
            writer.newLine();

            while (running) {
                try {
                    String[] row = dataQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (row != null) {
                        writer.write(row[0] + "," + row[1] + "," + row[2]);
                        writer.newLine();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            }

            while (!dataQueue.isEmpty()) {
                String[] row = dataQueue.poll();
                if (row != null) {
                    writer.write(row[0] + "," + row[1] + "," + row[2]);
                    writer.newLine();
                }
            }

            writer.flush();
            System.out.println("CSV Worker: Recording finished and file saved securely.");

        } catch (IOException e) {
            System.err.println("Critical IO Error inside CSV Recording Thread: " + e.getMessage());
        }
    }
}