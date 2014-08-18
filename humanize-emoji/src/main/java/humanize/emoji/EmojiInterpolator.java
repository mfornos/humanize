package humanize.emoji;

import static humanize.text.util.InterpolationHelper.interpolate;
import humanize.spi.MessageFormat;
import humanize.text.util.Replacer;

import java.util.regex.Pattern;

/**
 * <p>
 * Provides easy text interpolation of Emoji symbols.
 * </p>
 * <h5>Examples:</h5>
 * 
 * <pre>
 * EmojiInterpolator.interpolateAlias(&quot;&lt;img src=\&quot;imgs/{0}.png\&quot; title=\&quot;{0}\&quot; /&gt;&quot;, &quot;Hi :sparkles:!&quot;);
 * // == &quot;Hi &lt;img src=\&quot;imgs/sparkles.png\&quot; title=\&quot;sparkles\&quot; /&gt;!&quot;
 * 
 * EmojiInterpolator.interpolateChars(&quot;&lt;img src=\&quot;imgs/{0}.png\&quot; /&gt;&quot;,
 *         &quot;Lorem ipsum \uE025 dolorem\uE30D and dolorem sit amet&quot;);
 * // ==
 * // &quot;Lorem ipsum &lt;img src=\&quot;imgs/uE025.png\&quot; /&gt; dolorem&lt;img src=\&quot;imgs/uE30D.png\&quot; /&gt; and dolorem sit amet&quot;
 * 
 * </pre>
 * 
 * This class is *deprecated* in favor of {@link Emoji}
 * 
 * @see Emoji
 * 
 */
@Deprecated
public final class EmojiInterpolator
{

    static class EmojiAliasInterpolator implements Replacer
    {

        private final MessageFormat msgFormat;

        public EmojiAliasInterpolator(String pattern)
        {

            this.msgFormat = new MessageFormat(pattern);

        }

        @Override
        public String replace(String text)
        {

            return msgFormat.render(text);

        }
    }

    static class EmojiCharInterpolator implements Replacer
    {

        private final MessageFormat msgFormat;

        public EmojiCharInterpolator(String pattern)
        {

            this.msgFormat = new MessageFormat(pattern);

        }

        @Override
        public String replace(String in)
        {

            StringBuilder uc = new StringBuilder();
            for (char c : in.toCharArray())
            {
                uc.append('u');
                uc.append(Integer.toHexString(c).toUpperCase());
            }
            return msgFormat.render(uc.toString());

        }
    }

