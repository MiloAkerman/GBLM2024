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
		ArrayList<MapLocation> spawnTrapsLocations = getSpawnTrapLocation();

		// build traps at spawn
		for (MapLocation spawnTrapLocation : spawnTrapsLocations) {
			if (rc.canBuild(TrapType.EXPLOSIVE, spawnTrapLocation)) rc.build(TrapType.EXPLOSIVE, spawnTrapLocation);
		}


		// Found a flag! First priority is to pick up and carry back
		for (FlagInfo flag : enemyFlagsInfo) {
			if (rc.canPickupFlag(flag.getLocation())) {
				rc.pickupFlag(flag.getLocation());
				pathfinding.setDestination(spawn);
			}
		}

		if(enemyDucksAttack.length > 0 && turnCount >= 200) {
			// Enemies present, takes priority over anything else


			if(enemyDucksAttack.length >= MIN_ENEMIES_FOR_EXPL && rc.canBuild(TrapType.EXPLOSIVE, enemyDucksAttack[0].location))
				rc.build(TrapType.EXPLOSIVE, enemyDucksAttack[0].location);
			Attack: {
				// TODO: no random
				// prioritize weakest enemy duck that within attack radius
				// improvement, use comms to gang up within mutual attack radius on a duck 1 by 1, weakest first
				RobotInfo weakest = enemyDucksAttack[0];
				for (RobotInfo enemy : enemyDucksAttack) {
					if (enemy.health < weakest.health && rc.canAttack(enemy.getLocation())) {
						weakest = enemy;
					}
				}

				if (rc.canAttack(weakest.getLocation())) rc.attack(weakest.getLocation());

				if (!rc.hasFlag()) {
					// Back off after attacking
					pathfinding.moveOnce(rc.getLocation().directionTo(weakest.getLocation()).opposite());
				}
			}


		} else {
			// No enemies in attack range

			FlagCheck: if(teamFlagsInfo.length > 0) {
				for(FlagInfo flag : teamFlagsInfo) {
					if(flag.getLocation().equals(currLoc)) {
						pathfinding.doNotMove = true; //TODO: Setter
					}
				}

				FlagInfo randomFlag = teamFlagsInfo[rng.nextInt(teamFlagsInfo.length)];
				if(rc.canSenseLocation(randomFlag.getLocation()) && rc.senseRobotAtLocation(randomFlag.getLocation()) == null) {
					pathfinding.moveOnce(currLoc.directionTo(randomFlag.getLocation()));
				}
			}

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
	public static ArrayList<MapLocation> getSpawnTrapLocation() {
		MapLocation[] spawnLocations = rc.getAllySpawnLocations();
		ArrayList<MapLocation> spawnTrapsLocations = new ArrayList<MapLocation>();

		for (MapLocation spawnLocation : spawnLocations) {

			spawnTrapsLocations.add(new MapLocation(spawnLocation.x-1, spawnLocation.y));
			spawnTrapsLocations.add(new MapLocation(spawnLocation.x+1, spawnLocation.y));
			spawnTrapsLocations.add(new MapLocation(spawnLocation.x-1, spawnLocation.y-1));
			spawnTrapsLocations.add(new MapLocation(spawnLocation.x, spawnLocation.y-1));
			spawnTrapsLocations.add(new MapLocation(spawnLocation.x+1, spawnLocation.y-1));
			spawnTrapsLocations.add(new MapLocation(spawnLocation.x-1, spawnLocation.y+1));
			spawnTrapsLocations.add(new MapLocation(spawnLocation.x, spawnLocation.y+1));
			spawnTrapsLocations.add(new MapLocation(spawnLocation.x+1, spawnLocation.y+1));

		}

		return spawnTrapsLocations;
	}

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
}
