package denis.blockjumper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.constraint.solver.widgets.Rectangle;

/**
 * Created by denis.couñago on 26/01/2018.
 */

public class block {
    private int position;
    private Paint paint = new Paint();
    private GameView gameView;
    private int ySpeed = 0;
    private int width = 100, height = 100;
    private int x = 0, y = 0 - height;
    private boolean fixed = false;
    private Rect rec;

    public block(GameView gameView, int position, int width) {
        this.gameView = gameView;
        this.position = position;
        this.x = gameView.getColumns()[position];
        this.width = width;

        // De pruebas, la altura igual al ancho
//        this.height = width;
//        this.y = 0 - width;
    }

    public void draw(Canvas canvas) {
        if (!fixed) {
            if (ySpeed < gameView.GRAVITY) {
                ySpeed++;
            }
            y = y + ySpeed;
            if (y >= gameView.getRows()[position] - height) {
                y = gameView.getRows()[position] - height;
                fixed = true;
                gameView.setRows(position, height);
            }
        }
        paint.setColor(Color.CYAN);
        paint.setStrokeWidth(3);
        rec = new Rect(x, y, x + width, y + height);
        canvas.drawRect(rec, paint);

    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public boolean isFixed() {
        return fixed;
    }

    public boolean isIntersection(Rect rec) {
        if (this.rec.intersect(rec)) {
            return true;
        } else {
            return false;
        }
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
