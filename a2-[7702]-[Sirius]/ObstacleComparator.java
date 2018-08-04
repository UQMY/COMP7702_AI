import java.util.*;

public class ObstacleComparator implements Comparator {
	
	public int compare(Object o1, Object o2) {
		int d = (int) (((Obstacle) o1).getRect().getY() * 10000) - (int) (((Obstacle) o2).getRect().getY() * 10000);
		if (d == 0)
			return (int) (((Obstacle) o1).getRect().getHeight() * 10000) - (int) (((Obstacle) o2).getRect().getHeight() * 10000);
		else
			return d;
	}

}
