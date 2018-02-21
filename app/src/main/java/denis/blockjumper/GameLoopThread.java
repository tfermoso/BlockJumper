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
    private final long FPS = 60;
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
            System.out.println("");
            System.out.print("hola1");
            Canvas c = null;
            startTime = System.currentTimeMillis();
            try {
                c = view.getHolder().lockCanvas();
                System.out.print("Hola1.1");
                synchronized (view.getHolder()) {
                    if (c != null) {
                        System.out.print("Hola1.2");
                        view.draw(c);
                        System.out.print("test");
                    } else {
                        c = view.getHolder().lockCanvas();
                    }
                    System.out.print("Hola1.66");
                }
                System.out.print("Hola1.3");
            } finally {
                if (c != null) {
                    view.getHolder().unlockCanvasAndPost(c);
                }
                System.out.print("Hola1.4");
            }
            sleepTime = tiksPS - (System.currentTimeMillis() - startTime);
            System.out.print("hola2");
            try {
                if (sleepTime > 20) {
                    Thread.sleep(sleepTime);
                } else {
                    Thread.sleep(20);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.print("Hola3");
        }

        System.out.println("He dejado de funcionar");
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        view.finalStop();
    }
}
