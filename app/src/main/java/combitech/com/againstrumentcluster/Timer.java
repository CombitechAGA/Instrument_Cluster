package combitech.com.againstrumentcluster;

public class Timer implements Runnable {
    private long newFrame = 0;
    private long oldFrame = 0;
    private long elapsedTime = 0;
    private long deadline = 0;
    private boolean repeat = false;
    private boolean isRunning;
    private Thread timerThread;
    private Runnable onDeadline;

    public Timer(Runnable onDeadline, long deadline) {
        if (onDeadline == null) {
            throw new NullPointerException("Null Runnable in constructor");
        }
        this.onDeadline = onDeadline;
        this.deadline = deadline;
    }

    public Timer(Runnable onDeadline) {
        this(onDeadline, 0);
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public void start() {
        elapsedTime = 0;
        timerThread = new Thread(this, "Timer Thread");
        timerThread.setDaemon(true);
        timerThread.start();
    }

    public void stop() {
        if (timerThread != null) {
            isRunning = false;
            timerThread.interrupt();
            try {
                timerThread.join();
                timerThread = null;
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void run() {
        isRunning = true;
        if (repeat) {
            runLoop();
        } else {
            runOnce();
        }
    }

    private void runLoop() {
        newFrame = System.currentTimeMillis();
        oldFrame = System.currentTimeMillis();
        while (isRunning) {
            while (elapsedTime < deadline) {
                try {
                    newFrame = System.currentTimeMillis();
                    elapsedTime += newFrame - oldFrame;
                    oldFrame = newFrame;
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    if (!isRunning) {
                        break;
                    }
                    continue;
                }
            }
            elapsedTime = elapsedTime - deadline;
            if (isRunning)
                new Thread(onDeadline).start();
        }
    }

    private void runOnce() {
        newFrame = System.currentTimeMillis();
        oldFrame = System.currentTimeMillis();
        while (elapsedTime < deadline) {
            try {
                newFrame = System.currentTimeMillis();
                elapsedTime += newFrame - oldFrame;
                oldFrame = newFrame;
                Thread.sleep(1);
            } catch (InterruptedException e) {
                if (!isRunning) {
                    break;
                }
                continue;
            }
        }
        if (isRunning)
            new Thread(onDeadline).start();
    }
}
