package za.co.wethinkcode.robots.models.impediment;

   public  class Obstacle implements Impediments {
        private final int x1, y1, x2, y2;
        private final String type;

        public Obstacle(int x1, int y1, int x2, int y2, String type) {
            this.x1 = x1; this.y1 = y1;
            this.x2 = x2; this.y2 = y2;
            this.type = type;
        }

        public boolean isAt(int x, int y) {
            return (x >= x1 && x <= x2) && (y <= y1 && y >= y2);
        }

        public String getType() { return type; }
    }
