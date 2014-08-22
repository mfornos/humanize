package humanize.emoji;

import humanize.emoji.EmojiChar.Vendor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;

/**
 * Unified Emoji for Java.
 * 
 * @see Unicode Emoji (working draft)
 *      http://www.unicode.org/reports/tr51/tr51-1d.html
 */
public final class Emoji
{

    private static final Charset UTF8 = Charset.forName("UTF8");

    private static final String DB_EMOJI_DATA = "/db/emoji-data.txt";
    private static final String DB_EMOJI_SOURCES = "/db/emoji-sources.txt";

    private static final List<EmojiChar> EMOJI_CHARS = new ArrayList<EmojiChar>();
    private static final Map<String, EmojiChar> HEX_INDEX = new HashMap<String, EmojiChar>();
    private static final Map<String, EmojiChar> RAW_INDEX = new HashMap<String, EmojiChar>();
    private static final Map<VendorKey, EmojiChar> VENDORS_INDEX = new HashMap<VendorKey, EmojiChar>();
    private static final Multimap<String, EmojiChar> ANNOTATIONS_INDEX = ArrayListMultimap.create();

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
     * Finds an emoji character by hexadecimal code.
     * 
     * @param hex
     *            the hexadecimal code
     * @return the corresponding emoji character or null if not found
     */
    public static EmojiChar findByHexCode(String hex)
    {
        return getInstance()._findByHexCode(hex.toUpperCase());
    }

    /**
     * Finds an emoji character by vendor code point.
     * 
     * @param vendor
     *            the vendor
     * @param point
     *            the raw character for the code point in the vendor space
     * @return the corresponding emoji character or null if not found
     */
    public static EmojiChar findByVendorCodePoint(Vendor vendor, String point)
    {
        Emoji emoji = Emoji.getInstance();
        return emoji._findByVendorCodePoint(vendor, point);
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
            loadData();
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
            collectAnnotations(found, parts, annotation);
        }

        return found.isEmpty() ?
                Collections.<EmojiChar> emptyList() : asSortedList(found);
    }

    private EmojiChar _findByCodePoint(String code)
    {
        return RAW_INDEX.get(code);
    }

    private EmojiChar _findByHexCode(String hex)
    {
        return HEX_INDEX.get(hex);
    }

    private EmojiChar _findByVendorCodePoint(Vendor vendor, String code)
    {
        return VENDORS_INDEX.get(new VendorKey(vendor, code));
    }

    private EmojiChar _singleByAnnotations(String annotations)
    {
        List<EmojiChar> found = _findByAnnotations(annotations);
        return found.isEmpty() ? null : found.iterator().next();
    }

    private <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c)
    {
        List<T> list = new ArrayList<T>(c);
        Collections.sort(list);
        return list;
    }

    private void collectAnnotations(Collection<EmojiChar> found,
            Collection<String> parts,
            String annotation)
    {
        if (!ANNOTATIONS_INDEX.containsKey(annotation))
            return;

        Collection<EmojiChar> echars = ANNOTATIONS_INDEX.get(annotation);
        for (EmojiChar echar : echars)
        {
            if (echar.hasAnnotations(parts))
            {
                found.add(echar);
            }
        }
    }

    private StreamLineProcessor emojiDataProcessor()
    {
        return new StreamLineProcessor()
        {
            @Override
            protected void consumeLine(String line)
            {
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
        };
    }

    private StreamLineProcessor emojiSourcesProcessor()
    {
        return new StreamLineProcessor()
        {
            @Override
            protected void consumeLine(String line)
            {
                String[] row = line.split(";");
                String unified = trim(row[0]);
                String unicode = codePointsToString(unified.split(" "));
                EmojiChar echar = _findByCodePoint(unicode);
                if (echar != null)
                {
                    map(echar, Vendor.DOCOMO, row, 1);
                    map(echar, Vendor.KDDI, row, 2);
                    map(echar, Vendor.SOFT_BANK, row, 3);
                }
            }

            private void map(EmojiChar echar, Vendor vendor, String[] row, int index)
            {
                if (row.length <= index)
                {
                    return;
                }

                String code = trim(row[index]);

                if (!Strings.isNullOrEmpty(code))
                {
                    String raw = codePointToString(code);
                    echar.map(vendor, code, raw);

                    VENDORS_INDEX.put(new VendorKey(vendor, raw), echar);
                }
            }
        };
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

    private void index(EmojiChar echar)
    {
        // Index by code points (raw characters)
        RAW_INDEX.put(echar.getRaw(), echar);

        // Index by hex code
        HEX_INDEX.put(echar.getCode(), echar);

        // Index by annotations
        for (String annotation : echar.getAnnotations())
        {
            ANNOTATIONS_INDEX.put(annotation, echar);
        }
    }

    private void load(String path, LineProcessor<Void> processor) throws IOException
    {
        InputStream in = Emoji.class.getResourceAsStream(path);
        Preconditions.checkNotNull(in, "%s not found in the classpath!", path);

        InputStreamReader isr = new InputStreamReader(in, UTF8);
        BufferedReader br = new BufferedReader(isr);

        CharStreams.readLines(br, processor);
    }

    private void loadData() throws IOException
    {
        load(DB_EMOJI_DATA, emojiDataProcessor());
        load(DB_EMOJI_SOURCES, emojiSourcesProcessor());
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
        if (Strings.isNullOrEmpty(str))
            return str;

        return str.replaceAll("\\s+", " ").trim();
    }

    private static class LazyHolder
    {
        private static final Emoji INSTANCE = new Emoji();
    }

    private abstract class StreamLineProcessor implements LineProcessor<Void>
    {

        @Override
        public Void getResult()
        {
            return null;
        }

        public boolean processLine(String line) throws IOException
        {
            if (Strings.isNullOrEmpty(line) || line.indexOf('#') == 0)
            {
                return true;
            }

            consumeLine(line);

            return true;
        }

        abstract protected void consumeLine(String line);;
    }

    private final class VendorKey
    {
        private final Vendor vendor;
        private final String code;

        public VendorKey(Vendor vendor, String code)
        {
            this.vendor = vendor;
            this.code = code;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;

            VendorKey other = (VendorKey) obj;
            return Objects.equal(vendor, other.vendor)
                    && Objects.equal(code, other.code);
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(vendor, code);
        }
    }
}
