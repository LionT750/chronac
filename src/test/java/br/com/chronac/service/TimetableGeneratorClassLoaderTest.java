package br.com.chronac.service;

import br.com.chronac.demo.TimetableDemoData;
import br.com.chronac.domain.Timetable;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Guards the fat-jar failure that no other test could see.
 *
 * SolverConfig does not hold the Class objects it is handed - it stores their names
 * and resolves them again through the calling thread's context classloader. Solve on
 * a thread whose context classloader cannot see our classes and the solver dies with
 * "The solutionClass (br.com.chronac.domain.Timetable) cannot be found". That is
 * exactly what happened when the demo ran on a ForkJoinPool common-pool worker inside
 * the Spring Boot jar, and no test caught it because a test classpath is flat: the
 * system classloader there can load everything.
 *
 * These two tests recreate the condition instead of the packaging.
 */
class TimetableGeneratorClassLoaderTest {

    /** A loader that resolves nothing, standing in for a fat jar's system loader. */
    private static final ClassLoader BLIND = new ClassLoader(null) {
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            throw new ClassNotFoundException(name);
        }
    };

    @Test
    void solvesOnAThreadWhoseContextClassLoaderCannotSeeTheDomain() throws Exception {
        Thread solver = new Thread(() -> {
            Timetable solved = new TimetableGenerator()
                    .solveSeeded(TimetableDemoData.buildDemoProblem(), TimetableGenerator.DEMO_SEED, 2, 2);
            assertNotNull(solved.getScore());
        });
        solver.setContextClassLoader(BLIND);
        solver.start();
        solver.join();
    }

    /**
     * The exact shape TimetableDemoSolver uses at startup. Common-pool workers do not
     * inherit the caller's context classloader, which is what broke the jar.
     */
    @Test
    void solvesOnTheForkJoinCommonPool() throws ExecutionException, InterruptedException {
        CompletableFuture.supplyAsync(() -> new TimetableGenerator()
                .solveSeeded(TimetableDemoData.buildDemoProblem(), TimetableGenerator.DEMO_SEED, 2, 2))
                .thenAccept(solved -> assertNotNull(solved.getScore()))
                .get();
    }
}
