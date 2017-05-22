package net.team33.hash.mission;

import java.util.Collections;
import java.util.List;

public class Task {

    /**
     * The files or rather directories, that shall be (recursively) worked on
     */
    public List<String> paths = Collections.singletonList(".");

    /**
     * Indicates whether a file's hash should be noted in a hash-file ({@code false})
     * or in the filename of any file ({@code true})
     */
    public boolean rename = false;

    /**
     * The path of a directory, where unique files should be moved to
     * <p>
     * May be {@code null} or missing, than unique files will remain unmoved
     */
    public String sieved = "../[sieved]";

    /**
     * The path of a directory, where duplicate files should be moved to
     * <p>
     * May be {@code null} or missing, than duplicate files will remain unmoved
     */
    public String trash = "../[trash]";
}
