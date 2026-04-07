import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Bộ đếm thời gian hiển thị lên JLabel.
 */
public class Feature {
    private final JLabel label;
    private Timer timer;
    private int seconds;

    public Feature(JLabel label) {
        this.label = label;
        this.seconds = 0;
        updateLabel();
    }

    /**
     * Bắt đầu đếm thời gian.
     */
    public void start() {
        stop();
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                seconds++;
                SwingUtilities.invokeLater(Feature.this::updateLabel);
            }
        }, 1000, 1000);
    }

    /**
     * Dừng timer.
     */
    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Reset thời gian về 0.
     */
    public void reset() {
        seconds = 0;
        updateLabel();
    }

    public int getTime() {
        return seconds;
    }

    private void updateLabel() {
        int mins = seconds / 60;
        int secs = seconds % 60;
        label.setText(String.format("%02d:%02d", mins, secs));
    }
}