    public static final Pattern EMOJI_ALIAS = Pattern.compile(":("
            + "bowtie|smile|blush|smiley|relaxed|smirk|heart_eyes|kissing_heart|kissing_face"
            + "|flushed|relieved|satisfied|grin|wink|wink2|tongue|unamused|sweat|pensive|disappointed"
            + "|confounded|fearful|cold_sweat|persevere|cry|sob|joy|astonished|scream|angry|rage|sleepy"
            + "|mask|imp|alien|yellow_heart|blue_heart|purple_heart|heart|green_heart|broken_heart"
            + "|heartbeat|heartpulse|cupid|sparkles|star|star2|anger|exclamation|question|grey_exclamation"
            + "|grey_question|zzz|dash|sweat_drops|notes|musical_note|fire|hankey|poop|shit|\\+1|thumbsup"
            + "|-1|thumbsdown|ok_hand|punch|fist|v|wave|hand|open_hands|point_up|point_down|point_left"
            + "|point_right|raised_hands|pray|point_up_2|clap|muscle|metal|walking|runner|couple|dancer"
            + "|dancers|ok_woman|no_good|information_desk_person|bow|couplekiss|couple_with_heart|massage"
            + "|haircut|nail_care|boy|girl|woman|man|baby|older_woman|older_man|person_with_blond_hair"
            + "|man_with_gua_pi_mao|man_with_turban|construction_worker|cop|angel|princess|guardsman|skull"
            + "|feet|lips|kiss|ear|eyes|nose|feelsgood|finnadie|goberserk|godmode|hurtrealbad|rage1|rage2"
            + "|rage3|rage4|suspect|trollface|sunny|umbrella|cloud|snowman|moon|zap|cyclone|ocean|cat|dog"
            + "|mouse|hamster|rabbit|wolf|frog|tiger|koala|bear|pig|cow|boar|monkey_face|monkey|horse"
            + "|racehorse|camel|sheep|elephant|snake|bird|baby_chick|chicken|penguin|bug|octopus"
            + "|tropical_fish|fish|whale|dolphin|bouquet|cherry_blossom|tulip|four_leaf_clover|rose"
            + "|sunflower|hibiscus|maple_leaf|leaves|fallen_leaf|palm_tree|cactus|ear_of_rice|shell"
            + "|octocat|squirrel|bamboo|gift_heart|dolls|school_satchel|mortar_board|flags|fireworks"
            + "|sparkler|wind_chime|rice_scene|jack_o_lantern|ghost|santa|christmas_tree|gift|bell"
            + "|tada|balloon|cd|dvd|camera|movie_camera|computer|tv|iphone|fax|phone|telephone|minidisc"
            + "|vhs|speaker|loudspeaker|mega|radio|satellite|loop|mag|unlock|lock|key|scissors|hammer"
            + "|bulb|calling|email|mailbox|postbox|bath|toilet|seat|moneybag|trident|smoking|bomb|gun"
            + "|pill|syringe|football|basketball|soccer|baseball|tennis|golf|8ball|swimmer|surfer|ski"
            + "|spades|hearts|clubs|diamonds|gem|ring|trophy|space_invader|dart|mahjong|clapper|memo"
            + "|pencil|book|art|microphone|headphones|trumpet|saxophone|guitar|part_alternation_mark"
            + "|shoe|sandal|high_heel|lipstick|boot|shirt|tshirt|necktie|dress|kimono|bikini|ribbon"
            + "|tophat|crown|womans_hat|closed_umbrella|briefcase|handbag|beer|beers|cocktail|sake"
            + "|fork_and_knife|hamburger|fries|spaghetti|curry|bento|sushi|rice_ball|rice_cracker|rice"
            + "|ramen|stew|bread|egg|oden|dango|icecream|shaved_ice|birthday|cake|apple|tangerine"
            + "|watermelon|strawberry|eggplant|tomato|coffee|tea|109|house|school|office|post_office"
            + "|hospital|bank|convenience_store|love_hotel|hotel|wedding|church|department_store"
            + "|city_sunrise|city_sunset|japanese_castle|european_castle|tent|factory|tokyo_tower"
            + "|mount_fuji|sunrise_over_mountains|sunrise|stars|statue_of_liberty|rainbow|ferris_wheel"
            + "|fountain|roller_coaster|ship|speedboat|boat|sailboat|airplane|rocket|bike|blue_car|car"
            + "|red_car|taxi|bus|police_car|fire_engine|ambulance|truck|train|station|bullettrain_front"
            + "|bullettrain_side|ticket|fuelpump|traffic_light|warning|construction|beginner|atm"
            + "|slot_machine|busstop|barber|hotsprings|checkered_flag|crossed_flags|jp|kr|cn|us|fr|es|it"
            + "|ru|gb|de|one|two|three|four|five|six|seven|eight|nine|zero|hash|arrow_backward|arrow_down"
            + "|arrow_forward|arrow_left|arrow_lower_left|arrow_lower_right|arrow_right|arrow_up"
            + "|arrow_upper_left|arrow_upper_right|rewind|fast_forward|ok|new|top|up|cool|cinema|koko"
            + "|signal_strength|u5272|u55b6|u6307|u6708|u6709|u6e80|u7121|u7533|u7a7a|sa|restroom|mens"
            + "|womens|baby_symbol|no_smoking|parking|wheelchair|metro|wc|secret|congratulations"
            + "|ideograph_advantage|underage|id|eight_spoked_asterisk|eight_pointed_black_star"
            + "|heart_decoration|vs|vibration_mode|mobile_phone_off|chart|currency_exchange|aries"
            + "|taurus|gemini|cancer|leo|virgo|libra|scorpius|sagittarius|capricorn|aquarius|pisces"
            + "|ophiuchus|six_pointed_star|a|b|ab|o2|red_circle|black_square|white_square|clock1|clock10"
            + "|clock11|clock12|clock2|clock3|clock4|clock5|clock6|clock7|clock8|clock9|o|x|copyright"
            + "|registered|tm|shipit" + "):");

