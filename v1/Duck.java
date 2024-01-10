package v1;
import battlecode.common.*;
import static v1.Constants.*;
import java.util.*;

public class Duck extends RobotPlayer {
	public static MapLocation destination;

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

				destination = new MapLocation(mapWidth - currLoc.x, mapHeight - currLoc.y);
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
		// ATTACKER, not specialized
		if(enemyDucks.length > 0) {
			RobotInfo enemy = enemyDucks[rng.nextInt(enemyDucks.length)];
			if(rc.canAttack(enemy.getLocation())) rc.attack(enemy.getLocation());
			if(allyDucks.length < 2) tryMove(rc.getLocation().directionTo(enemy.getLocation()).opposite());
			else tryMove(rc.getLocation().directionTo(enemy.getLocation()));
		}

		pathfindMove();
	}

	// Greedy pathfinding
	// TODO: Implement BFS for faster pathfinding
	// TODO: Will keep moving after reaching destination
	public static void pathfindMove() throws GameActionException {
		if(!rc.isMovementReady()) return;
		// We have somewhere to go
		if(destination != null && rc.getLocation() != null) {
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
	public static void tryMove(Direction dir) throws GameActionException {
		if(rc.canMove(dir)) rc.move(dir);
		else if(rc.canMove(dir.rotateLeft())) rc.move(dir.rotateLeft());
		else if(rc.canMove(dir.rotateRight())) rc.move(dir.rotateRight());
	}
}
