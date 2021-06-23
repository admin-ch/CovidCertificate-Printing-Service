package ch.admin.bag.covidcertificate.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UvciUtils {
    public static String mapFilenameFromUVCI(String uvci) {
        var uvciParts = uvci.split(":");
        var filename = uvciParts[uvciParts.length - 1];
        return filename + ".pdf";
    }
}
