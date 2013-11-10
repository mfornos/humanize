package humanize.faces.renderkit.html;

public interface HTML
{
    // universal attributes
    String DIR_ATTR = "dir";
    String LANG_ATTR = "lang";
    String STYLE_ATTR = "style";
    String TITLE_ATTR = "title";
    // "class" cannot be used as property name
    String STYLE_CLASS_ATTR = "styleClass";

    // Common event handler attributes
    String ONCLICK_ATTR = "onclick";
    String ONDBLCLICK_ATTR = "ondblclick";
    String ONMOUSEDOWN_ATTR = "onmousedown";
    String ONMOUSEUP_ATTR = "onmouseup";
    String ONMOUSEOVER_ATTR = "onmouseover";
    String ONMOUSEMOVE_ATTR = "onmousemove";
    String ONMOUSEOUT_ATTR = "onmouseout";
    String ONKEYPRESS_ATTR = "onkeypress";
    String ONKEYDOWN_ATTR = "onkeydown";
    String ONKEYUP_ATTR = "onkeyup";
    String ONFOCUS_ATTR = "onfocus";
    String ONBLUR_ATTR = "onblur";

    String[] COMMON_PASSTROUGH_ATTRIBUTES =
            new String[] { DIR_ATTR, LANG_ATTR, STYLE_ATTR, STYLE_CLASS_ATTR, ONCLICK_ATTR, ONDBLCLICK_ATTR,
                    ONMOUSEDOWN_ATTR, ONMOUSEUP_ATTR, ONMOUSEOVER_ATTR, ONMOUSEMOVE_ATTR, ONMOUSEOUT_ATTR,
                    ONKEYPRESS_ATTR, ONKEYDOWN_ATTR, ONKEYUP_ATTR, ONFOCUS_ATTR, ONBLUR_ATTR };

    String SPAN_ELEM = "span";
    String ID_ATTR = "id";
    String CLASS_ATTR = "class";
}
