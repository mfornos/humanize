package humanize.util;

import static humanize.util.Constants.DEFAULT_SLUG_SEPARATOR;

/**
 * Parameterization classes collection.
 * 
 */
public final class Parameters
{

    private Parameters()
    {
        //
    }

    /**
     * Parameterization for pluralize calls.
     * 
     */
    public final static class PluralizeParams
    {
        /**
         * Builder method.
         * 
         * @param one
         *            Format for a single element
         * @return a new pluralize parameters instance
         */
        public static PluralizeParams begin(String one)
        {
            PluralizeParams p = new PluralizeParams();
            return p.one(one);
        }

        /**
         * Format for a single element
         */
        public String one;

        /**
         * Format for many elements
         */
        public String many;

        /**
         * Format for no element
         */
        public String none;

        /**
         * Extended parameters for the specified formats
         */
        public Object[] exts;

        private PluralizeParams()
        {
            //
        }

        public PluralizeParams exts(Object... exts)
        {
            this.exts = exts;
            return this;
        }

        public PluralizeParams many(String many)
        {
            this.many = many;
            return this;
        }

        public PluralizeParams none(String none)
        {
            this.none = none;
            return this;
        }

        public PluralizeParams one(String one)
        {
            this.one = one;
            return this;
        }

    }

    /**
     * Parameterization for slugify calls.
     * 
     */
    public final static class SlugifyParams
    {
        /**
         * Builder method.
         * 
         * @return a new slugify parameters instance
         */
        public static SlugifyParams begin()
        {
            return new SlugifyParams();
        }

        /**
         * Separator slug
         */
        public String separator;

        /**
         * Controls the transformation to lowercase
         */
        public boolean isToLowerCase;

        private SlugifyParams()
        {
            this.separator = DEFAULT_SLUG_SEPARATOR;
            this.isToLowerCase = true;
        }

        public SlugifyParams separator(String separator)
        {
            this.separator = separator;
            return this;
        }

        public SlugifyParams toLowerCase(boolean isToLowerCase)
        {
            this.isToLowerCase = isToLowerCase;
            return this;
        }

    }
}
