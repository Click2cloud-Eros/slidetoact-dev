package com.ncorti.slidetoact.utils;

import java.io.IOException;
import java.util.Optional;
import ohos.app.Context;
import ohos.global.configuration.DeviceCapability;
import ohos.global.resource.NotExistException;
import ohos.global.resource.RawFileEntry;
import ohos.global.resource.Resource;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.global.resource.solidxml.TypedAttribute;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;

/**
 * Resource related utils.
 */
public class Utils {

    private Utils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * get the path from id.
     *
     * @param context the context
     * @param id      the id
     * @return the path from id
     */
    public static String getPathById(Context context, int id) {
        String path = "";
        if (context == null) {
            return path;
        }
        ResourceManager manager = context.getResourceManager();
        if (manager == null) {
            return path;
        }
        try {
            path = manager.getMediaPath(id);
        } catch (IOException | NotExistException | WrongTypeException e) {
            path = "";
        }
        return path;
    }

    /**
     * get the pixel map.
     *
     * @param context the context
     * @param id      the id
     * @return the pixel map
     */
    public static Optional<PixelMap> getPixelMap(Context context, int id) {
        String path = getPathById(context, id);
        if (path == null || path.length() == 0) {
            return Optional.empty();
        }
        RawFileEntry assetManager = context.getResourceManager().getRawFileEntry(path);
        ImageSource.SourceOptions options = new ImageSource.SourceOptions();
        options.formatHint = "image/png";
        ImageSource.DecodingOptions decodingOptions = new ImageSource.DecodingOptions();
        try {
            Resource asset = assetManager.openRawFile();
            ImageSource source = ImageSource.create(asset, options);
            return Optional.ofNullable(source.createPixelmap(decodingOptions));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static int dp2px(float dp) {
        return (int) (dp * TypedAttribute.computeTranslateRatio(new DeviceCapability()));
    }
}
