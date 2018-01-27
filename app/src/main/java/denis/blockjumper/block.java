package denis.blockjumper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by denis.couñago on 26/01/2018.
 */

public class block {
    private Paint paint = new Paint();
    private GameView gameView;
    int x = 0, y = 0;
    int ySpeed = 0;
    int width = 100, height = 100;

    public block(GameView gameView, int x) {
        this.gameView = gameView;
        this.x = x;
    }

    public void draw(Canvas canvas) {
        if (ySpeed < gameView.GRAVITY){
            ySpeed++;
        }
        y = y+ySpeed;
        paint.setColor(Color.CYAN);
        paint.setStrokeWidth(3);
        canvas.drawRect(x, y, x+width, y+height, paint);
    }

    public int getY() {
        return y;
    }
}
