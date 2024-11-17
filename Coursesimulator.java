import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class Coursesimulator {
    private final PreferenceGenerationStrategy strategy;
    private final Random random;

    public Coursesimulator(PreferenceGenerationStrategy strategy) {
        this.strategy = strategy;
        this.random = new Random();
    }
    
    private String generatesubject() {
        String[] subjects = {"MAT", "MAP", "HSS", "LAN", "MEC", "ECO", "BIO", "INF", "PHY", "CHI"};
        String matter = subjects[random.nextInt(subjects.length)];
        String number = Integer.toString(300 + random.nextInt(200));
        return matter + number;
    }
    public List<Course> generateCourses(int numCourses, int minsize, int maxsize, int changesize) {
        List<Course> courses = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) {
            int size;
            if (random.nextDouble() < 0.8) {
                size = random.nextInt(changesize - minsize + 1) + minsize;
            } else {
                size = random.nextInt(maxsize - changesize + 1) + changesize;
            }
            courses.add(new Course(generatesubject(), size));
            courses.get(i).setTypePreferences(strategy.generatePreferences(courses.get(i)));
        }
        return courses;
    }
}