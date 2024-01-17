package hades;
import battlecode.common.*;
import hades.Constants.*;

public class Setup extends Duck {
    public static void run(RobotController rc) throws GameActionException {
        MapLocation currLoc = rc.getLocation();

        // TODO: Move to function
        for(MapInfo loc : rc.senseNearbyMapInfos(-1)) {
            if(loc.isWater()) {
                if(rc.canFill(loc.getMapLocation())) {
                    rc.fill(loc.getMapLocation());
                    pathfinding.moveOnce(currLoc.directionTo(loc.getMapLocation()));
                    break;
                }
            }
        }

        if(turnCount > 100) return; // Prepare for match start

        MapLocation crumb = findCrumbs(currLoc);
        if(crumb != null) pathfinding.moveOnce(currLoc.directionTo(crumb));
        else pathfinding.moveOnce(Constants.DIRECTIONS[rng.nextInt(Constants.DIRECTIONS.length)]);
    }
}
