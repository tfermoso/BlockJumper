package denis.blockjumper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Created by denis.couñago on 10/01/2018.
 */

public class GameView extends SurfaceView {
    private SurfaceHolder holder;
    private GameLoopThread gameLoopThread;
    private GameView self = this;
    private PlayerSprite playerSprite;
    public int GRAVITY = 50;
    private int WIDTH;
    private List<block> blockList;

    public GameView(final Context context) {
        super(context);
        holder = getHolder();
        blockList = new ArrayList<block>();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                WIDTH = self.getWidth();

                if (gameLoopThread == null || !gameLoopThread.isRunning()) {
                    gameLoopThread = new GameLoopThread(self);
                }
                if (gameLoopThread.getState() == Thread.State.NEW) {
                    playerSprite = new PlayerSprite(self);
                    gameLoopThread.setRunning(true);
                    gameLoopThread.start();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                gameLoopThread.setRunning(false);
                while (retry) {
                    try {
                        gameLoopThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

    }

    public List<block> getBlockList() {
        return blockList;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int pointerIndex = event.getActionIndex();
                int pointerId = event.getPointerId(pointerIndex);
                float x;
                try {
                    x = event.getX(pointerId);
                } catch (IllegalArgumentException ex) {
                    x = event.getX();
                }
                int each = WIDTH / 3;
                if (x < each) {
                    if (playerSprite.getMoving() != -1) {
                        playerSprite.move(-1);
                    }
                } else if (x > each * 2) {
                    if (playerSprite.getMoving() != 1) {
                        playerSprite.move(1);
                    }
                } else {
                    playerSprite.jump();
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
                playerSprite.move(0);
                return false;

        }
        return super.onTouchEvent(event);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.BLACK);
        playerSprite.draw(canvas);
        for (block blo : blockList) {
            blo.draw(canvas);
        }
    }
}
