package humanize.emoji;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Unified emoji for Java.
 * 
 * @see Unicode Emoji (working draft)
 *      http://www.unicode.org/reports/tr51/tr51-1d.html
 */
public final class Emoji
{
    /**
     * Represents an emoji character in accordance with Unicode emoji data
     * files.
     * 
     */
    public static final class EmojiChar implements Comparable<EmojiChar>, Serializable
    {

        private static final long serialVersionUID = 697634381168152779L;

        private final int ordering;
        private final String code;
        private final String defaultStyle;
        private final String sources;
        private final String name;
        private final String version;
        private final String raw;
        private final Collection<String> annotations;

        public EmojiChar(String code, String defaultStyle,
                int ordering, Collection<String> annotations,
                String sources, String version,
                String raw, String name)
        {
            this.code = code;
            this.defaultStyle = defaultStyle;
            this.ordering = ordering;
            this.annotations = annotations;
            this.sources = sources;
            this.name = name;
            this.version = version;
            this.raw = raw;
        }

        @Override
        public int compareTo(EmojiChar o)
        {
            Integer siz = annotations.size();
            int r = siz.compareTo(o.annotations.size());

            if (r == 0)
            {
                r = Integer.valueOf(ordering).compareTo(o.ordering);
            }

            return r;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;

            EmojiChar other = (EmojiChar) obj;
            return Objects.equal(ordering, other.ordering)
                    && Objects.equal(code, other.code);
        }

        public Collection<String> getAnnotations()
        {
            return annotations;
        }

        /**
         * @return the Unicode code point
         */
        public String getCode()
        {
            return code;
        }

        /**
         * The proposed default presentation style for each character. Separate
         * rows show the presentation with and without variation selectors,
         * where applicable. Flags are shown with images.
         * 
         * @return the default presentation style for this character
         */
        public String getDefaultStyle()
        {
            return defaultStyle;
        }

        /**
         * @return the name of this character
         */
        public String getName()
        {
            return name;
        }

        /**
         * Draft ordering of emoji characters that groups like characters
         * together. Unlike the labels or annotations, each character only
         * occurs once.
         * 
         * @return the unique ordering identifier of this character
         */
        public Integer getOrdering()
        {
            return ordering;
        }

        /**
         * @return a raw string representation
         */
        public String getRaw()
        {
            return raw;
        }

        /**
         * A view of when different emoji were added to Unicode, and the
         * sources. The sources indicate where a Unicode character corresponds
         * to a character in the source. In many cases, the character had
         * already been encoded well before the source was considered for other
         * characters.
         * 
         * @return the concatenated source letter codes
         * @see EmojiSource
         */
        public String getSources()
        {
            return sources;
        }

        /**
         * A view of when different emoji were added to Unicode, by Unicode
         * version.
         * 
         * @return the version of this character
         */
        public String getVersion()
        {
            return version;
        }

        public boolean hasAnnotation(String annotation)
        {
            return this.annotations.contains(annotation);
        }

        public boolean hasAnnotations(Collection<String> annotations)
        {
            return this.annotations.containsAll(annotations);
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(ordering, code);
        }

        @Override
        public String toString()
        {
            return Objects.toStringHelper(this)
                    .add("code", code)
                    .add("defaultStyle", defaultStyle)
                    .add("ordering", ordering)
                    .add("annotations", annotations)
                    .add("sources", sources)
                    .add("name", name)
                    .add("version", version)
                    .add("raw", raw)
                    .toString();
        }

    }

    public enum EmojiSource
    {
        J("JCarrier", "Japanese telephone carriers"),
        A("ARIB", ""),
        Z("ZDings", "Zapf Dingbats"),
        W("WDings", "Wingdings and Webdings"),
        X("Others", "");

        private String name;
        private String desc;

        private EmojiSource(String name, String desc)
        {
            this.name = name;
            this.desc = desc;
        }

        public String getDesc()
        {
            return desc;
        }

        public String getName()
        {
            return name;
        }
    }

    private static class LazyHolder
    {
        private static final Emoji INSTANCE = new Emoji();
    }

    private static final String DB_FILE = "/db/emoji-data.txt";
    private static final Charset UTF8 = Charset.forName("UTF8");

    private static final List<EmojiChar> EMOJI_CHARS = new ArrayList<EmojiChar>();
    private static final Map<String, Integer> RAW_INDEX = new HashMap<String, Integer>();
    private static final Map<String, Collection<Integer>> ANNOTATIONS_INDEX = new HashMap<String, Collection<Integer>>();

    /**
     * Transforms a list of Unicode code points, as hex strings, into a proper
     * encoded string.
     * 
     * @param points
     *            The list of Unicode code point as a hex strings
     * @return the concatenation of the proper encoded string for the given
     *         points
     * @see Emoji#codePointToString(String)
     */
    public static String codePointsToString(String... points)
    {
        StringBuilder ret = new StringBuilder();

        for (String hexPoint : points)
        {
            ret.append(codePointToString(hexPoint));
        }

        return ret.toString();
    }

    /**
     * Transforms an Unicode code point, given as a hex string, into a proper
     * encoded string. Supplementary code points are encoded in UTF-16 as
     * required by Java.
     * 
     * @param point
     *            The Unicode code point as a hex string
     * @return the proper encoded string reification of a given point
     */
    public static String codePointToString(String point)
    {
        String ret;

        if (Strings.isNullOrEmpty(point))
        {
            return point;
        }

        int unicodeScalar = Integer.parseInt(point, 16);

        if (Character.isSupplementaryCodePoint(unicodeScalar))
        {
            ret = String.valueOf(Character.toChars(unicodeScalar));
        } else
        {
            ret = String.valueOf((char) unicodeScalar);
        }

        return ret;
    }

