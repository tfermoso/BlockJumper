package denis.blockjumper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

import denis.blockjumper.Globals.Prefs;
import denis.blockjumper.SpirtesClass.PlayerSprite;
import denis.blockjumper.SpirtesClass.block;
import denis.blockjumper.Thread.GameLoopThread;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by denis.couñago on 10/01/2018.
 */

public class GameView extends SurfaceView {
    private GameLoopThread gameLoopThread;
    private GameView self = this;
    private PlayerSprite playerSprite;
    private int WIDTH, HEIGHT;
    private int points = 0, columnWidth, columns[],rows[];
    private Paint pointsPaint = new Paint();
    private ArrayList<ArrayList<block>> columnsBlock;
    private Random rm = new Random();
    private int i = 0;
    // Default options
    public int GRAVITY = 40;
    private int numberOfColumns = 8;
    private int BOX_INTERVAL = 50, MAX_INTERVAL_BOX = 100;
    private boolean BOXED_BOX = false, GOD_MODE = false;
    // ---------------
    private boolean created = false, customGame = false, nextCoin = false;
    private Bitmap background, block_image, coin_image;

    private MediaPlayer mediaPlayer;

    public GameView(final Context context) {
        super(context);
        SurfaceHolder holder = getHolder();

        SharedPreferences prefs = getContext().getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
        numberOfColumns = prefs.getInt(Prefs.COLUMN_NUMBER, 8);
        BOX_INTERVAL = prefs.getInt(Prefs.BOX_INTERVAL, 50);
        MAX_INTERVAL_BOX = prefs.getInt(Prefs.MAX_BOX_INTERVAL, 100);
        GRAVITY = prefs.getInt(Prefs.MAX_GRAVITY, 40);
        BOXED_BOX = prefs.getBoolean(Prefs.BOXED_BOX, false);
        GOD_MODE = prefs.getBoolean(Prefs.GOD_MODE, false);

        if (numberOfColumns != 8 ||
                BOX_INTERVAL != 50 ||
                MAX_INTERVAL_BOX != 100 ||
                GRAVITY != 40 ||
                BOXED_BOX ||
                GOD_MODE) {
            customGame = true;
            Toast toast = Toast.makeText(getContext(), "Custom game", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }

        pointsPaint = new Paint();
        pointsPaint.setColor(Color.WHITE);
        pointsPaint.setTextSize(60);
        pointsPaint.setTextAlign(Paint.Align.CENTER);


        //Backgound sound
        mediaPlayer = MediaPlayer.create(getContext(), R.raw.gameplay_looping);
        mediaPlayer.start();

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (numberOfColumns == 0) {
                    Toast.makeText(getContext(), "Can't create a game with 0 columns", Toast.LENGTH_SHORT).show();
                    Activity activity = (Activity) getContext();
                    mediaPlayer.stop();
                    activity.finish();
                }
                if (!created) {
                    points = 0;
                    WIDTH = self.getWidth();
                    HEIGHT = self.getHeight();

                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.background);
                    float aspectRatio = b.getWidth() / (float) b.getHeight();
                    int width = WIDTH;
                    int height = Math.round(width / aspectRatio);
                    background = Bitmap.createScaledBitmap(b, width, height, false).copy(Bitmap.Config.RGB_565, true);

                    columnWidth = (int) Math.floor(WIDTH / numberOfColumns);
                    System.out.println(WIDTH + " / " + numberOfColumns + " = " + columnWidth);

                    Bitmap bl = BitmapFactory.decodeResource(getResources(), R.drawable.block_gameold).copy(Bitmap.Config.RGB_565, true);
                    Bitmap bl2 = BitmapFactory.decodeResource(getResources(), R.drawable.silver_coin).copy(Bitmap.Config.ARGB_8888, true);
                    block_image = Bitmap.createScaledBitmap(bl, columnWidth, (BOXED_BOX) ? columnWidth : 100, false);
                    coin_image = Bitmap.createScaledBitmap(bl2, columnWidth, (BOXED_BOX) ? columnWidth : 100, false);

                    columns = new int[numberOfColumns];
                    columnsBlock = new ArrayList<>();
                    rows = new int[numberOfColumns];
                    for (int i = 0; i < numberOfColumns; i++) {
                        columnsBlock.add(new ArrayList<block>());
                        columns[i] = columnWidth * i;
                        rows[i] = self.getHeight();
                    }
                    playerSprite = new PlayerSprite(self);
                    if (GOD_MODE) {
                        playerSprite.setGodMode(true);
                    }
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
                mediaPlayer.stop();
//                boolean retry = true;
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
        if (i > BOX_INTERVAL) {
            int total = 0;
            for (int i = 0; i < rows.length; i++) {
                if (rows[i] <= HEIGHT / 3) {
                    total++;
                }
            }
            if (total < numberOfColumns) {
                int random = rm.nextInt(numberOfColumns);
                while (rows[random] <= HEIGHT / 3) {
                    System.out.println("CANT PUT THERE");
                    random = rm.nextInt(numberOfColumns);
                }
                block blo;
                if (nextCoin) {
                    nextCoin = false;
                    blo = new block(this, random, columnWidth, coin_image, BOXED_BOX, true);
                } else {
                    blo = new block(this, random, columnWidth, block_image, BOXED_BOX, false);
                }
                columnsBlock.get(random).add(blo);
                i = 0;
            }
        }

        if (MAX_INTERVAL_BOX != 0 && points % MAX_INTERVAL_BOX == 0 && BOX_INTERVAL > 15) {
            BOX_INTERVAL--;
            GRAVITY++;
        }
    }

    @Override
    public void draw(Canvas canvas) {

        super.draw(canvas);
//        canvas.drawColor(Color.BLACK);
//        Rect rect = new Rect(0,0,WIDTH,HEIGHT);
        canvas.drawBitmap(background, 0, 0, null);
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
        if (points % 200 == 0){
            nextCoin = true;
        }
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
        System.out.println("Points: " + points);
        gameLoopThread.setRunning(false);
        // Stop music
        mediaPlayer.stop();
        mediaPlayer = MediaPlayer.create(getContext(), R.raw.lose_music);
        mediaPlayer.start();
        // Wait 2 seconds
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void finalStop() {
        if (!customGame) {
            SharedPreferences prefs = getContext().getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
            int scoreOld = prefs.getInt(Prefs.SCORE, 0);
            System.out.println(scoreOld + " - " + points);
            if (scoreOld < points) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(Prefs.SCORE, points);
                editor.apply();
            }
        }
        Toast.makeText(getContext(), "Score: " + points, Toast.LENGTH_LONG).show();

        Activity activity = (Activity) getContext();
        mediaPlayer.stop();
        activity.finish();
    }
}
