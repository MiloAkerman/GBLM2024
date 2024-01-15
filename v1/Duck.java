package v1;
import battlecode.common.*;
import static v1.Constants.*;
import java.util.*;

/**
 * Code for ALL ducks
 * @author Milo
 */
/*
 * Currently implemented:
 * - Pick up flag and carry back to base
 * - Heal if no enemies
 * - Attack if in range of enemies
 *      - If backup, close in. Otherwise, back off.

 *  Important todos
 *  - Implement building
 *  - Improve micro and micro
 *  - Macro through comms
 */
public class Duck extends RobotPlayer {
	static BugPathfinding pathfinding;

	public static void setup() throws GameActionException {
		pathfinding = new BugPathfinding(rc, DEFAULT_PASSABILITY);
	}

	// ---------------------------------------------- MAIN LOOP  ------------------------------------------------
	public static void run() throws GameActionException {
		// We haven't spawned yet, attempt to spawn
		if (!rc.isSpawned()){ if(!trySpawn()) return; }

		MapLocation currLoc = rc.getLocation();
		RobotInfo[] enemyDucksVision = rc.senseNearbyRobots(-1, oppTeam);
		RobotInfo[] enemyDucksAttack = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, oppTeam);
		RobotInfo[] allyDucksVision = rc.senseNearbyRobots(-1, myTeam);
		RobotInfo[] allyDucksHeal = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, myTeam);
		FlagInfo[] enemyFlagsInfo = rc.senseNearbyFlags(-1, oppTeam);
		FlagInfo[] teamFlagsInfo = rc.senseNearbyFlags(-1, myTeam);

		// Found a flag! First priority is to pick up and carry back
		for (FlagInfo flag : enemyFlagsInfo) {
			if (rc.canPickupFlag(flag.getLocation())) {
				rc.pickupFlag(flag.getLocation());
				pathfinding.setDestination(spawn);
			}
		}

		if(enemyDucksAttack.length > 0 && turnCount >= 200) {
			// Enemies present, takes priority over anything else


			MapLocation bestTrap = spawnTrap(TrapType.EXPLOSIVE);
			if(bestTrap != null) rc.build(TrapType.EXPLOSIVE, bestTrap);

			Attack: {
				// TODO: no random
				RobotInfo enemy = enemyDucksAttack[rng.nextInt(enemyDucksAttack.length)];
				if (rc.canAttack(enemy.getLocation())) rc.attack(enemy.getLocation());

				if (!rc.hasFlag()) {
					// Back off after attacking
					pathfinding.moveOnce(rc.getLocation().directionTo(enemy.getLocation()).opposite());
				}
			}


		} else {
			// No enemies in attack range
			if(pathfinding.getDestination() == null) {
				MapLocation[] flags = rc.senseBroadcastFlagLocations();
				if(flags.length != 0) {
					MapLocation closestFlag = flags[0];
					if(flags.length > 1 && currLoc.distanceSquaredTo(flags[1]) < currLoc.distanceSquaredTo(closestFlag))
						closestFlag = flags[1];
					if(flags.length > 2 && currLoc.distanceSquaredTo(flags[2]) < currLoc.distanceSquaredTo(closestFlag))
						closestFlag = flags[2];

					pathfinding.setDestination(closestFlag);
				}
			}

			// ...but enemies in vision range
			if(enemyDucksVision.length > 0 && turnCount >= 200 && !rc.hasFlag()) {

				MapLocation bestTrap = spawnTrap(TrapType.EXPLOSIVE);
				if(bestTrap != null) rc.build(TrapType.EXPLOSIVE, bestTrap);

				RobotInfo enemy = enemyDucksVision[rng.nextInt(enemyDucksVision.length)];
				if(allyDucksHeal.length > 0) {
					pathfinding.moveOnce(currLoc.directionTo(enemy.location));
					RobotInfo bestHeal = bestHeal(allyDucksHeal);
					if(bestHeal != null) rc.heal(bestHeal.location);
				} else {
					pathfinding.moveOnce(currLoc.directionTo(enemy.location).opposite());
				}

			} else {
				if (turnCount < 200) {
					Setup.run(rc);
				} else {
					// No enemies whatsoever

					RobotInfo bestHeal = bestHeal(allyDucksHeal);
					if(bestHeal != null) rc.heal(bestHeal.location);

					MapLocation crumb = findCrumbs(currLoc);
					if(crumb != null) pathfinding.moveOnce(currLoc.directionTo(crumb));
				}
			}
		}

		pathfinding.step();
		AttackSecondPass: {
			enemyDucksAttack = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, oppTeam);
			if(enemyDucksAttack.length > 0) {
				RobotInfo enemy = enemyDucksAttack[rng.nextInt(enemyDucksAttack.length)];
				if (rc.canAttack(enemy.getLocation())) rc.attack(enemy.getLocation());
			}
		}

	}

	// ---------------------------------------------- HELPERS  ------------------------------------------------
	public static boolean trySpawn() throws GameActionException {
		MapLocation[] spawnLocations = rc.getAllySpawnLocations();

		// TODO: Deterministic spawning
		MapLocation randomLoc = spawnLocations[rng.nextInt(spawnLocations.length)];
		if (rc.canSpawn(randomLoc)) {
			rc.spawn(randomLoc);
			spawn = rc.getLocation();
			pathfinding.setDestination(new MapLocation(mapWidth - spawn.x, mapHeight - spawn.y));

			return true;
		} else {
			return false;
		}
  	}

	public static MapLocation findCrumbs(MapLocation currLoc) throws GameActionException {
		MapLocation[] crumbs = rc.senseNearbyCrumbs(-1);
		MapLocation bestCrumb = null;
		if (crumbs.length > 0) {
			for (MapLocation crumb : crumbs) {
				if ((bestCrumb == null || currLoc.distanceSquaredTo(crumb) < currLoc.distanceSquaredTo(bestCrumb)) && rc.sensePassability(crumb)) {
					bestCrumb = crumb;
				}
			}
			if(bestCrumb != null && !rc.hasFlag()) pathfinding.moveOnce(currLoc.directionTo(bestCrumb));
		}
		return bestCrumb;
	}

	public static RobotInfo bestHeal(RobotInfo[] allyDucks) throws GameActionException {
		if (allyDucks.length > 0) {
			RobotInfo leastHealthDuck = allyDucks[0];
			for (RobotInfo allyDuck : allyDucks) {
				if ((allyDuck.hasFlag && allyDuck.health < GameConstants.DEFAULT_HEALTH) || (!leastHealthDuck.hasFlag && (allyDuck.health < leastHealthDuck.health) && rc.canHeal(leastHealthDuck.getLocation()))) {
					leastHealthDuck = allyDuck;
				}
			}
			if (rc.canHeal(leastHealthDuck.getLocation())) return leastHealthDuck;
			else return null;
		} else {
			return null;
		}
	}

	public static MapLocation spawnTrap(TrapType type) throws GameActionException {
		int traps = 0;
		ArrayList<MapLocation> placeableLocations = new ArrayList<>();

		for(MapLocation spawn : rc.getAllySpawnLocations()) {
			if(rc.canSenseLocation(spawn) && rc.senseMapInfo(spawn).getTrapType() != TrapType.NONE) {
				traps++;
				if(rc.canBuild(type, spawn)) placeableLocations.add(spawn);
			}
		}
		if(traps <= MAX_SPAWN_TRAPS && !placeableLocations.isEmpty()) {
			return placeableLocations.get(0);
		} else {
			return null;
		}
	}
}