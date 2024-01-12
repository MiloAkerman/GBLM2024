package v1;
import battlecode.common.*;
import static v1.Constants.*;
import java.util.*;

/**
 * Code for ALL ducks

 * Currently implemented:
 * - Pick up flag and carry back to base
 * - Attack if in range of enemies
 *      - If backup, close in. Otherwise, back off.

 *  Important todos
 *  - SPLIT FILE INTO MANY CLASSES FOR BETTER ORGANIZATION
 *  - Implement healing and building
 *  - Improve attacker micro and micro
 *  - Macro through comms
 *
 * @author Milo
 */
public class Duck extends RobotPlayer {
	public static MapLocation destination;
	public static MapLocation adjacentWall;
	public static int bugNavTurn = -1;
	public static ArrayList<MapLocation> mLine = new ArrayList<>();

	public static void setup() throws GameActionException {
		// When robot is instantiated (not spawned)
	}

	public static void run() throws GameActionException {
		// We haven't spawned yet
		if (!rc.isSpawned()){
			MapLocation[] spawnLocs = rc.getAllySpawnLocations();
			// Pick a random spawn location to attempt spawning in.
			MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
			if (rc.canSpawn(randomLoc)) {
				rc.spawn(randomLoc);
				MapLocation currLoc = rc.getLocation();

				setPathfinding(new MapLocation(mapWidth - currLoc.x, mapHeight - currLoc.y));
				spawn = currLoc;
			} else {
				return;
			}
		}

		// Determine roles
		RobotInfo[] enemyDucks = rc.senseNearbyRobots(-1, oppTeam);
		RobotInfo[] allyDucks = rc.senseNearbyRobots(-1, myTeam);
		FlagInfo[] flagsInfos = rc.senseNearbyFlags(-1, oppTeam);

		// TODO: PARAMEDIC: Heal during combat, not specialized

		// Found a flag! First priority is to pick up and carry back.
        for (FlagInfo flag : flagsInfos) {
            if (rc.canPickupFlag(flag.getLocation())) {
                rc.pickupFlag(flag.getLocation());
                setPathfinding(spawn);
            }
        }

        // Attack when in range of enemies
		if(enemyDucks.length > 0) {
			RobotInfo enemy = enemyDucks[rng.nextInt(enemyDucks.length)];
			if(rc.canAttack(enemy.getLocation())) rc.attack(enemy.getLocation());

			if(!rc.hasFlag()) {
				// If not with allies, attack and back off
				// TODO: try to back off only from their attack range, not all of vision range
				if(allyDucks.length < 2) tryMove(rc.getLocation().directionTo(enemy.getLocation()).opposite());
				// If with allies, attack and move towards
				// TODO: make more macro-y. (Units should still move away when they're done attacking)
				else tryMove(rc.getLocation().directionTo(enemy.getLocation()));
			}
		}

		stepPathfinding();
	}

	// --------------------------------------- PATHFINDING ----------------------------------------------------

	// Greedy pathfinding
	// TODO: Implement BFS for faster pathfinding
	// TODO: Will keep moving after reaching destination
	public static void stepPathfinding() throws GameActionException {
		if(!rc.isSpawned()) return; // Do we exist?
		if(!rc.isMovementReady()) return; // Can we move?

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

			if(bestMove == null) return; // should never happen
			MapLocation newLoc = currLoc.add(bestMove);

			if(!rc.sensePassability(newLoc)) {
				// Cannot move, begin bugnav
				bugNavTurn = 1;
				Direction newBestMove = rightmostMovableTile(bestMove);
				if(newBestMove != null) {
					adjacentWall = currLoc.add(newBestMove.rotateLeft());
					rc.move(newBestMove);
				}
			} else if (bugNavTurn != -1) {
				// Can move, but bugnaving
				bugNavTurn++;

				if(bugNavTurn > 2 && isOnMLine()) {
					// Bugnav can end
					adjacentWall = null;
					rc.move(bestMove);
				} else {
					// Continue bugnav
					Direction newBestMove = rightmostMovableTile(currLoc.directionTo(adjacentWall));
					if(newBestMove != null) {
						adjacentWall = currLoc.add(newBestMove.rotateLeft());
						rc.move(newBestMove);
					}
				}
			} else {
				// Can move, no bugnav. Just move
				rc.move(bestMove);
			}
		}

	}
	public static void setPathfinding(MapLocation newDestination) {
		MapLocation currLoc = rc.getLocation();
		destination = newDestination;
		adjacentWall = null;

		// Modified Bresenham algorithm
		int xDist = Math.abs(newDestination.x - currLoc.x);
		int yDist = -Math.abs(newDestination.y - currLoc.y);
		int xStep = (currLoc.x < newDestination.x ? +1 : -1);
		int yStep = (currLoc.y < newDestination.y ? +1 : -1);
		int error = xDist + yDist;

		int x0 = currLoc.x;
		int y0 = currLoc.y;
		while(x0 != newDestination.x || y0 != newDestination.y) {
			if (2*error - yDist > xDist - 2*error) {
				error += yDist;
				x0 += xStep;
			} else {
				error += xDist;
				y0 += yStep;
			}

			mLine.add(new MapLocation(x0, y0));
		}
	}
	public static void resetPathfinding() {
		destination = null;
		adjacentWall = null;
		mLine = new ArrayList<>();
	}
	public static void tryMove(Direction dir) throws GameActionException {
		if(rc.canMove(dir)) rc.move(dir);
		else if(rc.canMove(dir.rotateLeft())) rc.move(dir.rotateLeft());
		else if(rc.canMove(dir.rotateRight())) rc.move(dir.rotateRight());
	}

	public static Direction rightmostMovableTile(Direction initDir) throws GameActionException {
		Direction dir = initDir;
		for(int i = 0; i < 6; i++) {
			dir = dir.rotateRight();
			if(rc.canMove(dir)) return dir;
		}
		return null;
	}
	public static boolean isOnMLine() throws GameActionException {
		MapLocation currLoc = rc.getLocation();
		for(MapLocation loc : mLine) {
			if(currLoc.equals(loc)) return true;
		}
		return false;
	}
}
