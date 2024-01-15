package v1;
import battlecode.common.*;
import java.util.*;
import static v1.Constants.*;

public class BugPathfinding extends Pathfinding {
    private MapLocation destination;
    private MapLocation startLoc;
    private Direction currDir;
    private boolean goalSide = false;

    BugPathfinding(RobotController rc) {
        super(rc);
    }
    BugPathfinding(RobotController rc, PassabilityType passType) {
        super(rc, passType);
    }


    // ------------------------------------------ GETTERS AND SETTERS  ----------------------------------------------
    public void setDestination(MapLocation newDestination) {
        destination = newDestination;
        startLoc = rc.getLocation();
        //currDir = null;
    }
    public void resetDestination() {
        destination = null;
        startLoc = null;
        //currDir = null;
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
            rc.setIndicatorString("Bugnav move: " + bestMove);
            if(bestMove != null && rc.canMove(bestMove)) {
                rc.move(bestMove);
            } else if (bestMove != null) {
                Direction newBestMove = GreedyPathfinding.bestGreedyMove(rc, destination);
                if(newBestMove != null) {
                    rc.move(newBestMove);
                    startLoc = rc.getLocation();
                }
            }
        } else {
            rc.setIndicatorString("No destination!!");
        }
    }

    // ------------------------------------------------ INFO -----------------------------------------------------
    @Override
    public Direction getBestDirection(MapLocation destination) throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        Direction targetDir = currLoc.directionTo(destination);
        MapLocation adjacentLoc = rc.adjacentLocation(targetDir);

        if(destination.equals(currLoc)) {
            resetDestination();
            return null;
        };

        if (currDir == null && passStrat.canPass(adjacentLoc)) {
            rc.setIndicatorString("Can move to my favorite!! :3 " + targetDir);
            return targetDir;
        } else {
            if (currDir == null) {
                currDir = targetDir;
                rc.setIndicatorString("Blocked D: currDir is " + currDir);
                goalSide = !isLeft(startLoc, destination, currLoc);
            } else {
                rc.setIndicatorString("We keep chugging on...");
            }

            if (goalSide == isLeft(startLoc, destination, currLoc)) {
                currDir = null;
                return null;
            }

            for (int i = 0; i < 8; i++) {
                MapLocation followWallLocation = rc.adjacentLocation(currDir);

                if (passStrat.canPass(followWallLocation)) {
                    Direction bestDir = currDir;
                    if(rc.canMove(currDir)) currDir = currLoc.add(currDir).directionTo(currLoc.add(currDir.rotateRight()));
                    return bestDir;
                } else {
                    currDir = currDir.rotateLeft();
                }
            }
        }

        return null;
    }

    public boolean isLeft(MapLocation lineStart, MapLocation lineEnd, MapLocation point) {
        return ((lineEnd.x - lineStart.x)*(point.y - lineStart.y) - (lineEnd.y - lineStart.y)*(point.x - lineStart.x)) > 0;
    }

    @Override
    public void moveOnce(Direction dir) throws GameActionException {
        super.moveOnce(dir);
        startLoc = rc.getLocation();
        currDir = null;
    }
}
