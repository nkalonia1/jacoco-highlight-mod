package jacoco.report.internal.html.parse.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nkalonia1 on 3/28/16.
 */
public class NameString {
    private static final Pattern _WILD_WORD = Pattern.compile("\\*");
    private static final Pattern _WILD_CHAR = Pattern.compile("\\?");
    private static final Pattern _WILD;
    static {
        _WILD = Pattern.compile(_WILD_WORD.pattern() + "|" + _WILD_CHAR.pattern());
    }
    private String _name;
    private Pattern _pattern;

    public NameString(String name) {
        _name = name == null ? "" : name;
        _pattern = compileNamePattern(_name);
    }

    private Pattern compileNamePattern(String name) {
        Matcher m = _WILD.matcher(name);
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        while (m.find()) {
            sb.append("\\Q").append(name.substring(idx, m.start())).append("\\E");
            if (_WILD_WORD.matcher(m.group()).matches()) {
                sb.append(".*");
            } else {
                sb.append(".");
            }
            idx = m.end();
        }
        if (idx < name.length()) {
            sb.append("\\Q").append(name.substring(idx, name.length())).append("\\E");
        }
        return Pattern.compile(sb.toString());
    }

    public String get() {
        return _name;
    }

    public boolean matches(String s) {
        return _pattern.matcher(s == null ? "" : s).matches();
    }
}
