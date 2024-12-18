public class ZZZTest {
    public static void main(String[] args) {
        Object obj = new Circle(5);

        // Switch-like behavior using instanceof and pattern matching (Java 14+)
        switch (obj) {
            case Circle c -> System.out.println("It's a Circle with radius: " + c.getRadius());
            case Square s -> System.out.println("It's a Square with side length: " + s.getSideLength());
            case Rectangle r -> System.out.println("It's a Rectangle with width: " + r.getWidth() + " and height: " + r.getHeight());
            default -> System.out.println("Unknown shape");
        }
    }
}

class Circle {
    private double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    public double getRadius() {
        return radius;
    }
}

class Square {
    private double sideLength;

    public Square(double sideLength) {
        this.sideLength = sideLength;
    }

    public double getSideLength() {
        return sideLength;
    }
}

class Rectangle {
    private double width, height;

    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
