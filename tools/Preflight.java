import java.nio.file.*;

/** Probes whether a java child process can create files inside the given directory. */
public final class Preflight {
    public static void main(String[] args) {
        try {
            Path dir = Paths.get(args[0]);
            Path probe = dir.resolve(".build-preflight-java");
            Files.createDirectories(probe);
            Files.delete(probe);
            System.out.println("[build-1.20.1] preflight: java can write " + dir);
        } catch (Exception e) {
            System.out.println("[build-1.20.1] preflight: java CANNOT write " + args[0]
                    + " (" + e.getClass().getSimpleName() + ")");
            System.exit(9);
        }
    }
}
