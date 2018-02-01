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

    private int numberOfColumns = 6;
    private int columnWidth;
    private int columns[];
    private int rows[];

    private List<block> blockList;
    private Random rm = new Random();
    private int i = 0;
    private int MAX_TO_BOX = 70;

    public GameView(final Context context) {
        super(context);
        holder = getHolder();
        blockList = new ArrayList<block>();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                WIDTH = self.getWidth();
                columnWidth = WIDTH / numberOfColumns;
                columns = new int[numberOfColumns];
                rows = new int[numberOfColumns];
                for (int i = 0; i < numberOfColumns; i++) {
                    columns[i] = columnWidth * i;
                    rows[i] = self.getHeight();
                }
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

    private void addBlock() {
        i++;
        if (i > MAX_TO_BOX) {
            int random = rm.nextInt(numberOfColumns);
            block blo = new block(this, random, columnWidth);
            blockList.add(blo);
            i = 0;
            if (MAX_TO_BOX > 20) {
                MAX_TO_BOX--;
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.BLACK);
        addBlock();
        for (block blo : blockList) {
            blo.draw(canvas);
        }
        playerSprite.draw(canvas);
    }

    public int[] getColumns() {
        return columns;
    }

    public int[] getRows() {
        return rows;
    }

    public void setRows(int index, int value) {
        rows[index] -= value;
    }

    public int isTouchingGround(int newX, int newY, int width) {
        int wich = numberOfColumns - 1;
        boolean both = false;
        for (int i = 0; i < numberOfColumns; i++) {
            if (newX < columns[i]) {
                if (newX + width > columns[i]) {
                    both = true;
                }
                if (i == 0) {
                    wich = 0;
                } else {
                    wich = i - 1;
                }
                break;
            }
        }
        if (both) {
            if (rows[wich] < rows[wich + 1]) {
                if (newY >= rows[wich]) {
                    return rows[wich];
                }
            } else {
                if (newY >= rows[wich + 1]) {
                    return rows[wich + 1];
                }
            }
        }
        if (newY >= rows[wich]) {
            return rows[wich];
        } else {
            return -1;
        }
    }
}
