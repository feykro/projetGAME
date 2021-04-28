package utils;

public enum Direction {
    SOUTH,
    NORTH,
    WEST,
    EAST;

    /**
     * convert string into direction
     * IllegalStateException if that was not a direction
     * @param direction string to convert
     * @return Direction converted
     */
    public static Direction getDirection(String direction) {
        return switch (direction.charAt(0)) {
            case 'W' -> WEST;
            case 'N' -> NORTH;
            case 'S' -> SOUTH;
            case 'E' -> EAST;
            default -> throw new IllegalStateException("Unexpected value: " + direction.charAt(0));
        };
    }
}
