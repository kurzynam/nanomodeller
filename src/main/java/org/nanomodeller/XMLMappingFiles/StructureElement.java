package org.nanomodeller.XMLMappingFiles;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import org.nanomodeller.Calculation.CalculationItem;
import org.nanomodeller.Globals;
import org.nfunk.jep.JEP;

public class StructureElement extends XMLTemplate{

    protected String groupID;
    protected String tag;
    protected String iconSVGcode;

    @XmlElements(@XmlElement(name="tag"))
    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }

    public Boolean getBool(String key){
        return Globals.isTrue(properties.get(key));
    }
    @XmlElement(name="GID")
    public String getGroupID() {
        return groupID;
    }
    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }
    public static void fillProperties(JEP parser, StructureElement structureElement, CalculationItem item) {
        for (Object key : structureElement.getProperties().keySet()){
            String sValue = structureElement.getString(key.toString());
            parser.parseExpression(sValue);
            double val = parser.getValue();
            item.setProperty(key.toString(), val);
            if (val > 0 || "0".equals(sValue) || "0.0".equals(sValue)) {
                item.skip(key.toString());
            }
        }
    }
    public String getIconSVGcode() {
        return iconSVGcode;
    }
    public void setIconSVGcode(String iconSVGcode) {
        this.iconSVGcode = iconSVGcode;
    }

}
