
package com.akiban.sql;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class RegexFilenameFilter implements FilenameFilter
{
    Pattern pattern;

    public RegexFilenameFilter(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public boolean accept(File dir, String name) {
        return pattern.matcher(name).matches();
    }
}
