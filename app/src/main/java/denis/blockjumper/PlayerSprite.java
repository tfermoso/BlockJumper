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
    private int currentColumn = 1, currentRow = 0;
    private boolean canJump = false, jumped = false;
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
        // Comprobamos el bote en caso de haber
        if (newX >= MAP_WIDTH - width - xSpeed) {
            newX = MAP_WIDTH - width;
//            xSpeed = -xSpeed / 2;
        } else if (newX <= 0 - xSpeed) {
            newX = 0;
//            xSpeed = -xSpeed / 2;
        }
        // Movemos horizontalmente
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
                xSpeed -= 2;
            } else if (xSpeed < 0) {
                xSpeed += 2;
            }
        }

        // Comprobamos que sprite debe cargar
        changeFrame();

        block comp = gameView.isTouchingBlock(rec);
        if (comp != null) {
            if (comp.getX() >= newX && comp.getY() < newY) {
                // Está a la derecha
                newX = comp.getX() - width;
//                xSpeed = -xSpeed / 2;
                xSpeed=0;
            } else if (comp.getX() + comp.getWidth() <= newX + width && comp.getY() < newY) {
                // Está a la izquierda
                newX = comp.getX() + comp.getWidth();
//                xSpeed = -xSpeed / 2;
                xSpeed=0;
            } else if (comp.getY() >= newY && comp.getX() < newX && comp.getX() + comp.getWidth() > newX) {
                // Está debajo
                newY = comp.getY() - height;

//                ySpeed = -ySpeed / 2;
                ySpeed=0;
                canJump = true;
                jumped = false;
            } else if (comp.getY() + comp.getHeight() <= newY) {
                // Está encima
            }
        } else {
            if (newY > MAP_HEIGHT - height) {
                newY = MAP_HEIGHT - height;
            }
//            int compY = gameView.isTouchingGround(newX, newY + height + ySpeed, width - 2);
//            if (compY > -1) {
            if (newY >= MAP_HEIGHT - height - ySpeed) {
//                newY = compY - height - ySpeed;
                newY = MAP_HEIGHT - height - ySpeed;
                if (jumped) {
//                    ySpeed = -ySpeed / 2;
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
        }
            newX = newX + xSpeed;
            newY = newY + ySpeed;

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
        int srcX = currentColumn * width;
        int srcY = currentRow * height;
        Rect src = new Rect(srcX, srcY, srcX + width, srcY + width);
        rec = new Rect(x, y, x + width, y + height);
        canvas.drawBitmap(bmp, src, rec, null);
    }

    public void move(int i) {
        moving = i;
    }

    public int getMoving() {
        return moving;
    }
}
