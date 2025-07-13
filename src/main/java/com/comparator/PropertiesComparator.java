package com.comparator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesComparator {

    public static boolean loadProperties(Properties props, File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            props.clear();
            props.load(fis);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
