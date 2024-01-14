package v1;
import v1.Constants.*;
import battlecode.common.*;

/*
    Currently using bugnav pathfinding. Below are other options in order of efficiency:
    1. Precomputed global BFS
    2. Parallelized Bellman-Ford    (Resource: https://discordapp.com/channels/386965718572466197/401058232346345473/1070487254969098263)
    3. TODO: Local BFS
*/
public abstract class Pathfinding  {
    RobotController rc;
    PassabilityStrategy passStrat;

    Pathfinding(RobotController rc) {
        this.rc = rc;
        this.passStrat = new PassabilityStrategy(rc, Constants.DEFAULT_PASSABILITY);
    }
    Pathfinding(RobotController rc, PassabilityType passType) {
        this.rc = rc;
        this.passStrat = new PassabilityStrategy(rc, passType);
    }

    /**
     * Continues to the path set in destination
     * @throws GameActionException Game error
     */
    public abstract void step() throws GameActionException;

    /**
     *
     * @param destination The pathfinding destination
     * @return Best direction to move
     * @throws GameActionException Game error
     * @Bytecode 400-600
     */
    public abstract Direction getBestDirection(MapLocation destination) throws GameActionException;

    /**
     * Move once in one direction or its adjacent directions
     * @param dir The direction to travel in
     * @throws GameActionException Game error
     */
    public void moveOnce(Direction dir) throws GameActionException {
        if(rc.canMove(dir)) rc.move(dir);
        else if(rc.canMove(dir.rotateLeft())) rc.move(dir.rotateLeft());
        else if(rc.canMove(dir.rotateRight())) rc.move(dir.rotateRight());
    }
}
