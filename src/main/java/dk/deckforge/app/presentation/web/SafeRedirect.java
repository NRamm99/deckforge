package dk.deckforge.app.presentation.web;

public final class SafeRedirect {

    private SafeRedirect() {
    }

    public static String byPathPrefix(String referer, String pathPrefix, String defaultPath) {
        if (defaultPath == null || defaultPath.isBlank()) {
            throw new IllegalArgumentException("defaultPath");
        }
        if (pathPrefix == null || pathPrefix.isBlank()) {
            return defaultPath;
        }
        if (referer == null || referer.isBlank()) {
            return defaultPath;
        }

        int idx = referer.indexOf(pathPrefix);
        if (idx < 0) {
            return defaultPath;
        }

        return referer.substring(idx);
    }
}

