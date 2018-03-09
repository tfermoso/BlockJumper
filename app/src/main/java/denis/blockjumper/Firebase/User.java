package denis.blockjumper.Firebase;

/**
 * Created by denis.couñago on 05/03/2018.
 */

public class User {
    private String name;
    private String password;
    private int points;
    private String date;

    public User(){}

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
