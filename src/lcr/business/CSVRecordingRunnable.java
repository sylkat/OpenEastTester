package lcr.business;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class CSVRecordingRunnable implements Runnable {

    private final File targetFile;
    private final BlockingQueue<String[]> dataQueue;
    private volatile boolean running = true; // Controls the consumer loop state from outside

    public CSVRecordingRunnable(File targetFile, BlockingQueue<String[]> dataQueue) {
        this.targetFile = targetFile;
        this.dataQueue = dataQueue;
    }

    /**
     * Public method to stop the recording thread gracefully.
     */
    public void stopRecording() {
        this.running = false;
    }

    @Override
    public void run() {
        try (FileWriter fw = new FileWriter(targetFile, false);
             BufferedWriter writer = new BufferedWriter(fw)) {

            // Standard instrumentation CSV header layout
            writer.write("Timestamp,Primary_Value,Secondary_Value");
            writer.newLine();

            // 1. Live Capturing Phase: Process incoming data packets as they arrive
            while (running) {
                try {
                    // Poll data from the queue with a short timeout to prevent thread lockups on exit
                    String[] row = dataQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (row != null) {
                        writer.write(row[0] + "," + row[1] + "," + row[2]);
                        writer.newLine();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running = false; // Break the active lifecycle if the runtime triggers a hard shutdown
                }
            }

            // 2. Drainage Phase: Flush out any remaining buffered data packets inside the queue safely
            while (!dataQueue.isEmpty()) {
                String[] row = dataQueue.poll(); // Instant poll without timeouts since no new data is expected
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