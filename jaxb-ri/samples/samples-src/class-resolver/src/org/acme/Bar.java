package org.acme;

/**
 * @author Kohsuke Kawaguchi
 */
public class Bar {
    // in this bean we bind properties, just to show that we can.
    private int x,y;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String toString() {
        return "Bar[x="+x+",y="+y+"]";
    }

}
