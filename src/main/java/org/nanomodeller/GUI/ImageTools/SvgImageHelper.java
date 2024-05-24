package org.nanomodeller.GUI.ImageTools;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.commons.io.FileUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

public class SvgImageHelper {


    public static BufferedImage imageFromSVGFile(String path) {
        File svgFile = new File(path);
        final BufferedImage[] imagePointer = new BufferedImage[1];


        String css = "svg {" +
                "shape-rendering: geometricPrecision;" +
                "text-rendering:  geometricPrecision;" +
                "color-rendering: optimizeQuality;" +
                "image-rendering: optimizeQuality;" +
                "}";

        try {
            File cssFile = File.createTempFile("batik-default-override-", ".css");
            FileUtils.writeStringToFile(cssFile, css);

            TranscodingHints transcoderHints = new TranscodingHints();
            transcoderHints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
            transcoderHints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION,
                    SVGDOMImplementation.getDOMImplementation());
            transcoderHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI,
                    SVGConstants.SVG_NAMESPACE_URI);
            transcoderHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
            transcoderHints.put(ImageTranscoder.KEY_USER_STYLESHEET_URI, cssFile.toURI().toString());

            TranscoderInput input = new TranscoderInput(new FileInputStream(svgFile));

            ImageTranscoder t = new ImageTranscoder() {

                @Override
                public BufferedImage createImage(int w, int h) {
                    return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                }

                @Override
                public void writeImage(BufferedImage image, TranscoderOutput out) {
                    imagePointer[0] = image;
                }
            };
            t.setTranscodingHints(transcoderHints);
            t.transcode(input, null);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return imagePointer[0];
    }

}
