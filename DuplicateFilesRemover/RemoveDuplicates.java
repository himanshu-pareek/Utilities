import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

class Util {
    public static String calculateSHA256Sum (Path filePath) throws Exception {
        byte[] data = Files.readAllBytes(filePath);
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
        return new BigInteger(1, hash).toString(16);
    }
}

/**
 * SHA256CalculatorFileVisitor
 */
class SHA256CalculatorFileVisitor implements FileVisitor<Path>{

    private Set<String> hashes;

    public SHA256CalculatorFileVisitor (Set<String> hashes) {
        this.hashes = hashes;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        try {
            String checksum = Util.calculateSHA256Sum(file);
            this.hashes.add(checksum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        exc.printStackTrace();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}

class DuplicateDeletorFileVisitor implements FileVisitor<Path>{

    private Set<String> hashes;

    public DuplicateDeletorFileVisitor (Set<String> hashes) {
        this.hashes = hashes;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        try {
            String checksum = Util.calculateSHA256Sum(file);
            if (this.hashes.contains(checksum)) {
                // Delete the file
                System.out.println("Deleting " + file + "...");
                Files.delete(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        exc.printStackTrace();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}

class RemoveDuplicatesHelper {

    private Path path1;
    private Path path2;

    public RemoveDuplicatesHelper (String dir1, String dir2) throws IOException {
        path1 = Paths.get(dir1).toRealPath();
        path2 = Paths.get(dir2).toRealPath();
    }

    public void removeDuplicates () throws IOException {
        Set<String> hashes = new HashSet<>();
        Files.walkFileTree(path1, new SHA256CalculatorFileVisitor(hashes));
        Files.walkFileTree(path2, new DuplicateDeletorFileVisitor(new HashSet<>(hashes)));
    }

    @Override
    public String toString() {
        return "" + path1 + ", " + path2;
    }
}

public class RemoveDuplicates {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java RemoveDuplicates <dir1> <dir2>");
            System.exit(1);
        }

        RemoveDuplicatesHelper helper = new RemoveDuplicatesHelper(args[0], args[1]);
        System.out.println(helper);
        helper.removeDuplicates();
    }
}
