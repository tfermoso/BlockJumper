package denis.blockjumper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by denis.couñago on 26/01/2018.
 */

public class block {
    private int position;
    private Paint paint = new Paint();
    private GameView gameView;
    int ySpeed = 0;
    int width = 100, height = 100;
    int x = 0, y = 0-height;
    boolean fixed = false;

    public block(GameView gameView, int position,int width) {
        this.gameView = gameView;
        this.position = position;
        this.x = gameView.getColumns()[position];
        this.width = width;
    }

    public void draw(Canvas canvas) {
        if (!fixed) {
            if (ySpeed < gameView.GRAVITY) {
                ySpeed++;
            }
            y = y + ySpeed;
            System.out.println(gameView.getRows()[position]-height);
            if (y >= gameView.getRows()[position]-height){
                y = gameView.getRows()[position]-height;
                fixed=true;
                gameView.setRows(position,height);
            }
        }
        paint.setColor(Color.CYAN);
        paint.setStrokeWidth(3);
        canvas.drawRect(x, y, x + width, y + height, paint);

    }

    public int getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }
}
