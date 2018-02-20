package denis.blockjumper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.lang.reflect.Array;
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
    private int WIDTH, HEIGHT;

    private int points = 0;

    private int numberOfColumns = 6;
    private int columnWidth;
    private int columns[];
    private ArrayList<ArrayList<block>> columnsBlock;
    private int rows[];

    private Random rm = new Random();
    private int i = 0;
    //    private int MAX_TO_BOX = 70;
    private int MAX_TO_BOX = 50;

    public GameView(final Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                WIDTH = self.getWidth();
                HEIGHT = self.getHeight();
                columnWidth = WIDTH / numberOfColumns;
                columns = new int[numberOfColumns];
                columnsBlock = new ArrayList<>();
                rows = new int[numberOfColumns];
                for (int i = 0; i < numberOfColumns; i++) {
                    columnsBlock.add(new ArrayList<block>());
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
                int each = WIDTH / 5;
                if (x < each) {
                    if (playerSprite.getMoving() != -1) {
                        playerSprite.move(-1);
                    }
                } else if (x > each * 4) {
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
            while (rows[random] <= HEIGHT / 3) {
                random = rm.nextInt(numberOfColumns);
            }
            block blo = new block(this, random, columnWidth);
            columnsBlock.get(random).add(blo);
            i = 0;
            if (MAX_TO_BOX > 15) {
                MAX_TO_BOX--;
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.BLACK);
        addBlock();
        drawblock(canvas);
        points++;
        playerSprite.draw(canvas);
    }

    private void drawblock(Canvas canvas) {
        int comp = 0;
        for (ArrayList<block> Ablo : columnsBlock) {
            if (Ablo.size() > 1 && Ablo.get(1).isFixed()) {
                comp++;
            }
            for (block blo : Ablo) {
                blo.draw(canvas);
            }
        }
        if (comp == 6) {
            System.out.println("Deleting bottom");
            for (ArrayList<block> Ablo : columnsBlock) {
                Ablo.remove(0);
                for (block blo : Ablo) {
                    blo.setFixed(false);
                }
            }
            for (int j = 0; j < rows.length; j++) {
                rows[j] = HEIGHT;
            }
        }
    }

    public int[] getColumns() {
        return columns;
    }

    public int[] getRows() {
        return rows;
    }

    public int getPoints() {
        return points;
    }

    public void setRows(int index, int value) {
        rows[index] -= value;
    }

    public ArrayList<ArrayList<block>> getColumnsBlock() {
        return columnsBlock;
    }

    public void endGame() {
        System.out.println("Puntos: " + points);
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
        System.out.println("Saliendo");

    }
}
