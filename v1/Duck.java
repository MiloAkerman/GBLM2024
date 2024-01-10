package v1;
import battlecode.common.*;
import static v1.Constants.*;
import java.util.*;

public class Duck extends RobotPlayer {
	public static MapLocation destination;

	public static void run() throws GameActionException {
		MapLocation currLoc = rc.getLocation();

		destination = new MapLocation(mapWidth - currLoc.x, mapHeight - currLoc.y);
		pathfindMove();
	}

	// Greedy pathfinding
	// TODO: Implement BFS for faster pathfinding
	// TODO: Will keep moving after reaching destination
	public static void pathfindMove() throws GameActionException {

		// We have somewhere to go
		if(destination != null) {
			MapLocation currLoc = rc.getLocation();
			int currDist = currLoc.distanceSquaredTo(destination);

			Direction bestMove = null;
			for (Direction direction : DIRECTIONS) {
				MapLocation newLoc = currLoc.add(direction);
				int newDist = newLoc.distanceSquaredTo(destination);
				if ((newDist < currDist || bestMove == null) && rc.canMove(direction)) {
					bestMove = direction;
				}
			}

			if(bestMove != null) {
				rc.move(bestMove);
			}
		}

	}
}
