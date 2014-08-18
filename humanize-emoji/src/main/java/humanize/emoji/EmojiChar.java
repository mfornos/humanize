package humanize.emoji;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Objects;

/**
 * Represents an emoji character in accordance with Unicode emoji data files.
 * 
 */
public final class EmojiChar implements Comparable<EmojiChar>, Serializable
{

    public static final int VENDOR_MAP_HEX = 0;
    public static final int VENDOR_MAP_RAW = 1;

    private static final long serialVersionUID = 697634381168152779L;

    private final int ordering;
    private final String code;
    private final String defaultStyle;
    private final String sources;
    private final String name;
    private final String version;
    private final String raw;
    private final Collection<String> annotations;
    private final HashMap<Vendor, String[]> mappings;

    public EmojiChar(String code, String defaultStyle,
            int ordering, Collection<String> annotations,
            String sources, String version,
            String raw, String name)
    {
        this.code = code;
        this.defaultStyle = defaultStyle;
        this.ordering = ordering;
        this.annotations = annotations;
        this.mappings = new HashMap<Vendor, String[]>();
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
        return Collections.unmodifiableCollection(annotations);
    }

    /**
     * @return the Unicode code point
     */
    public String getCode()
    {
        return code;
    }

    /**
     * The proposed default presentation style for each character. Separate rows
     * show the presentation with and without variation selectors, where
     * applicable. Flags are shown with images.
     * 
     * @return the default presentation style for this character
     */
    public String getDefaultStyle()
    {
        return defaultStyle;
    }

    public String[] getMapping(Vendor vendor)
    {
        return mappings.get(vendor);
    }

    public Map<Vendor, String[]> getMappings()
    {
        return Collections.unmodifiableMap(mappings);
    }

    /**
     * @return the name of this character
     */
    public String getName()
    {
        return name;
    }

    /**
     * Draft ordering of emoji characters that groups like characters together.
     * Unlike the labels or annotations, each character only occurs once.
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
     * A view of when different emoji were added to Unicode, and the sources.
     * The sources indicate where a Unicode character corresponds to a character
     * in the source. In many cases, the character had already been encoded well
     * before the source was considered for other characters.
     * 
     * @return the concatenated source letter codes
     * @see EmojiSource
     */
    public String getSources()
    {
        return sources;
    }

    /**
     * A view of when different emoji were added to Unicode, by Unicode version.
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

    public boolean hasMapping(Vendor vendor)
    {
        return mappings.containsKey(vendor);
    }

    public String mapTo(Vendor vendor)
    {
        String[] m = getMapping(vendor);
        return m == null ? null : m[VENDOR_MAP_RAW];
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("code", code)
                .add("mappings", mappings)
                .add("defaultStyle", defaultStyle)
                .add("ordering", ordering)
                .add("annotations", annotations)
                .add("sources", sources)
                .add("name", name)
                .add("version", version)
                .add("raw", raw)
                .toString();
    }

    protected void map(Vendor vendor, String code, String raw)
    {
        mappings.put(vendor, new String[] { code, raw });
    }

    public enum EmojiSource
    {
        J("JCarrier", "Japanese telephone carriers"),
        A("ARIB", ""),
        Z("ZDings", "Zapf Dingbats"),
        W("WDings", "Wingdings and Webdings"),
        X("Others", "");

        private final String name;
        private final String desc;

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

    public enum Vendor
    {
        DOCOMO("DoCoMo"),
        KDDI("KDDI"),
        SOFT_BANK("SoftBank");

        private final String name;

        private Vendor(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
    }

}
