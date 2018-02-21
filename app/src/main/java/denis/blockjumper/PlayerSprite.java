package denis.blockjumper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Gravity;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by denis.couñago on 26/01/2018.
 */

class PlayerSprite {
    private static final int BMP_ROWS = 4, BMP_COLUMNS = 3;
    private static final int MAX_SPEED_X_L = -20, MAX_SPEED_X_R = 20;
    private int x = 0, y = 0, xSpeed = 0, ySpeed = 0;
    private int MAP_WIDTH, MAP_HEIGHT, width, height;
    private int currentColumn = 1, currentRow = 0;
    private boolean canJump = false, ended = false;
    private GameView gameView;
    private Bitmap bmp;
    private int moving = 0;
    private int changeFrame = 0;
    private int animationOrder = 0;
    private Rect rec;


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

    private void changeFrame() {
        if (++changeFrame >= 8) {
            if (currentColumn >= 2) {
                animationOrder = -1;
            } else if (currentColumn <= 0) {
                animationOrder = 1;
            }
            currentColumn += animationOrder;
            changeFrame = 0;
        }
    }

    private void update() {
        int newX = x;
        int newY = y;

        if (moving == -1) {
            currentRow = 1;
            animationOrder = -1;
            if (xSpeed > MAX_SPEED_X_L) {
                xSpeed -= 2;
            }
        } else if (moving == 1) {
            currentRow = 2;
            animationOrder = 1;
            if (xSpeed < MAX_SPEED_X_R) {
                xSpeed += 2;
            }
        } else {
            currentRow = 0;
            animationOrder = 0;
            if (xSpeed > 0) {
                xSpeed = (xSpeed > 1) ? xSpeed - 2 : 0;
            } else if (xSpeed < 0) {
                xSpeed = (xSpeed < -1) ? xSpeed + 2 : 0;
            }
        }

        if (newY >= MAP_HEIGHT - height - ySpeed) {
            newY = MAP_HEIGHT - height;
            ySpeed = (ySpeed > 3 || ySpeed < -3) ? -ySpeed / 3 : 0;
            if (!canJump) {
                canJump = true;
            }
        } else {
            // Si está en el aire, aumentamos la gravedad hasta el máximo
            if (ySpeed < gameView.GRAVITY) {
                ySpeed += 4;
            } else if (ySpeed > gameView.GRAVITY) {
                ySpeed = gameView.GRAVITY;
            }
        }
        if (newX >= MAP_WIDTH - width - xSpeed) {
            newX = MAP_WIDTH - width;
            xSpeed = (xSpeed > 3 || xSpeed < -3) ? -xSpeed / 3 : 0;
        } else if (newX <= 0 - xSpeed) {
            newX = 0;
            xSpeed = (xSpeed > 3 || xSpeed < -3) ? -xSpeed / 3 : 0;
        }

        Rect temprec = new Rect(newX + xSpeed, newY + ySpeed, newX + xSpeed + width, newY + ySpeed + height);
        for (ArrayList<block> ar : gameView.getColumnsBlock()) {
            for (block comp : ar) {
                if (comp.isIntersection(temprec)) {
                    if (newY + height > comp.getY() + comp.getHeight() &&
                            newX + width - 10 > comp.getX() &&
                            newX < comp.getX() + comp.getWidth() - 10) {
                        System.out.println("You lost");
                        ended = true;
                    } else if (newX <= comp.getX() && newY + height - 10 > comp.getY()) {
                        // Estás a la izquierda, bloque a la derecha
                        newX = comp.getX() - width;
                        xSpeed = (xSpeed > 3 || xSpeed < -3) ? -xSpeed / 3 : 0;
                    } else if (newX + width > comp.getX() + comp.getWidth() && newY + height - 10 > comp.getY()) {
                        // Estas a la derecha, bloque a la izquierda
                        newX = comp.getX() + comp.getWidth();
                        xSpeed = (xSpeed > 3 || xSpeed < -3) ? -xSpeed / 3 : 0;
                    } else if (newY < comp.getY() + 10) {
                        // Estas encima, bloque debajo
                        newY = comp.getY() - height;
                        ySpeed = (ySpeed > 4 || ySpeed < -4) ? -ySpeed / 4 : 0;
                        canJump = true;
                    } else {
                        System.out.println("Huh? where I am?");
                    }
                }
            }
        }

        x = newX + xSpeed;
        y = newY + ySpeed;
        changeFrame();
    }

    public void jump() {
        if (canJump) {
            ySpeed = -40;
            canJump = false;
        }
    }

    public void draw(Canvas canvas) {
        update();
        int srcX = currentColumn * width;
        int srcY = currentRow * height;
        Rect src = new Rect(srcX, srcY, srcX + width, srcY + width);
        rec = new Rect(x, y, x + width, y + height);
        canvas.drawBitmap(bmp, src, rec, null);
        if (ended){
            gameView.endGame();
        }
    }

    public void move(int i) {
        moving = i;
    }

    public int getMoving() {
        return moving;
    }
}
