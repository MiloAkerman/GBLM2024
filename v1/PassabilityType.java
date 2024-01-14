package v1;

/**
 * Categorizes different types of passability strategies for pathfinding
 */
public enum PassabilityType {
    /**
     * All tiles are allowed if they are in the map
     */
    ALLOKAY,

    /**
     * Walls and water are rejected, but dams are allowed
     */
    DAMSOKAY,

    /**
     * Walls, water, ducks rejected. Dams allowed.
     */
    DAMSNODUCKS,

    /**
     * Walls, waters, and dams rejected
     */
    NOPERMANENT,

    /**
     * Walls, water, dams, and other ducks rejected
     */
    NOOBSTACLE
}