    /**
     * Finds emoji characters for the given annotations.
     * 
     * @param annotations
     *            The list of annotations separated by spaces
     * @return or an empty list if there is no match
     */
    public static List<EmojiChar> findByAnnotations(String annotations)
    {
        return getInstance()._findByAnnotations(annotations);
    }

    /**
     * Finds an emoji character by Unicode code point.
     * 
     * @param code
     *            the Unicode code point
     * @return the corresponding emoji character or null if not found
     */
    public static EmojiChar findByCodePoint(String code)
    {
        return getInstance()._findByCodePoint(code);
    }

    /**
     * Finds a single emoji character for the given annotations.
     * 
     * @param annotations
     *            The list of annotations separated by spaces
     * @return a matching emoji character or null if none found
     */
    public static EmojiChar singleByAnnotations(String annotations)
    {
        return getInstance()._singleByAnnotations(annotations);
    }

    private static Emoji getInstance()
    {
        return LazyHolder.INSTANCE;
    }

    private Emoji()
    {
        try
        {
            loadDatabase();
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private List<EmojiChar> _findByAnnotations(String annotations)
    {
        Collection<EmojiChar> found = new HashSet<EmojiChar>();
        Collection<String> parts = Arrays.asList(annotations.split("\\s+"));

        for (String annotation : parts)
        {
            if (ANNOTATIONS_INDEX.containsKey(annotation))
            {
                Collection<Integer> pointers = ANNOTATIONS_INDEX.get(annotation);
                for (Integer ordering : pointers)
                {
                    EmojiChar ec = EMOJI_CHARS.get(ordering);
                    if (ec.hasAnnotations(parts))
                    {
                        found.add(ec);
                    }
                }
            }
        }

        // Needed for template signatures
        List<EmojiChar> retList;
        if (found.isEmpty())
        {
            retList = Collections.emptyList();
        }
        else
        {
            retList = asSortedList(found);
        }

        return retList;
    }

    private EmojiChar _findByCodePoint(String code)
    {
        Integer ordering = RAW_INDEX.get(code);
        EmojiChar emojiChar = ordering == null ? null : EMOJI_CHARS.get(ordering);

        return emojiChar;
    }

    private EmojiChar _singleByAnnotations(String annotations)
    {
        List<EmojiChar> found = findByAnnotations(annotations);
        return found.isEmpty() ? null : found.iterator().next();
    }

    private <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c)
    {
        List<T> list = new ArrayList<T>(c);
        Collections.sort(list);
        return list;
    }

    private String extractCode(String str)
    {
        return str.replaceAll("U\\+", "");
    }

    private List<String> extractList(String list)
    {
        String[] tmp = list.split(",");
        List<String> clean = new ArrayList<String>();

        for (String s : tmp)
        {
            clean.add(s.trim());
        }

        return clean;
    }

    private void index(EmojiChar ec)
    {
        int ordering = ec.getOrdering();

        // Index by code points (raw characters)
        RAW_INDEX.put(ec.getRaw(), ordering);

        // Index by annotations
        for (String annotation : ec.getAnnotations())
        {
            Collection<Integer> pointers;
            if (ANNOTATIONS_INDEX.containsKey(annotation))
            {
                pointers = ANNOTATIONS_INDEX.get(annotation);
            } else
            {
                pointers = new HashSet<Integer>();
                ANNOTATIONS_INDEX.put(annotation, pointers);
            }
            pointers.add(ordering);
        }
    }

    private void loadDatabase() throws IOException
    {
        InputStream resource = Emoji.class.getResourceAsStream(DB_FILE);
        Preconditions.checkNotNull(resource, "%s not found in the classpath!", DB_FILE);
        loadDatabase(resource);
    }

    private void loadDatabase(InputStream in) throws IOException
    {

        InputStreamReader isr = new InputStreamReader(in, UTF8);
        BufferedReader br = new BufferedReader(isr);

        for (String line; (line = br.readLine()) != null;)
        {
            if (line.indexOf('#') == 0)
            {
                continue;
            }

            String[] row = line.split(";");
            String code = extractCode(trim(row[0]));
            String defaultStyle = trim(row[1]);
            int ordering = Integer.parseInt(trim(row[2]));
            List<String> annotations = extractList(trim(row[3]));
            String[] rest = parseRemaining(trim(row[4]));
            String sources = rest[0];
            String version = rest[1];
            String raw = rest[2];
            String name = rest[3];

            EmojiChar ec = new EmojiChar(code, defaultStyle,
                    ordering, annotations, sources,
                    version, raw, name);

            EMOJI_CHARS.add(ec);

            index(ec);

        }

        br.close();

    }

    private String[] parseRemaining(String in)
    {
        String[] res = new String[4];
        String[] fp = in.split("#", 2);

        // sources
        res[0] = trim(fp[0]);

        // v.g. 'V1.1 (☻) black smiling face'
        Pattern expr = Pattern.compile("(V\\d+\\.\\d+)\\s\\((.+)\\)\\s(.+)");
        Matcher matcher = expr.matcher(trim(fp[1]));

        if (matcher.matches())
        {
            // version
            res[1] = matcher.group(1);
            // char
            res[2] = matcher.group(2);
            // name
            res[3] = matcher.group(3);
        } else
        {
            throw new RuntimeException("Error loading: " + in);
        }

        return res;
    }

    private String trim(String str)
    {
        return str.replaceAll("\\s+", " ").trim();
    }
}
