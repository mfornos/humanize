package humanize.text;

import humanize.spi.MessageFormat;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestMaskFormat
{

    @Test
    public void instanceTest() throws ParseException
    {

        MaskFormat mf = new MaskFormat("_# __ _____#-_");
        Assert.assertEquals(mf.getPlaceholder(), '_');
        Assert.assertEquals(mf.getMask(), "_# __ _____#-_");

        Assert.assertEquals(mf.format("A/5881850 1"), "A 58 81850-1");

        mf.setMask("-- -- -");
        mf.setPlaceholder('-');
        Assert.assertEquals(mf.format("10010"), "10 01 0");
        StringBuffer sb = new StringBuffer();
        mf.format("10010", sb, new FieldPosition(0));
        Assert.assertEquals(sb.toString(), "10 01 0");
        sb = new StringBuffer();
        Assert.assertEquals(mf.format("10010", sb, null).toString(), "10 01 0");
        Assert.assertEquals(mf.format(null, sb, null), null);

        Assert.assertEquals(mf.parseObject(""), "");
        Assert.assertEquals(mf.parseObject(null), null);
        Assert.assertEquals(mf.parseObject("10 01 0"), "10010");
        Assert.assertEquals(mf.parseObject("10 01 0", null), "10010");

        Assert.assertEquals(mf.parse("99 99 A"), "9999A");
        try
        {
            mf.parse("90890");
            Assert.fail();
        } catch (ParseException e)
        {
            //
        }

        Format fmt = MaskFormat.factory().getFormat("", "____ ____ __", null);
        Assert.assertEquals(fmt.parseObject("1234 5678 90"), "1234567890");

    }

    @Test
    public void maskFormatTest()
    {

        Assert.assertEquals(MaskFormat.format("helo", "123"), "helo");
        Assert.assertEquals(MaskFormat.format("", "hi"), "hi");
        Assert.assertEquals(MaskFormat.format(null, "hi"), "hi");
        Assert.assertEquals(MaskFormat.format("", null), null);
        Assert.assertEquals(MaskFormat.format(null, null), null);
        Assert.assertEquals(MaskFormat.format("____ ____ __", "1234567890"), "1234 5678 90");
        Assert.assertEquals(MaskFormat.format("____#/__#/__", "2008-11-28"), "2008/11/28");
        Assert.assertEquals(MaskFormat.format("__\\___", "1010"), "10_10");
        Assert.assertEquals(MaskFormat.format("$$_$$", "1010", '$'), "10_10");
        Assert.assertEquals(MaskFormat.format("1bla_bla__bla bla bla 12", "010"), "1bla0bla10bla bla bla 12");

    }

    @Test
    public void maskParseTest() throws ParseException
    {

        Assert.assertEquals(MaskFormat.parse("", "hi"), "hi");
        Assert.assertEquals(MaskFormat.parse((String) null, (String) null), null);
        Assert.assertEquals(MaskFormat.parse("____ ____ __", "1234 5678 90"), "1234567890");
        Assert.assertEquals(MaskFormat.parse("____-__-__", "2008-11-28"), "20081128");
        Assert.assertEquals(MaskFormat.parse("__\\___", "10_10"), "1010");
        Assert.assertEquals(MaskFormat.parse("$$_$$", "10_10", '$'), "1010");
        Assert.assertEquals(MaskFormat.parse("1bla_bla__bla bla bla 12", "1bla0bla10bla bla bla 12"), "010");

        try
        {
            MaskFormat.parse("helo", "123");
            Assert.fail();
        } catch (ParseException ex)
        {
            //
        }

    }

    @Test
    public void messageFmtTest()
    {

        MessageFormat msg = new MessageFormat("Hello {0}");
        msg.setFormat(0, new MaskFormat("__ ____|_"));
        Assert.assertEquals(msg.render("001100Z"), "Hello 00 1100|Z");

        java.text.MessageFormat smsg = new java.text.MessageFormat("Hello {0}");
        smsg.setFormat(0, new MaskFormat("__ ____|_"));
        Assert.assertEquals(smsg.format(new String[] { "001100Z" }), "Hello 00 1100|Z");

    }

}
