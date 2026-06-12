import com.upc.edubridge.course.model.Course;
import com.upc.edubridge.course.repository.CourseRepository;
import com.upc.edubridge.resource.repository.ResourceRepository;
import com.upc.edubridge.student.repository.StudentRepository;
import com.upc.edubridge.teacher.repository.TeacherTaskRepository;
import com.upc.edubridge.tutoring.repository.TutoringRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public DataInitializer(CourseRepository courseRepository, StudentRepository studentRepository, org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {

        System.out.println(">> EduBridge: Sincronizando con base de datos real...");


    }
}