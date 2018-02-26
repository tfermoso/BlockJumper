package denis.blockjumper;

import android.graphics.Canvas;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.List;
import java.util.Random;

/**
 * Created by denis.couñago on 10/01/2018.
 */

public class GameLoopThread extends AsyncTask {
    private final long FPS = 40;
    private GameView view;
    private boolean running = false;

    public GameLoopThread(GameView view) {
        this.view = view;
    }

    public void setRunning(boolean running) {
        System.out.println("Seteando runing "+running);
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        System.out.println("Corriendo!");
        long tiksPS = 1000 / FPS;
        long startTime;
        long sleepTime;

        while (running) {
            Canvas c = null;
            startTime = System.currentTimeMillis();
            try {
                c = view.getHolder().lockCanvas();
                synchronized (view.getHolder()) {
                    if (c != null) {
                        view.draw(c);
                    } else {
                        c = view.getHolder().lockCanvas();
                    }
                }
            } finally {
                if (c != null) {
                    view.getHolder().unlockCanvasAndPost(c);
                }
            }
            sleepTime = tiksPS - (System.currentTimeMillis() - startTime);
            try {
                if (sleepTime > 20) {
                    Thread.sleep(sleepTime);
                } else {
                    Thread.sleep(20);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        view.finalStop();
        System.out.println("He dejado de funcionar");
    }
}
