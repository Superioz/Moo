package de.superioz.moo.api.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IOUtil {

    /**
     * Get the next id for a server
     *
     * @param folder The folder
     * @param name   The name
     * @return The id
     */
    public static int getNextId(File folder, String name, String split, String ending, int start) {
        File[] files = folder.listFiles();
        int id = start;

        if(files == null || files.length == 0) {
            return id;
        }

        int count = start;
        while(id == start){
            File f = new File(folder, name + split + count + ending);

            if(!f.exists()) {
                id = count;
            }
            count++;
        }
        return id;
    }

    public static int getNextId(File folder, String split, String name){
        return getNextId(folder, name, split, "", 1);
    }

    /**
     * Checks if a file named "fileName" exists inside given src file
     *
     * @param src      The src file
     * @param fileName The name of the to-search file
     * @return The result
     */
    public static boolean doesExistsInside(File src, String fileName) {
        return new File(src, fileName).exists();
    }

    /**
     * Deletes a file (if it's a directory recursively)
     *
     * @param file The file/directory
     * @return The result
     */
    public static boolean deleteFile(File file) {
        if(!file.isDirectory()) {
            return file.delete();
        }

        try {
            Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return !file.exists();
    }

    /**
     * Copies one file to another file
     *
     * @param src  The file to be copied
     * @param dest The destination of the copy-operation
     */
    public static boolean copyFiles(File src, File dest) {
        boolean r = false;

        if(!dest.exists()) {
            r = dest.mkdirs();
        }

        File[] files = src.listFiles();
        if(files == null) {
            try {
                Files.copy(src.toPath(), new File(dest, src.getName()).toPath(),
                        StandardCopyOption.COPY_ATTRIBUTES);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        for(File srcFiles : files) {
            if(srcFiles.isDirectory()) {
                r = copyFiles(srcFiles, new File(dest, srcFiles.getName()));
                continue;
            }

            try {
                Files.copy(srcFiles.toPath(), new File(dest, srcFiles.getName()).toPath(),
                        StandardCopyOption.COPY_ATTRIBUTES);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        return r;
    }

    /**
     * Moves the source file to the destination file (similar to {@link #copyFiles(File, File)} but with deletion)<br>
     *
     * @param src    The source
     * @param newSrc The new source
     * @param dest   The destination
     * @return The result
     */
    public static boolean moveFiles(File src, String newSrc, File dest) {
        boolean r = false;

        if(!dest.exists()) {
            r = dest.mkdirs();
        }

        File[] files = src.listFiles();
        if(files == null) {
            try {
                Files.move(src.toPath(), new File(dest, newSrc).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        for(File srcFiles : files) {
            if(srcFiles.isDirectory()) {
                r = moveFiles(srcFiles, newSrc, new File(dest, srcFiles.getName()));
                continue;
            }

            try {
                Files.move(srcFiles.toPath(), new File(dest, srcFiles.getName()).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        return r;
    }

}
