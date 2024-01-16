package v1;
import battlecode.common.*;
import v1.Constants.*;

public class Setup extends Duck {
    public static void run(RobotController rc) throws GameActionException {
        if(turnCount > 150) return; // Prepare for match start

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

        MapLocation crumb = findCrumbs(currLoc);
        if(crumb != null) pathfinding.moveOnce(currLoc.directionTo(crumb));
        else pathfinding.moveOnce(Constants.DIRECTIONS[rng.nextInt(Constants.DIRECTIONS.length)]);
    }
}
