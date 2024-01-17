package hades;

import battlecode.common.*;

/**
 * Passability strategy for pathfinding
 *
 * @author Milo
 */
public class PassabilityStrategy {
    PassabilityType passType;
    RobotController rc;

    PassabilityStrategy(RobotController rc) {
        this.rc = rc;
        this.passType = Constants.DEFAULT_PASSABILITY;
    }
    PassabilityStrategy(RobotController rc, PassabilityType passType) {
        this.rc = rc;
        this.passType = passType;
    }

    public boolean canPass(MapLocation location) throws GameActionException {
        if(!rc.canSenseLocation(location)) return false;

        MapInfo locInfo = rc.senseMapInfo(location);
        boolean hasDuck = rc.senseRobotAtLocation(location) != null;
        if(passType == PassabilityType.ALLOKAY) return true;
        if(passType == PassabilityType.DAMSOKAY) return locInfo.isPassable() || !(locInfo.isWall() || locInfo.isWater());
        if(passType == PassabilityType.DAMSNODUCKS) return !hasDuck && (locInfo.isPassable() || !(locInfo.isWall() || locInfo.isWater()));
        if(passType == PassabilityType.NOPERMANENT) return locInfo.isPassable();
        if(passType == PassabilityType.NOOBSTACLE) return !hasDuck && locInfo.isPassable();
        return false;
    }
    public boolean canPass(MapLocation location, PassabilityType passType) throws GameActionException {
        if(!rc.canSenseLocation(location)) return false;

        MapInfo locInfo = rc.senseMapInfo(location);
        boolean hasDuck = rc.senseRobotAtLocation(location) != null;
        if(passType == PassabilityType.ALLOKAY) return true;
        if(passType == PassabilityType.DAMSOKAY) return locInfo.isPassable() || !(locInfo.isWall() || locInfo.isWater());
        if(passType == PassabilityType.DAMSNODUCKS) return !hasDuck && (locInfo.isPassable() || (locInfo.isWall() || locInfo.isWater()));
        if(passType == PassabilityType.NOPERMANENT) return locInfo.isPassable();
        if(passType == PassabilityType.NOOBSTACLE) return !hasDuck && locInfo.isPassable();
        return false;
    }
}
