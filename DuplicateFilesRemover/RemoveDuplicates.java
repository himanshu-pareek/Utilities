import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

class Util {
    public static String caluclateChecksum (Path filePath) throws IOException, NoSuchAlgorithmException {
        byte[] data = Files.readAllBytes(filePath);
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
        return new BigInteger(1, hash).toString(16);
    }
}

class DuplicateRemover implements FileVisitor<Path> {

    Set<String> checksums;

    public DuplicateRemover() {
        this.checksums = new HashSet<>();
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        try {
            String checksum = Util.caluclateChecksum(file);
            if (this.checksums.contains(checksum)) {
                // Delete file
                System.out.println("Deleting " + file + "...");
                Files.delete(file);
                System.out.println("Deleted " + file + ".");
            } else {
                this.checksums.add(checksum);
            }
        } catch (NoSuchAlgorithmException e) {
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

public class RemoveDuplicates {

    public static void main(String[] args) {
        Set<Path> paths = new HashSet<>();
        for (String dir: args) {
            try {
                Path path = Paths.get(dir).toRealPath();
                paths.add(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileVisitor<Path> visitor = new DuplicateRemover();
        paths.forEach(path -> {
            try {
                Files.walkFileTree(path, visitor);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
