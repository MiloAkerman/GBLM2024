package v1;
import battlecode.common.*;

import java.util.Random;

/**
 * Mostly boilerplate. Directs functionality to individual duck units.
 *
 * @author Milo
 */
public strictfp class RobotPlayer {
    static RobotController rc;
    static Team myTeam;
    static Team oppTeam;
    static int turnCount = 0;
    static int spawnTurnCount = 0;
    static Random rng = new Random(9791);

    static int mapWidth, mapHeight;

    // Run is the method called as soon as a duck is created. Returns when duck dies.
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        // General setup. All ducks regardless of type will need this
        RobotPlayer.rc = rc;
        myTeam = rc.getTeam();
        oppTeam = rc.getTeam().opponent();
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        turnCount = rc.getRoundNum();

        // Duck setup
        try {
            Duck.setup();
        } catch (GameActionException e) {
            System.out.println("GameActionException");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }

        // While loop means duck is alive. Will run 1 iteration per turn
        while(true) {
            turnCount += 1;
            spawnTurnCount += 1;

            try {
                Duck.run();
            } catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                // End turn and wait for next
                Clock.yield();
            }
        }
    }
}
