package denis.blockjumper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by denis.couñago on 10/01/2018.
 */

public class GameView extends SurfaceView {
    private String PREFS_NAME = "MY_PREFS";
    private SurfaceHolder holder;
    private GameLoopThread gameLoopThread;
    private GameView self = this;
    private PlayerSprite playerSprite;
    public int GRAVITY = 50;
    private int WIDTH, HEIGHT;
    private int points = 0;
    private Paint pointsPaint = new Paint();
    private int numberOfColumns = 8;
    private int columnWidth;
    private int columns[];
    private ArrayList<ArrayList<block>> columnsBlock;
    private int rows[];
    private Random rm = new Random();
    private int i = 0;
    private int MAX_TO_BOX = 50;
    private boolean created = false;

    public GameView(final Context context) {
        super(context);
        holder = getHolder();

        pointsPaint = new Paint();
        pointsPaint.setColor(Color.WHITE);
        pointsPaint.setTextSize(60);
        pointsPaint.setTextAlign(Paint.Align.CENTER);

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (!created) {
                    points = 0;
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
                    playerSprite = new PlayerSprite(self);
                    created = true;
                }
                if (gameLoopThread == null || !gameLoopThread.isRunning()) {
                    gameLoopThread = new GameLoopThread(self);
                    gameLoopThread.setRunning(true);
                    gameLoopThread.execute();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                gameLoopThread.setRunning(false);
//                while (retry) {
//                    try {
////                        gameLoopThread.join();
//                        retry = false;
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
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
            if (MAX_TO_BOX > 10) {
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
        if (comp == numberOfColumns) {
            System.out.println("Deleting bottom");
            for (ArrayList<block> Ablo : columnsBlock) {
                if (Ablo.size() > 0) {
                    Ablo.remove(0);
                    for (block blo : Ablo) {
                        blo.setFixed(false);
                    }
                }
            }
            for (int j = 0; j < rows.length; j++) {
                rows[j] = HEIGHT;
            }
        }

        points++;

        canvas.drawText(points + "", WIDTH / 2, 100, pointsPaint);
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

    public ArrayList<ArrayList<block>> getColumnsBlock() {
        return columnsBlock;
    }

    public void endGame() {
        System.out.println("Puntos: " + points);
        boolean retry = true;
        gameLoopThread.setRunning(false);
    }

    public void finalStop() {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int scoreOld = prefs.getInt("score", 0);
        System.out.println(scoreOld + " - " + points);
        if (scoreOld < points) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("score", points);
            editor.apply();
        }
        Toast.makeText(getContext(), "Puntos: "+points, Toast.LENGTH_LONG).show();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Activity activity = (Activity) getContext();
        activity.finish();
    }
}
