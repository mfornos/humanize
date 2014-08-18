package humanize.emoji;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import humanize.emoji.EmojiChar.Vendor;

import java.util.Collection;

import org.testng.annotations.Test;

public class TestEmoji
{

    private static final String SIMPLE_CP = "\u2639";
    private static final String UTF16_COMPOUND_CP = "\uD83C\uDDEF\uD83C\uDDF5";
    private static final String COMPOUND_CP = "\u0037\u20E3";
    private static final String UTF16_CP = "\uD83D\uDD36";

    @Test
    public void testByAnnotations()
    {
        Collection<EmojiChar> faces = Emoji.findByAnnotations("face");
        assertNotNull(faces, "Faces");
        assertEquals(faces.size(), 139, "Faces");

        Collection<EmojiChar> smilingFaces = Emoji.findByAnnotations("face smile");
        assertNotNull(smilingFaces, "Smiling faces");
        assertEquals(smilingFaces.size(), 19, "Smiling faces");

        EmojiChar catFace = Emoji.singleByAnnotations("cat smile");
        assertEquals(catFace.getName(), "smiling cat face with open mouth");

        EmojiChar flag = Emoji.singleByAnnotations("japan flag");
        assertEquals(flag.getName(), "flag for Japan");
    }

    @Test
    public void testByCodePoint()
    {
        EmojiChar simple = Emoji.findByCodePoint(SIMPLE_CP);
        assertNotNull(simple, "Simple face was not found");

        EmojiChar supplementary = Emoji.findByCodePoint(UTF16_CP);
        assertNotNull(supplementary, "Orange diamond was not found");

        EmojiChar compound = Emoji.findByCodePoint(COMPOUND_CP);
        assertNotNull(compound, "7 keyboard was not found");

        EmojiChar japanFlag = Emoji.findByCodePoint(UTF16_COMPOUND_CP);
        assertNotNull(japanFlag, "Japan flag was not found");
    }

    @Test
    public void testCodePointToRaw()
    {
        assertEquals(Emoji.codePointToString("2639"), SIMPLE_CP, "Simple face");
        assertEquals(Emoji.codePointToString("1F536"), UTF16_CP, "Orange diamond");
        assertEquals(Emoji.codePointsToString("0037", "20E3"), COMPOUND_CP, "7 keyboard");
        assertEquals(Emoji.codePointsToString("1F1EF", "1F1F5"), UTF16_COMPOUND_CP, "Japan flag");

        assertEquals(Emoji.codePointToString(""), "", "Empty");
        assertNull(Emoji.codePointToString(null));

        try
        {
            Emoji.codePointToString("whatever");
            fail("Bad data");
        } catch (NumberFormatException ex)
        {
            //
        }
    }

    @Test
    public void testVendorCodePoint()
    {
        String tradeMark = "™";

        EmojiChar echar = Emoji.findByVendorCodePoint(Vendor.DOCOMO, "\uF9D7");
        assertEquals(echar.getRaw(), tradeMark, "DoCoMo");

        echar = Emoji.findByVendorCodePoint(Vendor.SOFT_BANK, "\uFBD7");
        assertEquals(echar.getRaw(), tradeMark, "Soft Bank");

        echar = Emoji.findByVendorCodePoint(Vendor.SOFT_BANK, "\uAAAB");
        assertNull(echar, "Not found");
    }

    @Test
    public void testRawCode()
    {
        EmojiChar simple = Emoji.singleByAnnotations("face frowning human");
        assertEquals(simple.getRaw(), SIMPLE_CP, "Simple face code point");

        EmojiChar supplementary = Emoji.findByCodePoint(UTF16_CP);
        assertEquals(supplementary.getRaw(), UTF16_CP, "Orange diamond code point");

        EmojiChar compound = Emoji.findByCodePoint(COMPOUND_CP);
        assertEquals(compound.getRaw(), COMPOUND_CP, "7 keyboard code point");
    }

}
