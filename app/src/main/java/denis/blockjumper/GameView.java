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
    private int WIDTH;

    private int numberOfColumns = 6;
    private int columnWidth;
    private int columns[];
    private ArrayList<ArrayList<block>> columnsBlock;
    private int rows[];

    //    private List<block> blockList;
    private Random rm = new Random();
    private int i = 0;
    private int MAX_TO_BOX = 70;

    public GameView(final Context context) {
        super(context);
        holder = getHolder();
//        blockList = new ArrayList<block>();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                WIDTH = self.getWidth();
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
            columnsBlock.get(random).add(blo);
//            blockList.add(blo);
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
        for (ArrayList<block> Ablo : columnsBlock) {
            for (block blo : Ablo) {
                blo.draw(canvas);
            }
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

    public block isTouchingBlock(Rect rec) {
        for (List<block> Lbloc : columnsBlock) {
            for (block blo : Lbloc) {
                if (blo.isIntersection(rec)) {
                    return blo;
                }
            }
        }
        return null;
    }
//    public int isTouchingGround(int newX, int newY, int width) {
//        int wich = numberOfColumns - 1;
//        boolean both = false;
//        for (int i = 0; i < numberOfColumns; i++) {
//            if (newX < columns[i]) {
//                // Comprobamos si está en 2 cuadrados diferentes
//                if (newX + width > columns[i]) {
//                    both = true;
//                }
//                if (i == 0) {
//                    wich = 0;
//                } else {
//                    wich = i - 1;
//                }
//                break;
//            }
//        }
//        // Si está en 2 cuadrados diferentes la altura será el de la más alta
//        if (both) {
//            if (rows[wich] < rows[wich + 1]) {
//                if (newY >= rows[wich]) {
//                    return rows[wich];
//                }
//            } else {
//                if (newY >= rows[wich + 1]) {
//                    return rows[wich + 1];
//                }
//            }
//        }
//        if (newY >= rows[wich]) {
//            return rows[wich];
//        } else {
//            return -1;
//        }
//    }
//
//    public int isTouchingWall(int x, int y, int width) {
//        int wich = numberOfColumns - 1;
//        for (int i = 0; i < numberOfColumns; i++) {
//            if (x < columns[i]) {
//                // Comprobamos si está en 2 cuadrados diferentes
//                if (i == 0) {
//                    wich = 0;
//                } else {
//                    wich = i - 1;
//                }
//                break;
//            }
//        }
//        System.out.println(columns[wich] + " - " + x + " - " + rows[wich] + " - " + y);
//        if (wich > 0 && columns[wich] >= x) {
//            if (rows[wich - 1] < y) {
//                return -1;
//            }
//        }
//        if (width < numberOfColumns && columns[wich] + columnWidth <= x + width) {
//            if (rows[wich + 1] < y) {
//                return 1;
//            }
//        }
//        return 0;
//    }
}
