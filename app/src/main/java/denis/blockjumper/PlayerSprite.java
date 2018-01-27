package denis.blockjumper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Gravity;

/**
 * Created by denis.couñago on 26/01/2018.
 */

class PlayerSprite {
    private static final int BMP_ROWS = 4, BMP_COLUMNS = 3;
    private static final int MAX_SPEED_X_L = -20, MAX_SPEED_X_R = 20;
    private int x = 0, y = 0, xSpeed = 0, ySpeed = 0;
    private int MAP_WIDTH, MAP_HEIGHT, width, height;
    private int currentFrame = 0, currentColumn = 0;
    private boolean canJump = false, jumped = false;
    private GameView gameView;
    private Bitmap bmp;
    private int moving = 0;


    public PlayerSprite(GameView gameView) {
        this.gameView = gameView;
        this.MAP_WIDTH = gameView.getWidth();
        this.MAP_HEIGHT = gameView.getHeight();
        bmp = BitmapFactory.decodeResource(gameView.getResources(), R.drawable.good1);
        this.width = bmp.getWidth() / BMP_COLUMNS;
        this.height = bmp.getHeight() / BMP_ROWS;
        this.x = MAP_WIDTH / 2 - (this.width / 2);
        this.y = MAP_HEIGHT - this.height;
    }

    private void update() {
        int newX = x;
        int newY = y;
        if (newX > MAP_WIDTH - width - xSpeed) {
            xSpeed = -xSpeed / 2;
        } else if (newX < 0) {
            xSpeed = -xSpeed / 2;
        }
        if (newY >= MAP_HEIGHT - height - ySpeed) {
            y = MAP_HEIGHT - height;
            if (jumped) {
                ySpeed = -ySpeed / 2;
                canJump = true;
                jumped = false;
            } else {
                jumped = true;
            }
        } else {
            // Si está en el aire, aumentamos la gravedad hasta el máximo
            if (ySpeed < gameView.GRAVITY) {
                ySpeed += 4;
            }
            if (ySpeed > gameView.GRAVITY) {
                ySpeed = gameView.GRAVITY;
            }
        }
        // Movemos horizontalmente
        if (moving == -1) {
            if (xSpeed > MAX_SPEED_X_L) {
                xSpeed-=2;
            }
        } else if (moving == 1) {
            if (xSpeed < MAX_SPEED_X_R) {
                xSpeed+=2;
            }
        } else {
            if (xSpeed > 0) {
                xSpeed-=2;
            } else if (xSpeed < 0) {
                xSpeed+=2;
            }
        }
        // Comprobamos que no se haya pasado del limite con ySpeed y xSpeed
        if (y > MAP_HEIGHT - height - ySpeed) {
            y = MAP_HEIGHT - height - ySpeed;
        }
        if (x-xSpeed <= 0) {
            x = 0 + xSpeed;
        } else if (x >= MAP_WIDTH - width - xSpeed) {
            x = MAP_WIDTH - width - xSpeed;
        }
        newX = x + xSpeed;
        newY = y + ySpeed;

        x = newX;
        y = newY;
    }

    public void jump() {
        if (canJump) {
            ySpeed = -40;
            canJump = false;
        }
    }

    public void draw(Canvas canvas) {
        update();
        int srcX = currentFrame * width;
        int srcY = currentColumn * height;
        Rect src = new Rect(srcX, srcY, srcX + width, srcY + width);
        Rect dist = new Rect(x, y, x + width, y + height);
        canvas.drawBitmap(bmp, src, dist, null);
    }

    public void move(int i) {
        moving = i;
    }

    public int getMoving() {
        return moving;
    }
}
