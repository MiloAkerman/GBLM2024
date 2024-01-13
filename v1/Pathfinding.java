package v1;
import battlecode.common.*;
import java.util.*;
import static v1.Constants.*;

public class Pathfinding {
    private static MapLocation destination;
    private static int bugNavTurns;
    public static MapLocation bugNavHitPoint;
    private static MapLocation bugNavObstacle;
    public static ArrayList<MapLocation> mLine = new ArrayList<>();

    // ------------------------------------------ GETTERS AND SETTERS  ----------------------------------------------
    public static void setDestination(RobotController rc, MapLocation newDestination) {
        destination = newDestination;

        MapLocation currLoc = rc.getLocation();
        bugNavTurns = -1;
        bugNavHitPoint = null;
        bugNavObstacle = null;

        // Modified Bresenham algorithm
        int xDist = Math.abs(newDestination.x - currLoc.x);
        int yDist = -Math.abs(newDestination.y - currLoc.y);
        int xStep = (currLoc.x < newDestination.x ? +1 : -1);
        int yStep = (currLoc.y < newDestination.y ? +1 : -1);
        int error = xDist + yDist;

        int x0 = currLoc.x;
        int y0 = currLoc.y;
        // TODO: Line should reach until end of map
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
    public static void resetDestination() {
        destination = null;
        bugNavTurns = -1;
        bugNavHitPoint = null;
        bugNavObstacle = null;
        mLine = new ArrayList<>();
    }
    public static MapLocation getDestination() {
        return destination;
    }


    // ------------------------------------------------- ACTION ----------------------------------------------------
    /*
        Currently using greedy pathfinding. Below are other options in order of efficiency:
        1. Precomputed global BFS
        2. Parallelized Bellman-Ford    (Resource: https://discordapp.com/channels/386965718572466197/401058232346345473/1070487254969098263)
        3. TODO: Local BFS
        4. TODO: BUG2 / BUG1            (Resource: https://www.cs.cmu.edu/~motionplanning/lecture/Chap2-Bug-Alg_howie.pdf)
    */
    /**
     * Continues to the path set in destination, optional pathfinding end callback
     * @param rc Robot Controller
     * @throws GameActionException Game error
     */
    public static void step(RobotController rc) throws GameActionException {
        if(!rc.isSpawned()) return;         // We do not exist
        if(!rc.isMovementReady()) return;   // Already moved

        // We have somewhere to go and we exist
        if(destination != null) {
            Direction bestMove = getBestDirection(rc, destination, 3);
            if(bestMove != null && rc.canMove(bestMove)) {
                rc.move(bestMove);
            }
        }

    }

    public static void bugNav(RobotController rc) throws GameActionException {
        if(!rc.isSpawned()) return;         // We do not exist
        if(!rc.isMovementReady()) return;   // Already moved

        // We have somewhere to go and we exist
        if(destination != null) {
            if(isOnMLine(rc)) {
                // On mLine, keep moving! Wait for other ducks.
                // If goal hit, reset pathfinding
                // If obstacle hit, bugNavTurns = 1, hitPoint, obstacle = nearestRight; then move rightmost
            } else if (bugNavTurns != -1) {
                // Going around obstacle: nearestRight starting from dir to obstacle
                // TODO: Dam behavior
                // If dam hit, wait in line?
                // If goal hit, reset pathfinding
                // If hitPoint hit, raise error
                // If on mLine, dist < hitPoint dist, release from bugNav
                // Check if at other end and closer
            }
        }
    }

    /**
     * Move once in one direction or its adjacent directions
     * @param rc Robot Controller
     * @param dir The direction to travel in
     * @throws GameActionException Game error
     */
    public static void moveOnce(RobotController rc, Direction dir) throws GameActionException {
        if(rc.canMove(dir)) rc.move(dir);
        else if(rc.canMove(dir.rotateLeft())) rc.move(dir.rotateLeft());
        else if(rc.canMove(dir.rotateRight())) rc.move(dir.rotateRight());
    }

    // ------------------------------------------------ INFO -----------------------------------------------------
    /**
     *
     * @param rc Robot Controller
     * @param destination The pathfinding destination
     * @param passabilityLevel 0 = Within map; 1 = No Walls or Water; 2 = No Walls, Water, Dams; 3 = Not Occupied
     * @return Best direction to move
     * @throws GameActionException Game error
     * @Bytecode 400-600
     */
    public static Direction getBestDirection(RobotController rc, MapLocation destination, int passabilityLevel) throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        int bestDist = currLoc.distanceSquaredTo(destination);

        Direction bestMove = null;
        for (Direction direction : DIRECTIONS) {
            MapLocation newLoc = currLoc.add(direction);

            // Get info of tile in new location
            MapInfo newLocInfo = null;
            if(rc.canSenseLocation(newLoc)) newLocInfo = rc.senseMapInfo(newLoc);
            else continue;

            // Determine if tile is closer to destination and meets passability rules
            int newDist = newLoc.distanceSquaredTo(destination);
            if ((newDist < bestDist || bestMove == null)) {
                if(     passabilityLevel >= 1 && (newLocInfo.isWater() || newLocInfo.isWall())
                    ||  passabilityLevel >= 2 && !rc.sensePassability(newLoc)
                    ||  passabilityLevel >= 3 && rc.senseRobotAtLocation(newLoc) != null)
                    continue;
                bestMove = direction;
                bestDist = newDist;
            }
        }

        return bestMove;
    }

    public static Direction nearestRightTile(RobotController rc, Direction initDir, int passabilityLevel) throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        Direction dir = initDir;

        for(int i = 0; i < 6; i++) {
            dir = dir.rotateRight();

            MapLocation loc = currLoc.add(dir);
            //if(rc.canMove(dir) || (isDam(loc))) return dir;
        }
        return null;
    }

    public static boolean isOnMLine(RobotController rc) throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        for(MapLocation loc : mLine) {
            if(currLoc.equals(loc)) return true;
        }
        return false;
    }
}
