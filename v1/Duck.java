package v1;
import battlecode.common.*;
import static v1.Constants.*;
import java.util.*;

<<<<<<< HEAD
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
=======
>>>>>>> parent of d711f3b (Flag-catching + docs)
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
<<<<<<< HEAD

		// Found a flag! First priority is to pick up and carry back.
        for (FlagInfo flag : flagsInfos) {
            if (rc.canPickupFlag(flag.getLocation())) {
                rc.pickupFlag(flag.getLocation());
                setPathfinding(spawn);
            }
        }

        // Attack when in range of enemies
=======
		// ATTACKER, not specialized
>>>>>>> parent of d711f3b (Flag-catching + docs)
		if(enemyDucks.length > 0) {
			RobotInfo enemy = enemyDucks[rng.nextInt(enemyDucks.length)];
			if(rc.canAttack(enemy.getLocation())) rc.attack(enemy.getLocation());
			if(allyDucks.length < 2) tryMove(rc.getLocation().directionTo(enemy.getLocation()).opposite());
			else tryMove(rc.getLocation().directionTo(enemy.getLocation()));
		}

		stepPathfinding();
	}

	// Greedy pathfinding
	// TODO: Implement BFS for faster pathfinding
	// TODO: Will keep moving after reaching destination
<<<<<<< HEAD
	public static void stepPathfinding() throws GameActionException {
		if(!rc.isSpawned()) return; // Do we exist?
		if(!rc.isMovementReady()) return; // Can we move?

=======
	public static void pathfindMove() throws GameActionException {
		if(!rc.isMovementReady()) return;
>>>>>>> parent of d711f3b (Flag-catching + docs)
		// We have somewhere to go
		if(destination != null) {
			MapLocation currLoc = rc.getLocation();
			Direction bestMove = getBestDirection(false);

			if(bestMove == null) return; // should never happen
			MapLocation newLoc = currLoc.add(bestMove);

			// Not currently bugnaving, and chosen mapLoc is wall or water
			if(bugNavTurn == -1 && rc.canSenseLocation(newLoc) && (rc.senseMapInfo(newLoc).isWall() || rc.senseMapInfo(newLoc).isWater())) {
				// Cannot move, begin bugnav
				bugNavTurn = 1;
				setPathfinding(destination);
				Direction newBestMove = rightmostMovableTile(bestMove);
				if(newBestMove != null) {
					adjacentWall = currLoc.add(newBestMove.rotateLeft());
					if(rc.canMove(bestMove)) {
						rc.move(newBestMove);
						rc.setIndicatorString("Beginning bugnav... Moving to best move (" + newBestMove + ")");
					}
				}
				rc.setIndicatorString("Beginning bugnav... Cannot move.");

			// Currently bugnaving
			} else if (bugNavTurn != -1) {
				bugNavTurn++;

				// End bugnaving if turn > 2 and on ML line OR no adjacent wall
				if(bugNavTurn > 2 && (isOnMLine() || adjacentWall == null)) {
					adjacentWall = null;
					bugNavTurn = -1;

					bestMove = getBestDirection(true);
					if(bestMove != null && rc.canMove(bestMove)) {
						rc.move(bestMove);
						rc.setIndicatorString("Bugnav over! Moving to best move (" + bestMove + ") instead...");
					} else {
						rc.setIndicatorString("Bugnav over! Cannot move");
					}

				// Bugnav cannot end. Keep going!
				} else {
					// Continue bugnav
					if(adjacentWall == null) {
						rc.setIndicatorString("No wall to bugnav. Shouldn't happen.");
						return;
					}

					Direction newBestMove = rightmostMovableTile(currLoc.directionTo(adjacentWall));
					if(newBestMove != null) {
						adjacentWall = currLoc.add(newBestMove.rotateLeft());
						if(rc.canMove(newBestMove)) {
							bestMove = getBestDirection(true);
							if(bestMove != null) {
								rc.move(bestMove);
								rc.setIndicatorString("Bugnav continues!! Moving to best move (" + bestMove + ") instead...");
							} else {
								rc.setIndicatorString("Bugnav continues! But cannot move...?");
							}
						}
					}
				}

			// Can move, no bugnav. Just move
			} else {
				bestMove = getBestDirection(true);
				if(bestMove != null) {
					rc.move(bestMove);
					rc.setIndicatorString("No bugnav. Moving to best move (" + bestMove + ") instead...");
				} else {
					rc.setIndicatorString("No bugnav, but nowhere to move...?");
				}
			}
		}

	}
	public static void setPathfinding(MapLocation newDestination) {
		MapLocation currLoc = rc.getLocation();
		destination = newDestination;
		rc.setIndicatorLine(currLoc, newDestination, 255, 20, 20);
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

	public static Direction getBestDirection(boolean shouldBeAbleToMove) throws GameActionException {
		MapLocation currLoc = rc.getLocation();
		int bestDist = currLoc.distanceSquaredTo(destination);
		Direction bestMove = null;

		for (Direction direction : DIRECTIONS) {
			MapLocation newLoc = currLoc.add(direction);
			int newDist = newLoc.distanceSquaredTo(destination);
			if ((newDist < bestDist || bestMove == null)) {
				if(shouldBeAbleToMove && !rc.canMove(direction)) continue;
				bestMove = direction;
				bestDist = newDist;
			}
		}

		return bestMove;
	}
	public static Direction rightmostMovableTile(Direction initDir) throws GameActionException {
		MapLocation currLoc = rc.getLocation();
		Direction dir = initDir;

		for(int i = 0; i < 6; i++) {
			dir = dir.rotateRight();

			MapLocation loc = currLoc.add(dir);
			if(rc.canMove(dir) || (isDam(loc))) return dir;
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
	public static boolean isDam(MapLocation loc) throws GameActionException {
		if(rc.canSenseLocation(loc)) {
			MapInfo mInfo = rc.senseMapInfo(loc);
            return !rc.canMove(rc.getLocation().directionTo(loc)) && (!mInfo.isWall() && !mInfo.isWater());
		} else return false;
	}
}
