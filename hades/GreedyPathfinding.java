package hades;
import static hades.Constants.*;
import battlecode.common.*;

public class GreedyPathfinding extends Pathfinding {
    private MapLocation destination;

    GreedyPathfinding(RobotController rc) {
        super(rc);
    }
    GreedyPathfinding(RobotController rc, PassabilityType passType) {
        super(rc, passType);
    }


    // ------------------------------------------ GETTERS AND SETTERS  ----------------------------------------------
    public void setDestination(RobotController rc, MapLocation newDestination) {
        destination = newDestination;
    }
    public void resetDestination() {
        destination = null;
    }
    public MapLocation getDestination() {
        return destination;
    }

    // ------------------------------------------------- ACTION ----------------------------------------------------
    @Override
    public void step() throws GameActionException {
        if(!rc.isSpawned()) return;         // We do not exist
        if(!rc.isMovementReady()) return;   // Already moved

        // We have somewhere to go and we exist
        if(destination != null) {
            Direction bestMove = getBestDirection(destination);
            if(bestMove != null && rc.canMove(bestMove)) {
                rc.move(bestMove);
            }
        }
    }

    // ------------------------------------------------ INFO -----------------------------------------------------
    @Override
    public Direction getBestDirection(MapLocation destination) throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        int bestDist = currLoc.distanceSquaredTo(destination);

        Direction bestMove = null;
        for (Direction direction : DIRECTIONS) {
            MapLocation newLoc = currLoc.add(direction);
            if(!rc.canSenseLocation(newLoc)) continue;

            // Determine if tile is closer to destination and meets passability rules
            int newDist = newLoc.distanceSquaredTo(destination);
            if ((newDist < bestDist || bestMove == null)) {
                if(!passStrat.canPass(newLoc)) continue;
                bestMove = direction;
                bestDist = newDist;
            }
        }

        return null;
    }

    public static Direction bestGreedyMove(RobotController rc, MapLocation destination) throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        int bestDist = currLoc.distanceSquaredTo(destination);

        Direction bestMove = null;
        for (Direction direction : DIRECTIONS) {
            MapLocation newLoc = currLoc.add(direction);
            if(!rc.canSenseLocation(newLoc)) continue;

            // Determine if tile is closer to destination and meets passability rules
            int newDist = newLoc.distanceSquaredTo(destination);
            if ((newDist < bestDist || bestMove == null)) {
                if(!rc.canMove(direction)) continue;
                bestMove = direction;
                bestDist = newDist;
            }
        }

        return bestMove;
    }
}
