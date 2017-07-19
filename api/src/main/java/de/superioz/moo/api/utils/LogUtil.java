package de.superioz.moo.api.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Month;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogUtil {

    /**
     * Compress latest.log
     *
     * @param path      The label
     * @param folder    The folder
     * @param logFormat The log format
     * @return The result
     */
    public static boolean compressLastLog(String path, String folder, String logFormat) {
        boolean r = false;

        // compresses old latest.logging
        File f = new File(Paths.get(path).toString());
        if(f.exists()) {
            String date = new SimpleDateFormat("dd-MM-yyyy").format(f.lastModified());
            String pattern = folder + "/" + logFormat;

            File f0;

            int c = 1;

            while((f0 = new File(MessageFormat.format(pattern, date, c))).exists()){
                c++;
            }
            r = GzipUtil.compress(Paths.get(f.getPath()), Paths.get(f0.getPath()));
        }
        return r;
    }

    /**
     * Sorts compressed logs into a new folder
     *
     * @param folder    The log
     * @param logFolder The logFolder name
     * @return The result
     */
    public static boolean sortCompressedLogs(File folder, String logFolder) {
        File[] files = folder.listFiles();
        if(files == null) return false;

        for(File file : files) {
            if(!file.getName().endsWith(".log.gz")) continue;
            String fileName = file.getName();
            String[] firstSplit = fileName.split("\\.log\\.gz", 2);
            fileName = firstSplit[0];
            String[] secondSplit = fileName.split("-");
            int count = Integer.parseInt(secondSplit[secondSplit.length - 1]);
            String fileNameWithoutCount = fileName.replace("-" + count, "");

            String[] spl = fileName.split("-");
            if(spl.length < 3) continue;

            // get the foldername (e.g. October, 2016)
            String folderName = "";
            try {
                int monthId = Integer.parseInt(spl[1]);
                int year = Integer.parseInt(spl[2]);
                Month month = Month.of(monthId);

                folderName = StringUtil.upperFirstLetter(month.name().toLowerCase()) + ", " + year;
            }
            catch(Exception ex) {
                continue;
            }
            if(folderName.isEmpty()) continue;

            // create the folder
            File targetFolder = new File(logFolder + "/" + folderName);
            int newCount = IOUtil.getNextId(targetFolder, fileNameWithoutCount, "-", ".log.gz", 0);

            IOUtil.moveFiles(file, fileNameWithoutCount + "-" + newCount + ".log.gz", targetFolder);
        }
        return true;
    }

}
