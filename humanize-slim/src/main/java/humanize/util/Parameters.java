package humanize.util;

import static humanize.util.Constants.DEFAULT_SLUG_SEPARATOR;
import humanize.time.TimeMillis;

import com.google.common.base.Preconditions;

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
     * Pace format parameters.
     * 
     */
    public final static class PaceParameters
    {
        /**
         * Builder method.
         * 
         * @param one
         *            Format for a single element
         * @return a new pace parameters instance
         */
        public static PaceParameters begin(String one)
        {
            PaceParameters p = new PaceParameters();
            return p.one(one);
        }

        /**
         * Pluralization formats
         */
        public PluralizeParams plural;

        /**
         * The interval of the pace in milliseconds.
         * 
         * @see TimeMillis
         */
        public long interval;

        private PaceParameters()
        {
            //
        }

        public void checkArguments()
        {
            Preconditions.checkArgument(plural != null, "Plural parameters are required");
        }

        public PaceParameters exts(Object... exts)
        {
            this.plural.exts(exts);
            return this;
        }

        public PaceParameters interval(long interval)
        {
            this.interval = interval;
            return this;
        }

        public PaceParameters interval(TimeMillis interval)
        {
            this.interval = interval.millis();
            return this;
        }

        public PaceParameters many(String many)
        {
            this.plural.many(many);
            return this;
        }

        public PaceParameters none(String none)
        {
            this.plural.none(none);
            return this;
        }

        public PaceParameters one(String one)
        {
            this.plural = PluralizeParams.begin(one);
            return this;
        }

        public PaceParameters plural(PluralizeParams plural)
        {
            this.plural = plural;
            return this;
        }
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