    // TODO range regular expression
    public static final Pattern EMOJI_UCHARS = Pattern.compile("(" + "\uE415|" + "\uE056|" + "\uE057|" + "\uE414|"
            + "\uE405|" + "\uE106|" + "\uE418|" + "\uE417|" + "\uE40D|" + "\uE40A|" + "\uE404|" + "\uE105|" + "\uE409|"
            + "\uE40E|" + "\uE402|" + "\uE108|" + "\uE403|" + "\uE058|" + "\uE407|" + "\uE401|" + "\uE40F|" + "\uE40B|"
            + "\uE406|" + "\uE413|" + "\uE411|" + "\uE412|" + "\uE410|" + "\uE107|" + "\uE059|" + "\uE416|" + "\uE408|"
            + "\uE40C|" + "\uE11A|" + "\uE10C|" + "\uE32C|" + "\uE32A|" + "\uE32D|" + "\uE328|" + "\uE32B|" + "\uE022|"
            + "\uE023|" + "\uE327|" + "\uE329|" + "\uE32E|" + "\uE32F|" + "\uE335|" + "\uE334|" + "\uE021|" + "\uE337|"
            + "\uE020|" + "\uE336|" + "\uE13C|" + "\uE330|" + "\uE331|" + "\uE326|" + "\uE03E|" + "\uE11D|" + "\uE05A|"
            + "\uE00E|" + "\uE421|" + "\uE420|" + "\uE00D|" + "\uE010|" + "\uE011|" + "\uE41E|" + "\uE012|" + "\uE422|"
            + "\uE22E|" + "\uE22F|" + "\uE231|" + "\uE230|" + "\uE427|" + "\uE41D|" + "\uE00F|" + "\uE41F|" + "\uE14C|"
            + "\uE201|" + "\uE115|" + "\uE428|" + "\uE51F|" + "\uE429|" + "\uE424|" + "\uE423|" + "\uE253|" + "\uE426|"
            + "\uE111|" + "\uE425|" + "\uE31E|" + "\uE31F|" + "\uE31D|" + "\uE001|" + "\uE002|" + "\uE005|" + "\uE004|"
            + "\uE51A|" + "\uE519|" + "\uE518|" + "\uE515|" + "\uE516|" + "\uE517|" + "\uE51B|" + "\uE152|" + "\uE04E|"
            + "\uE51C|" + "\uE51E|" + "\uE11C|" + "\uE536|" + "\uE003|" + "\uE41C|" + "\uE41B|" + "\uE419|" + "\uE41A|"
            + "\uE04A|" + "\uE04B|" + "\uE049|" + "\uE048|" + "\uE04C|" + "\uE13D|" + "\uE443|" + "\uE43E|" + "\uE04F|"
            + "\uE052|" + "\uE053|" + "\uE524|" + "\uE52C|" + "\uE52A|" + "\uE531|" + "\uE050|" + "\uE527|" + "\uE051|"
            + "\uE10B|" + "\uE52B|" + "\uE52F|" + "\uE528|" + "\uE01A|" + "\uE134|" + "\uE530|" + "\uE529|" + "\uE526|"
            + "\uE52D|" + "\uE521|" + "\uE523|" + "\uE52E|" + "\uE055|" + "\uE525|" + "\uE10A|" + "\uE109|" + "\uE522|"
            + "\uE019|" + "\uE054|" + "\uE520|" + "\uE306|" + "\uE030|" + "\uE304|" + "\uE110|" + "\uE032|" + "\uE305|"
            + "\uE303|" + "\uE118|" + "\uE447|" + "\uE119|" + "\uE307|" + "\uE308|" + "\uE444|" + "\uE441|" + "\uE036|"
            + "\uE157|" + "\uE038|" + "\uE153|" + "\uE155|" + "\uE14D|" + "\uE156|" + "\uE501|" + "\uE158|" + "\uE43D|"
            + "\uE037|" + "\uE504|" + "\uE44A|" + "\uE146|" + "\uE50A|" + "\uE505|" + "\uE506|" + "\uE122|" + "\uE508|"
            + "\uE509|" + "\uE03B|" + "\uE04D|" + "\uE449|" + "\uE44B|" + "\uE51D|" + "\uE44C|" + "\uE124|" + "\uE121|"
            + "\uE433|" + "\uE202|" + "\uE135|" + "\uE01C|" + "\uE01D|" + "\uE10D|" + "\uE136|" + "\uE42E|" + "\uE01B|"
            + "\uE15A|" + "\uE159|" + "\uE432|" + "\uE430|" + "\uE431|" + "\uE42F|" + "\uE01E|" + "\uE039|" + "\uE435|"
            + "\uE01F|" + "\uE125|" + "\uE03A|" + "\uE14E|" + "\uE252|" + "\uE137|" + "\uE209|" + "\uE154|" + "\uE133|"
            + "\uE150|" + "\uE320|" + "\uE123|" + "\uE132|" + "\uE143|" + "\uE50B|" + "\uE514|" + "\uE513|" + "\uE50C|"
            + "\uE50D|" + "\uE511|" + "\uE50F|" + "\uE512|" + "\uE510|" + "\uE50E|" + "\uE436|" + "\uE437|" + "\uE438|"
            + "\uE43A|" + "\uE439|" + "\uE43B|" + "\uE117|" + "\uE440|" + "\uE442|" + "\uE446|" + "\uE445|" + "\uE11B|"
            + "\uE448|" + "\uE033|" + "\uE112|" + "\uE325|" + "\uE312|" + "\uE310|" + "\uE126|" + "\uE127|" + "\uE008|"
            + "\uE03D|" + "\uE00C|" + "\uE12A|" + "\uE00A|" + "\uE00B|" + "\uE009|" + "\uE316|" + "\uE129|" + "\uE141|"
            + "\uE142|" + "\uE317|" + "\uE128|" + "\uE14B|" + "\uE211|" + "\uE114|" + "\uE145|" + "\uE144|" + "\uE03F|"
            + "\uE313|" + "\uE116|" + "\uE10F|" + "\uE104|" + "\uE103|" + "\uE101|" + "\uE102|" + "\uE13F|" + "\uE140|"
            + "\uE11F|" + "\uE12F|" + "\uE031|" + "\uE30E|" + "\uE311|" + "\uE113|" + "\uE30F|" + "\uE13B|" + "\uE42B|"
            + "\uE42A|" + "\uE018|" + "\uE016|" + "\uE015|" + "\uE014|" + "\uE42C|" + "\uE42D|" + "\uE017|" + "\uE013|"
            + "\uE20E|" + "\uE20C|" + "\uE20F|" + "\uE20D|" + "\uE131|" + "\uE12B|" + "\uE130|" + "\uE12D|" + "\uE324|"
            + "\uE301|" + "\uE148|" + "\uE502|" + "\uE03C|" + "\uE30A|" + "\uE042|" + "\uE040|" + "\uE041|" + "\uE12C|"
            + "\uE007|" + "\uE31A|" + "\uE13E|" + "\uE31B|" + "\uE006|" + "\uE302|" + "\uE319|" + "\uE321|" + "\uE322|"
            + "\uE314|" + "\uE503|" + "\uE10E|" + "\uE318|" + "\uE43C|" + "\uE11E|" + "\uE323|" + "\uE31C|" + "\uE034|"
            + "\uE035|" + "\uE045|" + "\uE338|" + "\uE047|" + "\uE30C|" + "\uE044|" + "\uE30B|" + "\uE043|" + "\uE120|"
            + "\uE33B|" + "\uE33F|" + "\uE341|" + "\uE34C|" + "\uE344|" + "\uE342|" + "\uE33D|" + "\uE33E|" + "\uE340|"
            + "\uE34D|" + "\uE339|" + "\uE147|" + "\uE343|" + "\uE33C|" + "\uE33A|" + "\uE43F|" + "\uE34B|" + "\uE046|"
            + "\uE345|" + "\uE346|" + "\uE348|" + "\uE347|" + "\uE34A|" + "\uE349|" + "\uE21C|" + "\uE21D|" + "\uE21E|"
            + "\uE21F|" + "\uE220|" + "\uE221|" + "\uE222|" + "\uE223|" + "\uE224|" + "\uE225|" + "\uE210|" + "\uE232|"
            + "\uE233|" + "\uE235|" + "\uE234|" + "\uE236|" + "\uE237|" + "\uE238|" + "\uE239|" + "\uE23B|" + "\uE23A|"
            + "\uE23D|" + "\uE23C|" + "\uE24D|" + "\uE212|" + "\uE24C|" + "\uE213|" + "\uE214|" + "\uE507|" + "\uE203|"
            + "\uE20B|" + "\uE22A|" + "\uE22B|" + "\uE226|" + "\uE227|" + "\uE22C|" + "\uE22D|" + "\uE215|" + "\uE216|"
            + "\uE217|" + "\uE218|" + "\uE228|" + "\uE151|" + "\uE138|" + "\uE139|" + "\uE13A|" + "\uE208|" + "\uE14F|"
            + "\uE20A|" + "\uE434|" + "\uE309|" + "\uE315|" + "\uE30D|" + "\uE207|" + "\uE229|" + "\uE206|" + "\uE205|"
            + "\uE204|" + "\uE12E|" + "\uE250|" + "\uE251|" + "\uE14A|" + "\uE149|" + "\uE23F|" + "\uE240|" + "\uE241|"
            + "\uE242|" + "\uE243|" + "\uE244|" + "\uE245|" + "\uE246|" + "\uE247|" + "\uE248|" + "\uE249|" + "\uE24A|"
            + "\uE24B|" + "\uE23E|" + "\uE532|" + "\uE533|" + "\uE534|" + "\uE535|" + "\uE21A|" + "\uE219|" + "\uE21B|"
            + "\uE02F|" + "\uE024|" + "\uE025|" + "\uE026|" + "\uE027|" + "\uE028|" + "\uE029|" + "\uE02A|" + "\uE02B|"
            + "\uE02C|" + "\uE02D|" + "\uE02E|" + "\uE332|" + "\uE333|" + "\uE24E|" + "\uE24F|" + "\uE537)");

    public static String interpolateAlias(String pattern, String text)
    {

        return interpolate(text, EmojiInterpolator.EMOJI_ALIAS, new EmojiAliasInterpolator(pattern));

    }

    public static String interpolateChars(String pattern, String text)
    {

        return interpolate(text, EmojiInterpolator.EMOJI_UCHARS, new EmojiCharInterpolator(pattern));

    }

    private EmojiInterpolator()
    {
        //
    }

}
