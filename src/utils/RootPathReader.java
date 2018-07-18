package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Created by Eddie on 2018/7/17.
 */
public class RootPathReader {
    Stack<File> fileStack;
    Iterator<File> curFilesIterator;


    public RootPathReader(String rootPath) {
        fileStack = new Stack<>();
        File rootFile = new File(rootPath);
        if (rootFile.isDirectory()) {
            fileStack.push(rootFile);
        }
    }

    public File nextFile() {
        if (fileStack.empty() && (curFilesIterator == null || !curFilesIterator.hasNext())) {
            return null;
        } else {
            while ((curFilesIterator == null || !curFilesIterator.hasNext()) && !fileStack.empty()) {
                File curDir = fileStack.pop();
                File[] allFilesAndDirInDir = curDir.listFiles();
                List<File> filesList = new ArrayList<>();
                for (File file: allFilesAndDirInDir) {
                    if (file.isDirectory()) {
                        fileStack.push(file);
                    } else {
                        filesList.add(file);
                    }
                }
                curFilesIterator = filesList.iterator();
                break;
            }
            if (curFilesIterator.hasNext()) {
                return curFilesIterator.next();
            } else {
                return null;
            }
        }
    }
}
