package utils;

public enum Direction {
    SOUTH,
    NORTH,
    WEST,
    EAST;

    public static Direction getDirection(String direction) {
        switch (direction.charAt(0)) {
            case 'W':
                return WEST;
            case 'N':
                return NORTH;
            case 'S':
                return SOUTH;
            case 'E':
                return EAST;
            default:
                throw new IllegalStateException("Unexpected value: " + direction.charAt(0));
        }
    }
}
