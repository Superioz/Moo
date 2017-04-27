package de.superioz.moo.api.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GzipUtil {

    /**
     * Compresses file from given filePath into a gzipPath
     *
     * @param filePath The file to be compressed
     * @param gzipPath The path to compress to (.gz)
     * @return The result
     */
    public static boolean compress(Path filePath, Path gzipPath) {
        byte[] buffer = new byte[1024];

        try {
            FileOutputStream fos = new FileOutputStream(gzipPath.toString());
            GZIPOutputStream gzos = new GZIPOutputStream(fos);
            FileInputStream in = new FileInputStream(filePath.toString());

            int len;
            while((len = in.read(buffer)) > 0){
                gzos.write(buffer, 0, len);
            }

            in.close();

            gzos.finish();
            gzos.close();
            fos.close();
            return true;
        }
        catch(IOException ex) {
            System.err.println("Exception while compressing '" + filePath.toString() + "':");
            ex.printStackTrace();
        }
        return false;
    }

}
